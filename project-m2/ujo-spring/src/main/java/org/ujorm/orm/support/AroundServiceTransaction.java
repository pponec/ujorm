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
import org.aspectj.lang.ProceedingJoinPoint;
import org.ujorm.logger.UjoLogger;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.Session;

/**
 * ServiceTransaction
 * @author Hampl
 */
@Deprecated
public class AroundServiceTransaction {

    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(AroundServiceTransaction.class);
    final private UjoSessionFactory ujoSessionFactory;
    final private ThreadLocal<AtomicInteger> deepHolder = new ThreadLocal<AtomicInteger>();

    public AroundServiceTransaction(UjoSessionFactory ujoSessionFactory) {
        this.ujoSessionFactory = ujoSessionFactory;
    }

    protected Session getSession() {
        return ujoSessionFactory.getDefaultSession();
    }

    public Object aroundFilter(ProceedingJoinPoint call) throws Throwable {
        try {
            if (incCalling()) { // the first aroundFilter call
                beginTransaction();
            }
            return doCall(call);
        } catch (Throwable e) {
            markForRollback();
            throw e;
        } finally {
            if (decCalling()) { // the last aroundFilter call
                finishTransaction();
            }
        }
    }

    protected void beginTransaction() {
        LOGGER.log(UjoLogger.TRACE, "Auto transaction registred/started");
        ujoSessionFactory.setAutoTransaction(true);
    }

    protected void markForRollback() {
        getSession().markForRolback();
    }

    /** Don't throw exception - method is called from finally block, so origin exception would be lost */
    protected void finishTransaction (){
        try {
            LOGGER.log(UjoLogger.TRACE, "Auto transaction ending (commit/rollback)");
            final Session session = getSession();
            // rollback if there was error
            if (session.isRollbackOnly()) {
                LOGGER.log(UjoLogger.DEBUG, "Transaction rolling back because it has been marked as rollback-only");
                session.rollback();
            } else {
                session.commit();
            }
        } catch (Throwable e) {
            // don't throw exception - method is called from finally block, so origin exception would be lost
            LOGGER.log(UjoLogger.ERROR, "Transaction close error", e);
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
        if (deepHolder.get() == null) {
            deepHolder.set(new AtomicInteger(1));

            return true;

        } else {
            AtomicInteger deep = deepHolder.get();
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
        final AtomicInteger deep = deepHolder.get();

        if (deep.decrementAndGet() == 0) {
            deepHolder.set(null);
            return true;
        } else {
            return false;
        }
    }

}
