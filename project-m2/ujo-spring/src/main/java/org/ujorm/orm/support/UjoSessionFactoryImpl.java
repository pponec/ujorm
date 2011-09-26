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

import org.ujorm.logger.UjoLogger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.ujorm.logger.UjoLoggerFactory;
import org.ujorm.orm.OrmHandler;
import org.ujorm.orm.Session;

/**
 *
 * @author Hampl
 */
public class UjoSessionFactoryImpl implements UjoSessionFactory, UjoSessionFactoryAOP, UjoSessionFactoryFilter {

    private static final UjoLogger LOGGER = UjoLoggerFactory.getLogger(UjoSessionFactoryImpl.class);
    final private ThreadLocal<UjoSessionFactoryThreadImpl> holder = new ThreadLocal<UjoSessionFactoryThreadImpl>();
    private OrmHandler handler;

    @Override
    public Session getDefaultSession() {
        return getThreadImpl().getDefaultSession();

    }

    @Override
    public void setAutoTransaction(boolean b) {
        getThreadImpl().setAutoTransaction(b);
    }

    @Override
    public boolean isHasBeenrollbacked() {
        return getThreadImpl().isHasBeenrollbacked();
    }

    @Override
    public void setHasBeenrollbacked(boolean b) {
        getThreadImpl().setHasBeenrollbacked(b);
    }

    @Override
    public Object aroundSession(ProceedingJoinPoint call) throws Throwable {
        Object result = getThreadImpl().aroundSession(call);
        if (getThreadImpl().isSessionClosed()) {
            removeThreadImpl();
        }
        return result;
    }

    @Override
    public void openSession() {
        getThreadImpl().openSession();
    }

    @Override
    public void closeSession() {
        getThreadImpl().closeSession();
        removeThreadImpl();
    }

    private UjoSessionFactoryThreadImpl getThreadImpl() {
        if (holder.get() == null) {
            if (handler == null) {
                throw new NullPointerException("OrmHandler is null! Please set it using setHandler(OrmHandler handler) method");
            }
            holder.set(new UjoSessionFactoryThreadImpl(handler));
        }
        return holder.get();
    }

    private void removeThreadImpl() {
        holder.remove();
    }

    public void setHandler(OrmHandler handler) {
        this.handler = handler;
    }
}
