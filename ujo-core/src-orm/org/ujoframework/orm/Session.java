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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujoframework.implementation.orm.*;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.orm.metaModel.Db;
import org.ujoframework.orm.metaModel.DbColumn;
import org.ujoframework.orm.metaModel.DbTable;
import org.ujoframework.orm.sample.BoDatabase;
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
    private HashMap<Db,Connection> connections = new HashMap<Db,Connection>();


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
        Db database = null;
        String errMessage = "Can't make commit of DB ";

        for (Db db : connections.keySet()) {
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

    public <UJO extends TableUjo> Query<UJO> createQuery(Class<UJO> aClass, Expression<UJO> expA) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public BoDatabase getDatabase() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** Insert object into table. */
    public void save(TableUjo ujo) throws IllegalStateException {
        PreparedStatement ps = null;

        try {
            DbTable table = DbHandler.getInstance().findTableModel((Class) ujo.getClass());
            table.assignPrimaryKey(ujo);
            Db db = DbTable.DATABASE.of(table);
            String sql = db.createInsert(ujo);
            LOGGER.log(Level.INFO, sql);
            Connection conn = getConnection(db);
            ps = conn.prepareStatement(sql);
            assignValues(ps, ujo);
            ps.executeUpdate(); // execute insert statement
        } catch (Throwable e) {
            Db.close(null, ps, null, false);
            throw new IllegalStateException("ILLEGAL SQL INSERT", e);
        }
        Db.close(null, ps, null, true);
    }

    /** Assign values into the prepared statement */
    public int assignValues(PreparedStatement ps, TableUjo table) throws SQLException {
        final DbTable dbTable = DbHandler.getInstance().findTableModel((Class) table.getClass());
        final List<DbColumn> columns = DbTable.COLUMNS.getList(dbTable);
        return assignValues(ps, table, columns, 0);
    }


    /** Assign values into the prepared statement */
    protected int assignValues(PreparedStatement ps, TableUjo table, List<DbColumn> columns, int columnOffset) throws SQLException {
        for (DbColumn column : columns) {
            UjoProperty property = DbColumn.TABLE_PROPERTY.of(column);
            Object value = table!=null ? property.of(table) : null ;

            if (column.isForeignKey()) {
                columnOffset += assignValues(ps, (TableUjo) value, column.getForeignColumns(), columnOffset);
            }
            else if (column.isColumn()) {
                ++columnOffset;
                Class type = property.getType();
                int sqlType = DbColumn.DB_TYPE.of(column).getSqlType();


                try {
                    if (value==null) {
                        ps.setNull(columnOffset, sqlType);
                    } else switch (sqlType) {
                        case Types.DATE:
                            final java.sql.Date sqlDate = new java.sql.Date(((java.util.Date) value).getTime());
                            ps.setDate(columnOffset, sqlDate);
                            break;
                        case Types.TIMESTAMP:
                            final java.sql.Timestamp sqlStamp = new java.sql.Timestamp(((java.util.Date) value).getTime());
                            ps.setTimestamp(columnOffset, sqlStamp);
                            break;
                        case Types.TIME:
                            final java.sql.Time sqlTime = new java.sql.Time(((java.util.Date) value).getTime());
                            ps.setTime(columnOffset, sqlTime);
                            break;
                        default:
                            ps.setObject(columnOffset, value, sqlType);
                            break;
                    }
                } catch (Throwable e) {
                    String msg = String.format("table: %s, column %s, columnOffset: %d, value: %s", table.getClass().getSimpleName(), column, columnOffset, value);
                    throw new IllegalStateException(msg, e);
                }
            }
        }
        return columnOffset;
    }


    public <UJO extends TableUjo> UJO load(Class ujo, Object id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    public <UJO extends TableUjo> UJO single(Query query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public <UJO extends TableUjo> UjoIterator<UJO> iterate(Query<UJO> query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public <UJO extends TableUjo> UjoIterator<UJO> iterate(UjoProperty property) {
        throw new UnsupportedOperationException("Not yet implemented: " + property);
    }

    /** Get connection for a required database and set an autocommit na false. */
    public Connection getConnection(Db database) throws IllegalStateException {
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

     /** Close all DB connections.
     * @throws java.lang.IllegalStateException The exception contains a bug from Connection close;
     */
    public void close() throws IllegalStateException {

        Throwable exception = null;
        Db database = null;
        String errMessage = "Can't close connection for DB ";

        for (Db db : connections.keySet()) {
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
