/*
 * Copyright 2007-2026 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ujorm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * This interface is a descriptor of the {@link Ujo} attribute. The Key contains only meta-data;
 * therefore, the implementation never contains business data.
 * Each instance of the Key must be located in a {@code public static final} field of a Ujo implementation.
 * The Key is not intended to be serializable because each instance is unique to its related Java field.
 * An appropriate solution for serialization is to use a decorator class, such as {@code KeyRing}.
 * <br>See <a href="package-summary.html#UJO">general information</a> about the framework or explore existing implementations.
 *
 * @author Pavel Ponec
 * @see Ujo
 */
@Unmodifiable
@SuppressWarnings("deprecation")
public interface Key<UJO, VALUE> extends CharSequence, Comparable<Key> {

    /** Returns the name of the Key (e.g., "name"). */
    @NotNull String name();

    /** Returns the full name of the Key, including the simple name of the
     * domain class separated by a dot (e.g., "Employee.name").
     */
    @NotNull String fullName();

    /** Returns the type of the value associated with this key. */
    @NotNull Class<VALUE> type();

    /** Checks if the value type is a primitive. */
    default boolean primitiveType() {
        return type().isPrimitive();
    }

    /** Returns the class of the domain Ujo object. */
    @NotNull Class<UJO> domainClass();

    /** Returns the name of the database column label. */
    @NotNull String columnLabel();

    /** Indicates whether the database column is required. */
    boolean required();

    /** Indicates whether the column is a primary key. */
    boolean primaryKey();

    /** Indicates whether the column is a foreign key. */
    boolean foreignKey();

    /**
     * Sets a type-safe value to the specified Ujo object.
     * This method always calls {@link Ujo#setValue(Key, Object)}.
     *
     * @param bean The target Ujo object.
     * @param value The value to assign.
     * @throws UnsupportedOperationException If the key is read-only.
     * @see Ujo#setValue(Key, Object)
     */
    void setValue(@NotNull UJO bean, @Nullable VALUE value) throws UnsupportedOperationException;

    /**
     * Gets a type-safe value from the specified Ujo object.
     *
     * @param bean The source Ujo object. Must not be null.
     * @return The type-safe value from the Ujo object.
     * @throws NullPointerException If the bean is null.
     * @see Ujo#getValue(Key)
     * @see #of(Object)
     */
    VALUE getValue(@NotNull UJO bean);

    /** Returns a default value used when the current property value is null.
     * This feature is only relevant if the default value is not null.
     */
    @Nullable VALUE getDefaultValue();

    /** Indicates whether the property value of the given Ujo is equal to the default value of this key. */
    boolean isDefault(@NotNull UJO ujo);

    /**
     * An alias for the method {@link #getValue(Object)}.
     */
    default VALUE of(@NotNull final UJO ujo) {
        return getValue(ujo);
    }

    /** Returns the index of the key.
     * The index is useful for sorting keys in {@code UjoManager.readProperties(Class)}.
     */
    short index();

    /** Returns a name of the key. */
    @Override
    String toString();

    /** Returns a default value for substitution.
     * Defaults to {@code null} unless overridden.
     * @see #getDefaultValue()
     */
    @Nullable
    default VALUE getDefault() {
        return null;
    }

    /** Returns true if the key type is a subtype of, or equal to, the specified class. */
    default boolean isTypeOf(@NotNull final Class<?> type) {
        return type().isAssignableFrom(type);
    }

    /** Returns true if the domain type is a subtype of, or equal to, the specified class. */
    default boolean isDomainOf(@NotNull final Class<?> type) {
        return domainClass().isAssignableFrom(type);
    }

    @Override
    default int length() {
        return name().length();
    }

    @Override
    default char charAt(final int index) {
        return name().charAt(index);
    }

    @Override
    @NotNull
    default CharSequence subSequence(final int start, final int end) {
        return name().subSequence(start, end);
    }

    @Override
    default int compareTo(@NotNull final Key o) {
        final var i1 = this.index();
        final var i2 = o.index();
        return Integer.compare(i1, i2);
    }

    // ---- Join methods ---

    /** Joins all key names using a dot delimiter. */
    default <V2> String join(
            @NotNull final Key<VALUE,V2> key2) {
        return name() + '.' + key2.name();
    }

    /** Joins all key names using a dot delimiter. */
    default <V2,V3> String join(
            @NotNull final Key<VALUE,V2> key2,
            @NotNull final Key<V2, V3> key3) {
        return name() +
                '.' + key2.name() +
                '.' + key3.name();
    }

    /** Joins all key names using a dot delimiter. */
    default <V2,V3,V4> String join(
            @NotNull final Key<VALUE,V2> key2,
            @NotNull final Key<V2, V3> key3,
            @NotNull final Key<V3, V4> key4,
            @NotNull final Key<?, ?>... keys) {
        var result = new StringBuilder(64).append(name())
                .append('.').append(key2.name())
                .append('.').append(key3.name())
                .append('.').append(key4.name());
            for (var key : keys) {
                result.append('.').append(key);
            }
            return result.toString();
    }

}