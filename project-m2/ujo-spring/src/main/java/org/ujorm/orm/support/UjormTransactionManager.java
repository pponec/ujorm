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
package org.ujorm.orm.support;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 *
 * @author hampl
 */
public class UjormTransactionManager extends AbstractPlatformTransactionManager {

    private static final Logger LOGGER = Logger.getLogger(UjormTransactionManager.class.getName());
    /** UjoSessionFactory is thread safe
     * so we do not need to use transaction Objects
     */
    private UjoSessionFactory ujoSessionFactory;
    //call stack @Transtactional deep
    private AtomicInteger deep;

    @Override
    protected Object doGetTransaction() throws TransactionException {
        LOGGER.log(Level.INFO, "getTransaction returning new Object");
        //can not return null because it will not call commit
        return new Object();
    }

    @Override
    protected void doBegin(Object o, TransactionDefinition td) throws TransactionException {
        if (incCalling()) {
            LOGGER.log(Level.FINEST, "Auto transaction registred/started");
            ujoSessionFactory.setAutoTransaction(true);
        }
    }

    @Override
    protected void doCommit(DefaultTransactionStatus dts) throws TransactionException {
        dts.isGlobalRollbackOnly();
        if (decCalling()) {
            LOGGER.log(Level.FINEST, "Auto transaction ending (commit/rollback)");

            if (ujoSessionFactory.getDefaultSession().isRollbackOnly()) {
                LOGGER.log(Level.WARNING, "Rolling back transaction becaouse has been mark as roll back only");
                rollback(dts);
                return;
            }
            LOGGER.log(Level.INFO, "commiting transaction ...");
            ujoSessionFactory.getDefaultSession().commit();
            ujoSessionFactory.setHasBeenrollbacked(false);
        }
    }

    @Override
    protected void doRollback(DefaultTransactionStatus dts) throws TransactionException {
        LOGGER.log(Level.WARNING, "rolling back transaction");

        ujoSessionFactory.getDefaultSession().rollback();
        ujoSessionFactory.setHasBeenrollbacked(true);
        deep = null;
    }

    public void setUjoSessionFactory(UjoSessionFactory ujoSessionFactory) {
        this.ujoSessionFactory = ujoSessionFactory;
    }

    /**
     *
     * @return true pokud je na zacatku deep 0 tedy jde o prvni vstup do sluzebni vrstvy
     */
    private boolean incCalling() {
        if (deep == null) {
            deep = new AtomicInteger(1);
            return true;
        } else {
            deep.incrementAndGet();
            return false;
        }
    }

    /**
     *
     * @return true pokud je na konci deep 0 tedy o posledni vystup do sluzebni vrstvy
     */
    private boolean decCalling() {
        if (deep.decrementAndGet() == 0) {
            deep = null;
            return true;
        } else {
            return false;
        }
    }
}
