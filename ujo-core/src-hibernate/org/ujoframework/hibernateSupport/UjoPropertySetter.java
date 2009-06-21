/*
 *  Copyright 2009 Tomáš Hampl
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
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.property.Setter;
import org.ujoframework.core.UjoManager;

/**
 *
 * @author hampl
 */
public class UjoPropertySetter implements Setter {

    final private String propertyName;
    private UjoProperty ujoProperty = null;

    public UjoPropertySetter(String propertyName) {
        this.propertyName = propertyName;
    }

    public void set(Object target, Object value, SessionFactoryImplementor factory) throws HibernateException {
        Ujo ut = (Ujo) target;
        if (ujoProperty == null) {
            initProperty(ut);
        }
        ut.writeValue(ujoProperty, value);
    }

    /**
     * inspired from BackrefPropertyAccessor
     **/
    public String getMethodName() {
        return null;
    }

    /**
     * inspired from BackrefPropertyAccessor
     **/
    public Method getMethod() {
        return null;
    }

    /**
     *init the ujo property reference
     **/
    private void initProperty(Ujo target) {
        ujoProperty = UjoManager.getInstance().findProperty(target, propertyName, true);
    }
}
