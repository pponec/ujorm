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
import org.jetbrains.annotations.Nullable;
import org.ujorm.Ujo;
import org.ujorm.Key;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.property.Setter;

/**
 * A support for the <a href="https://www.hibernate.org/" target="_top">Hibernate</a> framework.
 * See the <a href="package-summary.html#Hibernate">description</a> for more information.
 * @author hampl
 */
final public class UjoPropertySetter implements Setter {

    final private String propertyName;
    private Key ujoProperty = null;

    public UjoPropertySetter(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * @param target Value must by type of Ujo
     */
    @Override
    public final void set(final Object target, final Object value, final SessionFactoryImplementor factory) throws HibernateException {
        if (ujoProperty == null) {
            ujoProperty = ((Ujo)target).readKeys().findDirectKey(propertyName, true);
        }
        ((Ujo) target).writeValue(ujoProperty, value);
    }

    /**
     * inspired from BackrefPropertyAccessor
     **/
    @Override
    public String getMethodName() {
        return propertyName;
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
