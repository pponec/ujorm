/*
 *  Copyright 2009-2014 Tomas Hampl, Pavel Ponec
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

import javax.annotation.Nonnull;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.OrmHandlerProvider;
import org.ujorm.orm.Session;
import org.ujorm.tools.Assert;

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
    @Nonnull
    final private Object dummy = new Object();

    /** Assign a provider of the OrmHandler */
    public void setOrmHandlerProvider(@Nonnull final OrmHandlerProvider ormHandlerProvider) {
        this.handler = ormHandlerProvider.getOrmHandler();
    }

    /** Return a transaction object for the current transaction state. */
    @Override @Nonnull
    protected Object doGetTransaction() throws TransactionException {
        LOGGER.log(UjoLogger.TRACE, "GetTransaction is running");
        return dummy;
    }

    /** Begin a new transaction with semantics according to the given transaction */
    @Override
    protected void doBegin(final Object tr, final TransactionDefinition td) throws TransactionException {
        LOGGER.log(UjoLogger.TRACE, "Auto transaction registred/started");
        Session localSession = session.get();
        if (localSession == null) {
            localSession = handler.createSession();
            session.set(localSession);
        }
        localSession.beginTransaction();
    }

    /** Begin a new transaction with semantics according to the given transaction */
    protected void doEnd(final boolean commit, @Nonnull final Session localSession) throws TransactionException {
        if (localSession.isClosed()) {
            final String msg = "Transaction is closed, can't be " + (commit ? "commited" : "rollbacked");
            throw new TransactionException(msg) {
                private static final long serialVersionUID = 1L;
            };
        }
        try {
            if (LOGGER.isLoggable(UjoLogger.TRACE)) {
               LOGGER.log(UjoLogger.TRACE
                       , "Transaction is finished on the "
                       + (commit ? "Commit" : "Rollback"));
            }
            if (commit) {
               localSession.commitTransaction();
            } else try {
               localSession.rollbackTransaction();
            } catch (Exception e) {
               // The rollback exception must not be thrown, because the original one could be overlapped.
               LOGGER.log(UjoLogger.ERROR, "Rollback failed", e);
            }
        } finally {
            if (localSession.getTransaction() == null) {
                session.remove();
                localSession.close();
            }
        }
    }

    /** Perform an actual commit of the given transaction. */
    @Override
    protected void doCommit(@Nonnull final DefaultTransactionStatus dts) throws TransactionException {
        final Session localSession = getLocalSession();
        final boolean rollbackOnly = dts.isGlobalRollbackOnly() || localSession.isRollbackOnly();
        if (rollbackOnly) {
            LOGGER.log(UjoLogger.WARN, "Rolling back transaction becaouse has been mark as roll back only");
        }
        doEnd(!rollbackOnly, localSession);
    }

    /** Perform an actual rollback of the given transaction. */
    @Override
    protected void doRollback(final DefaultTransactionStatus dts) throws TransactionException {
        doEnd(false, getLocalSession());
    }

    /** Return a local default session */
    @Nonnull
    public Session getLocalSession() throws IllegalStateException {
        final Session result = session.get();
        Assert.state(result != null, "ORM session does not exists, check pointcut mapping");
        return result;
    }
}
