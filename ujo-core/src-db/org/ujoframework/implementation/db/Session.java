/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.implementation.db;

import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.implementation.db.sample.BoDatabase;
import org.ujoframework.tools.criteria.Expression;

/**
 *
 * @author pavel
 */
public class Session {

    public void commit() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public <UJO extends TableUjo> Query<UJO> createQuery(Class<UJO> aClass, Expression<UJO> expA) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public BoDatabase getDatabase() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void rollback() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void save(TableUjo ujo) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public <UJO extends TableUjo> UJO Load(Class ujo, Object id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public <UJO extends TableUjo> UJO single(Query query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public <UJO extends TableUjo> UjoIterator<UJO> iterate(Query<UJO> query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public <UJO extends TableUjo> UjoIterator<UJO> iterate(UjoProperty property) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
