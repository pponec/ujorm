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
package org.ujorm.extensions;

import java.util.List;
import java.util.Map;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.Ujo;

/**
 * A weak Key implementation for a common use.
 * @author Pavel Ponec
 */
public class WeakKeyImpl<VALUE>
        extends Property<Ujo, VALUE>
        implements WeakKey<VALUE> {

    /** Default constructor */
    WeakKeyImpl() {
        super(UNDEFINED_INDEX);
    }

    /** Constructor with property name */
    WeakKeyImpl(String name) {
        super(UNDEFINED_INDEX);
        init(name, null, null, null, UNDEFINED_INDEX, false);
    }

    /**
     * It is an extended method for setting an appropriate type safe value to a common Map object.
     */
    @Override
    public void setValue(Map<String, ? super VALUE> map, VALUE value) {
        map.put(getName(), value);
    }

    /**
     * It is an extended method for getting an appropriate type safe value from a common Map object.
     * @param ujo If a NULL parameter is used then an exception NullPointerException is throwed.
     * @return Returns a type safe value from the map object.
     */
    @Override
    final public VALUE getValue(Map<String, ? super VALUE> map) {
        return of(map);
    }

    /**
     * A shortcut for the method {@link #getValue(java.util.Map) } .
     * @see #getValue(java.util.Map)
     */
    @Override
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
    final public VALUE getValue(List<? super VALUE> list) {
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

    /** The WeakKeyImpl does not support chaining of the Keys. */
    @Override
    @Deprecated
    public <VALUE_PAR> CompositeKey<Ujo, VALUE_PAR> add(Key<? extends VALUE, VALUE_PAR> property) {
        throw new UnsupportedOperationException("An unsupported operation in the " + getClass());
    }
}
