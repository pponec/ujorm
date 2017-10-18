package org.ujorm.spring;

import java.util.List;
import javax.annotation.Nonnull;
import org.ujorm.Key;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.orm.Session;
import org.ujorm.orm.metaModel.MetaColumn;

/**
 * General <strong>data access object</strong> for quick implementation.
 * The class is a single thread ready only.
 * @author Pavel Ponec
 */
public class CommonDao<T extends OrmUjo> extends AbstractDao<T> {

    /** Constuctor */
    public CommonDao(@Nonnull final UjormTransactionManager transactionManager) {
        super(transactionManager);
    }

    /** Get session */
    @Nonnull
    public Session getSession() {
        return getSessionDao();
    }

    /** Create a new query */
    @Nonnull
    public <U extends T> Query<U> createQuery(@Nonnull final Criterion<U> criteron) {
        return createQueryDao(criteron);
    }

    /** Save or update an persistent object */
    public <U extends T> void saveOrUpdate(@Nonnull final U bo) {
        saveOrUpdateDao(bo);
    }

    /** Save a persistent object to database */
    public <U extends T> void save(@Nonnull final U bo) {
        saveDao(bo);
    }

    /** Save list of persistent objects to database */
    public <U extends T> void save(@Nonnull final List<U> bos) {
        saveDao(bos);
    }

    /** Update a persistent object on database */
    public <U extends T> int update(@Nonnull final U bo) {
        return deleteDao(bo);
    }

    /** Update list of persistent objects on database */
    public <U extends T> int update(@Nonnull final List<U> bos) {
        return deleteDao(bos);
    }

    /** Delete a persistent object from database */
    public <U extends T> int delete(@Nonnull final U bo) {
        return deleteDao(bo);
    }

    /** Delete list of persistent objects from database */
    public <U extends T> int delete(@Nonnull final List<U> bos) {
        return deleteDao(bos);
    }

    /** Delete list of persistent objects from database */
    public <U extends T> boolean exists(@Nonnull final Criterion<U> criteron) {
        return existsDao(criteron);
    }

    /** Delete list of persistent objects from database */
    public <U extends T> boolean exists(@Nonnull final Class<U> entity) {
        return existsDao(entity);
    }

    /** Get column model */
    public <U extends T> MetaColumn getColumnModel(@Nonnull final Key<U,?> compositeKey) {
        return getColumnDao(compositeKey);
    }


}
