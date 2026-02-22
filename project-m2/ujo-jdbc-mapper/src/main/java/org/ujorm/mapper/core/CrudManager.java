package org.ujorm.mapper.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.core.impl.AbstractUjo;
import org.ujorm.mapper.impl.Context;
import org.ujorm.mapper.model.ColumnModel;
import org.ujorm.mapper.model.TableModel;

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
    /** Remove it ? */
    @Deprecated
    private final Context context;
    private final TableModel<D> tableModel;
    private final ColumnModel<D, V> pkModel;
    private final Key<D, V> pk;
    /** Inserted columns */
    private ColumnModel<D, Object>[] insertedColumns = null;

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
        this.tableModel = TableModel.of(domainHandler, context);
        this.pkModel = (ColumnModel<D, V>) tableModel.pk();
        this.pk = pkModel.key();
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
        var pkOriginalValue = getPrimaryKeyValue(domain);
        var columns = createInsertedColumns(pkOriginalValue);
        var sql = new StringBuilder(256)
                .append("INSERT INTO ")
                .append(domainHandler.getDatabaseTable())
                .append(" (");
        for (int i = 0; i < columns.length; i++) {
            var key = columns[i];
            sql.append(i == 0 ? "" : ", ").append(key.column());
        }

        sql.append(") VALUES (?");
        for (int j = columns.length - 1; j > 0; j--) {
            sql.append(",?");
        }
        sql.append(")");

        return run(sql, ps -> {
            for (int i = 0; i < columns.length; i++) {
                var column = columns[i];
                var value = column.valueOf(domain);
                if (column.relation()) {
                    if (value != null) {
                        value = column.foreignKey().getValue(value);
                    }
                }
                ps.setObject(i + 1, value, column.jdbcType());
            }
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
                .append("SELECT \n");  // "*"
        for (var attrib : this.tableModel.attributes()) {
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
                    var ujo = AbstractUjo.of(domainHandler);
                    for (var attrib : tableModel.attributes()) {
                        var value = rs.getObject(attrib.column(), attrib.key().getType());
                        ujo.setValue(attrib.keyObject(), value);
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

        var attributes = tableModel.attributes();
        var i = new AtomicInteger();
        for (var attrib : attributes) {
            if (!attrib.key().equals(pk)) {
                sql.append(i.getAndIncrement() == 0 ? "" : ", ")
                        .append(attrib.column()).append(" = ?");
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
    private V getPrimaryKeyValue(@NotNull final D domain) {
        return this.pk.getValue(domain);
    }

    /** Exclude PK according to the PK value. */
    @NotNull
    protected ColumnModel<D,Object>[] createInsertedColumns(@Nullable V pkValue) {
        var columns = this.tableModel.attributes();
        if (pkValue != null) {
            return columns.toArray(ColumnModel[]::new);
        }
        if (this.insertedColumns == null) {
            insertedColumns = new ColumnModel[columns.size() - 1];
            int i = 0;
            for (var attrib : this.tableModel.attributes()) {
                if (attrib != pkModel) insertedColumns[i++] = attrib;
            }
        }
        return this.insertedColumns;
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