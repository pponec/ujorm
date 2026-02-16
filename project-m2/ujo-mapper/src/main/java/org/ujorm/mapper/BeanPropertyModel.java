package org.ujorm.mapper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public record BeanPropertyModel(
        /** Name of property from the class field. */
        String propertyName,
        /** Type of property from the class field. */
        Class<?> propertyType,
        /** Name of the getter method */
        String getter,
        /** Missing setter has the NULL value */
        @Nullable
        String setter
) {

    public boolean isPrimitive() {
        return propertyType.isPrimitive();
    }

    /**
     * Creates a list of BeanPropertyModel for the given class (Bean or Record).
     *
     * @param beanOrRecord The class to inspect.
     * @return List of property models describing the class attributes.
     */
    @NotNull
    public static List<BeanPropertyModel> of(@NotNull Class<?> beanOrRecord) {
        var result = new ArrayList<BeanPropertyModel>();

        if (beanOrRecord.isRecord()) {
            for (var component : beanOrRecord.getRecordComponents()) {
                result.add(new BeanPropertyModel(
                        component.getName(),
                        component.getType(),
                        component.getName(), // Getter name in Record is the component name
                        null // Records are immutable, so setter is null
                ));
            }
        } else {
            for (var field : beanOrRecord.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }

                var fieldName = field.getName();
                var fieldType = field.getType();
                var methodSuffix = capitalize(fieldName);

                // Resolve getter based on boolean logic preference
                var getterName = resolveGetter(beanOrRecord, fieldType, methodSuffix);

                // Resolve setter
                var setterName = resolveSetter(beanOrRecord, fieldType, methodSuffix);

                // Only add properties where a getter exists (as getter field is not nullable)
                if (getterName != null) {
                    result.add(new BeanPropertyModel(fieldName, fieldType, getterName, setterName));
                }
            }
        }
        return result;
    }

    /**
     * Resolves the getter name with preference for "is" prefix on primitive booleans.
     */
    private static String resolveGetter(Class<?> clazz, Class<?> type, String suffix) {
        var prefixGet = "get" + suffix;
        var prefixIs = "is" + suffix;

        var hasGet = hasMethod(clazz, prefixGet);
        var hasIs = hasMethod(clazz, prefixIs);

        if (type == boolean.class) {
            // For primitive boolean: prefer "is" if both exist (or if only "is" exists)
            if (hasIs) {
                return prefixIs;
            }
            return hasGet ? prefixGet : null;
        } else if (type == Boolean.class) {
            // For Object Boolean: check both.
            // Standard convention prefers "get", but we support "is".
            // If both exist, we stick to standard "get" unless only "is" is found.
            if (hasGet) {
                return prefixGet;
            }
            return hasIs ? prefixIs : null;
        } else {
            // Non-boolean types
            return hasGet ? prefixGet : null;
        }
    }

    /**
     * Resolves the setter name.
     */
    private static String resolveSetter(Class<?> clazz, Class<?> type, String suffix) {
        var setterName = "set" + suffix;
        return hasMethod(clazz, setterName, type) ? setterName : null;
    }

    /**
     * Helper to check if a method exists.
     */
    private static boolean hasMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            clazz.getMethod(name, parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Helper to capitalize the first letter.
     */
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}