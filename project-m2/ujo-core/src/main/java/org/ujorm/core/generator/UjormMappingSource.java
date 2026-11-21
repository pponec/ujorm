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
import org.ujorm.annotation.*;

import java.lang.reflect.Field;

/** Reads the Ujorm mapping annotations ({@code @UColumn}, {@code @UTable}, ...). */
final class UjormMappingSource implements MappingSource {

    @Override
    public @NotNull String prefix() {
        return "U";
    }

    @Override
    public boolean isTransient(@NotNull Field field) {
        return field.isAnnotationPresent(UTransient.class);
    }

    @Override
    public boolean isId(@NotNull Field field) {
        return field.isAnnotationPresent(UId.class);
    }

    @Override
    public boolean isManyToOne(@NotNull Field field) {
        return field.isAnnotationPresent(UManyToOne.class);
    }

    @Override
    public @Nullable ColumnInfo column(@NotNull Field field) {
        var column = field.getAnnotation(UColumn.class);
        return column != null ? new ColumnInfo(column.name(), column.nullable()) : null;
    }

    @Override
    public @Nullable ColumnInfo joinColumn(@NotNull Field field) {
        var column = field.getAnnotation(UJoinColumn.class);
        return column != null ? new ColumnInfo(column.name(), column.nullable()) : null;
    }

    @Override
    public @Nullable ConvertInfo convert(@NotNull Field field) {
        var convert = field.getAnnotation(UConvert.class);
        return convert != null ? new ConvertInfo(convert.converter(), convert.disableConversion(), "") : null;
    }

    @Override
    public @Nullable Boolean enumOrdinal(@NotNull Field field) {
        var enumerated = field.getAnnotation(UEnumerated.class);
        return enumerated != null ? enumerated.value() == UEnumType.ORDINAL : null;
    }

    @Override
    public boolean isEntity(@NotNull Class<?> type) {
        return type.isAnnotationPresent(UEntity.class)
                || type.isAnnotationPresent(UTable.class);
    }

    @Override
    public @Nullable TableInfo table(@NotNull Class<?> type) {
        var table = type.getAnnotation(UTable.class);
        return table != null ? new TableInfo(table.name(), table.schema(), table.catalog()) : null;
    }
}
