package org.ujorm.core.generator;

import jakarta.persistence.Table;
import java.util.List;
import java.util.function.Function;

public record DomainModel(
        /** Data class of the bean or record. */
        Class<?> domainClass,
        /** Database table attribues */
        DatabaseModel database,
        /** List of properties */
        List<DomainPropertyModel> properties
) {

    /** Is the domain object the Record ?*/
    public boolean isRecord() {
        return domainClass.isRecord();
    }

    /**
     * Factory method to create a BeanModel instance.
     *
     * @param domainClass The class to inspect.
     * @return result - The populated BeanModel.
     */
    public static DomainModel of(Class<?> domainClass) {
        var properties = DomainPropertyModel.of(domainClass);
        var database = tableModel(domainClass);
        return new DomainModel(domainClass, database, properties);
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

    /** Get data from annotation */
    public static DatabaseModel tableModel(Class<?> beanClass) {
        var tableAnnotation = beanClass.getAnnotation(Table.class);
        var table = getFromTable(tableAnnotation, Table::name,
                toSnakeCase(beanClass.getSimpleName()));
        var schema = getFromTable(tableAnnotation, Table::schema, null);
        var catalog = getFromTable(tableAnnotation, Table::catalog, null);
        return new DatabaseModel(table, schema, catalog);
    }
}