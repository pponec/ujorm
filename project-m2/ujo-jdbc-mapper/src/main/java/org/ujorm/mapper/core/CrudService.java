package org.ujorm.mapper.core;

import org.jetbrains.annotations.NotNull;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.core.impl.AbstractUjo;
import org.ujorm.mapper.impl.JdbcTypeProvider;
import org.ujorm.mapper.impl.MapperContext;
import org.ujorm.tools.jdbc.SqlParamBuilder;

import java.sql.JDBCType;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @param <D> Domain class
 * @param <V> Primary key class
 */
public class CrudService<D,V> {

    private final SqlParamBuilder sqlBuilder;
    private final DomainHandler<D> domainHandler;
    private final MapperContext mapperContext;
    private final Key<D,V> keyId;
    private final JDBCType jdbcTypeId;

    public CrudService(SqlParamBuilder sqlBuilder, DomainHandler<D> domainHandler, MapperContext mapperContext) {
        this.sqlBuilder = sqlBuilder;
        this.domainHandler = domainHandler;
        this.mapperContext = mapperContext;
        this.keyId = findId();
        this.jdbcTypeId = jdbcType(keyId);
    }

    /** Find the id key */
    private Key<D,V> findId() {
        for (var id : domainHandler.getKeyList()) {
            if (id.primaryKey()) {
                return (Key<D,V>) id;
            }
        }
        if (mapperContext.isFirstPropertyIsIdentifier()) {
            return (Key<D,V>) domainHandler.getKeyList().get(0);
        } else {
            throw new NoSuchElementException("No primary key was found in the class: "  + domainHandler.getDomainClass());
        }
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
        for(var key : domainHandler.getKeyList()) {
            sqlBuilder.bindObject(key.getName(), key.getValue(domain), jdbcType(key));
        }

        // Assign PK to the domain
        var filledPK = getPrimaryKeyValue(domain) != null;
        if (filledPK) {
            sqlBuilder.execute();
            return domain;
        } else {
            sqlBuilder.executeInsert();
            V id = (V) sqlBuilder.generatedLastKey(rs -> rs.getLong(1)); // TODO
            var ujo = AbstractUjo.of(domain, domainHandler);
            ujo.setValue(keyId, id);
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
        return deleteById(keyId.getValue(domain));
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
                .bindObject("id", jdbcTypeId, id)
                .execute();
    }

    /** Return a value of the Primary Key */
    private V getPrimaryKeyValue(@NotNull D domain) {
        return this.keyId.getValue(domain);
    }

    private static JDBCType jdbcType(Key<?,?> key) {
        return JdbcTypeProvider.findJdbcType(key.getType());
    }


}
