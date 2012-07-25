/*
 *  Copyright 2012-2012 Pavel Ponec
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
package org.ujorm.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.ujorm.Ujo;
import org.ujorm.UjoAction;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.UjoPropertyList;
import org.ujorm.core.UjoPropertyListImpl;

/**
 * UJO adapter for the Spring Application Context.
 * @author Pavel Ponec
 */
abstract public class AbstractAplicationContextAdapter implements Ujo, ApplicationContextAware {

    /** Spring application Conext */
    private ApplicationContext context;

    /** Default adapter. */
    public AbstractAplicationContextAdapter() {
    }

    /** Assign context directly for the case this instance is not a Spring bean. */
    protected AbstractAplicationContextAdapter(ApplicationContext context) {
        setApplicationContext(context);
    }

    /** A delegat for the method {@link #getBean(org.ujorm.Key). */
    @Override
    @SuppressWarnings("unchecked")
    final public Object readValue(Key property) {
        return getBean(property);
    }

    /** The bean is a factory type, the writeValue method is not supported. */
    @Deprecated
    @Override
    final public void writeValue(Key property, Object value) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Method is not supported");
    }

    /** An delegat for the method {@link ApplicationContext#getBean(java.lang.String, java.lang.Class)} */
    @SuppressWarnings("unchecked")
    public <T> T getBean(Key<? extends AbstractAplicationContextAdapter, T> property) throws BeansException {
        return (T) context.getBean(property.getName(), property.getType());
    }

    /** Assing the application context by Spring framework only. Do not call the method directly. */
    @Override
    final public void setApplicationContext(ApplicationContext ac) throws BeansException {
        if (this.context != null) {
            throw new UnsupportedOperationException("The application context is assigned yet");
        }
        this.context = ac;
    }

    /** Original application context */
    protected ApplicationContext getApplicationContext() {
        return context;
    }

    /** The method must be implemented in the child class. */
    @Override
    public KeyList<?> readKeys() {
        throw new UnsupportedOperationException("The implementation must be in the a child class");
    }


    @Override
    final public UjoPropertyList readProperties() {
        return new UjoPropertyListImpl(readKeys());
    }

    // ------------- STATIC METHODS -------------------

    @Override
    public boolean readAuthorization(UjoAction action, Key property, Object value) {
        return true;
    }

}
