/*
 * Copyright 2007-2026 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ujorm.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * This interface is a descriptor of the {@link Ujo} attribute. The Key contains only meta-data
 * and therefore the Property implementation never contains business data. 
 * Each instance of the Key must be located in the {@code public static final} field of some Ujo implementation.
 * The Key can't have a serializable feature never, because its instance is the unique for a related java field.
 * An appropriate solution solution for serialization is to use a decorator class KeyRing.
 * <br>See a <a href="package-summary.html#UJO">general information</a> about current framework or see some implementations.
 *
 * @author Pavel Ponec
 * @see Ujo
 * @opt attributes
 * @opt operations
 */
@Unmodifiable
@SuppressWarnings("deprecation")
public interface Key <UJO extends Object,VALUE> extends CharSequence, Comparable<Key> {

    /** Returns a name of the Key. */
    @NotNull String getName();

    /** Returns a name of the Key including  a simple class name (without package)
     * separated by the dot (.) character. */
    @NotNull String getFullName();

    /** Returns a class of the current key. */
    @NotNull Class<VALUE> getType();

    /** Is the value type primitive? */
    default boolean primitiveType() {
        return getType().isPrimitive();
    }

    /** Returns a class of the domain Ujo object. */
    @NotNull Class<UJO> getDomainType();

    /** Name of database column */
    @NotNull String columnName();

    /** Is the database column required? */
    boolean required();

    /** Is the column primary key ? */
    boolean primaryKey();

    /**
     * It is a basic method for setting an appropriate type safe value to an Ujo object.
     * <br>The method calls a method
     * {@link Ujo#setValue(Key, Object)}
     * always.
     * @param bean Related Ujo object
     * @param value A value to assign.
     * @see Ujo#setValue(Key, Object)
     */
    void setValue(@NotNull UJO bean, @Nullable VALUE value) throws UnsupportedOperationException;

    /**
     * It is a basic method for getting an appropriate type safe value from an Ujo object.
     * @param bean If a NULL parameter is used then an exception NullPointerException is throwed.
     * @return Returns a type safe value from the ujo object.
     * @see Ujo#getValue(Key)
     * @see #getValue(UJO)
     */
    VALUE getValue(@NotNull UJO bean);

    /** Method returns a default value for substitution of the <code>null</code> value for the current key.
     * The feature is purposeful only if the default value is not <code>null</code> and a propert value is <code>null</code> .
     */
    @Nullable VALUE getDefaultValue();

    /** Indicates whether a parameter value of the ujo "equal to" this key default value. */
    boolean isDefault(@NotNull UJO ujo);

    /**
     * An alias for the method {@link #of(UJO)}.
     */
    default VALUE of(@NotNull final UJO ujo) {
        return getValue(ujo);
    };

    /** Returns a key index .
     * <br>The index is reasonable for an implementation an <code>ArrayUjo</code> class and the value is used is used
     * <br>for a sorting of Keys in a method <code>UjoManager.readProperties(Class type)</code> .
     */
    int getIndex();

    /** Method returns a default value for substitution of the <code>null</code> value for the current key.
     * The feature is purposeful only if the default value is not <code>null</code> and a property value is <code>null</code> .
     * @see Ujo#getValue(Key)
     */
    @Nullable
    default VALUE getDefault() {
        return null;
    }

    /** Returns true if the key type is a type or subtype of the parameter class. */
    default boolean isTypeOf(@NotNull final Class type) {
        return getType().isAssignableFrom(type);
    }

    /** Returns true if the domain type is a type or subtype of the parameter class. */
    default boolean isDomainOf(@NotNull final Class type) {
        return getDomainType().isAssignableFrom(type);
    }

    @Override
    default int length() {
        return getName().length();
    }

    @Override
    default char charAt(final int index) {
        return getName().charAt(index);
    }

    @Override
    @NotNull
    default CharSequence subSequence(final int start, final int end) {
        return getName().subSequence(start, end);
    }

    @Override
    default int compareTo(@NotNull final Key o) {
        final var i1 = this.getIndex();
        final var i2 = o.getIndex();
        return i1 < i2 ? -1 : i1 == i2 ? 0 : 1;
    }
}
