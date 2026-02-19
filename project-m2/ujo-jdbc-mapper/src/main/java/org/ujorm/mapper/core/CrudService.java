package org.ujorm.mapper.core;

import lombok.RequiredArgsConstructor;
import org.ujorm.core.DomainHandler;

import java.sql.Connection;

@RequiredArgsConstructor
public class CrudService<D,V> {

    private final Connection connection;
    private final DomainHandler<D> domainHandler;

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
