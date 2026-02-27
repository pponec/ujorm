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

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Transient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.common.Primitive;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Modifier;
import java.util.*;

public record DomainPropertyModel(
        /** Name of property from the class field. */
        String propertyName,
        /** Type of property from the class field. */
        Class<?> propertyType,
        /** Name of the getter method */
        String getter,
        /** Missing setter has the NULL value */
        @Nullable
        String setter,
        /** Name of database column */
        String dbColumName,
        /** Non-null column feature by the JPA annotation */
        boolean required,
        /** Database primary key */
        boolean primaryKey
) {

    public Class<?> propertyObjectType() {
        return Primitive.wrapPrimitive(propertyType);
    }

    /**
     * Creates a list of DomainPropertyModel for the given class (Bean or Record).
     * Parses JPA annotations to populate database column names, primary keys, and nullability.
     * Traverses the class hierarchy to include inherited properties.
     *
     * @param beanOrRecord The class to inspect.
     * @return List of property models describing the class attributes.
     */
    @NotNull
    public static List<DomainPropertyModel> of(@NotNull Class<?> beanOrRecord) {
        if (beanOrRecord.isRecord()) {
            return Arrays.stream(beanOrRecord.getRecordComponents())
                    .map(c -> {
                        var field = findField(beanOrRecord, c.getName());
                        if (field == null || field.isAnnotationPresent(Transient.class)) return null;
                        return createModel(c.getName(), c.getType(), c.getName(), null, field);
                    })
                    .filter(Objects::nonNull)
                    .toList();
        }

        var result = new ArrayList<DomainPropertyModel>();
        var currentClass = beanOrRecord;
        while (currentClass != null && currentClass != Object.class) {
            var classProperties = new ArrayList<DomainPropertyModel>();
            for (var field : currentClass.getDeclaredFields()) {
                var mods = field.getModifiers();
                if (Modifier.isStatic(mods)
                        || field.isSynthetic()
                        || Modifier.isTransient(mods)
                        || field.isAnnotationPresent(Transient.class)) {
                    continue;
                }
                var suffix = capitalize(field.getName());
                var getter = findGetter(beanOrRecord, field.getType(), suffix);

                if (getter != null) {
                    var setter = findMethod(beanOrRecord, "set" + suffix, field.getType());
                    classProperties.add(createModel(field.getName(), field.getType(), getter, setter, field));
                }
            }

            if (FieldOrderInspector.revertedOrder()) {
                Collections.reverse(classProperties);
            }
            // Add inherited properties before the subclass properties
            result.addAll(0, classProperties);
            currentClass = currentClass.getSuperclass();
        }
        return result;
    }

    /**
     * Helper to create the model and extract values from JPA annotations.
     *
     * @param name Name of the property.
     * @param type Type of the property.
     * @param getter Name of the getter method.
     * @param setter Name of the setter method.
     * @param element Annotated element (Field or RecordComponent) to inspect.
     * @return A populated DomainPropertyModel instance.
     */
    private static DomainPropertyModel createModel(String name, Class<?> type, String getter, String setter, AnnotatedElement element) {
        var isId = element.isAnnotationPresent(Id.class);
        var column = element.getAnnotation(Column.class);
        var joinColumn = element.getAnnotation(JoinColumn.class);
        var required = type.isPrimitive() || isId;
        var dbColName = "";

        if (column != null) {
            dbColName = column.name().isEmpty() ? "" : column.name();
            required = required || !column.nullable();
        } else if (joinColumn != null) {
            dbColName = joinColumn.name().isEmpty() ? "" : joinColumn.name();
            required = required || !joinColumn.nullable();
        }
        if (dbColName.isEmpty()) {
           dbColName = buildDbName(name);
        }

        return new DomainPropertyModel(name, type, getter, setter, dbColName, required, isId);
    }

    /** Converts a Java Bean property name to a snake_case database column name. */
    private static String buildDbName(@NotNull String propertyName) {
        if (propertyName == null || propertyName.isEmpty()) return propertyName;
        var resultBuilder = new StringBuilder();
        for (var i = 0; i < propertyName.length(); i++) {
            var c = propertyName.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                resultBuilder.append('_');
            }
            resultBuilder.append(Character.toLowerCase(c));
        }
        return resultBuilder.toString();
    }

    /**
     * Resolves the appropriate getter method name.
     *
     * @param clazz The class to inspect.
     * @param type The property type.
     * @param suffix The capitalized property name.
     * @return The getter method name, or null if none is found.
     */
    private static String findGetter(Class<?> clazz, Class<?> type, String suffix) {
        var isMethod = "is" + suffix;
        var getMethod = "get" + suffix;
        var hasIs = findMethod(clazz, isMethod, (Class<?>[]) null) != null;
        var hasGet = findMethod(clazz, getMethod, (Class<?>[]) null) != null;

        if (type == boolean.class && hasIs) return isMethod;
        if (hasGet) return getMethod;
        return hasIs ? isMethod : null;
    }

    /**
     * Safely finds a method by name and parameters.
     *
     * @param clazz The class to inspect.
     * @param name The method name.
     * @param params Method parameter types.
     * @return The method name if found, null otherwise.
     */
    private static String findMethod(Class<?> clazz, String name, Class<?>... params) {
        try {
            return clazz.getMethod(name, params).getName();
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Safely finds a declared field by name.
     */
    private static java.lang.reflect.Field findField(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param str The string to capitalize.
     * @return Capitalized string.
     */
    private static String capitalize(String str) {
        return (str == null || str.isEmpty())
                ? str
                : Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public boolean isPrimitive() {
        return propertyType.isPrimitive();
    }

    /**
     * Helper class to detect if the JVM returns fields in reversed order.
     */
    public static class FieldOrderInspector {
        @SuppressWarnings("unused")
        private int firstField = 1;
        @SuppressWarnings("unused")
        private int secondField = 2;

        /** Checks if JVM returns fields in reversed order.
         * @return True if fields are reversed, false otherwise.
         */
        public static boolean revertedOrder() {
            try {
                return "secondField".equals(FieldOrderInspector.class.getDeclaredFields()[0].getName());
            } catch (SecurityException | ArrayIndexOutOfBoundsException ex) {
                return false;
            }
        }
    }
}