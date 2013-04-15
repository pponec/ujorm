/*
 *  Copyright 2009-2010 Tomas Hampl
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
package org.ujorm.spring;

import java.util.logging.Level;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.Session;

/**
 * UjormTransactionManager, the singleton class
 * @author Tomáš Hampl, Pavel Ponec
 */
public class UjormTransactionManager extends AbstractPlatformTransactionManager {

    /** Logger */
    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(UjormTransactionManager.class);
    /** Orm Handler */
    private OrmHandler handler;
    /** Thrad local session */
    final private ThreadLocal<Session> session = new ThreadLocal<Session>();
    /** Dummy transaction object */
    final private Object dummy = new Object();

    /** Assign OrmHandler Provider */
    public void setOrmHandlerProvider(OrmHandlerProvider ormHandlerProvider) {
        this.handler = ormHandlerProvider.getOrmHandler();
    }

    /** Return a transaction object for the current transaction state. */
    @Override
    protected Object doGetTransaction() throws TransactionException {
        LOGGER.log(Level.INFO, "getTransaction returning new Object");
        return dummy;
    }

    /** Begin a new transaction with semantics according to the given transaction */
    @Override
    protected void doBegin(Object tr, TransactionDefinition td) throws TransactionException {
        LOGGER.log(Level.FINEST, "Auto transaction registred/started");
        Session localSession = session.get();
        if (localSession == null) {
            localSession = handler.createSession();
            session.set(localSession);
        }
        localSession.beginTransaction();
    }

    /** Begin a new transaction with semantics according to the given transaction */
    protected void doEnd(boolean commit, Session localSession) throws TransactionException {
        if (localSession.isClosed()) {
            throw new TransactionException("Transaction is closed") {};
        }
        try {
            if (commit) {
               LOGGER.log(Level.FINEST, "Auto transaction ending with the Commit");
               localSession.commitTransaction();
            } else {
               LOGGER.log(Level.FINEST, "Auto transaction ending with the Rollback");
               localSession.rollbackTransaction();
            }
        } finally {
            if (localSession.getTransaction() == null) {
                localSession.close();
                session.remove();
            }
        }
    }

    /** Perform an actual commit of the given transaction. */
    @Override
    protected void doCommit(DefaultTransactionStatus dts) throws TransactionException {
        final Session localSession = getLocalSession();
        final boolean rollbackOnly = dts.isGlobalRollbackOnly() || localSession.isRollbackOnly();
        if (rollbackOnly) {
            LOGGER.log(Level.WARNING, "Rolling back transaction becaouse has been mark as roll back only");
        }
        doEnd(!rollbackOnly, localSession);
    }

    /** Perform an actual rollback of the given transaction. */
    @Override
    protected void doRollback(DefaultTransactionStatus dts) throws TransactionException {
        LOGGER.log(Level.WARNING, "rolling back transaction");
        doEnd(false, getLocalSession());
    }

    /** Return a local default session */
    public Session getLocalSession() throws IllegalStateException {
        final Session result = session.get();
        if (result == null) {
            throw new IllegalStateException("Session does not exists, check pointcut mapping");
        }
        return result;
    }
}
