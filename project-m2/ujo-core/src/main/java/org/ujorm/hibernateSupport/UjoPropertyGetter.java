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

package org.ujorm.hibernateSupport;

import java.lang.reflect.Method;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.ujorm.Ujo;
import org.ujorm.Key;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.property.Getter;
import org.ujorm.core.UjoManager;


/**
 * A support for the <a href="https://www.hibernate.org/" target="_top">Hibernate</a> framework.
 * See the <a href="package-summary.html#Hibernate">description</a> for more information.
 * @author hampl
 */
final public class UjoPropertyGetter implements Getter {

    final private Key ujoProperty;

    /**
     * Create the new UjoPropertyGetter for an Ujo object.
     * @param propertyName The name of Key.
     * @param theClass Class type of Ujo.
     */
    public UjoPropertyGetter(String propertyName, Class theClass) {
        ujoProperty = UjoManager.getInstance().readKeys(theClass).findDirectKey(propertyName, true);
    }

    /**
     * @param target BO
     * @return actual value of key
     * @throws org.hibernate.HibernateException
     */
    @Override  @SuppressWarnings("unchecked")
    public final Object get(final Object target) throws HibernateException {
        return ujoProperty.of((Ujo) target);
    }

    /**
     * inspired from BasicPropertyAccessor
     **/
    @Override
    public Object getForInsert(Object owner, Map mergeMap, SessionImplementor session) throws HibernateException {
        return get(owner);
    }

    @Override
    public Class getReturnType() {
        return ujoProperty.getType();
    }

    /**
     * inspired from BackrefPropertyAccessor
     **/
    @Override
    public String getMethodName() {
        return ujoProperty.getName();
    }

    /**
     * inspired from BackrefPropertyAccessor
     **/
    @Override
    @Nullable
    public Method getMethod() {
        return null;
    }

}
