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

    /** Insert or update an persistent object
     * @deprecated Use the method insertOrUpdate() rather
     */
    @Deprecated
    public final <U extends T> void saveOrUpdate(@Nonnull final U bo) {
        insertOrUpdate(bo);
    }

    /** Insert a persistent object to database
     * @deprecated Use the method insertOrUpdate() rather
     */
    @Deprecated
    public final <U extends T> void save(@Nonnull final U bo) {
        insert(bo);
    }

    /** Insert list of persistent objects to database
     * @deprecated Use the method insertOrUpdate() rather
     */
    @Deprecated
    public final <U extends T> void save(@Nonnull final List<U> bos) {
        insert(bos);
    }

    /** Insert or update an persistent object
     * @since 1.84
     */
    public <U extends T> void insertOrUpdate(@Nonnull final U bo) {
        insertOrUpdateDao(bo);
    }

    /** Insert a persistent object to database
     * @since 1.84
     */
    public <U extends T> void insert(@Nonnull final U bo) {
        insertDao(bo);
    }

    /** Insert list of persistent objects to database
     * @since 1.84
     */
    public <U extends T> void insert(@Nonnull final List<U> bos) {
        insertDao(bos);
    }

    /** Update a persistent object on database */
    public <U extends T> int update(@Nonnull final U bo) {
        return updateDao(bo);
    }

    /** A database UPDATE of the {@link OrmUjo#readChangedProperties(boolean) modified columns} for the selected object.
     * Execution of the UPDATE SQL statement is conditional on the match of the original values with the database.
     * @param <U> Type of the business object
     * @param bo Business Object
     * @param original Original bo to compare
     * @param required The first attribute {@code REQUIRED} means the update is required, or the method throws an IllegalStateException.
     * @see OrmUjo#readChangedProperties(boolean)
     * @return The row count.
     */
    public <U extends T> int updateSecure
        ( @Nonnull final U bo
        , @Nullable final U original
        , @Nullable final OptionEnum ... required) {
        return updateSafelyDao(bo, original, required);
    }

    /** UPDATE database safely by a batch for the all {@link OrmUjo#readChangedProperties(boolean) modified columns} .
     * Execution of the UPDATE SQL statement is conditional on the match of the original values with the database.
     * @param <U> Type of the business object
     * @param bo Original business object object
     * @param batch An update batch to modify attributes of business object.
     * @param required The first attribute {@code REQUIRED} means the update is required, or the method throws an IllegalStateException.
     * @see OrmUjo#readChangedProperties(boolean)
     * @return The row count.
     */
    public <U extends OrmUjo> int updateSafely
        ( @Nonnull final Consumer<U> batch
        , @Nonnull final U bo
        , @Nullable final OptionEnum ... required) {
        return updateSafelyDao(batch, bo, required);
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
