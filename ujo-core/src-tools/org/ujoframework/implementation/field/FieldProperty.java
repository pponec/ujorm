/*
 *  Copyright 2008 Paul Ponec
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

package org.ujoframework.implementation.field;

import org.ujoframework.Ujo;
import org.ujoframework.extensions.SuperProperty;
import org.ujoframework.extensions.ValueAgent;

/**
 * A Map property implementation.
 * @see FieldProperty
 * @author Pavel Ponec
 */
public class FieldProperty<UJO extends Ujo,VALUE> 
    extends SuperProperty<UJO,VALUE> 
    implements ValueAgent<UJO,VALUE> {

    /** Property Agent */
    private final ValueAgent<UJO,VALUE> agent;
    
    /**
     * Constructor
     * @param name The parameter MUST be a JavaBeans property name. The name will be used for building a Java reflection method name in a time of the first call.
     * @param type Type of a JavaBeans setter input method or getter output method.
     */
    public FieldProperty(String name, Class<VALUE> type, ValueAgent<UJO,VALUE> agent) {
        super(name, type, -1);
        this.agent = agent;
    }
    
    /** Constructor with a default value
     * @param name The parameter MUST be a JavaBeans property name. The name will be used for building a Java reflection method name in a time of the first call.
     * @param defaultValue The value must be type of VALUE exactly (no child).
     */
    @SuppressWarnings("unchecked")
    public FieldProperty(String name, VALUE defaultValue, ValueAgent<UJO,VALUE> agent) {
        this(name, (Class<VALUE>) (Object) defaultValue.getClass(), agent);
        setDefault(defaultValue);
    }
    
    /** WARNING: There is recommended to call the method from the method Ujo.writeProperty(...) only.
     * <br>A direct call can bypass a important actions implemented in the writeProperty(method).
     */
    public void writeValue(final UJO ujo, final VALUE value) throws IllegalArgumentException {
        agent.writeValue(ujo, value);
    }
    
    /** WARNING: There is recommended to call the method from the method <code>Ujo.readProperty(...)</code> only.
     * <br>A direct call can bypass a important actions implemented in the <code>readProperty(method)</code>.
     */
    public VALUE readValue(final UJO ujo) throws IllegalArgumentException {
        return agent.readValue(ujo);
    }

}