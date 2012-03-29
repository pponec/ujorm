/*
 *  Copyright 2012 pavel.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.ujorm.orm;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import org.ujorm.orm.annot.PackagePrivate;
import org.ujorm.orm.metaModel.MetaDatabase;

/**
 * Transaction manager.
 * @author Pavel Ponec
 */
final public class Transaction {

    /** Current Sessin */
    final private Session session;
    /** Null value means a root. */
    final private Transaction parent;
    /** Store of the savepoints */
    final private Savepoint[] savepoints ;

    @PackagePrivate Transaction(Session session, Transaction parent) {
        this.session = session;
        this.parent = parent;
        this.savepoints = new Savepoint[session.getHandler().getDatabases().size()];
    }

    /** Returns true, if the transactioni the ROOT. */
    public boolean isRoot() {
        return parent==null;
    }

    /**
     * Assign new Savepoint
     * @param db Database meta-model
     * @param conn Database connection
     * @throws IllegalStateException An envelope for a run-time SQL exception
     */
    @PackagePrivate void assignSavepoint(MetaDatabase db, Connection conn) throws IllegalStateException {
        final int pointer = MetaDatabase.ORDER.of(db);
        if (savepoints[pointer] == null) {
            if (parent!=null) {
                parent.assignSavepoint(db, conn);
            }
            try {
                savepoints[pointer] = conn.setSavepoint();
            } catch (SQLException e) {
                throw new IllegalStateException("Cant save Savepoint", e);
            }
        }
    }

    /** Commit the current level of the beginTransaction.
     * @return Return a parrent Transaction or the value [@code null} for the root transaction.
     */
    public Transaction commit() {
        session.commit(true, this);
        return parent;
    }

    /** Rollback the current level of the beginTransaction.
     * @return Return a parrent Transaction or the value [@code null} for the root transaction.
     */
    public Transaction rollback() {
        session.commit(false, this);
        return parent;
    }

    /** Create a nested transaction */
    public Transaction nestedTransaction() {
        return session.beginTransaction();
    }

    /**
     * Get the current Session.
     * @return Not null values
     */
    public Session getSession() {
        return session;
    }

    /** Returns a parrent transaction.
     * @return  The null value means a root transaction-
     */
    @PackagePrivate Transaction getParent() {
        return parent;
    }

    /** Returns a Savepoint array or {@code null} in case a transaction root. */
    @PackagePrivate Savepoint[] getSavepoints() {
        return savepoints;
    }
}
