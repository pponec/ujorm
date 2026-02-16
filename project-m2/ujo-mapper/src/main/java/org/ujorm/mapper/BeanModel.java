package org.ujorm.mapper;

import jakarta.persistence.Table;

import java.util.List;

public record BeanModel(
        /** Class of the beam */
        Class<?> beanClass,
        /** Type of property from the class field. */
        String databaseTable,
        /** (Optional) The schema of the table. */
        String databaseSchema,
        /** (Optional) The catalog of the table. */
        String databaseCatalog,
        /** Name of the getter method */
        List<BeanPropertyModel> properties
) {
    public static BeanModel of(Class<?> beanClass) {
        return new BeanModel (beanClass, getDatabaseTable(beanClass), BeanPropertyModel.of(beanClass));
    }

    /**
     * Resolves the database table name.
     * Uses @Table(name="...") if present, otherwise converts the class name to snake_case.
     *
     * @param beanClass The class to inspect.
     * @return The table name.
     */
    static String getDatabaseTable(Class<?> beanClass) {
        var table = beanClass.getAnnotation(Table.class);
        return (table != null && !table.name().isEmpty())
                ? table.name()
                : toSnakeCase(beanClass.getSimpleName());
    }

    /**
     * Converts CamelCase string to snake_case using Regex.
     * Example: "UserProfile" -> "user_profile"
     */
    static String toSnakeCase(String text) {
        return text.replaceAll("(?<!^)(?=[A-Z])", "_").toLowerCase();
    }

}