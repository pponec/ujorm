/*
 *  Copyright 2009 Tomas Hampl
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
package org.ujoframework.hibernateSupport;

import java.lang.reflect.Method;
import java.util.Map;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;

import org.hibernate.HibernateException;
import org.hibernate.LazyInitializationException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.property.Getter;
import org.ujoframework.core.UjoManager;

/**
 *
 * @author hampl
 */
public class UjoLazyPropertyGetter implements Getter {

    final private UjoProperty ujoProperty;

    public UjoLazyPropertyGetter(String propertyName, Class theClass) {
        try {
            Ujo instance = (Ujo) theClass.newInstance();
            ujoProperty = UjoManager.getInstance().findProperty(instance, propertyName, true);
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't create an Ujo instance from the " + theClass, e);
        }
    }

    /**
     * @param target BO
     * @return actual value of property
     * @throws org.hibernate.HibernateException
     */
    public final Object get(final Object target) throws HibernateException {
        try {
            return ((Ujo) target).readValue(ujoProperty);
        } catch (LazyInitializationException lie) {
            solveLazy(target);
            return ((Ujo) target).readValue(ujoProperty);
        }
    }

    /**
     * inspired from BasicPropertyAccessor
     **/
    public Object getForInsert(Object owner, Map mergeMap, SessionImplementor session) throws HibernateException {
        return get(owner);
    }

    public Class getReturnType() {
        return ujoProperty.getType();
    }

    /**
     * inspired from BackrefPropertyAccessor
     **/
    public String getMethodName() {
        return ujoProperty.getName();

    }

    /**
     * inspired from BackrefPropertyAccessor
     **/
    public Method getMethod() {
        return null;
    }

    private void solveLazy(Object target) {
        //TODO : haw to call load, merge or lock ?
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
