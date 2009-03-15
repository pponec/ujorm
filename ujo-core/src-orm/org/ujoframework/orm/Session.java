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
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujoframework.implementation.orm.*;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.orm.metaModel.OrmDatabase;
import org.ujoframework.orm.metaModel.OrmTable;
import org.ujoframework.orm.sample.Database;
import org.ujoframework.tools.criteria.Expression;

/**
 * ORM session.
 * @author pavel
 */
@SuppressWarnings(value = "unchecked")
public class Session {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(Session.class.toString());


    /** Database connection */
    private HashMap<OrmDatabase,Connection> connections = new HashMap<OrmDatabase,Connection>();


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
                if (exception==null) {
                    exception = e;
                    database = db;
                }
            }
        }
        if (exception!=null) {
            throw new IllegalStateException(errMessage + database, exception);
        }
    }


    public <UJO extends TableUjo> Query<UJO> createQuery(Class<UJO> aClass, Expression<UJO> expression) {
        return new Query<UJO>(aClass, expression, this);
    }

    public Database getDatabase() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** Insert object into table. */
    public void save(TableUjo ujo) throws IllegalStateException {
        JdbcStatement statement = null;

        try {
            OrmTable table = OrmHandler.getInstance().findTableModel((Class) ujo.getClass());
            table.assignPrimaryKey(ujo);
            OrmDatabase db = OrmTable.DATABASE.of(table);
            String sql = db.createInsert(ujo);
            LOGGER.log(Level.INFO, sql);
            statement = getStatement(db, sql);
            statement.assignValues(ujo);
            LOGGER.log(Level.INFO, "VALUES: " + statement.getAssignedValues());
            statement.executeUpdate(); // execute insert statement
        } catch (Throwable e) {
            OrmDatabase.close(null, statement, null, false);
            throw new IllegalStateException("ILLEGAL SQL INSERT", e);
        }
        OrmDatabase.close(null, statement, null, true);
    }

    /** Run SQL SELECT by query. */
    public <UJO extends TableUjo> UjoIterator<UJO> iterate(Query<UJO> query) {
        JdbcStatement statement = null;
        Class<? extends TableUjo> type = query.getTableType();

        try {
            OrmTable table = OrmHandler.getInstance().findTableModel(type);
            OrmDatabase db = OrmTable.DATABASE.of(table);
            String sql = db.createSelect(query);
            LOGGER.log(Level.INFO, sql);
            ResultSet rs = statement.executeQuery(); // execute select statement
        } catch (Throwable e) {
            OrmDatabase.close(null, statement, null, false);
            throw new IllegalStateException("ILLEGAL SQL INSERT", e);
        }
        OrmDatabase.close(null, statement, null, true);
        return null;
    }



    public <UJO extends TableUjo> UJO load(Class ujo, Object id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public <UJO extends TableUjo> UJO single(Query query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public <UJO extends TableUjo> UjoIterator<UJO> iterate(UjoProperty property) {
        throw new UnsupportedOperationException("Not yet implemented: " + property);
    }

    /** Get connection for a required database and set an autocommit na false. */
    public Connection getConnection(OrmDatabase database) throws IllegalStateException {
        Connection result = connections.get(database);
        if (result==null) {
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
                if (exception==null) {
                    exception = e;
                    database = db;
                }
            }
        }
        if (exception!=null) {
            throw new IllegalStateException(errMessage + database, exception);
        }
    }

}
