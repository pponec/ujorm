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

import jakarta.persistence.AttributeConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/** Adapter of the JPA {@link AttributeConverter} declared by the {@code @Convert} annotation. */
public final class JpaConverter<V, D> implements DbConverter<V, D> {

    /**
     * Index of the converters with the {@code @Converter(autoApply = true)} annotation, written by the Ujorm APT processor.
     * Keep in sync with {@code org.ujorm.maven.UjormMetaProcessor.Const.CONVERTER_INDEX}.
     */
    public static final String CONVERTER_INDEX = "META-INF/ujorm/converters.lst";

    /** Converter of the application */
    private final AttributeConverter<V, D> converter;
    /** Database type resolved from the type arguments of the converter */
    private final Class<D> dbType;

    private JpaConverter(@NotNull AttributeConverter<V, D> converter, @NotNull Class<D> dbType) {
        this.converter = Objects.requireNonNull(converter, "Converter is required");
        this.dbType = Objects.requireNonNull(dbType, "Database type is required");
    }

    @Override
    public @NotNull Class<D> dbType() {
        return dbType;
    }

    @Override
    public @Nullable D toDb(@NotNull V value) {
        return converter.convertToDatabaseColumn(value);
    }

    @Override
    public @Nullable V fromDb(@NotNull D value) {
        return converter.convertToEntityAttribute(value);
    }

    /** Converters of the same class store equal values, because a converter is stateless. */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof JpaConverter<?, ?> other
                && converter.getClass() == other.converter.getClass()
                && dbType == other.dbType;
    }

    @Override
    public int hashCode() {
        return converter.getClass().hashCode();
    }

    @Override
    public String toString() {
        return converter.getClass().getSimpleName();
    }

    /**
     * Creates the adapter. The value type is inferred from the target,
     * because the generated code assigns a converter whose types are verified by the domain model.
     */
    @NotNull
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <V> DbConverter<V, ?> of(@NotNull AttributeConverter<?, ?> converter, @NotNull Class<?> dbType) {
        return new JpaConverter(converter, dbType);
    }
}
