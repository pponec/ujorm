package org.ujorm.spring;

import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    public AbstractDao(@NotNull final UjormTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /** Get a lazy loaded ORM session */
    @NotNull
    protected Session getSessionDao() {
        return transactionManager.getLocalSession();
    }

    /** Create a new query */
    @NotNull
    protected final <U extends T> Query<U> createQueryDao(@NotNull final Criterion<U> criteron) {
        return getSessionDao().createQuery(criteron);
    }

    /** Insert or update an persistent object */
    protected final <U extends T> void insertOrUpdateDao(@NotNull final U bo) {
        getSessionDao().insertOrUpdate(bo);
    }

    /** Insert a persistent object to database */
    protected final <U extends T> void insertDao(@NotNull final U bo) {
        getSessionDao().insert(bo);
    }

    /** Insert list of persistent objects to database */
    protected final <U extends T> void insertDao(@NotNull final List<U> bos) {
        getSessionDao().insert(bos);
    }

    /** Update a persistent object on database */
    protected final <U extends T> int updateDao(@NotNull final U bo) {
        return getSessionDao().update(bo);
    }

    /** Update a persistent object on database secure */
    protected final <U extends T> int updateSafelyDao
        ( @NotNull final U bo
        , @Nullable final U original
        , @Nullable final OptionEnum ... required) {
        return getSessionDao().updateSafely(bo, original, required);
    }

    /** UPDATE database safely  by a batch for the all {@link OrmUjo#readChangedProperties(boolean) modified columns} .
     * Execution of the UPDATE SQL statement is conditional on the match of the original values with the database.
     * @param <U> Type of the business object
     * @param bo Original business object object
     * @param batch An update batch to modify attributes of business object.
     * @param required Required result expected the one row modified exactly,
     * else method throws an {@link IllegalStateException} exception.
     * @see OrmUjo#readChangedProperties(boolean)
     * @return The row count.
     */
    protected <U extends OrmUjo> int updateSafelyDao
        ( @NotNull final Consumer<U> batch
        , @NotNull final U bo
        , @Nullable final OptionEnum ... required)
        {
        return getSessionDao().updateSafely(batch, bo, required);
    }

    /** Delete a persistent object from database */
    protected final <U extends T> int deleteDao(@NotNull final U bo) {
        return getSessionDao().delete(bo);
    }

    /** Delete list of persistent objects from database */
    protected final <U extends T> int deleteDao(@NotNull final List<U> bos) {
        return getSessionDao().delete(bos);
    }

    /** Delete list of persistent objects from database */
    protected final <U extends T> boolean existsDao(@NotNull final Criterion<U> criteron) {
        return getSessionDao().exists(criteron);
    }

    /** Delete list of persistent objects from database */
    protected final <U extends T> boolean existsDao(@NotNull final Class<U> entity) {
        return getSessionDao().exists(entity);
    }

    /** Get column model */
    protected final <U extends T> MetaColumn getColumnDao(@NotNull final Key<U,?> compositeKey) {
        return (MetaColumn) getSessionDao().getHandler().findColumnModel(compositeKey);
    }

}
