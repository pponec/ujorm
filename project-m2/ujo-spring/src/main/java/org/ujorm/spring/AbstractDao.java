package org.ujorm.spring;

import java.util.List;
import javax.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.ujorm.Key;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.orm.Session;
import org.ujorm.orm.metaModel.MetaColumn;

/**
 * Abstract <strong>data access object</strong> for quick implementation.
 * The class is a single thread only.
 * @author Pavel Ponec
 */
public abstract class AbstractDao<T extends OrmUjo> {

    /** Default transaction manager name */
    public static final String DEFAULT_TRANSACTION_MANAGER_NAME =  "ujormTransactionManager";

    /** Továrna na výrobu ORM Session.<br/>
     * Poznámka: tady není možné použít anotace
     * {@code @Inject} + {@code @Named("configurationUjoTransactionManager"}
     */
    private UjormTransactionManager transactionManager;

    /** Továrna na výrobu ORM Session */
    @Autowired
    private ApplicationContext applContext;

    /** Get name of a spring managed bean type of {@link UjormTransactionManager}.
     * <br>Overwrite the method, if you nead other name than {@code "ujormTransactionManager"} .
     */
    protected String getUjoTransactionBeanName() {
        return DEFAULT_TRANSACTION_MANAGER_NAME;
    }

    /** Get a lazy loaded ORM session */
    @Nonnull
    protected Session getSessionDao() {
        if (transactionManager == null) {
            transactionManager = (UjormTransactionManager) applContext.getBean(getUjoTransactionBeanName());
        }
        return transactionManager.getLocalSession();
    }

    /** Create a new query */
    @Nonnull
    final protected <U extends T> Query<U> createQueryDao(Criterion<U> criteron) {
        return getSessionDao().createQuery(criteron);
    }

    /** Save or update an persistent object */
    final protected <U extends T> void saveOrUpdateDao(@Nonnull U bo) {
        getSessionDao().saveOrUpdate(bo);
    }

    /** Save a persistent object to database */
    final protected <U extends T> void saveDao(@Nonnull U bo) {
        getSessionDao().save(bo);
    }

    /** Save list of persistent objects to database */
    final protected <U extends T> void saveDao(@Nonnull List<U> bos) {
        getSessionDao().save(bos);
    }

    /** Update a persistent object on database */
    final protected <U extends T> int doUpdate(@Nonnull U bo) {
        return getSessionDao().delete(bo);
    }

    /** Update list of persistent objects on database */
    final protected <U extends T> int updateDao(@Nonnull List<U> bos) {
        return getSessionDao().delete(bos);
    }

    /** Delete a persistent object from database */
    final protected <U extends T> int deleteDao(@Nonnull U bo) {
        return getSessionDao().delete(bo);
    }

    /** Delete list of persistent objects from database */
    final protected <U extends T> int deleteDao(@Nonnull List<U> bos) {
        return getSessionDao().delete(bos);
    }

    /** Delete list of persistent objects from database */
    final protected <U extends T> boolean existsDao(@Nonnull Criterion<U> criteron) {
        return getSessionDao().exists(criteron);
    }

    /** Delete list of persistent objects from database */
    final protected <U extends T> boolean existsDao(final Class<U> entity) {
        return getSessionDao().exists(entity);
    }

    /** Get column model */
    final protected <U extends T> MetaColumn getColumnDao(Key<U,?> compositeKey) {
        return (MetaColumn) getSessionDao().getHandler().findColumnModel(compositeKey);
    }

}
