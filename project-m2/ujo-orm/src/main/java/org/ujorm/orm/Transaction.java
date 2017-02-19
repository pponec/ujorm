/*
 *  Copyright 2012 Pavel Ponec
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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.xa.XAResource;
import org.ujorm.core.annot.PackagePrivate;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.orm.metaModel.MetaDatabase;

/**
 * Transaction manager.
 * @author Pavel Ponec
 */
final public class Transaction implements javax.transaction.Transaction{

    /** Current Sessin */
    final private Session session;
    /** Null value means a root. */
    final private Transaction parent;
    /** Store of the savepoints */
    final private Savepoint[] savepoints ;
    /** JTA Status */
    private int status;
    /** Rollback only, default value is {@code false} */
    private boolean rollbackOnly;

    @PackagePrivate Transaction(Session session, Transaction parent) {
        this.session = session;
        this.parent = parent;
        this.status = Status.STATUS_ACTIVE;
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
    @PackagePrivate void assignSavepoint(MetaDatabase db, Connection conn) throws IllegalUjormException {
        final int pointer = MetaDatabase.ORDER.of(db);
        if (savepoints[pointer] == null) {
            if (parent!=null) {
                parent.assignSavepoint(db, conn);
            }
            try {
                savepoints[pointer] = conn.setSavepoint();
                status = Status.STATUS_ACTIVE;
            } catch (SQLException e) {
                throw new IllegalUjormException("Cant save Savepoint", e);
            }
        }
    }

    /** Commit the current level of the beginTransaction. */
    @Override
    public void commit() throws IllegalUjormException {
        if (status==Status.STATUS_ACTIVE && !rollbackOnly) {
            status = Status.STATUS_COMMITTED;
            session.commit(true, this);
        } else if (this.rollbackOnly) {
            throw new SecurityException("Transaction have got status ROLLBACK_ONLY");
        } else {
            throw new IllegalUjormException("Transactíon state isn't STATUS_ACTIVE, but " + status);
        }
    }

    /** Rollback the current level of the beginTransaction. */
    @Override
    public void rollback() throws IllegalUjormException {
        if (status==Status.STATUS_ACTIVE) {
            status = Status.STATUS_ROLLEDBACK;
            session.commit(false, this);
        } else {
            throw new IllegalUjormException("Transactíon state isn't STATUS_ACTIVE, but " + status);
        }
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

    /** Returns a parent transaction.
     * @return  The null value means a root transaction-
     */
    @PackagePrivate Transaction getParent() {
        return parent;
    }

    /** Returns a Savepoint array or {@code null} in case a transaction root. */
    @PackagePrivate Savepoint[] getSavepoints() {
        return savepoints;
    }

    /** {@inheritDoc} */
    public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public boolean enlistResource(XAResource xaRes) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void registerSynchronization(Synchronization sync) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** Rollback transaction only, default value is {@code false} */
    @Override
    public void setRollbackOnly() {
        rollbackOnly = true;
    }

    /** JTA Status */
    @Override
    public int getStatus() {
        return status;
    }
}
