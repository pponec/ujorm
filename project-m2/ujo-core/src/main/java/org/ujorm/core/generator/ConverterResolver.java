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
package org.ujorm.core.generator;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.converter.EnumNameConverter;
import org.ujorm.core.converter.EnumOrdinalConverter;
import org.ujorm.core.converter.JpaConverter;
import org.ujorm.tools.common.Primitive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the value converter of a domain property and renders it as a Java expression
 * of the generated handler. A converter is created by its constructor there, so no reflection
 * is needed at runtime.
 */
final class ConverterResolver {

    /** Auto-applied converters by their value types, loaded lazily from the index */
    @Nullable
    private Map<Class<?>, Class<?>> autoApplied;

    /**
     * Returns a Java expression creating the value converter, or {@code null} for a value without conversion.
     * An auto-applied converter is not applied to a primary key or to a relation, like in JPA.
     *
     * @throws IllegalArgumentException An invalid converter declaration.
     */
    @Nullable
    String resolve(@NotNull Field field, @NotNull Class<?> type, boolean primaryKey, boolean foreignKey) {
        var convert = field.getAnnotation(Convert.class);
        if (convert != null) {
            if (!convert.disableConversion()) {
                checkConvert(field, convert, primaryKey, foreignKey);
                return createJpaConverter(field, type, convert.converter());
            }
        } else if (!primaryKey && !foreignKey) {
            var converterClass = autoApplied(field).get(Primitive.wrapPrimitive(type));
            if (converterClass != null) {
                return createJpaConverter(field, type, converterClass);
            }
        }
        return type.isEnum() ? createEnumConverter(field, type) : null;
    }

    private String createEnumConverter(Field field, Class<?> type) {
        var enumerated = field.getAnnotation(Enumerated.class);
        var converterClass = enumerated != null && enumerated.value() == EnumType.ORDINAL
                ? EnumOrdinalConverter.class
                : EnumNameConverter.class;
        return "%s.of(%s.class)".formatted(converterClass.getName(), type.getCanonicalName());
    }

    /** Checks the {@code @Convert} annotation of the field. */
    private void checkConvert(Field field, Convert convert, boolean primaryKey, boolean foreignKey) {
        if (!convert.attributeName().isEmpty()) {
            throw error(field, "the attribute name of the @Convert annotation is not supported");
        }
        if (convert.converter() == void.class) {
            throw error(field, "the @Convert annotation requires a converter class");
        }
        if (primaryKey) {
            throw error(field, "a primary key can't have a converter");
        }
        if (foreignKey) {
            throw error(field, "a relation can't have a converter");
        }
        if (field.isAnnotationPresent(Enumerated.class)) {
            throw error(field, "the @Convert and @Enumerated annotations can't be combined");
        }
    }

    private String createJpaConverter(Field field, Class<?> type, Class<?> converterClass) {
        var problem = findProblem(converterClass);
        if (problem != null) {
            throw error(field, problem);
        }
        var types = resolveTypes(converterClass);
        if (types == null) {
            throw error(field, unresolvedTypes(converterClass));
        }
        if (types[0] != Primitive.wrapPrimitive(type)) {
            throw error(field, "the converter %s converts the type %s rather than %s".formatted(
                    converterClass.getName(), types[0].getName(), type.getName()));
        }
        return "%s.of(new %s(), %s.class)".formatted(
                JpaConverter.class.getName(),
                converterClass.getCanonicalName(),
                types[1].getCanonicalName());
    }

    /**
     * Returns a reason why the class can't be a converter of the generated handler, or {@code null} for a valid one.
     * The generated handler creates the converter by its public constructor without arguments.
     */
    @Nullable
    private String findProblem(Class<?> converterClass) {
        if (!AttributeConverter.class.isAssignableFrom(converterClass)) {
            return "the class %s doesn't implement the %s".formatted(
                    converterClass.getName(), AttributeConverter.class.getSimpleName());
        }
        var valid = !converterClass.isInterface()
                && !Modifier.isAbstract(converterClass.getModifiers())
                && converterClass.getCanonicalName() != null;
        for (var c = converterClass; valid && c != null; c = c.getEnclosingClass()) {
            valid = Modifier.isPublic(c.getModifiers())
                    && (c.getEnclosingClass() == null || Modifier.isStatic(c.getModifiers()));
        }
        try {
            valid = valid && Modifier.isPublic(converterClass.getConstructor().getModifiers());
        } catch (NoSuchMethodException e) {
            valid = false;
        }
        if (!valid) {
            return ("the converter %s must be a public top-level or static nested class"
                    + " with a public constructor without arguments").formatted(converterClass.getName());
        }
        return null;
    }

    private String unresolvedTypes(Class<?> converterClass) {
        return "the type arguments of the converter %s can't be resolved".formatted(converterClass.getName());
    }

    /** Returns the value type and the database type of the converter, or {@code null} for unresolved types. */
    @Nullable
    private Class<?>[] resolveTypes(Class<?> converterClass) {
        var typeArguments = findTypeArguments(converterClass, AttributeConverter.class, Map.of());
        var valueType = typeArguments != null ? toClass(typeArguments[0]) : null;
        var dbType = typeArguments != null ? toClass(typeArguments[1]) : null;
        return valueType != null && dbType != null ? new Class<?>[] {valueType, dbType} : null;
    }

    /** Returns the auto-applied converters visible to the class loader of the domain class. */
    private Map<Class<?>, Class<?>> autoApplied(Field field) {
        if (autoApplied == null) {
            var loader = field.getDeclaringClass().getClassLoader();
            autoApplied = loader != null ? loadAutoApplied(loader) : Map.of();
        }
        return autoApplied;
    }

    /**
     * Loads the auto-applied converters from the index written by the Ujorm APT processor.
     * An entry whose class is missing or no longer auto-applied comes from an incremental build, so it is skipped.
     *
     * @throws IllegalArgumentException An invalid auto-applied converter.
     */
    Map<Class<?>, Class<?>> loadAutoApplied(ClassLoader loader) {
        var result = new HashMap<Class<?>, Class<?>>();
        try {
            for (var url : Collections.list(loader.getResources(JpaConverter.CONVERTER_INDEX))) {
                try (var reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                    for (var line = reader.readLine(); line != null; line = reader.readLine()) {
                        var className = line.trim();
                        if (!className.isEmpty() && !className.startsWith("#")) {
                            registerAutoApplied(className, loader, result);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read the index " + JpaConverter.CONVERTER_INDEX, e);
        }
        return Map.copyOf(result);
    }

    private void registerAutoApplied(String className, ClassLoader loader, Map<Class<?>, Class<?>> result) {
        final Class<?> converterClass;
        try {
            converterClass = Class.forName(className, false, loader);
        } catch (ClassNotFoundException e) {
            return;
        }
        var converter = converterClass.getAnnotation(Converter.class);
        if (converter == null || !converter.autoApply()) {
            return;
        }
        var problem = findProblem(converterClass);
        if (problem != null) {
            throw autoApplyError(converterClass, problem);
        }
        var types = resolveTypes(converterClass);
        if (types == null) {
            throw autoApplyError(converterClass, unresolvedTypes(converterClass));
        }
        var valueType = types[0];
        if (!isAutoApplicable(valueType)) {
            throw autoApplyError(converterClass, ("the auto-apply feature is not allowed for the type %s,"
                    + " use the @Convert annotation on the attribute").formatted(valueType.getName()));
        }
        var previous = result.putIfAbsent(valueType, converterClass);
        if (previous != null && previous != converterClass) {
            throw autoApplyError(converterClass, "the converter %s is auto-applied to the type %s too"
                    .formatted(previous.getName(), valueType.getName()));
        }
    }

    /**
     * An auto-applied converter is allowed for an application type only. A JDK type or an Enum has a mapping
     * of its own, so a converter missing in the index (after an incremental build) would change
     * the stored value silently. A missing converter of an application type fails on its unsupported type instead.
     */
    private boolean isAutoApplicable(Class<?> type) {
        var name = type.getName();
        return !type.isEnum()
                && !type.isArray()
                && !type.isPrimitive()
                && !name.startsWith("java.")
                && !name.startsWith("javax.");
    }

    /**
     * Finds the type arguments of the target generic type in the hierarchy of the type.
     *
     * @param type A class or a parameterized type to inspect.
     * @param target The generic type whose arguments are required.
     * @param variables Values of the type variables of the type.
     * @return Type arguments of the target, or {@code null} if the target is not found.
     */
    @Nullable
    private Type[] findTypeArguments(Type type, Class<?> target, Map<TypeVariable<?>, Type> variables) {
        final Class<?> raw;
        final var resolved = new HashMap<TypeVariable<?>, Type>();
        if (type instanceof ParameterizedType parameterized) {
            raw = (Class<?>) parameterized.getRawType();
            var parameters = raw.getTypeParameters();
            var arguments = parameterized.getActualTypeArguments();
            for (var i = 0; i < parameters.length; i++) {
                var argument = arguments[i];
                resolved.put(parameters[i], argument instanceof TypeVariable<?> variable
                        ? variables.getOrDefault(variable, variable)
                        : argument);
            }
        } else if (type instanceof Class<?> clazz) {
            raw = clazz;
        } else {
            return null;
        }

        if (raw == target) {
            var parameters = raw.getTypeParameters();
            var result = new Type[parameters.length];
            for (var i = 0; i < parameters.length; i++) {
                result[i] = resolved.getOrDefault(parameters[i], parameters[i]);
            }
            return result;
        }
        var supertypes = new ArrayList<Type>(List.of(raw.getGenericInterfaces()));
        if (raw.getGenericSuperclass() != null) {
            supertypes.add(raw.getGenericSuperclass());
        }
        for (var supertype : supertypes) {
            var result = findTypeArguments(supertype, target, resolved);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /** Converts a resolved type to its class, or returns {@code null} for an unresolved type. */
    @Nullable
    private Class<?> toClass(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }
        if (type instanceof ParameterizedType parameterized) {
            return (Class<?>) parameterized.getRawType();
        }
        return null;
    }

    private IllegalArgumentException autoApplyError(Class<?> converterClass, String reason) {
        var msg = "Invalid auto-applied converter %s: %s.".formatted(converterClass.getName(), reason);
        return new IllegalArgumentException(msg);
    }

    private IllegalArgumentException error(Field field, String reason) {
        var msg = "Invalid converter of the attribute %s.%s: %s."
                .formatted(field.getDeclaringClass().getSimpleName(), field.getName(), reason);
        return new IllegalArgumentException(msg);
    }
}
