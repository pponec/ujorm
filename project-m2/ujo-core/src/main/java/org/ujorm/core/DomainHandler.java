/*
 * Copyright 2026-2026 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.core;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.impl.AbstractUjo;

import java.util.List;
import java.util.NoSuchElementException;

public interface DomainHandler<D> {
    @NotNull Class<D> getDomainClass();
    /** Full database name from Java annotations. */
    @NotNull String getDatabaseTable();
    @NotNull List<Key<D, ?>> getKeyList();

    /**
     * Find key in metamodel.
     * @param name Property name.
     * @param genericType Optional generic class for the safe result type.
     * @return Key object.
     * @param <V> Value
     * @throws NoSuchElementException If not such element was found
     */
    @NotNull
    @SuppressWarnings("unchecked")
    <V> Key<D, V> getKey(@NonNull String name, @NotNull Class<V> genericType)
            throws NoSuchElementException;

    /**
     * Find key in metamodel.
     * @param name Property name.
     * @return Key object.
     * @throws NoSuchElementException If not such element was found
     */
    @NotNull
    @SuppressWarnings("unchecked")
    default Key<D, Object> getKey(@Nullable String name)
            throws NoSuchElementException {
        return getKey(name, null);
    }

    /**
     * Find key in metamodel.
     * @param order Property name.
     * @return Key object.
     * @throws IndexOutOfBoundsException If not such element was found
     */
    @NotNull
    @SuppressWarnings("unchecked")
    default Key<D, Object> getKey(@Nullable int order) {
        return (Key<D, Object>) this.getKeyList().get(order);
    }

    /**
     * Finds a key by a case-insensitive column name.
     * @param columnName  The case-insensitive name of the column.
     * @param required    Set to true if the result is required (usually throws an exception if not found).
     * @param genericType The expected target type of the value.
     * @param <V>         The type of the value.
     * @return An instance of the Key, or null if not found and not required.
     */
    @Nullable
    <V> Key<D, V> getKeyByColumn(@NonNull final String columnName, boolean required, @Nullable final Class<V> genericType);

    /** Return total count of the properties. */
    default int count() {
        return getKeyList().size();
    }

    /** Create a new domain object and assign values from the argument array. */
    D newDomain(Object... values);

    /** Create a new domain object and assign values from the argument array. */
    default AbstractUjo<D> newUjoDomain() {
        return AbstractUjo.of(this);
    }
}
