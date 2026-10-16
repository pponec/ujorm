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
package org.ujorm.core.converter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Converts a domain value to its JDBC representation and back.
 * <p>The interface is internal: an application provides a JPA {@code AttributeConverter} instead.
 * A {@code null} value is never passed to the converter, because it always means the SQL {@code NULL}.
 * An implementation must be stateless and thread-safe.
 *
 * @param <V> Type of the domain value
 * @param <D> Type of the database value passed to the JDBC driver
 */
public interface DbConverter<V, D> {

    /** Java type passed to the JDBC driver and read from the {@code ResultSet}. */
    @NotNull Class<D> dbType();

    /** Converts a domain value to the database one. */
    @Nullable D toDb(@NotNull V value);

    /** Converts a database value to the domain one. */
    @Nullable V fromDb(@NotNull D value);

    /**
     * Converts a nullable domain value by an optional converter.
     * The unchecked cast is limited to this place, because the converter type comes from a key.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    static Object toDb(@Nullable DbConverter<?, ?> converter, @Nullable Object value) {
        return converter != null && value != null
                ? ((DbConverter<Object, ?>) converter).toDb(value)
                : value;
    }
}
