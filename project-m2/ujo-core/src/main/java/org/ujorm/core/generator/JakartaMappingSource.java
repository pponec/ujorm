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

import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Reads the Jakarta Persistence annotations.
 * <p>The only class of the module which refers the {@code jakarta.persistence} API,
 * so it must be loaded only when the API is available - see {@link MappingAnnotations#JAKARTA_AVAILABLE}.
 */
final class JakartaMappingSource implements MappingSource {

    @Override
    public @NotNull String prefix() {
        return "";
    }

    @Override
    public boolean isTransient(@NotNull Field field) {
        return field.isAnnotationPresent(Transient.class);
    }

    @Override
    public boolean isId(@NotNull Field field) {
        return field.isAnnotationPresent(Id.class);
    }

    @Override
    public boolean isManyToOne(@NotNull Field field) {
        return field.isAnnotationPresent(ManyToOne.class);
    }

    @Override
    public @Nullable ColumnInfo column(@NotNull Field field) {
        var column = field.getAnnotation(Column.class);
        return column != null ? new ColumnInfo(column.name(), column.nullable()) : null;
    }

    @Override
    public @Nullable ColumnInfo joinColumn(@NotNull Field field) {
        var column = field.getAnnotation(JoinColumn.class);
        return column != null ? new ColumnInfo(column.name(), column.nullable()) : null;
    }

    @Override
    public @Nullable ConvertInfo convert(@NotNull Field field) {
        var convert = field.getAnnotation(Convert.class);
        return convert != null
                ? new ConvertInfo(convert.converter(), convert.disableConversion(), convert.attributeName())
                : null;
    }

    @Override
    public @Nullable Boolean enumOrdinal(@NotNull Field field) {
        var enumerated = field.getAnnotation(Enumerated.class);
        return enumerated != null ? enumerated.value() == EnumType.ORDINAL : null;
    }

    @Override
    public boolean isEntity(@NotNull Class<?> type) {
        return type.isAnnotationPresent(Entity.class)
                || type.isAnnotationPresent(Table.class);
    }

    @Override
    public @Nullable TableInfo table(@NotNull Class<?> type) {
        var table = type.getAnnotation(Table.class);
        return table != null ? new TableInfo(table.name(), table.schema(), table.catalog()) : null;
    }

    /** The JPA converter interface. */
    static Class<?> attributeConverterType() {
        return AttributeConverter.class;
    }

    /** The class carries the {@code @Converter(autoApply = true)} annotation. */
    static boolean isAutoApplied(@NotNull Class<?> converterClass) {
        var converter = converterClass.getAnnotation(Converter.class);
        return converter != null && converter.autoApply();
    }
}
