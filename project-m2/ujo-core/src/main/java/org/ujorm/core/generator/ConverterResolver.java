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
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.converter.EnumNameConverter;
import org.ujorm.core.converter.EnumOrdinalConverter;
import org.ujorm.core.converter.JpaConverter;
import org.ujorm.tools.common.Primitive;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the value converter of a domain property and renders it as a Java expression
 * of the generated handler. A converter is created by its constructor there, so no reflection
 * is needed at runtime.
 */
final class ConverterResolver {

    /**
     * Returns a Java expression creating the value converter, or {@code null} for a value without conversion.
     *
     * @throws IllegalArgumentException An invalid converter declaration.
     */
    @Nullable
    String resolve(@NotNull Field field, @NotNull Class<?> type, boolean primaryKey, boolean foreignKey) {
        var convert = field.getAnnotation(Convert.class);
        if (convert != null && !convert.disableConversion()) {
            return createJpaConverter(field, type, convert, primaryKey, foreignKey);
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

    private String createJpaConverter(Field field, Class<?> type, Convert convert, boolean primaryKey, boolean foreignKey) {
        var converterClass = convert.converter();
        if (!convert.attributeName().isEmpty()) {
            throw error(field, "the attribute name of the @Convert annotation is not supported");
        }
        if (converterClass == void.class) {
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
        if (!AttributeConverter.class.isAssignableFrom(converterClass)) {
            throw error(field, "the class %s doesn't implement the %s".formatted(
                    converterClass.getName(), AttributeConverter.class.getSimpleName()));
        }
        checkInstantiable(field, converterClass);

        var typeArguments = findTypeArguments(converterClass, AttributeConverter.class, Map.of());
        var valueType = typeArguments != null ? toClass(typeArguments[0]) : null;
        var dbType = typeArguments != null ? toClass(typeArguments[1]) : null;
        if (valueType == null || dbType == null) {
            throw error(field, "the type arguments of the converter %s can't be resolved"
                    .formatted(converterClass.getName()));
        }
        if (valueType != Primitive.wrapPrimitive(type)) {
            throw error(field, "the converter %s converts the type %s rather than %s".formatted(
                    converterClass.getName(), valueType.getName(), type.getName()));
        }
        return "%s.of(new %s(), %s.class)".formatted(
                JpaConverter.class.getName(),
                converterClass.getCanonicalName(),
                dbType.getCanonicalName());
    }

    /** The generated handler creates the converter by its public constructor without arguments. */
    private void checkInstantiable(Field field, Class<?> converterClass) {
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
            throw error(field, ("the converter %s must be a public top-level or static nested class"
                    + " with a public constructor without arguments").formatted(converterClass.getName()));
        }
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

    private IllegalArgumentException error(Field field, String reason) {
        var msg = "Invalid converter of the attribute %s.%s: %s."
                .formatted(field.getDeclaringClass().getSimpleName(), field.getName(), reason);
        return new IllegalArgumentException(msg);
    }
}
