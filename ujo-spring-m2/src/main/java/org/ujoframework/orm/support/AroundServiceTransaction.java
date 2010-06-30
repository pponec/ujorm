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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.ujoframework.orm.Session;

/**
 *
 * @author Hampl
 */
//TODO : multithread testing
public class AroundServiceTransaction /*extends AbstractServiceImpl*/ {

    private static Logger LOGGER = Logger.getLogger(AroundServiceTransaction.class.getName());
    final private UjoSessionFactory ujoSessionFactory;
    final private ThreadLocal<AtomicInteger> deepHolder = new ThreadLocal<AtomicInteger>();

    public AroundServiceTransaction(UjoSessionFactory ujoSessionFactory) {
        this.ujoSessionFactory = ujoSessionFactory;
    }

    protected Session getSession() {
        return ujoSessionFactory.getDefaultSession();
    }

    public Object aroundFilter(ProceedingJoinPoint call) throws Throwable {

        Exception ex = null;
        Object result = null;

        if (incCalling()) {
            LOGGER.log(Level.FINEST, "Auto transaction registred/started");
            ujoSessionFactory.setAutoTransaction(true);
        }
        try {
            result = doCall(call);
        } catch (Exception e) {
            ex = e;
        }
        if (decCalling()) {
            LOGGER.log(Level.FINEST, "Auto transaction ending (commit/rollback)");
            //rolback and write warning if there was sql error
            if (getSession().isRollbackOnly()) {
                LOGGER.log(Level.WARNING, "Rolling back transaction becaouse has been mark as roll back only", ex);
                rollback();
                // if exception is not catched send it
                return doReturn(ex, result);
            } else {
                //there was no sql error
                commit();
                return doReturn(ex, result);
            }
        } else {//this is not las aop call
            return doReturn(ex, result);

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
        AtomicInteger deep = deepHolder.get();


        if (deep.decrementAndGet() == 0) {
            deepHolder.set(null);


            return true;


        } else {
            return false;


        }
    }

    private void rollback() {
        getSession().rollback();
        ujoSessionFactory.setHasBeenrollbacked(true);



    }

    private void commit() {
        getSession().commit();
        ujoSessionFactory.setHasBeenrollbacked(false);

    }

    private Object doReturn(Exception ex, Object result) throws Throwable {
        if (ex != null) {
            throw ex;
        } else {
            return result;
        }
    }
}
