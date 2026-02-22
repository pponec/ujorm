package org.ujorm.mapper.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.core.impl.AbstractUjo;
import org.ujorm.mapper.impl.Context;
import org.ujorm.mapper.model.AttributeModel;
import org.ujorm.mapper.model.EntityModel;

import java.sql.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * The CRUD Manager for the JDBC API.
 * Each method may throw an unchecked {@link org.ujorm.tools.jdbc.SQLException}.
 *
 * @param <D> Domain class
 * @param <V> Primary key class
 */
public class CrudManager<D, V> {
    private static final Logger LOGGER = Logger.getLogger(CrudManager.class.getName());

    private final Connection connection;
    private final DomainHandler<D> domainHandler;
    private final Context context;
    private final EntityModel<D> entityModel;
    private final AttributeModel<D> pkModel;
    private final Key<D, V> pk;

    /** Size of batch for multi-insert and delete.
     * Note: This attribute is not fully implemented yet. */
    @Deprecated
    private final int batchSize;

    public CrudManager(@NotNull Class<D> domainClass, @NotNull Connection connection) {
        this(domainClass, connection, Context.ofDefault());
    }

    public CrudManager(@NotNull Class<D> domainClass, @NotNull Connection connection, @NotNull Context context) {
        this(domainClass, connection, context, 500);
    }

    @SuppressWarnings("unchecked")
    public CrudManager(@NotNull Class<D> domainClass, @NotNull Connection connection, @NotNull Context context, int batchSize) {
        this.connection = connection;
        this.domainHandler = context.domainService().getHandler(domainClass);
        this.context = context;
        this.entityModel = EntityModel.of(domainHandler, context);
        this.pkModel = entityModel.pk();
        this.pk = (Key<D, V>) pkModel.key();
        this.batchSize = batchSize;
    }

    /** Inserts multiple domain objects using a loop.
     * TODO: Implement a true multi-insert with batching support. */
    @SafeVarargs
    public final void insert(int batchSize, @NotNull D... domains) {
        for (var domain : domains) {
            insert(domain);
        }
    }

    /** Inserts a domain object into the database. */
    public D insert(@NotNull D domain) {
        var sql = new StringBuilder(256)
                .append("INSERT INTO ")
                .append(domainHandler.getDatabaseTable())
                .append(" (");

        var i = new AtomicInteger();
        for (var key : domainHandler.getKeyList()) {
            sql.append(i.getAndIncrement() == 0 ? "" : ", ").append(key.columnName());
        }

        sql.append(") VALUES (?");
        for (int j = domainHandler.getKeyList().size() - 1; j > 0; --j) {
            sql.append(",?");
        }
        sql.append(")");

        return run(sql, ps -> {
            for (var attrib : entityModel.attributes()) {
                var value = attrib.valueOf(domain);
                ps.setObject(attrib.index() + 1, value, attrib.jdbcType());
            }

            var pkOriginalValue = getPrimaryKeyValue(domain);
            if (pkOriginalValue != null) {
                ps.executeUpdate();
                return domain;
            } else {
                ps.executeUpdate();
                V id = null;
                try (var rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        id = rs.getObject(1, pk.getType());
                    }
                }
                if (id == null) {
                    throw new IllegalArgumentException("No ID value was generated.");
                }
                var ujo = AbstractUjo.of(domain, domainHandler);
                ujo.setValue(pk, id);
                return ujo.toDomainObject();
            }
        });
    }

    /** Reads a domain object by its identifier. */
    public D read(@NotNull V id) {
        var sql = new StringBuilder(128)
                .append("SELECT \n");  // "*" +

        for (var attrib : this.entityModel.attributes()) {
            sql.append(attrib.index() > 0 ? ", ": "  ");
            sql.append(attrib.column()).append(" AS ").append(attrib.name()).append("\n");
        }
        sql.append(" FROM ")
                .append(domainHandler.getDatabaseTable())
                .append(" WHERE ")
                .append(pk.columnName())
                .append(" = ?");

        return run(sql, ps -> {
            ps.setObject(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    var ujo = AbstractUjo.of(domainHandler.getDomainClass());
                    for (var attrib : entityModel.attributes()) {
                        var value = rs.getObject(attrib.columnName(), attrib.key().getType());
                        ujo.setValue(attrib.key(), value);
                    }
                    return ujo.toDomainObject();
                }
                return null;
            }
        });
    }

    /**
     * Updates a domain object.
     * @param domain Domain object to update.
     * @param properties Optional list of property names to update. If empty, all properties are updated.
     * @return The number of affected rows.
     */
    public long update(@NotNull D domain, String... properties) {
        var sql = new StringBuilder(256)
                .append("UPDATE ")
                .append(domainHandler.getDatabaseTable())
                .append(" SET ");

        var attributes = entityModel.attributes();
        var i = new AtomicInteger();
        for (var attrib : attributes) {
            if (!attrib.key().equals(pk)) {
                sql.append(i.getAndIncrement() == 0 ? "" : ", ")
                        .append(attrib.columnName()).append(" = ?");
            }
        }
        sql.append(" WHERE ").append(pk.columnName()).append(" = ?");

        return run(sql, ps -> {
            var idx = 1;
            for (var attrib : attributes) {
                if (!attrib.key().equals(pk)) {
                    ps.setObject(idx++, attrib.valueOf(domain), attrib.jdbcType());
                }
            }
            ps.setObject(idx, getPrimaryKeyValue(domain));
            return (long) ps.executeUpdate();
        });
    }

    /** Deletes a domain object. */
    public int delete(@NotNull D domain) {
        return deleteById(getPrimaryKeyValue(domain));
    }

    /** Deletes a domain object by its identifier. */
    public int deleteById(@NotNull V id) {
        var sql = "DELETE FROM " + domainHandler.getDatabaseTable() + " WHERE " + pk.columnName() + " = ?";
        return run(sql, ps -> {
            ps.setObject(1, id);
            return ps.executeUpdate();
        });
    }

    /** Returns the value of the primary key. */
    private V getPrimaryKeyValue(@NotNull D domain) {
        return this.pk.getValue(domain);
    }

    /** Logs and executes the SQL statement. */
    protected <R> R run(final CharSequence sql, final SqlFunction<PreparedStatement, R> fun) {
        try (var ps = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS)) {
            LOGGER.info(sql::toString);
            return fun.applyValue(ps);
        } catch (Exception ex) {
            throw (ex instanceof RuntimeException re) ? re : new IllegalStateException(ex);
        }
    }

    @FunctionalInterface
    protected interface SqlFunction<T, R> {
        R applyValue(T ps) throws Exception;
    }
}