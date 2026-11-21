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
import org.ujorm.core.generator.MappingSource.ColumnInfo;
import org.ujorm.core.generator.MappingSource.ConvertInfo;
import org.ujorm.core.generator.MappingSource.TableInfo;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Reads the mapping annotations of both sets: the Ujorm ones ({@code @UColumn}, ...) and the Jakarta Persistence ones.
 * <p>The Jakarta Persistence API is optional at runtime: its annotations are read only when the API is available.
 * An element can carry both variants of an annotation if they declare the same values,
 * otherwise the {@link IllegalArgumentException} is thrown, because no variant has a silent priority.
 */
final class MappingAnnotations {

    /** The Jakarta Persistence API is available to this module. It is evaluated once. */
    static final boolean JAKARTA_AVAILABLE = isClassAvailable("jakarta.persistence.Column");

    /** The default instance */
    private static final MappingAnnotations INSTANCE = new MappingAnnotations(JAKARTA_AVAILABLE);

    /** Reader of the Ujorm annotations */
    private final MappingSource ujorm = new UjormMappingSource();
    /** Reader of the Jakarta Persistence annotations, or {@code null} without the API */
    @Nullable
    private final MappingSource jakarta;

    /**
     * Creates the reader.
     * @param jakartaEnabled Read the Jakarta Persistence annotations, the API must be available then.
     */
    MappingAnnotations(boolean jakartaEnabled) {
        this.jakarta = jakartaEnabled ? new JakartaMappingSource() : null;
    }

    /** The Jakarta Persistence annotations are read. */
    boolean isJakartaEnabled() {
        return jakarta != null;
    }

    boolean isTransient(@NotNull Field field) {
        return ujorm.isTransient(field) || jakarta != null && jakarta.isTransient(field);
    }

    boolean isId(@NotNull Field field) {
        return ujorm.isId(field) || jakarta != null && jakarta.isId(field);
    }

    boolean isManyToOne(@NotNull Field field) {
        return ujorm.isManyToOne(field) || jakarta != null && jakarta.isManyToOne(field);
    }

    @Nullable
    ColumnInfo column(@NotNull Field field) {
        return merge(field, "Column", MappingSource::column);
    }

    @Nullable
    ColumnInfo joinColumn(@NotNull Field field) {
        return merge(field, "JoinColumn", MappingSource::joinColumn);
    }

    @Nullable
    ConvertInfo convert(@NotNull Field field) {
        return merge(field, "Convert", MappingSource::convert);
    }

    /** Returns {@code true} for the ordinal, {@code false} for the name, or {@code null} without the annotation. */
    @Nullable
    Boolean enumOrdinal(@NotNull Field field) {
        return merge(field, "Enumerated", MappingSource::enumOrdinal);
    }

    boolean isEntity(@NotNull Class<?> type) {
        return ujorm.isEntity(type) || jakarta != null && jakarta.isEntity(type);
    }

    @Nullable
    TableInfo table(@NotNull Class<?> type) {
        var result = ujorm.table(type);
        if (jakarta != null) {
            result = check(type.getName(), "Table", result, jakarta.table(type));
        }
        return result;
    }

    /** The class implements the JPA {@code AttributeConverter}; it is {@code false} without the API. */
    boolean isAttributeConverter(@NotNull Class<?> type) {
        return jakarta != null && JakartaMappingSource.attributeConverterType().isAssignableFrom(type);
    }

    /** Reads an annotation of the field from both sets. */
    @Nullable
    private <T> T merge(Field field, String annotation, BiFunction<MappingSource, Field, T> reader) {
        var result = reader.apply(ujorm, field);
        if (jakarta != null) {
            var name = field.getDeclaringClass().getSimpleName() + "." + field.getName();
            result = check(name, annotation, result, reader.apply(jakarta, field));
        }
        return result;
    }

    /** Returns the defined value, or throws an exception for the different values. */
    @Nullable
    private <T> T check(String element, String annotation, @Nullable T ujormValue, @Nullable T jakartaValue) {
        if (ujormValue != null && jakartaValue != null && !Objects.equals(ujormValue, jakartaValue)) {
            var msg = ("The %s has the annotations @U%s and @%s with different values: %s versus %s,"
                    + " keep one of them only.").formatted(element, annotation, annotation, ujormValue, jakartaValue);
            throw new IllegalArgumentException(msg);
        }
        return ujormValue != null ? ujormValue : jakartaValue;
    }

    /** Returns the default instance. */
    static MappingAnnotations of() {
        return INSTANCE;
    }

    /** Checks the class by the class loader of this module, because it links the reader of the annotations. */
    static boolean isClassAvailable(@NotNull String className) {
        try {
            Class.forName(className, false, MappingAnnotations.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
    }
}
