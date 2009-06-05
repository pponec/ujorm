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

package org.ujoframework.implementation.factory;

import java.lang.reflect.Constructor;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.AbstractProperty;
import org.ujoframework.extensions.ValueAgent;

/**
 * A Factory property implementation.
 * @see FactoryProperty
 * @author Pavel Ponec
 * @since ujo-tool
 */
public class FactoryProperty<UJO extends Ujo,VALUE>
    extends AbstractProperty<UJO,VALUE>
    implements ValueAgent<UJO, VALUE>
   {
    
    /** Constructor */
    final protected Constructor<VALUE> constructor;

    /**
     * Constructor
     * @param name The parameter MUST be a JavaBeans property name. The name will be used for building a Java reflection method name in a time of the first call.
     * @param type Type of a JavaBeans setter input method or getter output method.
     */
    public FactoryProperty(String name, Class<VALUE> type, int index) {
        super(name, type, index);
        Constructor<VALUE> c = null;
        try {
            c = type.getConstructor(Ujo.class, UjoProperty.class);
        } catch (Throwable e) {
            c = null;
        }
        constructor = c;
    }
    
    /** Create new Value.
     * <br>WARNING: There is recommended to call the method from the method <code>Ujo.readProperty(...)</code> only.
     * <br>A direct call can bypass a important actions implemented in the <code>readProperty(method)</code>.
     */
    public VALUE readValue(final UJO ujo) throws IllegalArgumentException {
        try {
            final VALUE result = constructor!=null 
                ? constructor.newInstance(ujo, this) 
                : getType().newInstance()
                ;
            return result;
        } catch (Throwable e) {
            throwException(e);
            return null;
        }
    }

    /** The method os not implemented. 
     * <br>WARNING: There is recommended to call the method from the method Ujo.writeProperty(...) only.
     * <br>A direct call can bypass a important actions implemented in the writeProperty(method).
     */
    public void writeValue(final UJO ujo, final VALUE value) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }
    
    
    /** Throw an RuntimeException */
    protected void throwException(Throwable e) throws RuntimeException {
       throw new IllegalArgumentException("The class " + getType().getName()
          + " must have got two parameters constructor type of Ujo and UjoProperty", e);
    }

    // --------- STATIC METHODS -------------------

    /** Returns a new instance of property where the default value is null.
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> FactoryProperty<UJO,VALUE> newInstance(String name, Class<VALUE> type, int index) {
        return new FactoryProperty<UJO,VALUE>(name, type, index);
    }

    /** Returns a new instance of property where the default value is null.
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> FactoryProperty<UJO,VALUE> newInstance(String name, Class<VALUE> type) {
        return newInstance(name, type, -1);
    }


}
