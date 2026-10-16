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

import java.util.Objects;

/** Maps an Enum to its ordinal by the annotation {@code @Enumerated(EnumType.ORDINAL)}. */
public final class EnumOrdinalConverter<E extends Enum<E>> implements DbConverter<E, Integer> {

    /** Enum type */
    private final Class<E> type;
    /** Enum items by their ordinals */
    private final E[] items;

    private EnumOrdinalConverter(@NotNull Class<E> type) {
        this.type = Objects.requireNonNull(type, "Enum type is required");
        this.items = type.getEnumConstants();
    }

    @Override
    public @NotNull Class<Integer> dbType() {
        return Integer.class;
    }

    @Override
    public @NotNull Integer toDb(@NotNull E value) {
        return value.ordinal();
    }

    @Override
    public @NotNull E fromDb(@NotNull Integer value) {
        if (value < 0 || value >= items.length) {
            var msg = "No enum item of the %s class for [%s]".formatted(type.getSimpleName(), value);
            throw new IllegalArgumentException(msg);
        }
        return items[value];
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '<' + type.getSimpleName() + '>';
    }

    /** Creates a converter for the Enum type. */
    @NotNull
    public static <E extends Enum<E>> EnumOrdinalConverter<E> of(@NotNull Class<E> type) {
        return new EnumOrdinalConverter<>(type);
    }
}
