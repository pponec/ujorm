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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Maps an Enum to its name, the default mapping of the Enum type. */
public final class EnumNameConverter<E extends Enum<E>> implements DbConverter<E, String> {

    /** Enum type */
    private final Class<E> type;
    /** Enum items by their names */
    private final Map<String, E> items;

    private EnumNameConverter(@NotNull Class<E> type) {
        this.type = Objects.requireNonNull(type, "Enum type is required");
        var constants = type.getEnumConstants();
        var map = new HashMap<String, E>(constants.length * 2);
        for (var constant : constants) {
            map.put(constant.name(), constant);
        }
        this.items = Map.copyOf(map);
    }

    @Override
    public @NotNull Class<String> dbType() {
        return String.class;
    }

    @Override
    public @NotNull String toDb(@NotNull E value) {
        return value.name();
    }

    @Override
    public @NotNull E fromDb(@NotNull String value) {
        var result = items.get(value);
        if (result == null) {
            var msg = "No enum item of the %s class for [%s]".formatted(type.getSimpleName(), value);
            throw new IllegalArgumentException(msg);
        }
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '<' + type.getSimpleName() + '>';
    }

    /** Creates a converter for the Enum type. */
    @NotNull
    public static <E extends Enum<E>> EnumNameConverter<E> of(@NotNull Class<E> type) {
        return new EnumNameConverter<>(type);
    }
}
