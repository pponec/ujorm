package org.ujorm.mapper.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.core.impl.AbstractUjo;
import org.ujorm.mapper.impl.Context;
import org.ujorm.mapper.model.AttributeModel;
import org.ujorm.mapper.model.EntityModel;
import org.ujorm.tools.jdbc.SqlParamBuilder;

import java.sql.JDBCType;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 *
 * @param <D> Domain class
 * @param <V> Primary key class
 */
public class CrudService<D,V> {
    private static final Logger LOGGER = Logger.getLogger(CrudService.class.getName());

    private final SqlParamBuilder sqlBuilder;
    private final DomainHandler<D> domainHandler;
    private final Context context;
    private final EntityModel<D> entityModel;
    private final AttributeModel<D> pkModel;
    private final Key<D,V> pk;

    public CrudService(SqlParamBuilder sqlBuilder, DomainHandler<D> domainHandler, Context context) {
        this.sqlBuilder = sqlBuilder;
        this.domainHandler = domainHandler;
        this.context = context;
        this.entityModel = EntityModel.of(domainHandler, context);
        this.pkModel = entityModel.pk();
        this.pk = (Key<D,V>) pkModel.key();
    }

    /** Create : multi insert */
    public void insert(D... domain) {
        throw new UnsupportedOperationException("TODO");
    }

    /** Create */
    public D insert(D domain) {
        var sql = new StringBuilder(256)
                .append("INSERT INTO ")
                .append(domainHandler.getDatabaseTable())
                .append(" ) VALUES ");
        var i = new AtomicInteger();
        for(var key : domainHandler.getKeyList()) {
            sql.append(i.getAndIncrement() == 0 ? " ( " : ", ")
                    .append(key.columnName());
        }
        sql.append(" ) VALUES ");
        i.set(0);
        for(var key : domainHandler.getKeyList()) {
            sql.append(i.getAndIncrement() == 0 ? " ( :" : ", :")
                    .append(key.getName());
        }
        sql.append(" )");
        sqlBuilder.sql(sql.toString());
        LOGGER.info(() -> sqlBuilder.toString());
        for(var key : domainHandler.getKeyList()) {
            sqlBuilder.bindObject(key.getName(), key.getValue(domain), jdbcType(key));
        }

        // Assign PK to the domain
        var pkOriginalValue = getPrimaryKeyValue(domain);
        if (pkOriginalValue != null) {
            sqlBuilder.execute();
            return domain;
        } else {
            sqlBuilder.executeInsert();
            V id = (V) sqlBuilder.generatedLastKey(rs -> rs.getLong(1)); // TODO:pop
            var ujo = AbstractUjo.of(domain, domainHandler);
            ujo.setValue(pk, id);
            return ujo.toDomainObject();
        }
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
    public long delete(D domain) {
        return deleteById(getPrimaryKeyValue(domain));
    }

    /**
     * Delete domain object
     * @param id Identifier
     * @return
     */
    public long deleteById(V id) {
        var sql = "DELETE FROM %s WHERE id = :id"
                .formatted(domainHandler.getDatabaseTable());
        return sqlBuilder.sql(sql)
                .bindObject(pk.getName(), id)
                .execute();
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


}
