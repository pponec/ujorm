package org.ujorm.spring;

import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.Key;
import org.ujorm.core.enums.OptionEnum;
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

    /** Továrna na výrobu ORM Session.<br/>
     * Poznámka: tady není možné použít anotace
     * {@code @Inject} + {@code @Named("configurationUjoTransactionManager"}
     */
    private final UjormTransactionManager transactionManager;

    /** Constructor */
    public AbstractDao(@Nonnull final UjormTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /** Get a lazy loaded ORM session */
    @Nonnull
    protected Session getSessionDao() {
        return transactionManager.getLocalSession();
    }

    /** Create a new query */
    @Nonnull
    protected final <U extends T> Query<U> createQueryDao(@Nonnull final Criterion<U> criteron) {
        return getSessionDao().createQuery(criteron);
    }

    /** Save or update an persistent object */
    protected final <U extends T> void saveOrUpdateDao(@Nonnull final U bo) {
        getSessionDao().saveOrUpdate(bo);
    }

    /** Save a persistent object to database */
    protected final <U extends T> void saveDao(@Nonnull final U bo) {
        getSessionDao().save(bo);
    }

    /** Save list of persistent objects to database */
    protected final <U extends T> void saveDao(@Nonnull final List<U> bos) {
        getSessionDao().save(bos);
    }

    /** Update a persistent object on database */
    protected final <U extends T> int updateDao(@Nonnull final U bo) {
        return getSessionDao().update(bo);
    }

    /** Update a persistent object on database secure */
    protected final <U extends T> int updateSafelyDao
        ( @Nonnull final U bo
        , @Nullable final U original
        , @Nullable final OptionEnum ... required) {
        return getSessionDao().updateSafely(bo, original, required);
    }

    /** UPDATE database safely  by a batch for the all {@link OrmUjo#readChangedProperties(boolean) modified columns} .
     * Execution of the UPDATE SQL statement is conditional on the match of the original values with the database.
     * @param <U> Type of the business object
     * @param bo Business Object
     * @param updateBatch Batch to modify attributes of business object.
     * @param required Required result expected the one row modified exactly,
     * else method throws an {@link IllegalStateException} exception.
     * @see OrmUjo#readChangedProperties(boolean)
     * @return The row count.
     */
    protected <U extends OrmUjo> int updateSafelyByDao
        ( @Nonnull final U bo
        , @Nonnull final Consumer<U> updateBatch
        , @Nullable final OptionEnum ... required)
        {
        return getSessionDao().updateSafelyBy(bo, updateBatch, required);
    }

    /** Delete a persistent object from database */
    protected final <U extends T> int deleteDao(@Nonnull final U bo) {
        return getSessionDao().delete(bo);
    }

    /** Delete list of persistent objects from database */
    protected final <U extends T> int deleteDao(@Nonnull final List<U> bos) {
        return getSessionDao().delete(bos);
    }

    /** Delete list of persistent objects from database */
    protected final <U extends T> boolean existsDao(@Nonnull final Criterion<U> criteron) {
        return getSessionDao().exists(criteron);
    }

    /** Delete list of persistent objects from database */
    protected final <U extends T> boolean existsDao(@Nonnull final Class<U> entity) {
        return getSessionDao().exists(entity);
    }

    /** Get column model */
    protected final <U extends T> MetaColumn getColumnDao(@Nonnull final Key<U,?> compositeKey) {
        return (MetaColumn) getSessionDao().getHandler().findColumnModel(compositeKey);
    }

}
