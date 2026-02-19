package org.ujorm.mapper.core;

import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.mapper.impl.MapperContext;
import org.ujorm.tools.jdbc.SqlParamBuilder;

import java.util.NoSuchElementException;

public class CrudService<D,V> {

    private final SqlParamBuilder builder;
    private final DomainHandler<D> domainHandler;
    private final MapperContext mapperContext;
    private final Key<D,V> keyId;

    public CrudService(SqlParamBuilder builder, DomainHandler<D> domainHandler, MapperContext mapperContext) {
        this.builder = builder;
        this.domainHandler = domainHandler;
        this.mapperContext = mapperContext;
        this.keyId = findId();
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
    public void insert(D domain) {
        throw new UnsupportedOperationException("TODO");
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
        var sql = "DELETE FROM %s WHERE id = :id".formatted(domainHandler.)
        throw new UnsupportedOperationException("TODO");
    }

    /**
     * Delete domain object
     * @param id Identifier
     * @return
     */
    public long deleteById(V id) {
        throw new UnsupportedOperationException("TODO");
    }

}
