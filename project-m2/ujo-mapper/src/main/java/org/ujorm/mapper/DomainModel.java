package org.ujorm.mapper;

import jakarta.persistence.Table;
import java.util.List;
import java.util.function.Function;

public record DomainModel(
        /** Data class of the bean or record. */
        Class<?> domainClass,
        /** Name of the database table. */
        String databaseTable,
        /** (Optional) The schema of the table. */
        String databaseSchema,
        /** (Optional) The catalog of the table. */
        String databaseCatalog,
        /** List of properties */
        List<DomainPropertyModel> properties
) {

    /**
     * Factory method to create a BeanModel instance.
     *
     * @param beanClass The class to inspect.
     * @return result - The populated BeanModel.
     */
    public static DomainModel of(Class<?> beanClass) {
        var tableAnnotation = beanClass.getAnnotation(Table.class);
        var table = getFromTable(tableAnnotation, Table::name,
                toSnakeCase(beanClass.getSimpleName()));
        var schema = getFromTable(tableAnnotation, Table::schema, null);
        var catalog = getFromTable(tableAnnotation, Table::catalog, null);
        var properties = DomainPropertyModel.of(beanClass);
        return new DomainModel(beanClass, table, schema, catalog, properties);
    }

    /**
     * Extracts a value from the Table annotation using the provided extractor.
     *
     * @param table     The Table annotation (can be null).
     * @param extractor The function to extract the string value (e.g., Table::name).
     * @param defaultValue The value to return if the annotation is null or the extracted value is empty.
     * @return result - The resolved string value.
     */
    private static String getFromTable(Table table, Function<Table, String> extractor, String defaultValue) {
        var result = (table != null) ? extractor.apply(table) : "";
        return result.isEmpty()
            ? defaultValue
            : result;
    }

    /**
     * Converts CamelCase string to snake_case using Regex.
     *
     * @param text The text to convert.
     * @return result - The converted string.
     */
    static String toSnakeCase(String text) {
        return text.replaceAll("(?<!^)(?=[A-Z])", "_").toLowerCase();
    }
}