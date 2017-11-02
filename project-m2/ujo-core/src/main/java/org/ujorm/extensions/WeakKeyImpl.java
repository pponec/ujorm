/*
 *  Copyright 2012-2014 Pavel Ponec
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
package org.ujorm.extensions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.WeakKey;
import org.ujorm.core.IllegalUjormException;
import org.ujorm.core.UjoManager;
import org.ujorm.core.WeakKeyFactory;
import static org.ujorm.extensions.PropertyModifier.*;

/**
 * The WeakKeyImpl is a default WeakKeyKey implementation without the generic parameter for the domain type.
 * The default implementation does not have the full support of several methods:
 * <ul>
 *   <li>{@link Key#getDomainType()} - method return the result {@code Ujo.class} always </li>
 *   <li>{@link Key#add(org.ujorm.Key)} - method is not supported by the default implementation</li>
 *   <li>{@link Key#descending() } - result is not type of WeakKey</li>
 * </ul>
 * , and some more methods are not fully type-safe.
 * On the other side the interface supports some operations with the {@link List} and {@link Map} interfaces.
 * To create new keys use an instance of the factory class {@link WeakKeyFactory}.
 * @author Pavel Ponec
 */
public class WeakKeyImpl<VALUE>
        extends Property<Ujo, VALUE>
        implements WeakKey<VALUE> {

    /** Constructor for an internal use only.
     * Use the factory {@link WeakKeyFactory} to create a new instance.
     * @see WeakKeyFactory
     */
    WeakKeyImpl(int index) {
        this(null, null, index);
    }

    /** Constructor for an internal use only.
     * Use the factory {@link WeakKeyFactory} to create a new instance.
     * @see WeakKeyFactory
     */
    public WeakKeyImpl(String name, VALUE defaultValue, int index) {
        super(index);
        init(NAME, name);
        init(DEFAULT_VALUE, defaultValue);
    }

    /**
     * It is an extended method for setting an appropriate type safe value to a common Map object.
     */
    @Override
    public void setValue(Map<String, ? super VALUE> map, VALUE value) {
        map.put(getName(), value);
    }

    /**
     * An alias for the method {@link #of(java.util.Map) } .
     * @see #getValue(java.util.Map)
     */
    @Override
    public final VALUE getValue(Map<String, ? super VALUE> map) {
        return of(map);
    }

   /**
     * It is an extended method for getting an appropriate type safe value from a common Map object.
     * @param ujo If a NULL parameter is used then an exception NullPointerException is throwed.
     * @return Returns a type safe value from the map object.
     */
    @Override
    @SuppressWarnings("unchecked")
    public VALUE of(Map<String, ? super VALUE> map) {
        final Object result = map.get(getName());
        return result != null ? (VALUE) result : getDefault();
    }

    /**
     * It is an extended method for getting an appropriate type safe value from a common Map object.
     * In case the size of the list is insufficient, the list is filled by the {@code null} values.
     */
    @Override
    public void setValue(List<? super VALUE> list, VALUE value) {
        final int i = getIndex();
        while (list.size() <= i) {
            list.add(null);
        }
        list.set(i, value);
    }

    /**
     * It is an extended method for getting an appropriate type safe value from a common Map object.
     * @param ujo If a NULL parameter is used then an exception NullPointerException is throwed.
     * @return Returns a type safe value from the map object.
     */
    @Override
    public final VALUE getValue(List<? super VALUE> list) {
        return getValue(list);
    }

    /**
     * A shortcut for the method {@link #getValue(java.util.List) } .
     * @see #getValue(java.util.List)
     */
    @Override
    public VALUE of(List<? super VALUE> list) {
        final int i = getIndex();
        final Object result = i < list.size() ? list.get(i) : null;
        return result != null ? (VALUE) result : getDefault();
    }

    /**
     * Returns an value from the Servlet Request.
     * @servletReqest An object type of: javax.servlet.ServletRequest, where the {@code null} result is replaced for a default value
     * @return Returns object converted to a required type.
     */
    @Override
    @SuppressWarnings("unchecked")
    public final VALUE getRequestValue(Object servletReqest) throws IllegalArgumentException {
        try {
            final Method m = servletReqest.getClass().getMethod("getParameter", String.class);
            final Object result = m.invoke(servletReqest, getName());
            return result!=null
                   ? (VALUE) UjoManager.getInstance().decodeValue(getType(), result.toString())
                   : getDefault() ;
        } catch (RuntimeException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalUjormException("Can't get parameter " + getName() + " from the servletRequest: " + servletReqest, e);
        }
    }

    /** The WeakKeyImpl does not support chaining of the Keys. */
    @Override
    @Deprecated
    public <T> CompositeKey<Ujo, T> add(Key<? super VALUE, T> key) {
        throw new UnsupportedOperationException("An unsupported operation in the " + getClass());
    }
}
