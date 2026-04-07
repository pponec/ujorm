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
import org.ujorm.core.criterion.CriterionProvider;

import java.util.Objects;

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
public interface Key<DOMAIN, VALUE> extends CharSequence, Comparable<Key>, CriterionProvider<VALUE> {

    /** Returns the name of the Key (e.g., "name"). */
    @NotNull String name();

    /**
     * Returns the full name of the Key, including the simple name of the
     * domain class separated by a dot (e.g., "Employee.name").
     */
    @NotNull
    default String fullName() {
        return domainClass().getSimpleName() + '.' + name();
    }

    /** Returns the type of the value associated with this key. */
    @NotNull Class<VALUE> type();

    /** Returns true if the domain type is a subtype of, or equal to, the specified class. */
    default boolean isDomainOf(@NotNull final Class<?> type) {
        return type.isAssignableFrom(domainClass());
    }

    /** Returns true if the key type is a subtype of, or equal to, the specified class. */
    default boolean isTypeOf(@NotNull final Class<?> type) {
        return type.isAssignableFrom(type());
    }

    /** Returns true if the value can be assigned to this Key. */
    default boolean isInstanceOf(@Nullable final Object value) {
        return type().isInstance(value);
    }

    /** Returns the class of the domain Ujo object. */
    @NotNull Class<DOMAIN> domainClass();


    /**
     * Sets a type-safe value to the specified Ujo object.
     * This method always calls {@link Ujo#setValue(Key, Object)}.
     *
     * @param bean The target Ujo object.
     * @param value The value to assign.
     * @throws UnsupportedOperationException If the key is read-only.
     * @see Ujo#setValue(Key, Object)
     */
    void setValue(@NotNull DOMAIN bean, @Nullable VALUE value) throws UnsupportedOperationException;

    /**
     * Gets a type-safe value from the specified Ujo object.
     *
     * @param bean The source Ujo object. Must not be null.
     * @return The type-safe value from the Ujo object.
     * @throws NullPointerException If the bean is null.
     * @see Ujo#getValue(Key)
     */
    VALUE getValue(@NotNull DOMAIN bean);

    /**
     * Returns a default value for substitution.
     * Defaults to {@code null} unless overridden.
     */
    @Nullable
    default VALUE getDefault() {
        return null;
    }

    /**
     * Returns a default value used when the current property value is null.
     * This feature is only relevant if the default value is not null.
     */
    @Nullable VALUE getDefaultValue();


    /**
     * Returns the index of the key.
     * The index is useful for sorting keys in {@code UjoManager.readProperties(Class)}.
     */
    short index();

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
    default int compareTo(@NotNull final Key o) {
        var i1 = this.index();
        var i2 = o.index();
        return Integer.compare(i1, i2);
    }

    /**
     * Returns true, if the key value equals to a parameter value. The key value can be null.
     *
     * @param domain A basic domain objects.
     * @param value Null value is supported.
     * @return Accordance
     */
    default boolean equals(@NotNull final DOMAIN domain, @Nullable final VALUE value) {
        return Objects.equals(getValue(domain), value);
    }

    /** Provides more information */
    @NotNull
    KeyInfo info();

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
            result.append('.').append(key.name());
        }
        return result.toString();
    }
}