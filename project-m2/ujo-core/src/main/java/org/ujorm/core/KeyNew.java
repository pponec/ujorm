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

package org.ujorm.core;

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
public interface KeyNew<UJO, VALUE> extends CharSequence, Comparable<KeyNew> {

    /** Returns the index of the key.
     * The index is useful for sorting keys in {@code UjoManager.readProperties(Class)}.
     */
    short index();

    /** Returns the name of the Key (e.g., "name"). */
    @NotNull String name();

    /** Returns the type of the value associated with this key. */
    @NotNull Class<VALUE> type();


    /** Indicates whether the property value of the given Ujo is equal to the default value of this key. */
    boolean isDefault(@NotNull UJO ujo);


    /**
     * Gets a type-safe value from the specified Ujo object.
     *
     * @param bean The source Ujo object. Must not be null.
     * @return The type-safe value from the Ujo object.
     * @throws NullPointerException If the bean is null.
     * @see Ujo#getValue(KeyNew)
     * @see #of(Object)
     */
    VALUE getValue(@NotNull UJO bean);

    /** Returns a default value used when the current property value is null.
     * This feature is only relevant if the default value is not null.
     */
    @Nullable VALUE getDefaultValue();


    /**
     * Sets a type-safe value to the specified Ujo object.
     * This method always calls {@link Ujo#setValue(KeyNew, Object)}.
     *
     * @param bean The target Ujo object.
     * @param value The value to assign.
     * @throws UnsupportedOperationException If the key is read-only.
     * @see Ujo#setValue(KeyNew, Object)
     */
    void setValue(@NotNull UJO bean, @Nullable VALUE value) throws UnsupportedOperationException;

    /**
     * An alias for the method {@link #getValue(Object)}.
     */
    default VALUE of(@NotNull final UJO ujo) {
        return getValue(ujo);
    }

    /** Returns a default value for substitution.
     * Defaults to {@code null} unless overridden.
     * @see #getDefaultValue()
     */
    @Nullable
    default VALUE getDefault() {
        return null;
    }

    /** Returns a name of the key. */
    @Override
    String toString();


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
    default int compareTo(@NotNull final KeyNew o) {
        final var i1 = this.index();
        final var i2 = o.index();
        return Integer.compare(i1, i2);
    }

    // ---- Join methods ---


    /** Returns the class of the domain Ujo object. */
    @NotNull Class<UJO> domainClass();

    /** Joins all key names using a dot delimiter. */
    default <V2> String join(
            @NotNull final KeyNew<VALUE,V2> key2) {
        return name() + '.' + key2.name();
    }

    /** Joins all key names using a dot delimiter. */
    default <V2,V3> String join(
            @NotNull final KeyNew<VALUE,V2> key2,
            @NotNull final KeyNew<V2, V3> key3) {
        return name() +
                '.' + key2.name() +
                '.' + key3.name();
    }

    /** Joins all key names using a dot delimiter. */
    default <V2,V3,V4> String join(
            @NotNull final KeyNew<VALUE,V2> key2,
            @NotNull final KeyNew<V2, V3> key3,
            @NotNull final KeyNew<V3, V4> key4,
            @NotNull final KeyNew<?, ?>... keys) {
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