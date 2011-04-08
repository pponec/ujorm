/*
 *  Copyright 2009-2010 Pavel Ponec
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
package org.ujoframework.orm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.core.UjoManager;
import org.ujoframework.extensions.PathProperty;
import org.ujoframework.implementation.orm.RelationToMany;
import org.ujoframework.orm.ao.CacheKey;
import org.ujoframework.orm.metaModel.MetaColumn;
import org.ujoframework.orm.metaModel.MetaDatabase;
import org.ujoframework.orm.metaModel.MetaPKey;
import org.ujoframework.orm.metaModel.MetaParams;
import org.ujoframework.orm.metaModel.MetaRelation2Many;
import org.ujoframework.orm.metaModel.MetaTable;
import org.ujoframework.criterion.Criterion;
import org.ujoframework.criterion.BinaryCriterion;
import org.ujoframework.criterion.ValueCriterion;
import org.ujoframework.orm.ao.CachePolicy;
import org.ujoframework.orm.metaModel.MetaProcedure;

/**
 * The ORM session.
 * <br />Methods of the session are not thread safe.
 * @author Pavel Ponec
 */
@SuppressWarnings(value = "unchecked")
public class Session {

    /** Common title to print the SQL VALUES */
    private static final String SQL_VALUES = "\n-- SQL VALUES: ";
    /** Exception SQL message prefix */
    public static final String SQL_ILLEGAL = "ILLEGAL SQL: ";
    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(Session.class.getName());
    /** Handler. */
    final private OrmHandler handler;
    /** Orm parameters. */
    final private MetaParams params;
    /** Two database connections set (common and sequence)  */
    final private HashMap<MetaDatabase, Connection>[] connections = new HashMap[]
        { new HashMap<MetaDatabase, Connection>(2) // common connections
        , new HashMap<MetaDatabase, Connection>(2) // sequence connections
    };
    /** A session cache */
    private Map<CacheKey, OrmUjo> cache;
    /** The rollback is allowed only */
    private boolean rollbackOnly = false;
    /** Closed session */
    private boolean closed = false;

    /** The default constructor */
    Session(OrmHandler handler) {
        this.handler = handler;
        this.params = handler.getParameters();
        clearCache(MetaParams.CACHE_POLICY.of(params));
    }

    /** Returns a handler */
    final public OrmHandler getHandler() {
        return handler;
    }

    /** Make a commit for all databases. */
    public void commit() {
        commit(true);
    }

    /** Make a rollback for all databases. */
    public void rollback() {
        commit(false);
    }

    /** Make commit/rollback for all 'production' databases.
     * @param commit if parameters is false than make a rollback.
     */
    public void commit(boolean commit) {
        if (commit && rollbackOnly) {
            commit(false);
            throw new IllegalStateException("The Ujorm session has got the 'rollbackOnly' state.");
        }

        Throwable exception = null;
        MetaDatabase database = null;
        String errMessage = "Can't make commit of DB ";

        try {
            MetaDatabase[] databases = connections[0].keySet().toArray(new MetaDatabase[connections[0].size()]);
            if (databases.length>1) {
                // Sort databases by a definition order:
                Arrays.sort(databases);
            }
            for (int i=0; i<databases.length; ++i) {
                database = databases[i];
                final Connection conn = connections[0].get(database);
                if (commit) {
                    conn.commit();
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "Commit of the {0}", database.getId());
                    }
                } else {
                    conn.rollback();
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "Rolback of the {0}", database.getId());
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, errMessage + database, e);
            throw new IllegalStateException(errMessage + database, exception);
        }
        rollbackOnly = false;
    }

    /** For all rows. */
    public <UJO extends OrmUjo> Query<UJO> createQuery(Class<UJO> aClass) {
        final Criterion<UJO> criterion = Criterion.where(true);
        return createQuery(aClass, criterion);
    }

    public <UJO extends OrmUjo> Query<UJO> createQuery(Class<UJO> aClass, Criterion<UJO> criterion) {
        MetaTable metaTable = handler.findTableModel(aClass);
        return new Query<UJO>(metaTable, criterion, this);
    }

    /** The table class is derived from the first criterion column. */
    public <UJO extends OrmUjo> Query<UJO> createQuery(Criterion<UJO> criterion) {
        MetaRelation2Many column = getBasicColumn(criterion);
        MetaTable table = MetaRelation2Many.TABLE.of(column);
        return new Query<UJO>(table, criterion, this);
    }

    /** Returns the first "basic" column of criterion. */
    public MetaRelation2Many getBasicColumn(Criterion criterion) {
        while (criterion.isBinary()) {
            criterion = ((BinaryCriterion) criterion).getLeftNode();
        }

        ValueCriterion exprValue = (ValueCriterion) criterion;
        if (exprValue.getLeftNode() == null) {
            return null;
        }
        UjoProperty property = exprValue.getLeftNode();
        while (!property.isDirect()) {
            property = ((PathProperty) property).getProperty(0);
        }

        MetaRelation2Many result = handler.findColumnModel(property);
        return result;
    }

    /** Returns the first Database instance. */
    final public <DB extends OrmUjo> DB getFirstDatabase() {
        return (DB) getDatabase(null);
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
        } catch (Exception e) {
            throw new RuntimeException("Can't create database from: " + dbType);
        }
    }

    /** INSERT or UPDATE object into table. */
    public void saveOrUpdate(final OrmUjo bo) throws IllegalStateException {
        if (bo.readSession() == null) {
            save(bo);
        } else {
            update(bo);
        }
    }

    /**
     * If parameter {@link MetaParams#INHERITANCE_MODE INHERITANCE_MODE} is TRUE so modify all parrents.
     * @param bo Object to an action
     * @param saveActio Value TRUE means SAVE, value FALSE means UPDATE.
     * @return Returns Table model for the parameter object.
     */
    private MetaTable modifyParent(final OrmUjo bo) {
        final MetaTable table = handler.findTableModel(bo.getClass());
        if (MetaParams.INHERITANCE_MODE.of(params)) {
            final OrmUjo parent = table.getParent(bo);
            if (parent != null) {
                saveOrUpdate(parent);
            }
        }
        return table;
    }

    /** INSERT object into table. */
    public void save(final OrmUjo bo) throws IllegalStateException {
        JdbcStatement statement = null;
        String sql = "";

        try {
            final MetaTable table = modifyParent(bo);
            table.assignPrimaryKey(bo, this);
            bo.writeSession(this); // Session must be assigned after assignPrimaryKey(). A bug was fixed thans to Pavel Slovacek
            MetaDatabase db = MetaTable.DATABASE.of(table);
            sql = db.getDialect().printInsert(bo, out(128)).toString();
            LOGGER.log(Level.INFO, sql);
            statement = getStatement(db, sql);
            statement.assignValues(bo);
            LOGGER.log(Level.INFO, SQL_VALUES + statement.getAssignedValues());
            statement.executeUpdate(); // execute insert statement
        } catch (Throwable e) {
            rollbackOnly = true;
            throw new IllegalStateException(SQL_ILLEGAL + sql, e);
        } finally {
            MetaDatabase.close(null, statement, null, true);
        }
    }

    /** Database UPDATE of the {@link OrmUjo#readChangedProperties(boolean) modified columns} for the selected object.
     * @see OrmUjo#readChangedProperties(boolean)
     * @return The row count.
     */
    public int update(OrmUjo bo) throws IllegalStateException {
        return update(bo, createPkCriterion(bo), true);
    }

    /** Database Batch UPDATE of the {@link OrmUjo#readChangedProperties(boolean) modified columns} along a criterion.
     * <br />Warning: method does affect to parent objects, see the {@link MetaParams#INHERITANCE_MODE} for more information.
     * @see OrmUjo#readChangedProperties(boolean)
     * @return The row count.
     */
    public int update(OrmUjo bo, Criterion criterion) {
        return update(bo, criterion, false);
    }

    /** Database Batch UPDATE of the {@link OrmUjo#readChangedProperties(boolean) modified columns} along a criterion.
     * @see OrmUjo#readChangedProperties(boolean)
     * @return The row count.
     */
    private int update(OrmUjo bo, Criterion criterion, boolean singleObject) {

        int result = 0;
        JdbcStatement statement = null;
        String sql = null;

        try {
            MetaTable table = singleObject
                ? modifyParent(bo)
                : handler.findTableModel((Class) bo.getClass())
                ;
            MetaDatabase db = MetaTable.DATABASE.of(table);
            List<MetaColumn> changedColumns = getOrmColumns(bo.readChangedProperties(true));
            if (changedColumns.isEmpty()) {
                LOGGER.warning("No changes to update in the object: " + bo);
                return result;
            }
            final MetaTable ormTable = handler.findTableModel(bo.getClass());
            final CriterionDecoder decoder = new CriterionDecoder(criterion, ormTable);
            sql = db.getDialect().printUpdate(ormTable, changedColumns, decoder, out(64)).toString();
            statement = getStatement(db, sql);
            statement.assignValues(bo, changedColumns);
            statement.assignValues(decoder);

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, sql + SQL_VALUES + statement.getAssignedValues());
            }
            result = statement.executeUpdate(); // execute update statement
            bo.writeSession(this);
        } catch (Throwable e) {
            rollbackOnly = true;
            MetaDatabase.close(null, statement, null, false);
            throw new IllegalStateException(SQL_ILLEGAL + sql, e);
        } finally {
            MetaDatabase.close(null, statement, null, true);
        }
        return result;
    }

    /** Delete all object object by the criterion from parameter.
     * <br />Warning 1: method does not remove deleted object from internal cache,
     *       however you can call method clearCache() to release all objects from the cache.
     * <br />Warning 2: method does not delete parent objects, see the {@link MetaParams#INHERITANCE_MODE} for more information.
     * @param criterion filter for deleting tables.
     * @return Returns a number of the realy deleted objects.
     */
    public <UJO extends OrmUjo> int delete(final Criterion<UJO> criterion) {
        final MetaRelation2Many column = getBasicColumn(criterion);
        final MetaTable table = MetaRelation2Many.TABLE.of(column);
        return delete(table, criterion);
    }

    /** Delete one object from the parameters.
     * <br />Warning: method does not remove deleted object from internal cache,
     *       however you can call method clearCache() to release all objects from the cache.
     * @param bo Business object to delete
     * @return Returns a number of the removing is OK.
     */
    public int delete(final OrmUjo bo) {
        MetaTable table = handler.findTableModel(bo.getClass());
        MetaColumn PK = table.getFirstPK();
        Criterion crn = Criterion.where(PK.getProperty(), PK.getValue(bo));
        int result = delete(table, crn);

        if (true) {
            // Remove the bo from an internal cache:
            removeCache(bo, MetaTable.PK.of(table));
        }

        // Delete parrent
        if (MetaParams.INHERITANCE_MODE.of(params)) {
            OrmUjo parent = table.getParent(bo);
            if (parent != null) {
                delete(parent);
            }
        }

        return result;
    }

    /** Delete all object object by the criterion from parameter.
     * <br />Warning: method does not remove deleted object from internal cache,
     *       however you can call method clearCache() to release all objects from the cache.
     * @param tableClass Type of table to delete
     * @param criterion filter for deleting tables.
     * @return Returns a number of the realy deleted objects.
     */
    public <UJO extends OrmUjo> int delete(final Class<UJO> tableClass, final Criterion<UJO> criterion) {
        final MetaTable tableModel = handler.findTableModel(tableClass);
        return delete(tableModel, criterion);
    }

    /** Delete all objects object form parameter
     * <br />Warning: method does not remove deleted object from internal cache,
     *       however you can call method clearCache() to release all objects from the cache.
     * @param tableModel Type of table to delete
     * @param criterion filter for deleting tables.
     * @return Returns a number of the realy deleted objects.
     */
    protected <UJO extends OrmUjo> int delete(final MetaTable tableModel, final Criterion<UJO> criterion) {
        int result = 0;
        JdbcStatement statement = null;
        String sql = "";

        try {
            final MetaDatabase db = MetaTable.DATABASE.of(tableModel);
            final CriterionDecoder decoder = new CriterionDecoder(criterion, tableModel);
            sql = db.getDialect().printDelete(tableModel, decoder, out(64)).toString();
            statement = getStatement(db, sql);
            statement.assignValues(decoder);

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, sql + SQL_VALUES + statement.getAssignedValues());
            }
            result = statement.executeUpdate(); // execute delete statement
        } catch (Throwable e) {
            rollbackOnly = true;
            MetaDatabase.close(null, statement, null, false);
            throw new IllegalStateException(SQL_ILLEGAL + sql, e);
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
            statement = getStatementCallable(db, sql);
            statement.assignValues(procedure);

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, sql + SQL_VALUES + statement.getAssignedValues());
            }
            statement.execute(); // execute call statement
            statement.loadValues(procedure);
        } catch (Throwable e) {
            rollbackOnly = true;
            MetaDatabase.close(null, statement, null, false);
            throw new IllegalStateException(SQL_ILLEGAL + sql, e);
        } finally {
            MetaDatabase.close(null, statement, null, true);
        }
    }

    /** Convert a property array to a column list. */
    protected List<MetaColumn> getOrmColumns(final UjoProperty... properties) {
        final List<MetaColumn> result = new ArrayList<MetaColumn>(properties.length);

        for (UjoProperty property : properties) {
            MetaRelation2Many column = handler.findColumnModel(property);
            if (column instanceof MetaColumn) {
                result.add((MetaColumn) column);
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
            Criterion crn = Criterion.where(ormColumn.getProperty(), ormColumn.getValue(bo));
            result = result != null
                    ? result.and(crn)
                    : crn;
        }
        return result != null
                ? result
                : Criterion.where(false);
    }

    /** Returns a count of rows */
    public <UJO extends OrmUjo> long getRowCount(Query<UJO> query) {
        long result = -1;
        JdbcStatement statement = null;
        ResultSet rs = null;

        MetaTable table = query.getTableModel();
        MetaDatabase db = MetaTable.DATABASE.of(table);
        String sql = "";

        try {
            sql = db.getDialect().printSelect(table, query, true, out(128)).toString();
            LOGGER.log(Level.INFO, sql);

            statement = getStatement(db, sql);
            statement.assignValues(query.getDecoder());
            LOGGER.log(Level.INFO, SQL_VALUES + statement.getAssignedValues());

            rs = statement.executeQuery(); // execute a select statement
            result = rs.next() ? rs.getLong(1) : 0;
        } catch (Exception e) {
            rollbackOnly = true;
            throw new RuntimeException(SQL_ILLEGAL + sql, e);
        } finally {
            MetaDatabase.close(null, statement, rs, false);
        }

        return result;
    }

    /** Run SQL SELECT by query. */
    public JdbcStatement getStatement(Query query) {
        JdbcStatement result = null;
        String sql = "";

        try {
            MetaTable table = query.getTableModel();
            MetaDatabase db = MetaTable.DATABASE.of(table);

            sql = db.getDialect().printSelect(table, query, false, out(360)).toString();
            query.setStatementInfo(sql);
            result = getStatement(db, sql);
            if (query.getLimit()>=0) {
                result.getPreparedStatement().setMaxRows(query.getLimit());
            }
            if (query.getFetchSize()>=0) {
                result.getPreparedStatement().setFetchSize(query.getFetchSize());
            }
            result.assignValues(query.getDecoder());

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, sql + SQL_VALUES + result.getAssignedValues());
            }
            return result;

        } catch (Throwable e) {
            rollbackOnly = true;
            throw new IllegalStateException(SQL_ILLEGAL + sql, e);
        }
    }

    /** Find column by a table type. */
    private MetaColumn findOrmColumn(MetaTable table, Class tableType) {
        for (MetaColumn column : MetaTable.COLUMNS.of(table)) {
            if (column.isForeignKey()
            &&  column.getProperty().getType()==tableType) {     // 1
            //  column.getForeignTable().getType()==tableType) { // 2
                return column;
            }
        }
        return null;
    }

    /** Iterate property of values
     * @param property Table property type of the RelationToMany.
     * @param value A value type of OrmUjo
     */
    public <UJO extends OrmUjo> UjoIterator<UJO> iterateInternal(RelationToMany property, OrmUjo value) {

        final Class tableClass = property.getItemType();
        final MetaTable table = handler.findTableModel(tableClass);
        final MetaColumn fColumn = findOrmColumn(table, value.getClass());

        if (fColumn == null) {
            MetaTable origTable = handler.findTableModel(value.getClass());
            if (origTable.isPersistent()) { // Is it not a DATABASE ?
                String msg = "Can't find a foreign key of " + table + " to a " + value.getClass().getSimpleName();
                throw new IllegalStateException(msg);
            }
        }

        Criterion crit = fColumn != null
                ? Criterion.where(fColumn.getProperty(), value)
                : Criterion.constant(table.getFirstPK().getProperty(), true);
        Query query = createQuery(table.getType(), crit);
        UjoIterator result = UjoIterator.getInstance(query);

        return result;
    }

    /** Get connection for a required database with an autocommit na false. */
    private Connection getConnection_(final MetaDatabase database, final int index) throws IllegalStateException {
        Connection result = connections[index].get(database);
        if (result == null) {
            assertOpenSession();
            try {
                result = database.createConnection();
            } catch (Exception e) {
                throw new IllegalStateException("Can't create an connection for " + database, e);
            }
            connections[index].put(database, result);
        }
        return result;
    }

    /** Get connection for a required database with an autocommit na false. */
    final public Connection getConnection(final MetaDatabase database) throws IllegalStateException {
        return getConnection_(database, 0);
    }

    /** Get sequence connection for a required database with an autocommit na false. For internal use only. */
    final Connection getSeqConnection(final MetaDatabase database) throws IllegalStateException {
        return getConnection_(database, 1);
    }

    /** Create new statement */
    public JdbcStatement getStatement(MetaDatabase database, CharSequence sql) throws SQLException {
        final JdbcStatement result = new JdbcStatement(getConnection(database), sql, handler);
        return result;
    }

    /** Create new statement */
    public JdbcStatement getStatementCallable(MetaDatabase database, String sql) throws SQLException {
        final JdbcStatement result = new JdbcStatement(getConnection(database).prepareCall(sql), handler);
        return result;
    }

    /**
     * Load UJO by a unique id. If a result is not found then a null value is passed.
     * @param tableType Type of Ujo
     * @param id Value ID
     */
    public <UJO extends OrmUjo> UJO load
        ( final Class<UJO> tableType
        , final Object id
        ) throws NoSuchElementException {
        final MetaTable table = handler.findTableModel(tableType);
        final MetaColumn column = table.getFirstPK();

        UjoManager.getInstance().assertAssign(MetaColumn.TABLE_PROPERTY.of(column), id);
        Criterion crn = Criterion.where(column.getProperty(), id);
        Query query = createQuery(crn);

        final OrmUjo result = query.uniqueResult();
        return (UJO) result;
    }

    /**
     * Load UJO by a unique id. If the result is not unique, then an exception is throwed.
     * @param relatedProperty Related property
     * @param id Valud ID
     * @param mandatory If result is mandatory then the method throws an exception if no object was found else returns null;
     */
    @SuppressWarnings("unchecked")
    public <UJO extends OrmUjo> UJO loadInternal
        ( final UjoProperty relatedProperty
        , final Object id
        , final boolean mandatory
        ) throws NoSuchElementException {
        assertOpenSession();
        MetaColumn column = (MetaColumn) handler.findColumnModel(relatedProperty);
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
                return (UJO) r;
            }
        }

        // SELECT DB row:
        Criterion<UJO> crn = Criterion.where(columns.get(0).getProperty(), id);
        UJO result = createQuery(crn).uniqueResult();
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
                } catch (Throwable e) {
                    LOGGER.log(Level.SEVERE, errMessage + db, e);
                    if (exception == null) {
                        exception = e;
                        database = db;
                    }
                }
            }
            cons.clear();
        }
        if (exception != null) {
            throw new IllegalStateException(errMessage + database, exception);
        }
    }

    /** Is the session closed? */
    public boolean isClosed() {
        return closed;
    }

    /** Assert the current session os open. */
    private final void assertOpenSession() throws IllegalStateException {
        if (closed) {
            throw new IllegalStateException("The session is closed ("+hashCode()+")");
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
                cache = new WeakHashMap<CacheKey, OrmUjo>();
                break;
            case SOLID_CACHE:
                cache = new HashMap<CacheKey, OrmUjo>();
                break;
            case NO_CACHE:
                cache = null;
                break;
            default:
                throw new IllegalArgumentException("Unsupported cache policy: " + policy);
        }
    }

    /** Returns parameters */
    final public MetaParams getParameters() {
        return params;
    }

    /** The rollback is allowed only */
    public boolean isRollbackOnly() {
        return rollbackOnly;
    }

    public void markForRolback() {
        rollbackOnly = true;
    }

    /** Build new Forign key.
     * @param property The property must be a relalation type of "many to one".
     * @throws IllegalStateException If a parameter property is not a foreign key.
     */
    public ForeignKey readFK(final OrmUjo ujo, final UjoProperty<?, ? extends OrmUjo> property) throws IllegalStateException {
        MetaColumn column = (MetaColumn) handler.findColumnModel(property);
        if (column!=null && column.isForeignKey()) {
            final Object result = column.getForeignColumns().get(0).getProperty().of(ujo);
            return new ForeignKey(result);
        } else {
            throw new IllegalStateException("The property '" + property + "' is not a foreign key");
        }
    }

    /** Reload values of the persistent object. <br>
     * Note: If the object has implemented the interface
     * {@link ExtendedOrmUjo ExtendedOrmUjo} than foreign keys are reloaded 
     * else a lazy initialization is loaded - for the first property depth.
     * @param ujo The persistent object to relading values.
     * @return The FALSE value means that the object is missing in the database.
     */
    @SuppressWarnings("unchecked")
    public boolean reload(final OrmUjo ujo) {
        if (ujo==null) {
            return false;
        }

        final MetaTable metaTable = handler.findTableModel(ujo.getClass());
        final MetaPKey pkeys = MetaTable.PK.getValue(metaTable);
        boolean fk = ujo instanceof ExtendedOrmUjo;

        Criterion<OrmUjo> criterion = null;
        for (MetaColumn c : MetaPKey.COLUMNS.of(pkeys)) {
            Criterion<OrmUjo> crn = Criterion.where(c.getProperty(), c.getValue(ujo));
            criterion = criterion!=null
                ? criterion.and(crn)
                : crn
                ;
        }

        OrmUjo result = createQuery(criterion).uniqueResult();
        if (result==null) {
            return false;
        }

        // Copy all properties to the original object
        ujo.writeSession(null);
        for (MetaColumn c : MetaTable.COLUMNS.of(metaTable)) {

            if (fk && c.isForeignKey()) {
                // Copy the foreign key only (the workaround of lazy loading):
                UjoProperty p = c.getProperty();
                ujo.writeValue(p, ((ExtendedOrmUjo)result).readFK(p));
            } else if (c.isColumn()) {
                c.getProperty().copy(result, ujo);
            }
        }
        ujo.writeSession(this);
        ujo.readChangedProperties(true); // Clear changed properties

        return true;
    }
    
    /** Create the closed session */
    public static Session newClosedSession(OrmHandler handler) {
        Session result = new Session(handler);
        result.close();
        return result;
    }

}
