/*
 *  Copyright 2008-2014 Pavel Ponec
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

package org.ujorm.implementation.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.extensions.Property;
import org.ujorm.extensions.ValueAgent;
import static org.ujorm.extensions.PropertyModifier.*;

/**
 * A Factory key implementation.
 * @see FactoryProperty
 * @author Pavel Ponec
 * @since ujo-tool
 */
public class FactoryProperty<UJO extends Ujo,VALUE>
    extends Property<UJO,VALUE>
    implements ValueAgent<UJO, VALUE>
   {

    /** Constructor */
    final protected Constructor<VALUE> constructor;

    /**
     * Constructor
     * @param name The parameter MUST be a JavaBeans property name. The name will be used for building a Java reflection method name in a time of the first call.
     * @param type Type of a JavaBeans setter input method or getter output method.
     */
    public FactoryProperty(String name, Class<VALUE> type) {
        this(name, type, UNDEFINED_INDEX);
    }

    /**
     * Constructor
     * @param name The parameter MUST be a JavaBeans property name. The name will be used for building a Java reflection method name in a time of the first call.
     * @param type Type of a JavaBeans setter input method or getter output method.
     */
    public FactoryProperty(String name, Class<VALUE> type, int index) {
        super(index);
        init(NAME, name);
        init(TYPE, type);
        Constructor<VALUE> c = null;
        try {
            c = type.getConstructor(Ujo.class, Key.class);
        } catch (RuntimeException | OutOfMemoryError | ReflectiveOperationException e) {
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
        } catch (RuntimeException | ReflectiveOperationException | OutOfMemoryError e) {
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
    protected void throwException(Throwable e) throws IllegalUjormException {
       throw new IllegalUjormException("The class " + getType().getName()
          + " must have got two parameters constructor type of Ujo and Key", e);
    }

    // --------- STATIC METHODS -------------------

    /** Returns a new instance of the Key where the default value is null.
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> FactoryProperty<UJO,VALUE> newInstance(String name, Class<VALUE> type, int index) {
        return new FactoryProperty<UJO,VALUE>(name, type, index);
    }

    /** Returns a new instance of the Key where the default value is null.
     * @hidden
     */
    public static <UJO extends Ujo,VALUE> FactoryProperty<UJO,VALUE> newInstance(String name, Class<VALUE> type) {
        return newInstance(name, type, Property.UNDEFINED_INDEX);
    }


}
