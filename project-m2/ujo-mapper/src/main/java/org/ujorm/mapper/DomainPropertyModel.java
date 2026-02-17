package org.ujorm.mapper;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    /**
     * Creates a list of BeanPropertyModel for the given class (Bean or Record).
     * Parses JPA annotations to populate database column names, primary keys, and nullability.
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
                        var element = field != null ? field : c;
                        return createModel(c.getName(), c.getType(), c.getName(), null, element);
                    })
                    .toList();
        }

        var result = new ArrayList<DomainPropertyModel>();
        for (var field : beanOrRecord.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                continue;
            }
            var suffix = capitalize(field.getName());
            var getter = findGetter(beanOrRecord, field.getType(), suffix);

            if (getter != null) {
                var setter = findMethod(beanOrRecord, "set" + suffix, field.getType());
                result.add(createModel(field.getName(), field.getType(), getter, setter, field));
            }
        }
        if (FieldOrderInspector.revertedOrder()) {
            Collections.reverse(result);
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
        var isNonNull = type.isPrimitive() || isId;

        if (column != null) {
            if (!column.name().isEmpty()) dbColName = column.name();
            if (!column.nullable()) isNonNull = true;
        } else if (joinColumn != null) {
            if (!joinColumn.name().isEmpty()) dbColName = joinColumn.name();
            if (!joinColumn.nullable()) isNonNull = true;
        }

        for (var annotation : element.getAnnotations()) {
            var annotName = annotation.annotationType().getSimpleName();
            if ("NotNull".equals(annotName) || "NonNull".equals(annotName)) {
                isNonNull = true;
            } else if ("Nullable".equals(annotName)) {
                isNonNull = false;
            }
        }

        return new DomainPropertyModel(name, type, getter, setter, dbColName, isNonNull, isId);
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