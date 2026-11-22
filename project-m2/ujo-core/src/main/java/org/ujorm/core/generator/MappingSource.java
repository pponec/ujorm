/*
 * Copyright 2026-2026 Pavel Ponec
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
package org.ujorm.core.generator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * One set of the mapping annotations (Ujorm or Jakarta Persistence) presented by a common view.
 * A method returns {@code null} (or {@code false}) when the element doesn't carry the annotation.
 */
interface MappingSource {

    /** Name of the annotation set for messages. */
    @NotNull String prefix();

    boolean isTransient(@NotNull Field field);

    boolean isId(@NotNull Field field);

    boolean isManyToOne(@NotNull Field field);

    @Nullable ColumnInfo column(@NotNull Field field);

    @Nullable ColumnInfo joinColumn(@NotNull Field field);

    @Nullable ConvertInfo convert(@NotNull Field field);

    /** Returns {@code true} for the ordinal, {@code false} for the name, or {@code null} without the annotation. */
    @Nullable Boolean enumOrdinal(@NotNull Field field);

    boolean isEntity(@NotNull Class<?> type);

    @Nullable TableInfo table(@NotNull Class<?> type);

    /** Values of a {@code @Column} or {@code @JoinColumn} annotation. */
    record ColumnInfo(@NotNull String name, boolean nullable) {}

    /** Values of a {@code @Convert} annotation; the {@code attributeName} is not supported by Ujorm. */
    record ConvertInfo(@NotNull Class<?> converter, boolean disableConversion, @NotNull String attributeName) {}

    /** Values of a {@code @Table} annotation. */
    record TableInfo(@NotNull String name, @NotNull String schema, @NotNull String catalog) {}
}
