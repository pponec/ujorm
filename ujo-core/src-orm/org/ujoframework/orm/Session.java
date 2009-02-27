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
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujoframework.implementation.orm.*;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.orm.metaModel.Db;
import org.ujoframework.orm.metaModel.DbTable;
import org.ujoframework.orm.sample.BoDatabase;
import org.ujoframework.tools.criteria.Expression;

/**
 * ORM session.
 * @author pavel
 */
public class Session {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(Session.class.toString());


    /** Database connection */
    private HashMap<Db,Connection> connections = new HashMap<Db,Connection>();

    public void commit() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public <UJO extends TableUjo> Query<UJO> createQuery(Class<UJO> aClass, Expression<UJO> expA) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public BoDatabase getDatabase() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void rollback() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** Insert object into table. */
    public void save(TableUjo ujo) {
        @SuppressWarnings("unchecked")
        DbTable table = DbHandler.getInstance().findTableModel( (Class) ujo.getClass());
        table.assignPrimaryKey(ujo);
        Db db = DbTable.DATABASE.of(table);
        String sql = db.createInsert(ujo);
        Connection conn = getConnection(db);

        
        
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

    /** Get connection for a required database. */
    public Connection getConnection(Db database) throws IllegalStateException {
        Connection result = connections.get(database);
        if (result==null) {
            try {
                result = database.createConnection();
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
    public void closeSession() throws IllegalStateException {

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
