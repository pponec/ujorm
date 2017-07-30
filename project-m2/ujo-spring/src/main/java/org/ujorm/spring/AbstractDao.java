package org.ujorm.spring;

import java.util.List;
import javax.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.ujorm.criterion.Criterion;
import org.ujorm.orm.OrmUjo;
import org.ujorm.orm.Query;
import org.ujorm.orm.Session;

/**
 * Abstract DAO for quick implementation.
 * The class is a single thread ready only.
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
    protected Session getSession() {
        if (transactionManager == null) {
            transactionManager = (UjormTransactionManager) applContext.getBean(getUjoTransactionBeanName());
        }
        return transactionManager.getLocalSession();
    }

    /** Create a new query */
    final protected <U extends T> Query<U> doQuery(Criterion<U> criteron) {
        return getSession().createQuery(criteron);
    }

    /** Save or update an persistent object */
    final protected <U extends T> void doSaveOrUpdate(@Nonnull U bo) {
        getSession().saveOrUpdate(bo);
    }

    /** Save a persistent object to database */
    final protected <U extends T> void doSave(@Nonnull U bo) {
        getSession().save(bo);
    }

    /** Save list of persistent objects to database */
    final protected <U extends T> void doSave(@Nonnull List<U> bos) {
        getSession().save(bos);
    }

    /** Update a persistent object on database */
    final protected <U extends T> int doUpdate(@Nonnull U bo) {
        return getSession().delete(bo);
    }

    /** Update list of persistent objects on database */
    final protected <U extends T> int doUpdate(@Nonnull List<U> bos) {
        return getSession().delete(bos);
    }

    /** Delete a persistent object from database */
    final protected <U extends T> int doDelete(@Nonnull U bo) {
        return getSession().delete(bo);
    }

    /** Delete list of persistent objects from database */
    final protected <U extends T> int doDelete(@Nonnull List<U> bos) {
        return getSession().delete(bos);
    }

    /** Delete list of persistent objects from database */
    final protected <U extends T> boolean doExists(Criterion<U> criteron) {
        return getSession().exists(criteron);
    }

}
