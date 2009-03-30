/*
 *  Copyright 2007 Paul Ponec
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

package org.ujoframework.implementation.bean;

import org.ujoframework.Ujo;
import org.ujoframework.core.BeanManager;
import org.ujoframework.extensions.AbstractProperty;
import org.ujoframework.extensions.ValueAgent;

/**
 * A Map property implementation.
 * @see BeanProperty
 * @author Pavel Ponec
 */
public class BeanProperty<UJO extends Ujo,VALUE> 
    extends AbstractProperty<UJO,VALUE>
    implements ValueAgent<Object,Object> {

    /** Bean Manager instance */
    private final BeanManager<Object,Object> beanManager;
    
    /**
     * Constructor
     * @param name The parameter MUST be a JavaBeans property name. The name will be used for building a Java reflection method name in a time of the first call.
     * @param type Type of a JavaBeans setter input method or getter output method.
     * @param index An order of property.
     */
    public BeanProperty(String name, Class<VALUE> type, final int index) {
        super(name, type, index);
        beanManager = BeanManager.getInstance(this);
    }
    
    /** Constructor with a default value
     * @param name The parameter MUST be a JavaBeans property name. The name will be used for building a Java reflection method name in a time of the first call.
     * @param defaultValue The value must be type of VALUE exactly (no child).
     * @param index An order of property.
     */
    @SuppressWarnings("unchecked")
    public BeanProperty(String name, VALUE defaultValue, final int index) {
        this(name, (Class<VALUE>) (Object) defaultValue.getClass(), index);
        setDefault(defaultValue);
    }
    
    /** WARNING: There is recommended to call the method from the method Ujo.writeProperty(...) only.
     * <br>A direct call can bypass a important actions implemented in the writeProperty(method).
     */
    public void writeValue(final Object bean, final Object value) throws IllegalArgumentException {
        beanManager.writeValue(bean, value);
    }
    
    /** WARNING: There is recommended to call the method from the method <code>Ujo.readProperty(...)</code> only.
     * <br>A direct call can bypass a important actions implemented in the <code>readProperty(method)</code>.
     */
    public Object readValue(final Object bean) throws IllegalArgumentException {
        return beanManager.readValue(bean);
    }
        
}
