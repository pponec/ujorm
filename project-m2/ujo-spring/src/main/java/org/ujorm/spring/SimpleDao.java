package org.ujorm.spring;

import java.util.List;
import javax.annotation.Nonnull;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.orm.Session;

/**
 * Simple DAO for quick implementation.
 * The class is a single thread ready only.
 * @author Pavel Ponec
 */
public class SimpleDao<T extends OrmUjo> extends AbstractDao<T> {

    /** Create a new query */
    public <U extends T> Query<U> createQuery(Criterion<U> criteron) {
        return doQuery(criteron);
    }

    /** Save or update an persistent object */
    public <U extends T> void saveOrUpdate(@Nonnull U bo) {
        doSaveOrUpdate(bo);
    }

    /** Save a persistent object to database */
    public <U extends T> void save(@Nonnull U bo) {
        doSave(bo);
    }

    /** Save list of persistent objects to database */
    public <U extends T> void save(@Nonnull List<U> bos) {
        doSave(bos);
    }

    /** Update a persistent object on database */
    public <U extends T> int update(@Nonnull U bo) {
        return doDelete(bo);
    }

    /** Update list of persistent objects on database */
    public <U extends T> int update(@Nonnull List<U> bos) {
        return doDelete(bos);
    }

    /** Delete a persistent object from database */
    public <U extends T> int delete(@Nonnull U bo) {
        return doDelete(bo);
    }

    /** Delete list of persistent objects from database */
    public <U extends T> int delete(@Nonnull List<U> bos) {
        return doDelete(bos);
    }

    /** Delete list of persistent objects from database */
    public <U extends T> boolean exists(Criterion<U> criteron) {
        return doExists(criteron);
    }

    /** Get session */
    @Override
    public final Session getSession() {
        return super.getSession();
    }

}
