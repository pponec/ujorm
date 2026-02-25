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
                        if (field == null || Modifier.isTransient(field.getModifiers())) return null;
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
                // Ignore static, synthetic, transient fields and JPA @Transient annotations
                if (Modifier.isStatic(field.getModifiers())
                        || field.isSynthetic()
                        || Modifier.isTransient(field.getModifiers())
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
     */
    private static DomainPropertyModel createModel(String name, Class<?> type, String getter, String setter, AnnotatedElement element) {
        var isId = element.isAnnotationPresent(Id.class);
        var column = element.getAnnotation(Column.class);
        var joinColumn = element.getAnnotation(JoinColumn.class);
        var dbColName = name;
        var required = type.isPrimitive() || isId;

        if (column != null) {
            if (!column.name().isEmpty()) dbColName = column.name();
            if (!column.nullable()) required = true;
        } else if (joinColumn != null) {
            if (!joinColumn.name().isEmpty()) dbColName = joinColumn.name();
            if (!joinColumn.nullable()) required = true;
        }

        return new DomainPropertyModel(name, type, getter, setter, dbColName, required, isId);
    }
    private static String findGetter(Class<?> clazz, Class<?> type, String suffix) {
        var is = "is" + suffix;
        var get = "get" + suffix;
        var hasIs = findMethod(clazz, is, (Class<?>[]) null) != null;
        var hasGet = findMethod(clazz, get, (Class<?>[]) null) != null;

        if (type == boolean.class && hasIs) return is;
        if (hasGet) return get;
        return hasIs ? is : null;
    }

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

    private static String capitalize(String str) {
        return (str != null && !str.isEmpty())
                ? Character.toUpperCase(str.charAt(0)) + str.substring(1)
                : str;
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

        /** Check if JVM returns fields in reversed order to fixing. */
        public static boolean revertedOrder() {
            try {
                return "secondField".equals(FieldOrderInspector.class.getDeclaredFields()[0].getName());
            } catch (SecurityException | ArrayIndexOutOfBoundsException ex) {
                return false;
            }
        }
    }
}