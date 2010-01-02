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

/**
 * The ORM session.
 * <br />Methods of the session are not thread safe.
 * @author Pavel Ponec
 */
@SuppressWarnings(value = "unchecked")
public class Session {

    /** Common title to print the SQL VALUES */
    private static final String SQL_VALUES  = "\n-- SQL VALUES: ";
    /** Exception SQL message prefix */
    public static final String SQL_ILLEGAL = "ILLEGAL SQL: ";

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(Session.class.getName());

    /** Handler. */
    final private OrmHandler handler;
    /** Orm parameters. */
    final private MetaParams params;

    /** Database connections (common and sequence)  */
    final private HashMap<MetaDatabase, Connection>[] connections = new HashMap[]
    { new HashMap<MetaDatabase, Connection>(2) // common connections
    , new HashMap<MetaDatabase, Connection>(2) // sequence connections
    };

    /** A session cache */
    private Map<CacheKey, OrmUjo> cache;

    /** The rollback is allowed only */
    private boolean rollbackOnly = false;

    /** The default constructor */
    Session(OrmHandler handler) {
        this.handler = handler;
        this.params = handler.getParameters();
        this.cache = MetaParams.CACHE_WEAK_MAP.of(params)
            ? new WeakHashMap<CacheKey, OrmUjo>()
            : new HashMap<CacheKey, OrmUjo>()
            ;
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

        for (MetaDatabase db : connections[0].keySet()) {
            try {
                Connection conn = connections[0].get(db);
                if (commit) {
                    conn.commit();
                } else {
                    conn.rollback();
                }
            } catch (Throwable e) {
                LOGGER.log(Level.SEVERE, errMessage + db, e);
                if (exception == null) {
                    exception = e;
                    database = db;
                }
            }
        }
        if (exception != null) {
            throw new IllegalStateException(errMessage + database, exception);
        }
        rollbackOnly = false;
    }

    /** For all rows. */
    public <UJO extends OrmUjo> Query<UJO> createQuery(Class<UJO> aClass) {
        final Criterion<UJO> criterion = Criterion.newInstance(true);
        return createQuery(aClass, criterion);
    }

    public <UJO extends OrmUjo> Query<UJO> createQuery(Class<UJO> aClass, Criterion<UJO> criterion) {
        return new Query<UJO>(aClass, criterion, this);
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
        if (exprValue.getLeftNode()==null) {
            return null;
        }
        UjoProperty property = exprValue.getLeftNode();
        while (!property.isDirect()) {
            property = ((PathProperty) property).getProperty(0);
        }

        MetaRelation2Many result = handler.findColumnModel(property);
        return result;
    }

    /** Returns a Database instance */
    public <DB extends OrmUjo> DB getDatabase(Class<DB> dbType) {
        try {
            DB result = dbType.newInstance();
            result.writeSession(this);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Can't create database from: " + dbType);
        }
    }

    /** INSERT or UPDATE object into table. */
    public void saveOrUpdate(OrmUjo bo) throws IllegalStateException {
        if (bo.readSession()==null) {
            save(bo);
        } else {
            update(bo);
        }
    }

    /** INSERT object into table. */
    public void save(OrmUjo bo) throws IllegalStateException {
        JdbcStatement statement = null;
        String sql = "";

        try {
            MetaTable table = handler.findTableModel((Class) bo.getClass());
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

    /** UPDATE object into table. */
    public int update(OrmUjo bo) throws IllegalStateException {
        int result = 0;
        JdbcStatement statement = null;
        String sql = null;

        try {
            MetaTable table = handler.findTableModel((Class) bo.getClass());
            MetaDatabase db = MetaTable.DATABASE.of(table);
            List<MetaColumn> changedColumns = getOrmColumns(bo.readChangedProperties(true));
            if (changedColumns.size()==0) {
                LOGGER.warning("No changes to update in the object: " + bo);
                return result;
            }
            final Criterion criterion = createPkCriterion(bo);
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
     * <br />Warning: method does not remove deleted object from internal cache,
     *       however you can call method clearCache() to release all objects from the cache.
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
    public boolean delete(final OrmUjo bo) {
        MetaTable table = getHandler().findTableModel(bo.getClass());
        MetaColumn PK = table.getFirstPK();
        Criterion crn = Criterion.newInstance(PK.getProperty(), PK.getValue(bo));
        int result = delete(table, crn);

        if (true) {
            // Remove the bo from an internal cache:
            removeCache(bo, MetaTable.PK.of(table));
        }
        return result>0;
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
            Criterion crn = Criterion.newInstance(ormColumn.getProperty(), ormColumn.getValue(bo));
            result = result!=null
                ? result.and(crn)
                : crn
                ;
        }
        return result!=null
            ? result
            : Criterion.newInstance(false)
            ;
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
            result = rs.next() ? rs.getLong(1) : 0 ;
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
            result = getStatement(db, sql);
            if (query.getMaxRows()!=0) result.getPreparedStatement().setMaxRows(query.getMaxRows());
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
            &&  column.getProperty().getType()==tableType) {
                return column;
            }
        }
        return null;
    }

    /** Iterate property of values
     * @param property Table property
     * @param value A value type of OrmUjo
     */
    public <UJO extends OrmUjo> UjoIterator<UJO> iterateInternal(RelationToMany property, OrmUjo value) {

        final Class tableClass = property.getItemType();
        final MetaTable table   = handler.findTableModel(tableClass);
        final MetaColumn column = findOrmColumn(table, value.getClass());

        if (column==null) {
            MetaTable origTable = handler.findTableModel(value.getClass());
            if (origTable.isPersistent()) { // Is it not a DATABASE ?
                String msg = "Can't find a foreign key of " + table + " to a " + value.getClass().getSimpleName();
                throw new IllegalStateException(msg);
            }
        }

        Criterion crit = column!=null
            ? Criterion.newInstance(column.getProperty(), value)
            : Criterion.newInstanceTrue(table.getFirstPK().getProperty())
            ;
        Query query = createQuery(tableClass, crit);
        UjoIterator result = UjoIterator.getInstance(query);

        return result;
    }

    /** Get connection for a required database with an autocommit na false. */
    private Connection getConnection_(final MetaDatabase database, final int index) throws IllegalStateException {
        Connection result = connections[index].get(database);
        if (result == null) {
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
        final JdbcStatement result = new JdbcStatement(getConnection(database), sql, getHandler());
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
        ) throws NoSuchElementException
    {
        final MetaTable table = handler.findTableModel(tableType);
        final MetaColumn column = table.getFirstPK();

        UjoManager.getInstance().assertAssign(MetaColumn.TABLE_PROPERTY.of(column), id);
        Criterion crn = Criterion.newInstance(column.getProperty(), id);
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
        ) throws NoSuchElementException
    {
        MetaColumn column = (MetaColumn) handler.findColumnModel(relatedProperty);
        List<MetaColumn> columns = column.getForeignColumns();
        if (columns.size() != 1) {
            throw new UnsupportedOperationException("There is supported only a one-column foreign key now: " + column);
        }

        // FIND CACHE:
        boolean cache = params.isCacheEnabled();
        MetaTable tableModel = null;
        if (cache) {
            tableModel = MetaColumn.TABLE.of(columns.get(0));
            OrmUjo r = findCache(tableModel.getType(), id);
            if (r!=null) {
                return (UJO) r;
            }
        }

        // SELECT DB
        Criterion crn = Criterion.newInstance(columns.get(0).getProperty(), id);
        Query query = createQuery(crn);
        UjoIterator iterator = UjoIterator.getInstance(query);

        final UJO result
            = (mandatory || iterator.hasNext())
            ? (UJO) iterator.next()
            : null
            ;
        if (iterator.hasNext()) {
            throw new RuntimeException("Ambiguous key " + id);
        }
        
        if (cache) {
            addCache(result, MetaTable.PK.of(tableModel));
        }
        return result;
    }

    /** Close and release all DB connections.
     * @throws java.lang.IllegalStateException The exception contains a bug from Connection close;
     */
    @SuppressWarnings("unchecked")
    public void close() throws IllegalStateException {

        cache = null;
        Throwable exception = null;
        MetaDatabase database = null;
        String errMessage = "Can't close connection for DB ";

        for (HashMap<MetaDatabase,Connection> cons : connections) {
            for (MetaDatabase db : cons.keySet()) {
                try {
                    Connection conn = cons.get(db);
                    if (conn!=null) conn.close();
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
        return result!=null;
    }


    /** Find object from internal cache */
    public OrmUjo findCache(Class type, Object pkey) {
        CacheKey key = CacheKey.newInstance(type, pkey);
        return cache.get(key);
    }

    /** Find object from internal cache */
    public OrmUjo findCache(Class type, Object... pkeys) {
        CacheKey key = CacheKey.newInstance(type, pkeys);
        return cache.get(key);
    }

    /** Clear the cache. */
    public void cacheClear() {
        new HashMap().clear();
        cache.clear();
    }

    /** Returns parameters */
    final public MetaParams getParameters() {
        return params;
    }

    /** The rollback is allowed only */
    public boolean isRollbackOnly() {
        return rollbackOnly;
    }


}

