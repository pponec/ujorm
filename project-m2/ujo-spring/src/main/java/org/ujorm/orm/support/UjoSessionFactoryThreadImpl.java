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
import org.ujorm.logger.UjoLogger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.ujorm.logger.UjoLoggerFactory;

import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.Session;

/**
 *
 * @author Hampl
 */
public class UjoSessionFactoryThreadImpl implements UjoSessionFactory, UjoSessionFactoryAOP, UjoSessionFactoryFilter {

    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(UjoSessionFactoryThreadImpl.class);
    final private OrmHandler handler;
    private AtomicInteger deep;
    private Session session;
    private boolean autoTransactionHolder = false;

    public UjoSessionFactoryThreadImpl(OrmHandler handler) {
        this.handler = handler;

    }

    @Override
    public Object aroundSession(ProceedingJoinPoint call) throws Throwable {
        doIncCalling();
        Object result = doCall(call);
        doDecCalling();
        return result;
    }

    private void doIncCalling() {
        if (incCalling()) {
            session = handler.createSession();
            LOGGER.log(Level.FINEST, "opening Ujorm session");
        }
    }

    private void doDecCalling() {
        if (decCalling()) {
            LOGGER.log(Level.FINEST, "closing Ujorm session");
            getSession().close();
        }
    }

    private Object doCall(ProceedingJoinPoint call) throws Throwable {

        if (call.getArgs() != null) {
            return call.proceed(call.getArgs());
        } else {
            return call.proceed();
        }
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
    @SuppressWarnings("unchecked")
    private boolean decCalling() {
        if (deep.decrementAndGet() == 0) {
            deep = null;
            return true;
        } else {
            return false;
        }
    }

    private Session getSession() {
        if (session == null) {
            throw new IllegalStateException("session doesnt exist, check pointcut mapping");
        } else {
            return session;
        }
    }

    @Override
    public Session getDefaultSession() {

        if (!getAutoTransaction()) {
            LOGGER.log(Level.WARNING, "geting session without autotransaction handling !!");
        }
        return getSession();
    }

    @Override
    public void setAutoTransaction(boolean b) {
        autoTransactionHolder = b;
    }

    public boolean getAutoTransaction() {
        return autoTransactionHolder;
    }

    @Override
    public void openSession() {
        doIncCalling();
    }

    @Override
    public void closeSession() {
        doDecCalling();
    }

    boolean isSessionClosed() {
        if (session == null) {
            return true;
        }
        if (session.isClosed()) {
            return true;
        }
        return false;
    }
}
