/*
 *  Copyright 2009-2022 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.orm;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import javax.transaction.Status;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.Ujo;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.core.UjoIterator;
import org.ujorm.core.UjoManager;
import org.ujorm.core.annot.PackagePrivate;
import org.ujorm.core.enums.OptionEnum;
import org.ujorm.criterion.BinaryCriterion;
import org.ujorm.criterion.Criterion;
import org.ujorm.criterion.ValueCriterion;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.ao.CacheKey;
import org.ujorm.orm.ao.CachePolicy;
import org.ujorm.orm.ao.LoadingPolicy;
import org.ujorm.orm.metaModel.MetaColumn;
import org.ujorm.orm.metaModel.MetaDatabase;
import org.ujorm.orm.metaModel.MetaPKey;
import org.ujorm.orm.metaModel.MetaParams;
import org.ujorm.orm.metaModel.MetaProcedure;
import org.ujorm.orm.metaModel.MetaRelation2Many;
import org.ujorm.orm.metaModel.MetaTable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.msg.MsgFormatter;

/**
 * The ORM session.
 * <br>Methods of the session are not thread safe.
 * @author Pavel Ponec
 * @composed * - 1 OrmHandler
 * @assoc - - - JdbcStatement
 */
@SuppressWarnings(value = "unchecked")
public class Session implements Closeable {

    /** Common title to print the SQL VALUES */
    private static final String SQL_VALUES = "-- SQL VALUES: ";
    /** Exception SQL message prefix */
    public static final String SQL_ILLEGAL = "ILLEGAL SQL: ";
    /** Clear the internal cache on the DELETE action */
    private static final boolean REMOVE_CACHE_ON_DELETE = true;
    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(Session.class);
    /** Handler. */
    final private OrmHandler handler;
    /** ORM parameters. */
    final private MetaParams params;
    /** Two database connections set (common and sequence)  */
    final private HashMap<MetaDatabase, Connection>[] connections = new HashMap[]
        { new HashMap<>(2) // common connections
        , new HashMap<>(2) // sequence connections
    };
    /** A session cache */
    private Map<CacheKey, OrmUjo> cache;
    /** The rollback is allowed only */
    private boolean rollbackOnly = false;
    /** Enable a lazy-loading of related Ujo object.
     * The default value is assigned from the parameter {@link MetaParams#LAZY_LOADING_ENABLED}.
     */
    private LoadingPolicy loadingPolicy;
    /** Closed session */
    private boolean closed = false;
    /** Transaction */
    private Transaction transaction;

    /** The default constructor */
    Session(OrmHandler handler) {
        this.handler = handler;
        this.params = handler.getParameters();
        this.loadingPolicy = MetaParams.LOADING_POLICY.of(params);
        clearCache(MetaParams.CACHE_POLICY.of(params));
    }

    /**
     * Create a new transaction or a sub-transaction.
     * @return Create new (sub) transaction
     * @throws IllegalStateException Throw the Exception if a Transaction is running
     */
    public Transaction beginTransaction() throws IllegalStateException {
        transaction = new Transaction(this, transaction);
        return transaction;
    }

    /** Returns the current transaction
      * @return Return {@code null} if no transaction is running.
      */
    public Transaction getTransaction() {
        return transaction;
    }

    /** Returns a handler */
    public final OrmHandler getHandler() {
        return handler;
    }

    /** Make a commit om all databases  for the current transaction level. */
    public void commitTransaction() {
        if (transaction!=null) {
            transaction.commit();
        } else {
            LOGGER.log(UjoLogger.WARN, "Transaction is not running");
            commit();
        }
    }

    /** Make a commit for all databases. */
    public void commit() {
        commit(true);
    }

    /** Make a rollback on all databases for the current transaction level. */
    public void rollbackTransaction() {
        if (transaction!=null) {
            transaction.rollback();
        } else {
            LOGGER.log(UjoLogger.WARN, "Transaction is not running");
            rollback();
        }
    }

    /** Make a rollback for all databases. */
    public final void rollback() {
        commit(false);
    }

    /** Make commit/rollback for all 'production' databases.
     * @param commit if parameters is false than make a rollback.
     */
    public final void commit(final boolean commit) throws IllegalStateException {
        commit(commit, null);
    }

    /** Make commit/rollback for all 'production' databases.
     * @param commit if parameters is false than make a rollback.
     * @param savepoint Nullable array of Savepoints to commit / release
     */
    @PackagePrivate void commit(final boolean commit, final Transaction transaction) throws IllegalUjormException {
        if (commit && rollbackOnly) {
            commit(false);
            throw new IllegalUjormException("The Ujorm session has got the 'rollbackOnly' state.");
        }

        final String errMessage = "Can't make commit of DB ";
        final Savepoint[] savepoint = transaction!=null ? transaction.getSavepoints() : null;
        MetaDatabase database = null;

        try {
            MetaDatabase[] databases = connections[0].keySet().toArray(new MetaDatabase[connections[0].size()]);
            if (databases.length>1) {
                // Sort databases by a definition order:
                Arrays.sort(databases);
            }
            final Level fineLevel = UjoLogger.DEBUG;
            for (int i=0; i<databases.length; ++i) {
                database = databases[i];
                final Connection conn = connections[0].get(database);
                if (commit) {
                    String commitRequest = null;
                    if (savepoint!=null) {
                        final Savepoint sp = savepoint[i];
                        if (sp!=null) {
                            database.getDialect().releaseSavepoint(conn, sp, false);
                        }
                        if (transaction.isRoot()) {
                            commitRequest = "Transaction commit of the ";
                        }
                    } else {
                        commitRequest = "Commit of the " ;
                    }
                    if (commitRequest!=null) {
                        conn.commit();
                        if (LOGGER.isLoggable(fineLevel)) {
                            LOGGER.log(fineLevel, "{}{}", commitRequest, database.getId());
                        }
                    }
                } else {
                    // Rollback:
                    if (savepoint!=null) {
                        final Savepoint sp = savepoint[i];
                        if (sp!=null) {
                            conn.rollback(sp);
                            database.getDialect().releaseSavepoint(conn, sp, true);
                        }
                    } else {
                        conn.rollback();
                    }
                    if (LOGGER.isLoggable(fineLevel)) {
                        LOGGER.log(fineLevel, "Rolback of the {}", database.getId());
                    }
                }
            }

            // Release the current transaction Level:
            this.transaction = transaction != null
                    ? transaction.getParent()
                    : null;

        } catch (RuntimeException | SQLException | OutOfMemoryError e) {
            LOGGER.log(UjoLogger.ERROR, "{}{}", errMessage, database, e);
            throw new IllegalUjormException(errMessage + database, e);
        }
        rollbackOnly = false;
    }

    /** Create a savepoint to all available databases */
    @PackagePrivate Savepoint[] setSavepoint() {
        if (rollbackOnly) {
            throw new IllegalUjormException("The Ujorm session has got the 'rollbackOnly' state.");
        }

        final Savepoint[] result;
        final String errMessage = "Can't save a savepoint to DB ";
        MetaDatabase database = null;

        try {
            MetaDatabase[] databases = connections[0].keySet().toArray(new MetaDatabase[connections[0].size()]);
            result = new Savepoint[databases.length];
            if (databases.length>1) {
                // Sort databases by a definition order:
                Arrays.sort(databases);
            }
            for (int i=0; i<databases.length; ++i) {
                database = databases[i];
                final Connection conn = connections[0].get(database);
                result[i] = conn.setSavepoint();
            }
        } catch (RuntimeException | SQLException |OutOfMemoryError e) {
            final String msg = MsgFormatter.format("{}{}", errMessage, database);
            LOGGER.log(UjoLogger.ERROR, msg, e);
            throw new IllegalUjormException(msg, e);
        }
        rollbackOnly = false;
        return result;
    }

    /** Create query for all table rows. */
    public <U extends OrmUjo> Query<U> createQuery(Class<U> aClass) {
        final Criterion<U> criterion = Criterion.where(true);
        return createQuery(criterion, aClass);
    }

    /** Create query.
     * @deprecated Use the method {@link #createQuery(org.ujorm.criterion.Criterion, java.lang.Class) createQuery(Criterion, Class)} rather.
     * @see #createQuery(org.ujorm.criterion.Criterion, java.lang.Class)
     * @see #createQuery(org.ujorm.criterion.Criterion)
     */
    @Deprecated
    public final <U extends OrmUjo> Query<U> createQuery(Class<U> aClass, Criterion<U> criterion) {
        return createQuery(criterion, aClass);
    }

    /** Create query. This method has a slightly higher performance
     * than the method {@link #createQuery(org.ujorm.criterion.Criterion) createQuery(Criterion)}
     *  without the Class parameter.
     * @see #createQuery(org.ujorm.criterion.Criterion)
     */
    public final <U extends OrmUjo> Query<U> createQuery
        ( @NotNull final Criterion<U> criterion
        , @NotNull final Class<U> aClass) {
        final MetaTable metaTable = handler.findTableModel(aClass);
        return new Query<>(metaTable, criterion, this);
    }

    /** The table class is derived from the first criterion column. */
    public final <U extends OrmUjo> Query<U> createQuery(@NotNull final Criterion<U> criterion) {
        final MetaRelation2Many column = getBasicColumn(criterion);
        final MetaTable table = MetaRelation2Many.TABLE.of(column);
        return new Query<>(table, criterion, this);
    }

    /** Returns {@code true} if exists any database row with the required condition. */
    public final <U extends OrmUjo> boolean exists(@NotNull final Criterion<U> criterion) {
        final MetaTable table = MetaRelation2Many.TABLE.of(getBasicColumn(criterion));
        return exists(table, criterion, table.getFirstPK().getKey());
    }

    /** Returns {@code true} if exists any database row for the required entity. */
    public final <U extends OrmUjo> boolean exists(@NotNull final Class<U> entity) {
        final MetaTable table = handler.findTableModel(entity);
        final Key pk = table.getFirstPK().getKey();
        return exists(table, pk.forAll(), pk);
    }

    /** Returns {@code true} if exists any database row for the required criterion. */
    protected final <U extends OrmUjo> boolean exists
        ( @NotNull final MetaTable table
        , @Nullable final Criterion<U> criterion
        , @NotNull final Key<U,?> pk) {
        final Ujo result = new Query<>(table, criterion, this)
                .setColumn(pk)
                .setLimit(1)
                .uniqueResult();
        return result != null;
    }

    /** Returns the first "basic" column of criterion.
     * @return Not null result
     */
    @Nullable
    public MetaRelation2Many getBasicColumn(@NotNull Criterion criterion) {
        while (criterion.isBinary()) {
            criterion = ((BinaryCriterion) criterion).getLeftNode();
        }

        ValueCriterion exprValue = (ValueCriterion) criterion;
        if (exprValue.getLeftNode() == null) {
            return null;
        }
        Key key = exprValue.getLeftNode();
        while (key.isComposite()) {
            key = ((CompositeKey) key).getKey(0);
        }

        MetaRelation2Many result = handler.findColumnModel(key, true);
        return result;
    }

    /** Returns the first Database instance. */
    public final <DB extends OrmUjo> DB getFirstDatabase() {
        return getDatabase(null);
    }

    /** Returns a Database instance. If parameter is null, than method returns the first database. */
    public <DB extends OrmUjo> DB getDatabase(Class<DB> dbType) {
        try {
            DB result = dbType!=null
                    ? dbType.newInstance()
                    : (DB) MetaDatabase.ROOT.of(handler.getDatabases().get(0))
                    ;
            result.writeSession(this);
            return result;
        } catch (RuntimeException | ReflectiveOperationException e) {
            throw new RuntimeException("Can't create database from: " + dbType, e);
        }
    }

    /** Make a statement INSERT or UPDATE into a database table
     * according to attribute {@link Session}. Related objects
     * must be inserted using an another call of the method.
     * The method cleans all flags of modified attributes.
     * @deprecated Use the method insertOrUpdate() rather
     */
    @Deprecated
    public final void saveOrUpdate(@NotNull final OrmUjo bo) throws IllegalStateException {
        insertOrUpdate(bo);
    }

    /** Make a statement INSERT or UPDATE into a database table
     * according to attribute {@link Session}. Related objects
     * must be inserted using an another call of the method.
     * The method cleans all flags of modified attributes.
     * @since 1.84
     */
    public void insertOrUpdate(@NotNull final OrmUjo bo) throws IllegalStateException {
        Assert.notNull(bo);
        if (bo.readSession() == null) {
            insert(bo);
        } else {
            update(bo);
        }
    }

    /**
     * If parameter {@link MetaParams#INHERITANCE_MODE INHERITANCE_MODE} is TRUE so modify all parrents.
     * @param bo Object to an action
     * @return Returns Table model for the parameter object.
     */
    private MetaTable modifyParent(final OrmUjo bo) {
        final MetaTable table = handler.findTableModel(bo.getClass());
        if (MetaParams.INHERITANCE_MODE.of(params)) {
            final OrmUjo parent = table.getParent(bo);
            if (parent != null) {
                insertOrUpdate(parent);
            }
        }
        return table;
    }

    /** INSERT object into table using the <a href="http://en.wikipedia.org/wiki/Insert_%28SQL%29">Multirow inserts</a>.
     * @param domains Business objects
     * @throws IllegalStateException
     * @see MetaParams#INSERT_MULTIROW_ITEM_LIMIT
     * @deprecated Use the method insert() rather
     */
    @Deprecated
    public final void save(final Collection<? extends OrmUjo> domains) throws IllegalStateException {
        Session.this.insert(domains);
    }

    /** INSERT object into table using the <a href="http://en.wikipedia.org/wiki/Insert_%28SQL%29">Multirow inserts</a>.
     * @param domains Business objects
     * @throws IllegalStateException
     * @see MetaParams#INSERT_MULTIROW_ITEM_LIMIT
     * @since 1.84
     */
    public void insert(final Collection<? extends OrmUjo> domains) throws IllegalStateException {
        final int multiLimit = params.get(MetaParams.INSERT_MULTIROW_ITEM_LIMIT);
        Session.this.insert(domains, multiLimit);
    }

    /** INSERT object into table using the <a href="http://en.wikipedia.org/wiki/Insert_%28SQL%29">Multirow inserts</a>.
     * The method cleans all flags of modified attributes.
     * @param domains List of the business object of the same class. If the list must not contain object of different types
     * @param multiLimit Row limit for the one insert.
     *        If the value will be out of range <1,bos.size()> than the value will be corrected.
     *        If the list item count is greater than multi limit so insert will be separated by more multirow inserts.
     * @throws IllegalStateException
     * @deprecated Use the method insert() rather
     */
    @Deprecated
    public final void save(final Collection<? extends OrmUjo> domains, int multiLimit) throws IllegalUjormException {
         Session.this.insert(domains, multiLimit);
    }

    /** INSERT object into table using the <a href="http://en.wikipedia.org/wiki/Insert_%28SQL%29">Multirow inserts</a>.
     * The method cleans all flags of modified attributes.
     * @param domains List of the business object of the same class. If the list must not contain object of different types
     * @param multiLimit Row limit for the one insert.
     *        If the value will be out of range <1,bos.size()> than the value will be corrected.
     *        If the list item count is greater than multi limit so insert will be separated by more multirow inserts.
     * @throws IllegalStateException
     * @since 1.84
     */
    public void insert(final Collection<? extends OrmUjo> domains, int multiLimit) throws IllegalUjormException {
        final List<? extends OrmUjo> bos = domains instanceof List
                ? (List<? extends OrmUjo>) domains
                : new ArrayList(domains);

        // ---------------- VALIDATIONS -----------------------------------

        if (Check.isEmpty(bos)) {
            LOGGER.log(UjoLogger.DEBUG, "The multi insert list is empty");
            return;
        }
        final MetaTable table = handler.findTableModel(bos.get(0).getClass());
        final MetaDatabase db =table.getDatabase();
        final int bosCount = bos.size();
        table.assertChangeAllowed();

        if (!db.getDialect().isMultiRowInsertSupported()) {
            for (OrmUjo bo : bos) {
                insert(bo);
            }
            return;
        }

        // ---------------- PREPARE -------------------------------------

        final boolean ihneritanceMode = MetaParams.INHERITANCE_MODE.of(params);
        for (OrmUjo bo : bos) {

            // 1. Update parent
            if (ihneritanceMode) {
                final OrmUjo parent = table.getParent(bo);
                if (parent != null) {
                    insertOrUpdate(parent);
                }
            }
            // 2. Assign primary key
            table.assignPrimaryKey(bo, this);
            // 3. Session must be assigned after assignPrimaryKey()
            bo.writeSession(this);
            // 4. Clean all flags of modified attributes
            bo.clearModificationFlags();
        }

        // --------------- PERFORMANCE -------------------------------------

        multiLimit = between(multiLimit, 1, bosCount); // Multi Limit correction;
        int idxFrom = 0;
        int idxTo = multiLimit;

        JdbcStatement statement = null;
        String sql = "";
        StringBuilder out = new StringBuilder(256);
        final boolean logEnabled = MetaParams.LOG_SQL_MULTI_INSERT.of(params);

        try {
            while (idxFrom < idxTo) {
                out.setLength(0);
                sql = db.getDialect().printInsert(bos, idxFrom, idxTo, out).toString();
                if (logEnabled) {
                    LOGGER.log(UjoLogger.INFO, sql);
                }
                statement = getStatement(db, sql, true);
                statement.assignValues(bos, idxFrom, idxTo);
                if (logEnabled && LOGGER.isLoggable(UjoLogger.DEBUG)) {
                    LOGGER.log(UjoLogger.DEBUG, "{}{}", SQL_VALUES, statement.getAssignedValues());
                }
                statement.executeUpdate(); // execute insert statement
                MetaDatabase.close(null, statement, null, true);
                statement = null;
                //
                idxFrom = idxTo;
                idxTo = between(idxFrom + multiLimit, idxFrom, bosCount);
            }

        } catch (RuntimeException | SQLException | IOException | OutOfMemoryError e) {
            rollbackOnly = true;
            throw new IllegalUjormException(SQL_ILLEGAL + sql, e);
        } finally {
            MetaDatabase.close(null, statement, null, true);
        }
    }

    /** Set value inside range */
    private int between(int value, final int min, final int max) {
        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }
        return value;
    }

    /** Insert all persistent attributes into DB table by an INSERT SQL statement.
     * The method cleans all flags of modified attributes.
     * @deprecated Use the method insert() rather
     */
    @Deprecated
    public final void save(@NotNull final OrmUjo bo) throws IllegalStateException {
        insert(bo);
    }

    /** Save all persistent attributes into DB table by an INSERT SQL statement.
     * The method cleans all flags of modified attributes.
     * @since 1.84
     */
    public void insert(@NotNull final OrmUjo bo) throws IllegalStateException {
        Assert.notNull(bo);
        JdbcStatement statement = null;
        String sql = "";

        try {
            // 1. Update parent
            final MetaTable table = modifyParent(bo);
            table.assertChangeAllowed();
            // 2. Assigh Primary Key
            table.assignPrimaryKey(bo, this);
            // 3. Session must be assigned after assignPrimaryKey(). A bug was fixed thans to Pavel Slovacek
            bo.writeSession(this);
            MetaDatabase db = table.getDatabase();
            sql = db.getDialect().printInsert(bo, out(128)).toString();
            LOGGER.log(UjoLogger.INFO, sql);
            statement = getStatement(db, sql, true);
            statement.assignValues(bo);
            LOGGER.log(UjoLogger.INFO, "{}{}", SQL_VALUES, statement.getAssignedValues());
            // 4. Execute:
            statement.executeUpdate(); // execute insert statement
            // 5. Clean all flags of modified attributes
            bo.clearModificationFlags();
        } catch (RuntimeException | SQLException | IOException | OutOfMemoryError e) {
            rollbackOnly = true;
            throw new IllegalUjormException(SQL_ILLEGAL + sql, e);
        } finally {
            MetaDatabase.close(null, statement, null, true);
        }
    }

    /** Database UPDATE of the {@link OrmUjo#readChangedProperties(boolean) modified columns} for the selected object.
     * The method cleans all flags of modified attributes.
     * @see OrmUjo#readChangedProperties(boolean)
     * @return The row count.
     */
    public int update(@NotNull final OrmUjo bo) throws IllegalStateException {
        return update(bo, createPkCriterion(bo), true);
    }

    /** A database UPDATE of the {@link OrmUjo#readChangedProperties(boolean) modified columns} for the selected object.
     * Execution of the UPDATE SQL statement is conditional on the match of the original values with the database.
     * It is recommended to fetch all original relational objects to eliminate lazy-loading.
     * The method cleans all flags of modified attributes.
     * @param bo Object to update
     * @param original A optiona object for Parameter Comparison
     * @param required Required result expected the one row modified exactly,
     * else method throws an {@link IllegalStateException} exception. The count value {@code -1} is ignored.
     * @return The row count.
     * @throws NoSuchElementException The row can not be updated because the modified columns have been changed.
     * @see OrmUjo#readChangedProperties(boolean)
     */
    public <U extends OrmUjo> int updateSafely
        ( @NotNull final U bo
        , @Nullable final U original
        , @Nullable final OptionEnum ... required
        ) throws NoSuchElementException {
        Criterion<U> crn = createPkCriterion(bo);
        if (original != null) {
            original.writeSession(this);
            for (Key aKey : bo.readKeys()) {
                if (bo.checkModificationFlag(aKey)) {
                    final Key<U,Object> key = (Key<U,Object>) aKey;
                    crn = crn.and(key.whereEq(key.of(original)));
                }
            }
        }
        final int result = update(bo, crn);
        final int expectedResult = 1;
        if (result >= 0 && result != expectedResult && Check.firstItem(OptionEnum.REQUIRED, required)) {
            final String msg = MsgFormatter.format("The method expects {} modified row,"
                + " but the real count is {} for the condition: {}"
                , expectedResult
                , result
                , crn.toStringFull());
            throw new NoSuchElementException(msg);
        }
        return result;
    }

    /** UPDATE database safely by a batch for the all {@link OrmUjo#readChangedProperties(boolean) modified columns} .
     * Execution of the UPDATE SQL statement is conditional on the match of the original values with the database.
     * @param <U> Type of the business object
     * @param bo Original business object object
     * @param batch An update batch to modify attributes of business object.
     * @param required Required result expected the one row modified exactly,
     * else method throws an {@link IllegalStateException} exception.
     * @return The row count where value -1  means: No changed column to update
     * @throws NoSuchElementException The row can not be updated because the modified columns have been changed.
     * @see OrmUjo#readChangedProperties(boolean)
     */
    public <U extends OrmUjo> int updateSafely
        ( @NotNull final Consumer<U> batch
        , @NotNull final U bo
        , @Nullable final OptionEnum ... required
        ) throws NoSuchElementException
        {
        int result = 0;
        final U original = (U) bo.cloneUjo();
        bo.clearModificationFlags(); // Clear all changes
        bo.writeSession(this);  // Enable a change manager
        batch.accept(bo); // Update required columns
        final LoadingPolicy originalPolicy = getLoadingPolicy();
        try {
            setLoadingPolicy(LoadingPolicy.CREATE_STUB); // Assign a STUB loading policy for primary keys
            result = updateSafely(bo, original, required);
        } finally {
            setLoadingPolicy(originalPolicy); // Restore the original policy
        }
        return result;
    }

    /** Database Batch UPDATE of the {@link OrmUjo#readChangedProperties(boolean) modified columns} along a criterion.
     * The method cleans all flags of modified attributes.
     * <br>Warning: method does affect to parent objects, see the {@link MetaParams#INHERITANCE_MODE} for more information.
     * @see OrmUjo#readChangedProperties(boolean)
     * @return The row count where value -1  means: No changed column to update
     */
    public <U extends OrmUjo> int update(@NotNull U bo, @NotNull Criterion<U> criterion) {
        return update(bo, criterion, false);
    }

    /** Database Batch UPDATE of the {@link OrmUjo#readChangedProperties(boolean) modified columns} along a criterion.
     * The method cleans all flags of modified attributes.
     * @see OrmUjo#readChangedProperties(boolean)
     * @return The row count where value -1  means: No changed column to update
     */
    private <U extends OrmUjo> int update(@NotNull final U bo, @NotNull final Criterion<U> criterion, boolean singleObject) {
        Assert.notNull(bo);

        int result = 0;
        JdbcStatement statement = null;
        String sql = null;

        try {
            final MetaTable table = singleObject
                ? modifyParent(bo)
                : handler.findTableModel(bo.getClass())
                ;
            table.assertChangeAllowed();
            final MetaDatabase db = table.getDatabase();
            final List<MetaColumn> changedColumns = getChangedOrmColumns(bo);
            bo.clearModificationFlags();
            if (changedColumns.isEmpty()) {
                LOGGER.log(UjoLogger.WARN, "No changed column to update {}", bo);
                return -1;
            }
            final CriterionDecoder decoder = new CriterionDecoder(criterion, table);
            sql = db.getDialect().printUpdate(changedColumns, decoder, out(64)).toString();
            statement = getStatement(db, sql, true);
            statement.assignValues(bo, changedColumns);
            statement.assignValues(decoder);

            if (LOGGER.isLoggable(UjoLogger.INFO)) {
                LOGGER.log(UjoLogger.INFO, "{} {}{}", sql, SQL_VALUES, statement.getAssignedValues());
            }
            result = statement.executeUpdate(); // execute update statement
            bo.writeSession(this);
        } catch (RuntimeException | SQLException | IOException | OutOfMemoryError e) {
            rollbackOnly = true;
            MetaDatabase.close(null, statement, null, false);
            throw new IllegalUjormException(SQL_ILLEGAL + sql, e);
        } finally {
            MetaDatabase.close(null, statement, null, true);
        }
        return result;
    }

    /** Delete all object object by the criterion from parameter.
     * <br>Warning 1: method does not remove deleted object from internal cache,
     *       however you can call method clearCache() to release all objects from the cache.
     * <br>Warning 2: method does not delete parent objects, see the {@link MetaParams#INHERITANCE_MODE} for more information.
     * @param criterion filter for deleting tables.
     * @return Returns a number of the really deleted objects.
     */
    public <U extends OrmUjo> int delete(final Criterion<U> criterion) {
        final MetaRelation2Many column = getBasicColumn(criterion);
        final MetaTable table = MetaRelation2Many.TABLE.of(column);
        return delete(table, criterion);
    }

    /** Delete an optional object from the parameters.
     * @param bo Business object to delete, or the {@code null} argument as a result of some nullable relation.
     * @return Returns a number of the removing items or the zero if the argumetn is {@code null}.
     */
    public int delete(final OrmUjo bo) {
        if (bo == null) {
            LOGGER.log(UjoLogger.DEBUG, "The null object isn't deleted");
            return 0;
        }
        final MetaTable table = handler.findTableModel(bo.getClass());
        table.assertChangeAllowed();
        final MetaColumn PK = table.getFirstPK();
        final Criterion crn = Criterion.where(PK.getKey(), PK.getValue(bo));
        final int result = delete(table, crn);

        if (REMOVE_CACHE_ON_DELETE) {
            removeCache(bo, MetaTable.PK.of(table));
        }

        // Delete parent
        if (MetaParams.INHERITANCE_MODE.of(params)) {
            final OrmUjo parent = table.getParent(bo);
            if (parent != null) {
                delete(parent);
            }
        }

        return result;
    }

    /** Delete all objects of the <strong>same type</strong> from database.
     * @param bos Business objects to delete, the the {@code null} argument is not allowed
     * and the {@code null} items are not allowed too.
     * @return Returns a number of the removing items or the zero if the argumetn is {@code empty}.
     */
    public <T extends OrmUjo> int delete(final List<T> bos) {
        if (bos.isEmpty()) {
            return 0;
        }

        final T firstBo = bos.get(0);
        final MetaTable table = handler.findTableModel(firstBo.getClass());
        table.assertChangeAllowed();
        final MetaColumn PK = table.getFirstPK();
        final List<Object> pKeys = new ArrayList<>(bos.size());
        for (T bo : bos) {
            pKeys.add(PK.getValue(bo));
            if (REMOVE_CACHE_ON_DELETE) {
                removeCache(bo, MetaTable.PK.of(table));
            }
        }

        final Criterion crn = Criterion.whereIn(PK.getKey(), pKeys);
        final int result = delete(table, crn);

        // Delete all parents:
        if (MetaParams.INHERITANCE_MODE.of(params)) {
            final List<OrmUjo> parents = new ArrayList<>(bos.size());
            for (T bo : bos) {
                final OrmUjo parent = table.getParent(bo);
                if (parent != null) {
                    parents.add(parent);
                }
            }
            delete(parents);
        }
        return result;
    }

    /** Delete all object object by the criterion from parameter.
     * <br>Warning: method does not remove deleted object from internal cache,
     *       however you can call method clearCache() to release all objects from the cache.
     * @param tableClass Type of table to delete
     * @param criterion filter for deleting tables.
     * @return Returns a number of the really deleted objects.
     */
    public <U extends OrmUjo> int delete(final Class<U> tableClass, final Criterion<U> criterion) {
        final MetaTable tableModel = handler.findTableModel(tableClass);
        return delete(tableModel, criterion);
    }

    /** Delete all objects object form parameter
     * <br>Warning: method does not remove deleted object from internal cache,
     *       however you can call method clearCache() to release all objects from the cache.
     * @param tableModel Type of table to delete
     * @param criterion filter for deleting tables.
     * @return Returns a number of the really deleted objects.
     */
    protected <U extends OrmUjo> int delete(final MetaTable tableModel, final Criterion<U> criterion) {
        tableModel.assertChangeAllowed();
        int result = 0;
        JdbcStatement statement = null;
        String sql = "";

        try {
            final MetaDatabase db = tableModel.getDatabase();
            final CriterionDecoder decoder = new CriterionDecoder(criterion, tableModel);
            sql = db.getDialect().printDelete(decoder, out(64)).toString();
            statement = getStatement(db, sql, true);
            statement.assignValues(decoder);

            if (LOGGER.isLoggable(UjoLogger.INFO)) {
                // TODO: String msg = org.ujorm.tools.ValueFormatter.formatSql(sql, parameters);
                LOGGER.log(UjoLogger.INFO, "{}{}", sql, SQL_VALUES, statement.getAssignedValues());
            }
            result = statement.executeUpdate(); // execute delete statement
        } catch (RuntimeException | SQLException | IOException | OutOfMemoryError e) {
            rollbackOnly = true;
            MetaDatabase.close(null, statement, null, false);
            throw new IllegalUjormException(SQL_ILLEGAL + sql, e);
        } finally {
            MetaDatabase.close(null, statement, null, true);
        }
        return result;
    }

    /** Call the stored procedure */
    protected void call(final DbProcedure procedure) {
        JdbcStatement statement = null;
        String sql = "";
        MetaDatabase db = procedure.metaProcedure.getDatabase();
        MetaProcedure mProcedure = procedure.metaProcedure();

        try {
            sql = db.getDialect().printCall(mProcedure, out(64)).toString();
            statement = getStatementCallable(db, sql, true);
            statement.assignValues(procedure);

            if (LOGGER.isLoggable(UjoLogger.INFO)) {
                LOGGER.log(UjoLogger.INFO, "{} {}{}", sql, SQL_VALUES, statement.getAssignedValues());
            }
            statement.execute(); // execute call statement
            statement.loadValues(procedure);
        } catch (RuntimeException | SQLException | IOException | OutOfMemoryError e) {
            rollbackOnly = true;
            MetaDatabase.close(null, statement, null, false);
            throw new IllegalUjormException(SQL_ILLEGAL + sql, e);
        } finally {
            MetaDatabase.close(null, statement, null, true);
        }
    }

    /** Get a list of modified columns */
    protected List<MetaColumn> getChangedOrmColumns(@NotNull final OrmUjo bo) {
        final KeyList<Ujo> keys = bo.readKeys();
        final List<MetaColumn> result = new ArrayList<>(keys.size());

        for (final Key key : keys) {
            if (bo.checkModificationFlag(key)) {
                final MetaRelation2Many column = handler.findColumnModel(key);
                if (column instanceof MetaColumn) {
                    result.add((MetaColumn) column);
                }
            }
        }
        return result;
    }

    /** Returns an criterion by a PrimaryKey */
    protected Criterion createPkCriterion(OrmUjo bo) {
        Criterion result = null;
        MetaTable ormTable = handler.findTableModel(bo.getClass());
        MetaPKey ormKey = MetaTable.PK.of(ormTable);
        List<MetaColumn> keys = MetaPKey.COLUMNS.of(ormKey);

        for (MetaColumn ormColumn : keys) {
            Criterion crn = Criterion.where(ormColumn.getKey(), ormColumn.getValue(bo));
            result = result != null
                    ? result.and(crn)
                    : crn;
        }
        return result != null
                ? result
                : Criterion.where(false);
    }

    /** Returns a count of rows */
    public <U extends OrmUjo> long getRowCount(Query<U> query) {
        long result = -1;
        JdbcStatement statement = null;
        ResultSet rs = null;

        MetaTable table = query.getTableModel();
        MetaDatabase db = table.getDatabase();
        String sql = "";

        try {
            sql = db.getDialect().printSelect(table, query, true, out(128)).toString();
            LOGGER.log(UjoLogger.INFO, sql);

            statement = getStatement(db, sql, false);
            statement.assignValues(query);
            LOGGER.log(UjoLogger.INFO, "{}{}", SQL_VALUES, statement.getAssignedValues());

            rs = statement.executeQuery(); // execute a select statement
            result = rs.next() ? rs.getLong(1) : 0;
        } catch (RuntimeException | IOException | SQLException e) {
            rollbackOnly = true;
            throw new RuntimeException(SQL_ILLEGAL + sql, e);
        } finally {
            MetaDatabase.close(null, statement, rs, false);
        }

        return result;
    }

    /** Run SQL SELECT by query. */
    @NotNull
    public JdbcStatement getStatement(@NotNull final Query query) {
        try {
            final MetaDatabase db = query.getTableModel().getDatabase();
            final JdbcStatement result = getStatement(db, query.getSqlStatement(true), false);
            if (query.getLimit()>=0) {
                result.getPreparedStatement().setMaxRows(query.getLimit());
            }
            if (query.getFetchSize()>=0) {
                result.getPreparedStatement().setFetchSize(query.getFetchSize());
            }
            result.assignValues(query);

            if (LOGGER.isLoggable(UjoLogger.INFO)) {
                LOGGER.log(UjoLogger.INFO, "{} {}{}", query, SQL_VALUES, result.getAssignedValues());
            }
            return result;

        } catch (RuntimeException | SQLException | OutOfMemoryError e) {
            rollbackOnly = true;
            throw new IllegalUjormException(SQL_ILLEGAL + query, e);
        }
    }

    /** Find column by a table type. */
    @Nullable
    private MetaColumn findOrmColumn(MetaTable table, Class tableType) {
        for (MetaColumn column : MetaTable.COLUMNS.getList(table)) {
            if (column.isForeignKey()
            &&  column.getType()==tableType) {                   // 1
            //  column.getForeignTable().getType()==tableType) { // 2
                return column;
            }
        }
        return null;
    }

    /** Iterate key of values
     * @param key Table key type of the RelationToMany.
     * @param value A value type of OrmUjo
     */
    public <U extends OrmUjo> UjoIterator<U> iterateInternal(RelationToMany key, OrmUjo value) {

        final Class tableClass = key.getItemType();
        final MetaTable table = handler.findTableModel(tableClass);
        final MetaColumn fColumn = findOrmColumn(table, value.getClass());

        if (fColumn == null) {
            final MetaTable origTable = handler.findTableModel(value.getClass());
            if (origTable.isPersistent()) { // Is it not a DATABASE ?
                final String msg = MsgFormatter.format("Can't find a foreign key of {} to a {}"
                    , table
                    , value.getClass().getSimpleName());
                throw new IllegalUjormException(msg);
            }
        }

        Criterion crit = fColumn != null
                ? Criterion.where(fColumn.getKey(), value)
                : Criterion.constant(table.getFirstPK().getKey(), true);
        Query query = createQuery(crit, table.getType());
        UjoIterator result = UjoIterator.of(query);

        return result;
    }

    /** Get or create DB connection for a required database with an autocommit om {@code false}.
     * @param database required database
     * @param index Value {@code 0} means the BASE connection, value {@code 1} means the SEQUENCE connection.
     * @throws IllegalStateException
     */
    private Connection getConnection_(final MetaDatabase database, final int index) throws IllegalUjormException {
        Connection result = connections[index].get(database);
        if (result == null) {
            assertOpenSession();
            try {
                result = database.createConnection();
            } catch (Exception e) {
                throw new IllegalUjormException("Can't create an connection for " + database, e);
            }
            connections[index].put(database, result);
        }
        return result;
    }

    /**
     * Get the first Connection where an autocommit is set to false.
     */
    public final Connection getFirstConnection() throws IllegalStateException {
        return getFirstConnection(true);
    }

    /**
     * Get the first Connection where an autocommit is set to false.
     * @param toModify By the value {@code false} is disabled to assign savepoints in an active transaction.
     */
    public final Connection getFirstConnection(boolean toModify) throws IllegalStateException {
        return getConnection(0, toModify);
    }

    /**
     * Get a Connection for a required databse by a database order number (index).
     * The autocommit is set to false.
     * @param databaseIndex The first database have got the index value: 0 .
     * @param toModify By the value {@code false} is disabled to assign savepoints in an active transaction.
     */
    public final Connection getConnection(int databaseIndex, final boolean toModify) throws IllegalStateException {
        final MetaDatabase metaDb = handler.getDatabases().get(databaseIndex);
        return getConnection(metaDb, toModify);
    }

    /** Get or create a Connection for a required database with an autocommit na false.
     * If a transaction is running, than assign savepoints.
     * @param database Database metamodel
     * @param toModify By the value {@code false} is disabled to assign savepoints in an active transaction.
     * @throws IllegalStateException An envelope for a run-time SQL exception
     */
    public final Connection getConnection(final MetaDatabase database, final boolean toModify) throws IllegalStateException {
        final Connection result = getConnection_(database, 0);
        if (this.transaction!=null && toModify) {
            this.transaction.assignSavepoint(database, result);
        }
        return result;
    }

    /** Get sequence connection for a required database with an autocommit na false. For internal use only. */
    public final Connection getSeqConnection(final MetaDatabase database) throws IllegalStateException {
        return getConnection_(database, 1);
    }

    /** Create new statement and assigng Savepoint for a trnasaction sase. */
    public JdbcStatement getStatement(@NotNull MetaDatabase database, @NotNull CharSequence sql, final boolean toModify) throws SQLException {
        final JdbcStatement result = new JdbcStatement(getConnection(database, toModify), sql, handler);
        return result;
    }

    /** Create new statement */
    public JdbcStatement getStatementCallable(MetaDatabase database, String sql, final boolean toModify) throws SQLException {
        final JdbcStatement result = new JdbcStatement(getConnection(database, toModify).prepareCall(sql), handler);
        return result;
    }

    /**
     * Load UJO by a unique id. If a result is not found then a null value is passed.
     * @param tableType Type of Ujo
     * @param id Value ID
     */
    public <U extends OrmUjo> U load
        ( final Class<U> tableType
        , final Object id
        ) throws NoSuchElementException {
        final MetaTable table = handler.findTableModel(tableType);
        final MetaColumn column = table.getFirstPK();

        UjoManager.assertAssign(MetaColumn.TABLE_KEY.of(column), id);
        Criterion crn = Criterion.where(column.getKey(), id);
        Query query = createQuery(crn);

        final OrmUjo result = query.uniqueResult();
        return (U) result;
    }

    /**
     * Load UJO by a unique id. If primary key is {@code null} or no result is found
     * then the {@code null} value is returned.
     * @param ujo Ujo object
     */
    @Nullable
    public <U extends OrmUjo> U loadBy(@Nullable U ujo) throws NoSuchElementException {
        if (ujo == null) {
            return ujo;
        }
        final MetaTable metaTable = handler.findTableModel(ujo.getClass());
        final MetaPKey pkeys = MetaTable.PK.of(metaTable);
        final boolean fk = ujo instanceof ExtendedOrmUjo;

        Criterion<U> criterion = null;
        for (MetaColumn c : MetaPKey.COLUMNS.getList(pkeys)) {
            final Object pk = c.getValue(ujo);
            if (pk == null) {
                return null;
            }
            final Criterion<U> crn = Criterion.where(c.getKey(), pk);
            criterion = criterion != null
                ? criterion.and(crn)
                : crn ;
        }

        final U result = createQuery(criterion).uniqueResult();
        return result;
    }

    /** Reload values of the persistent object. <br>
     * Note: If the object has implemented the interface
     * {@link ExtendedOrmUjo ExtendedOrmUjo} than foreign keys are reloaded
     * else a lazy initialization is loaded - for the first key depth.
     * @param ujo The persistent object to relading values.
     * @return The FALSE value means that the object is missing in the database.
     */
    @SuppressWarnings("unchecked")
    public boolean reload(final OrmUjo ujo) {
        if (ujo==null) {
            return false;
        }
        OrmUjo result = loadBy(ujo);
        if (result==null) {
            return false;
        }

        final MetaTable metaTable = handler.findTableModel(ujo.getClass());
        final boolean fk = ujo instanceof ExtendedOrmUjo;

        // Copy all key values back to the original object:
        ujo.writeSession(null);
        for (MetaColumn c : MetaTable.COLUMNS.getList(metaTable)) {

            if (fk && c.isForeignKey()) {
                // Copy the foreign key only (the workaround for lazy loading):
                final Key p = c.getKey();
                ujo.writeValue(p, ((ExtendedOrmUjo)result).readFK(p));
            } else if (c.isColumn()) {
                c.getKey().copy(result, ujo);
            }
        }
        ujo.writeSession(this);
        ujo.clearModificationFlags(); // Clear changed keys

        return true;
    }

    /**
     * Load UJO by a unique id. If the result is not unique, then an exception is throwed.
     * @param relatedProperty Related key
     * @param id Valid ID
     * @param mandatory If result is mandatory then the method throws an exception if no object was found else returns null;
     */
    @SuppressWarnings("unchecked")
    public <U extends OrmUjo> U loadInternal
        ( final Key relatedProperty
        , final Object id
        , final boolean mandatory
        ) throws NoSuchElementException {
        assertOpenSession();
        MetaColumn column = handler.findColumnModel(relatedProperty, true);
        List<MetaColumn> columns = column.getForeignColumns();
        if (columns.size() != 1) {
            throw new UnsupportedOperationException("There is supported only a one-column foreign key: " + column);
        }

        // FIND CACHE:
        MetaTable tableModel = null;
        if (cache!=null) {
            tableModel = MetaColumn.TABLE.of(columns.get(0));
            OrmUjo r = findCache(tableModel.getType(), id);
            if (r != null) {
                return (U) r;
            }
        }

        // SELECT DB row:
        final Criterion<U> crn = Criterion.where(columns.get(0).getKey(), id);
        final U result = createQuery(crn).uniqueResult();
        if (mandatory && result==null) {
            throw new RuntimeException("Deleted object for key " + id);
        }

        if (cache!=null) {
            addCache(result, MetaTable.PK.of(tableModel));
        }
        return result;
    }

    /** Close and release all DB connections.
     * @throws java.lang.IllegalStateException The exception contains a bug from Connection close;
     */
    @SuppressWarnings("unchecked")
    @Override
    public void close() throws IllegalStateException {

        closed = true;
        cache = null;
        Throwable exception = null;
        MetaDatabase database = null;
        String errMessage = "Can't close connection for DB ";

        for (HashMap<MetaDatabase, Connection> cons : connections) {
            for (MetaDatabase db : cons.keySet()) {
                try {
                    Connection conn = cons.get(db);
                    if (conn != null) {
                        conn.rollback(); // TODO
                        conn.close();
                    }
                } catch (RuntimeException | SQLException | OutOfMemoryError e) {
                    LOGGER.log(UjoLogger.ERROR, "{}{}", errMessage, db, e);
                    if (exception == null) {
                        exception = e;
                        database = db;
                    }
                }
            }
            cons.clear();
        }
        if (exception != null) {
            throw new IllegalUjormException(errMessage + database, exception);
        }
    }

    /** Is the session closed? */
    public boolean isClosed() {
        return closed;
    }

    /** Assert the current session os open. */
    private void assertOpenSession() throws IllegalUjormException {
        if (closed) {
            throw new IllegalUjormException("The session is closed ("+hashCode()+")");
        }
    }

    /** Create new StringBuilder instance */
    private StringBuilder out(int capacity) {
        return new StringBuilder(capacity);
    }

    /** Add value into cache */
    private void addCache(OrmUjo bo, MetaPKey pkey) {
        CacheKey key = CacheKey.newInstance(bo, pkey);
        cache.put(key, bo);
    }

    /** Remove selected BO from from internal cache */
    private boolean removeCache(OrmUjo bo, MetaPKey pkey) {
        final CacheKey key = CacheKey.newInstance(bo, pkey);
        final OrmUjo result = cache.remove(key);
        return result != null;
    }

    /** Find object from internal cache */
    public OrmUjo findCache(Class type, Object pkey) {
        assertOpenSession();
        final CacheKey key = CacheKey.newInstance(type, pkey);
        return cache.get(key);
    }

    /** Find object from internal cache */
    public OrmUjo findCache(Class type, Object... pkeys) {
        assertOpenSession();
        final CacheKey key = CacheKey.newInstance(type, pkeys);
        return cache.get(key);
    }

    /** Clear the cache. */
    public void clearCache() {
        if (cache!=null) {
           cache.clear();
        }
    }

    /** Clear cache and change its policy. */
    public final void clearCache(final CachePolicy policy) {
        assertOpenSession();
        switch (policy) {
            case PROTECTED_CACHE:
                cache = new WeakHashMap<>();
                break;
            case SOLID_CACHE:
                cache = new HashMap<>();
                break;
            case NO_CACHE:
                cache = null;
                break;
            default:
                throw new IllegalArgumentException("Unsupported cache policy: " + policy);
        }
    }

    /** Returns parameters */
    public final MetaParams getParameters() {
        return params;
    }

    /** The rollback is allowed only.
     * @return The result is {@code true} if an inner session attribute is true or
     * a related transaction have got the status equals {link Status#STATUS_ROLLEDBACK}.
     */
    public boolean isRollbackOnly() {
        return rollbackOnly
            || transaction!=null
            && transaction.getStatus()==Status.STATUS_ROLLEDBACK;
    }

    public void markForRolback() {
        rollbackOnly = true;
    }

    /** Build new Foreign key.
     * @param key The key must be a relation type of "many to one".
     * @throws IllegalStateException If a parameter key is not a foreign key.
     * @see #readFKValue(org.ujorm.Ujo, org.ujorm.Key)
     */
    public ForeignKey readFK(final OrmUjo ujo, final Key<?, ? extends OrmUjo> key) throws IllegalUjormException {
        final MetaColumn column = handler.findColumnModel(key);

        if (column==null || !column.isForeignKey()) {
            final String message = MsgFormatter.format("The key '{}.{}' is not a foreign key"
                    , ujo.getClass().getSimpleName()
                    , key);
            throw new IllegalUjormException(message);
        }

        final Object result = column.getForeignColumns().get(0).getKey().of(ujo);
        return new ForeignKey(result);
    }

    /** Get a Foreign key without database loading.
     * @param key The key must be a relation type of "many to one".
     * @throws IllegalStateException If a parameter key is not a foreign key.
     */
    public <U extends Ujo, V> V readFKValue(final U ujo, final Key<U, V> key) throws IllegalUjormException {
        final LoadingPolicy orig = getLoadingPolicy();
        try {
            setLoadingPolicy(LoadingPolicy.CREATE_STUB);
            return key.of(ujo);
        } finally {
            setLoadingPolicy(orig);
        }
    }

    /** Check dialect type */
    public final SqlDialect getDialect(Class<? extends OrmUjo> ormType) {
        return handler.findTableModel(ormType).getDatabase().getDialect();
    }

    /** Returns true, if ORM type have got any from listed dialects
     * @param ormType Entity type
     * @param dialects Entity dialect type
     * @return Returns true, if ORM type have got any from listed dialects
     */
    public boolean hasDialect(Class<? extends OrmUjo> ormType, Class<? extends SqlDialect> ... dialects) {
        final SqlDialect dialect = handler.findTableModel(ormType).getDatabase().getDialect();
        for (Class<? extends SqlDialect> dialectType : dialects) {
            if (dialectType.isInstance(dialect)) {
                return true;
            }
        }
        return false;
    }

    /** Create the closed session */
    public static Session newClosedSession(OrmHandler handler) {
        Session result = new Session(handler);
        result.close();
        return result;
    }

    /** Enable a lazy-loading of related Ujo object.
     * The default value is assigned from the parameter {@link MetaParams#LAZY_LOADING_ENABLED}.
     * @return the lazyLoadingEnabled
     * @deprecated Use the {@link #getLoadingPolicy() } rather.
     */
    @Deprecated
    public final LoadingPolicy getLazyLoading() {
        return getLoadingPolicy();
    }

    /** Enable a lazy-loading of related Ujo object.
     * The default value is assigned from the parameter {@link MetaParams#LAZY_LOADING_ENABLED}.
     * @param loadingPolicy the lazyLoadingEnabled to set
     * @deprecated Use the {@link #setLoadingPolicy(org.ujorm.orm.ao.LoadingPolicy) } rather.
     */
    @Deprecated
    public final void setLazyLoading(LoadingPolicy loadingPolicy) {
        setLoadingPolicy(loadingPolicy);
    }

    /** Enable a lazy-loading of related Ujo object.
     * The default value is assigned from the parameter {@link MetaParams#LAZY_LOADING_ENABLED}.
     * @return the lazyLoadingEnabled

     */
    public LoadingPolicy getLoadingPolicy() {
        return loadingPolicy;
    }

    /** Enable a lazy-loading of related Ujo object.
     * The default value is assigned from the parameter {@link MetaParams#LAZY_LOADING_ENABLED}.
     * @param lazyLoadingEnabled the lazyLoadingEnabled to set */
    public void setLoadingPolicy(LoadingPolicy lazyLoadingEnabled) {
        this.loadingPolicy = lazyLoadingEnabled;
    }

}
