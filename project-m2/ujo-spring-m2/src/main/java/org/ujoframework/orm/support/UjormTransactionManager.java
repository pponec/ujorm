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

package org.ujoframework.orm.support;

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

    private static Logger LOGGER = Logger.getLogger(UjormTransactionManager.class.getName());
    //UjoSessionFactory is thread safe so we do not need to use transaction Objects
    private UjoSessionFactory ujoSessionFactory;

    @Override
    protected Object doGetTransaction() throws TransactionException {
        LOGGER.log(Level.INFO, "getTransaction returning new Object");
        //    ujoSessionFactory.
        //can not return null because it will not call commit
        return new Object();
    }

    @Override
    protected void doBegin(Object o, TransactionDefinition td) throws TransactionException {
        
        LOGGER.log(Level.INFO, "Auto transaction registred/started");
        ujoSessionFactory.setAutoTransaction(true);
    }

    @Override
    protected void doCommit(DefaultTransactionStatus dts) throws TransactionException {


        dts.isGlobalRollbackOnly();
        if (ujoSessionFactory.getDefaultSession().isRollbackOnly()) {
            LOGGER.log(Level.WARNING, "Rolling back transaction becaouse has been mark as roll back only");

            rollback(dts);
            return;
        }

        LOGGER.log(Level.INFO, "commiting transaction ...");

        ujoSessionFactory.getDefaultSession().commit();
        ujoSessionFactory.setHasBeenrollbacked(false);
    }

    @Override
    protected void doRollback(DefaultTransactionStatus dts) throws TransactionException {
        LOGGER.log(Level.WARNING, "roling back transaction");

        ujoSessionFactory.getDefaultSession().rollback();
        ujoSessionFactory.setHasBeenrollbacked(true);
    }

    public void setUjoSessionFactory(UjoSessionFactory ujoSessionFactory) {
        this.ujoSessionFactory = ujoSessionFactory;
    }
}
