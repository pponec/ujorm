package org.ujorm.mapper.core;

import lombok.RequiredArgsConstructor;
import org.ujorm.core.DomainHandler;

@RequiredArgsConstructor
public class CrudService<D,V> {

    private final DomainHandler<D> domainHandler;

    /** Create : multi insert*/
    public void insert(D... domain) {

    }

    /** Create */
    public void insert(D domain) {

    }

    /** Read */
    public D read(V id) {

    }

    /**
     * Update
     * @param domain Domain object
     * @param properties Zero properties update all
     * @return
     */
    public long update(D domain, String... properties) {
        return 0;
    }

    /**
     * Delete domain object
     * @param domain
     * @return
     */
    public long delete(D domain) {
        return 0L;
    }

    /**
     * Delete domain object
     * @param id Identifier
     * @return
     */
    public long deleteById(V id) {
        return 0;
    }

}
