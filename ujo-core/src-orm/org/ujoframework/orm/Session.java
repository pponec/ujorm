/*
 *  Copyright 2009 Paul Ponec
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
import org.ujoframework.implementation.orm.*;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.extensions.PathProperty;
import org.ujoframework.implementation.orm.RelationToMany;
import org.ujoframework.orm.metaModel.OrmColumn;
import org.ujoframework.orm.metaModel.OrmDatabase;
import org.ujoframework.orm.metaModel.OrmPKey;
import org.ujoframework.orm.metaModel.OrmRelation2Many;
import org.ujoframework.orm.metaModel.OrmTable;
import org.ujoframework.tools.criteria.Expression;
import org.ujoframework.tools.criteria.ExpressionBinary;
import org.ujoframework.tools.criteria.ExpressionValue;

/**
 * ORM session.
 * @author Pavel Ponec
 */
@SuppressWarnings(value = "unchecked")
public class Session {

    /** Commin title to print the SQL VALUES */
    private static final String SQL_VALUES = "\n-- SQL VALUES: ";

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(Session.class.toString());

    /** Handler */
    private final OrmHandler handler;

    /** Database connection */
    private HashMap<OrmDatabase, Connection> connections = new HashMap<OrmDatabase, Connection>();

    /** A session cache */
    private Map<Object, TableUjo> cache = new WeakHashMap<Object, TableUjo>();

    public Session(OrmHandler handler) {
        this.handler = handler;
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

    /** Make commit/rollback for all databases.
     * @param commit if parameters is false than make a rollback.
     */
    protected void commit(boolean commit) {
        Throwable exception = null;
        OrmDatabase database = null;
        String errMessage = "Can't make commit of DB ";

        for (OrmDatabase db : connections.keySet()) {
            try {
                Connection conn = connections.get(db);
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
    }

    public <UJO extends TableUjo> Query<UJO> createQuery(Class<UJO> aClass, Expression<UJO> expression) {
        return new Query<UJO>(aClass, expression, this);
    }

    /** The table class is derived from the first expression column. */
    public <UJO extends TableUjo> Query<UJO> createQuery(Expression<UJO> expression) {
        OrmRelation2Many column = getBasicColumn(expression);
        OrmTable table = OrmRelation2Many.TABLE.of(column);
        return new Query<UJO>(table, expression, this);
    }

    /** Returns the first "basic" column of expression. */
    public OrmRelation2Many getBasicColumn(Expression expression) {
        while (expression.isBinary()) {
            expression = ((ExpressionBinary) expression).getLeftNode();
        }

        ExpressionValue exprValue = (ExpressionValue) expression;
        if (exprValue.getLeftNode()==null) {
            return null;
        }
        UjoProperty property = exprValue.getLeftNode();
        while (!property.isDirect()) {
            property = ((PathProperty) property).getProperty(0);
        }

        OrmRelation2Many result = handler.findColumnModel(property);
        return result;
    }

    /** Returns a Database instance */
    public <DB extends TableUjo> DB getDatabase(Class<DB> dbType) {
        try {
            DB result = dbType.newInstance();
            result.writeSession(this);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Can't create database from: " + dbType);
        }
    }

    /** INSERT object into table. */
    public void save(TableUjo ujo) throws IllegalStateException {
        JdbcStatement statement = null;
        String sql = "";

        try {
            OrmTable table = handler.findTableModel((Class) ujo.getClass());
            ujo.writeSession(this);
            table.assignPrimaryKey(ujo);
            OrmDatabase db = OrmTable.DATABASE.of(table);
            sql = db.getRenderer().printInsert(ujo, out(128)).toString();
            LOGGER.log(Level.INFO, sql);
            statement = getStatement(db, sql);
            statement.assignValues(ujo);
            LOGGER.log(Level.INFO, SQL_VALUES + statement.getAssignedValues());
            statement.executeUpdate(); // execute insert statement
        } catch (Throwable e) {
            OrmDatabase.close(null, statement, null, false);
            throw new IllegalStateException("ILLEGAL SQL INSERT", e);
        } finally {
            OrmDatabase.close(null, statement, null, true);
        }
    }

    /** UPDATE object into table. */
    public int update(TableUjo ujo) throws IllegalStateException {
        int result = 0;
        JdbcStatement statement = null;

        try {
            OrmTable table = handler.findTableModel((Class) ujo.getClass());
            OrmDatabase db = OrmTable.DATABASE.of(table);
            List<OrmColumn> changedColumns = getOrmColumns(ujo.readChangedProperties(true));
            if (changedColumns.size()==0) {
                LOGGER.warning("No changes to update in the object: " + ujo);
                return result;
            }
            final Expression expression = createPkExpression(ujo);
            final OrmTable ormTable = handler.findTableModel(ujo.getClass());
            final ExpressionDecoder decoder = new ExpressionDecoder(expression, ormTable);
            String sql = db.getRenderer().printUpdate(ormTable, changedColumns, decoder, out(64)).toString();
            statement = getStatement(db, sql);
            statement.assignValues(ujo, changedColumns);
            statement.assignValues(decoder);

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, sql + SQL_VALUES + statement.getAssignedValues());
            }
            result = statement.executeUpdate(); // execute update statement
            ujo.writeSession(this);
        } catch (Throwable e) {
            OrmDatabase.close(null, statement, null, false);
            throw new IllegalStateException("ILLEGAL SQL INSERT", e);
        } finally {
            OrmDatabase.close(null, statement, null, true);
        }
        return result;
    }

    /** Delete all object object form parameter.
     * @param tableType Type of table to delete
     * @param expression filter for deleting tables.
     * @return Returns a number of the realy deleted objects.
     */
    public <UJO extends TableUjo> int delete(final Expression<UJO> expression) {
        final OrmRelation2Many column = getBasicColumn(expression);
        final OrmTable table = OrmRelation2Many.TABLE.of(column);
        return delete(table, expression);
    }


    /** Delete all object object form parameter.
     * @param tableType Type of table to delete
     * @param expression filter for deleting tables.
     * @return Returns a number of the realy deleted objects.
     */
    public <UJO extends TableUjo> int delete(final Class<UJO> tableType, final Expression<UJO> expression) {
        final OrmTable table = handler.findTableModel(tableType);
        return delete(table, expression);
    }



    /** Delete all object object form parameter.
     * @param tableType Type of table to delete
     * @param expression filter for deleting tables.
     * @return Returns a number of the realy deleted objects.
     */
    protected <UJO extends TableUjo> int delete(final OrmTable ormTable, final Expression<UJO> expression) {
        int result = 0;
        JdbcStatement statement = null;
        String sql = "";

        try {
            final OrmDatabase db = OrmTable.DATABASE.of(ormTable);
            final ExpressionDecoder decoder = new ExpressionDecoder(expression, ormTable);
            sql = db.getRenderer().printDelete(ormTable, decoder, out(64)).toString();
            statement = getStatement(db, sql);
            statement.assignValues(decoder);

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, sql + SQL_VALUES + statement.getAssignedValues());
            }
            result = statement.executeUpdate(); // execute delete statement
        } catch (Throwable e) {
            OrmDatabase.close(null, statement, null, false);
            throw new IllegalStateException("ILLEGAL SQL UPDATE", e);
        } finally {
            OrmDatabase.close(null, statement, null, true);
        }
        return result;

    }


    /** Convert a property array to a column list. */
    protected List<OrmColumn> getOrmColumns(UjoProperty... properties) {
        final List<OrmColumn> result = new ArrayList<OrmColumn>(properties.length);

        for (UjoProperty property : properties) {
            OrmRelation2Many column = handler.findColumnModel(property);
            if (column instanceof OrmColumn) {
                result.add((OrmColumn) column);
            }
        }
        return result;
    }

    /** Returns an expression by a PrimaryKey */
    protected Expression createPkExpression(TableUjo table) {
        Expression result = null;
        OrmTable ormTable = handler.findTableModel(table.getClass());
        OrmPKey ormKey = OrmTable.PK.of(ormTable);
        List<OrmColumn> keys = OrmPKey.COLUMNS.of(ormKey);

        for (OrmColumn ormColumn : keys) {
            Expression expr = Expression.newInstance(ormColumn.getProperty(), ormColumn.getValue(table));
            result = result!=null
                ? result.and(expr)
                : expr
                ;
        }
        return result!=null
            ? result
            : Expression.newInstance(false)
            ;
    }

    /** Returns a count of rows */
    public <UJO extends TableUjo> long getRowCount(Query<UJO> query) {
        long result = -1;
        JdbcStatement statement = null;
        ResultSet rs = null;

        OrmTable table = query.getTableModel();
        OrmDatabase db = OrmTable.DATABASE.of(table);
        String sql = "";

        try {
            sql = db.getRenderer().printSelect(query, true);
            LOGGER.log(Level.INFO, sql);

            statement = getStatement(db, sql);
            statement.assignValues(query.getDecoder());
            LOGGER.log(Level.INFO, SQL_VALUES + statement.getAssignedValues());

            rs = statement.executeQuery(); // execute a select statement
            result = rs.next() ? rs.getLong(1) : 0 ;
        } catch (Exception e) {
            throw new RuntimeException("Can't perform SQL statement: " + sql, e);
        } finally {
            OrmDatabase.close(null, statement, rs, false);
        }
        return result;
    }

    /** Run SQL SELECT by query. */
    public <UJO extends TableUjo> UjoIterator<UJO> iterate(Query<UJO> query) {
        JdbcStatement statement = null;
        String sql = "";

        try {
            OrmTable table = query.getTableModel();
            OrmDatabase db = OrmTable.DATABASE.of(table);
            sql = db.getRenderer().printSelect(query, false);
            statement = getStatement(db, sql);
            statement.assignValues(query.getDecoder());

            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, sql + SQL_VALUES + statement.getAssignedValues());
            }
            ResultSet rs = statement.executeQuery(); // execute a select statement
            UjoIterator<UJO> result = UjoIterator.getIntance(query, rs);
            return result;

        } catch (Throwable e) {
            throw new IllegalStateException("ILLEGAL SQL SELECT: " + sql, e);
        }
    }

    public <UJO extends TableUjo> UJO single(Query query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** Find column by a table type. */
    private OrmColumn findOrmColumn(OrmTable table, Class tableType) {
        for (OrmColumn column : OrmTable.COLUMNS.of(table)) {
            if (column.isForeignKey()
            &&  column.getProperty().getType()==tableType) {
                return column;
            }
        }
        return null;
    }

    /** Iterate property of values
     * @param property Table property
     * @param value A value type of TableUjo
     */
    public <UJO extends TableUjo> UjoIterator<UJO> iterateInternal(RelationToMany property, TableUjo value) {

        final Class tableClass = property.getItemType();
        final OrmTable table   = handler.findTableModel(tableClass);
        final OrmColumn column = findOrmColumn(table, value.getClass());

        if (column==null) {
            OrmTable origTable = handler.findTableModel(value.getClass());
            if (origTable.isPersistent()) {
                String msg = "Can't find a foreign key of " + table + " to a " + value.getClass().getSimpleName();
                throw new IllegalStateException(msg);
            }
        }

        Expression expr = column!=null 
            ? Expression.newInstance(column.getProperty(), value)
            : Expression.newInstanceTrue(table.getFirstPK().getProperty())
            ;
        Query query = createQuery(tableClass, expr);
        UjoIterator result = iterate(query);

        return result;
    }

    /** Get connection for a required database and set an autocommit na false. */
    public Connection getConnection(OrmDatabase database) throws IllegalStateException {
        Connection result = connections.get(database);
        if (result == null) {
            try {
                result = database.createConnection();
                result.setAutoCommit(false);
            } catch (Exception e) {
                throw new IllegalStateException("Can't create an connection for " + database, e);
            }
            connections.put(database, result);
        }
        return result;
    }

    /** Create new statement */
    public JdbcStatement getStatement(OrmDatabase database, CharSequence sql) throws SQLException {
        final JdbcStatement result = new JdbcStatement(getConnection(database), sql);
        return result;
    }



    /**
     * Load UJO by a unique id. If a result is not found then a null value is passed.
     * @param tableType Type of Ujo
     * @param id Value ID
     */
    public <UJO extends TableUjo> UJO load
        ( final Class<UJO> tableType
        , final Object id
        ) throws NoSuchElementException
    {
        final boolean mandatory = false;
        final OrmTable table = handler.findTableModel(tableType);
        final OrmColumn column = table.getFirstPK();

        Expression expr = Expression.newInstance(column.getProperty(), id);
        Query query = createQuery(expr);
        UjoIterator iterator = iterate(query);

        final UJO result
            = (mandatory || iterator.hasNext())
            ? (UJO) iterator.next()
            : null
            ;
        if (iterator.hasNext()) {
            throw new RuntimeException("Ambiguous key " + id);
        }
        return result;
    }

    /**
     * Load UJO by a unique id. If the result is not unique, then an exception is throwed.
     * @param relatedProperty Related property
     * @param id Valud ID
     * @param mandatory If result is mandatory then the method throws an exception if no object was found else returns null;
     */
    public <UJO extends TableUjo> UJO load
        ( final UjoProperty relatedProperty
        , final Object id
        , final boolean mandatory
        ) throws NoSuchElementException
    {
        OrmColumn column = (OrmColumn) handler.findColumnModel(relatedProperty);
        List<OrmColumn> columns = column.getForeignColumns();
        if (columns.size() != 1) {
            throw new UnsupportedOperationException("There is supported only a one-column foreign key now: " + column);
        }
        Expression expr = Expression.newInstance(columns.get(0).getProperty(), id);
        Query query = createQuery(expr);
        UjoIterator iterator = iterate(query);

        final UJO result
            = (mandatory || iterator.hasNext())
            ? (UJO) iterator.next()
            : null
            ;
        if (iterator.hasNext()) {
            throw new RuntimeException("Ambiguous key " + id);
        }
        return result;
    }

    /** Close all DB connections.
     * @throws java.lang.IllegalStateException The exception contains a bug from Connection close;
     */
    public void close() throws IllegalStateException {

        Throwable exception = null;
        OrmDatabase database = null;
        String errMessage = "Can't close connection for DB ";

        for (OrmDatabase db : connections.keySet()) {
            try {
                Connection conn = connections.get(db);
                conn.close();
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
    }

    /** Create new StringBuilder instance */
    private StringBuilder out(int capacity) {
        return new StringBuilder(capacity);
    }
}
