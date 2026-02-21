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
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * The CRUD Manager for the JDBC API.
 * Each method may throw an unchecked {@link org.ujorm.tools.jdbc.SQLException}.
 * @param <D> Domain class
 * @param <V> Primary key class
 */
public class CrudManager<D,V> {
    private static final Logger LOGGER = Logger.getLogger(CrudManager.class.getName());

    private final Connection connection;
    private final DomainHandler<D> domainHandler;
    private final Context context;
    private final EntityModel<D> entityModel;
    private final AttributeModel<D> pkModel;
    private final Key<D,V> pk;

    public CrudManager(Class<D> domainClass, Connection connection) {
        this(domainClass, connection, Context.ofDefault());
    }

    public CrudManager(Class<D> domainClass, Connection connection, Context context) {
        this.connection = connection;
        this.domainHandler = context.domainService().getHandler(domainClass);
        this.context = context;
        this.entityModel = EntityModel.of(domainHandler, context);
        this.pkModel = entityModel.pk();
        this.pk = (Key<D,V>) pkModel.key();
    }

    /** Create : multi insert
     * TODO: implement a multi-insert with the batch limit.
     * */
    public void insert(int batchSize, D... domains) {
        for (var domain : domains) {
            insert(domain);
        }
    }

    /** Create */
    public D insert(D domain) {
        var sql = new StringBuilder(256)
                .append("INSERT INTO ")
                .append(domainHandler.getDatabaseTable())
                .append(" ) VALUES ");
        var i = new AtomicInteger();
        for (var key : domainHandler.getKeyList()) {
            sql.append(i.getAndIncrement() == 0 ? " ( " : ", ")
                    .append(key.columnName());
        }
        sql.append(" ) VALUES ( ?");
        for (int j = domainHandler.getKeyList().size() - 1; j > 0; --j) {
            sql.append(",?");
        }
        sql.append(" )");

        return run(sql, ps -> {
            for (var attrib : this.entityModel.attributes()) {
                var value = attrib.valueOf(domain);
                ps.setObject(attrib.index(), value, attrib.jdbcType()) ;
            }

            // Assign PK to the domain
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
                if (id == null) throw new IllegalArgumentException("No id value is avalilable");
                var ujo = AbstractUjo.of(domain, domainHandler);
                ujo.setValue(pk, id);
                return ujo.toDomainObject();
            }
        });
    }

    /** Read */
    public D read(V id) {
       throw new UnsupportedOperationException("TODO");
    }

    /**
     * Update
     * @param domain Domain object
     * @param properties Zero properties update all
     * @return
     */
    public long update(D domain, String... properties) {
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Delete domain object
     * @param domain
     * @return
     */
    public int delete(D domain) {
        return deleteById(getPrimaryKeyValue(domain));
    }

    /**
     * Delete domain object
     * @param id Identifier
     * @return
     */
    public int deleteById(V id) {
        var sql = "DELETE FROM %s WHERE id = ?".formatted(domainHandler.getDatabaseTable());
        return run(sql, ps -> {
            ps.setObject(1, id);
            return ps.executeUpdate();
        });
    }

    /** Return a value of the Primary Key */
    private V getPrimaryKeyValue(@NotNull D domain) {
        return this.pk.getValue(domain);
    }

    @Deprecated
    @Nullable
    private JDBCType jdbcType(Key<?,?> key) {
        return context.commonService().findJdbcType(key.getType());
    }

    /** Log the SQL statement + execute it. */
    protected <R> R run(final CharSequence sql, final SqlFunction<PreparedStatement, R> fun) {
        try (var ps = connection.prepareStatement(sql.toString()) ) {
            LOGGER.info(() -> sql.toString());
            return fun.apply(ps);
        } catch (Exception ex) {
            throw (ex instanceof RuntimeException re) ? re : new IllegalStateException(ex);
        }
    }

    @FunctionalInterface
    protected interface SqlFunction<T, R> extends Function<T, R> {
        @Override
        default R apply(final T ps) {
            try {
                return applyValue(ps);
            } catch (SQLException ex) {
                throw org.ujorm.tools.jdbc.SQLException.of(ex);
            } catch (Exception ex) {
                throw (ex instanceof RuntimeException re) ? re : new IllegalStateException(ex);
            }
        }

        R applyValue(T ps) throws Exception;
    }
}
