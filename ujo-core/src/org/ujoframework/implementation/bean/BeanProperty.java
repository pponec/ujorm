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
import org.ujoframework.extensions.SuperProperty;
import org.ujoframework.extensions.ValueAgent;

/**
 * A Map property implementation.
 * @see BeanProperty
 * @author Pavel Ponec
 */
public class BeanProperty<UJO extends Ujo,VALUE> 
    extends SuperProperty<UJO,VALUE> 
    implements ValueAgent<UJO, Object> {

    /** Bean Manager instance */
    private final BeanManager<UJO, Object> beanManager;
    
    /**
     * Constructor
     * @param name The parameter MUST be a JavaBeans property name. The name will be used for building a Java reflection method name in a time of the first call.
     * @param type Type of a JavaBeans setter input method or getter output method.
     */
    public BeanProperty(String name, Class<VALUE> type) {
        super(name, type, -1);
        beanManager = BeanManager.getInstance(this);
    }
    
    /** Constructor with a default value
     * @param name The parameter MUST be a JavaBeans property name. The name will be used for building a Java reflection method name in a time of the first call.
     * @param defaultValue The value must be type of VALUE exactly (no child).
     */
    @SuppressWarnings("unchecked")
    public BeanProperty(String name, VALUE defaultValue) {
        this(name, (Class<VALUE>) (Object) defaultValue.getClass());
        setDefault(defaultValue);
    }
    
    /** WARNING: There is recommended to call the method from the method Ujo.writeProperty(...) only.
     * <br>A direct call can bypass a important actions implemented in the writeProperty(method).
     */
    public void writeValue(final UJO ujo, final Object value) throws IllegalArgumentException {
        beanManager.writeValue(ujo, value);
    }
    
    /** WARNING: There is recommended to call the method from the method <code>Ujo.readProperty(...)</code> only.
     * <br>A direct call can bypass a important actions implemented in the <code>readProperty(method)</code>.
     */
    public Object readValue(final UJO ujo) throws IllegalArgumentException {
        return beanManager.readValue(ujo);
    }
        
}
