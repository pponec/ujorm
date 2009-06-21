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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.property.Getter;


/**
 *
 * @author hampl
 */
public class UjoPropertyGetter implements Getter {

    private UjoProperty ujoProperty = null;
    private String propertyName;
    private Class propertyClass;

    public UjoPropertyGetter(String propertyName, Class theClass) {
        this.propertyName = propertyName;
        initPropertyClass(theClass);
    }

    /**
     * @param target BO
     * @return actual value of property
     * @throws org.hibernate.HibernateException
     * TODO : what if target is not Ujo
     */
    public Object get(Object target) throws HibernateException {
        Ujo ut = (Ujo) target;

        if (ujoProperty == null) {
            initProperty(ut);
        }
        return ut.readValue(ujoProperty);
    }

    /**
     * inspired from BasicPropertyAccessor
     **/
    public Object getForInsert(Object owner, Map mergeMap, SessionImplementor session) throws HibernateException {
        return get(owner);
    }

    public Class getReturnType() {
        return propertyClass;
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
     * TOOD : some better way to get property class in construction time of this
     * 
     **/
    private void initPropertyClass(Class theClass) {
        Ujo u = null;
        try {
            //create new temp instance of target BO
            u = (Ujo) theClass.newInstance();
            //call init property to find property and set property class
            initProperty(u);
            //set property to null ?? will this property be another instance ?
            ujoProperty = null;
            //set temp BO to null
            u = null;

        } catch (InstantiationException ex) {
            Logger.getLogger(UjoPropertyGetter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(UjoPropertyGetter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param target BO
     *
     * find UJOproperty in target and assign to this
     *
     */
    private void initProperty(Ujo target) {

        for (UjoProperty ujoProp : target.readProperties()) {
            if (ujoProp.getName().equalsIgnoreCase(propertyName)) {
                ujoProperty = ujoProp;
                propertyClass = ujoProperty.getType();
                break;
            }
        }
        if (ujoProperty == null) {
            throw new RuntimeException("property with name " + propertyName + "not found in defined BO " + target);
        }
    }
}
