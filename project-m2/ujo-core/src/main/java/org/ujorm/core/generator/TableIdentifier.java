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

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.Check;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Database table model identifiers
 *
 * @param table Name of the database table.
 * @param schema (Optional) The schema of the table.
 * @param catalog (Optional) The catalog of the schema.
 */
public record TableIdentifier(
        @NotNull
        String table,
        @Nullable
        String schema,
        @Nullable
        String catalog
) {

    /**
     * Merges the soft (annotation-based) table identifier with the real database metadata.
     * The schema and catalog are updated from the real database metadata only if they were
     * explicitly defined in the entity mapping (not null or empty). This logic prevents
     * hardcoding default database schemas or catalogs into the generated SQL, which ensures
     * query portability across different database environments.
     *
     * @param real The real table identifier obtained from database metadata.
     * @return result - A new merged TableIdentifier instance.
     */
    public TableIdentifier merge(@NotNull TableIdentifier real) {
        return new TableIdentifier(
                real.table,
                mergeValue(this.schema, real.schema),
                mergeValue(this.catalog, real.catalog)
        );
    }

    /**
     * Helper method to merge a single string value.
     *
     * @param original The original value from the annotation.
     * @param real The real value from the database.
     * @return The merged value.
     */
    private String mergeValue(String original, String real) {
        return Check.isEmpty(original) ? null : real;
    }

    /** Get Table name in the full format: {@code catalog.schema.table} . */
    public String getQualifiedName() {
        return Stream.of(
                        catalog,
                        schema,
                        table)
                .filter(s -> Check.hasLength(s))
                .collect(Collectors.joining("."));
    }

    /**
     * Get table name in the full format with each identifier segment quoted separately.
     * Example with "[]": {@code [catalog].[schema].[table]}.
     */
    public String getQualifiedName(@NotNull String quotes) {
        var result = new StringBuilder(64);
        var q = Check.hasLength(quotes) ? quotes : " ";
        writeQualifiedName(q.charAt(0), q.charAt(q.length() - 1), result);
        return result.toString();
    }

    /** Writes a qualified table name to a target writer without creating intermediate strings. */
    public void writeQualifiedName(char open, char close, StringBuilder writer) {
        var hasQuotes = open != ' ' || close != ' ';
        var startLength = writer.length();

        appendSegment(writer, catalog, hasQuotes, open, close, startLength);
        appendSegment(writer, schema, hasQuotes, open, close, startLength);
        appendSegment(writer, table, hasQuotes, open, close, startLength);
    }

    private void appendSegment(StringBuilder writer, String segment, boolean hasQuotes, char open, char close, int startLength) {
        if (Check.hasLength(segment)) {
            if (writer.length() > startLength) {
                writer.append('.');
            }
            if (hasQuotes) {
                writer.append(open).append(segment).append(close);
            } else {
                writer.append(segment);
            }
        }
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

    /**
     * Extracts a value from the Table annotation using the provided extractor.
     *
     * @param table     The Table annotation (can be null).
     * @param extractor The function to extract the string value (e.g., Table::name).
     * @param defaultValue The value to return if the annotation is null or the extracted value is empty.
     * @return result - The resolved string value.
     */
    static String getFromTable(Table table, Function<Table, String> extractor, String defaultValue) {
        if (table == null) return defaultValue;
        var value = extractor.apply(table);
        return value.isEmpty() ? defaultValue : value;
    }

    /**
     * The class is mapped to a database table, so it can be a target of a relation.
     * <p>Note that a foreign key is assigned by the {@code @JoinColumn} annotation too,
     * hence the type of a foreign key needs not to be an entity at all - a raw value
     * of a foreign key column is a common case.
     *
     * @param type A type of a domain property.
     * @return The type is a database entity.
     */
    public static boolean isEntity(@NotNull Class<?> type) {
        return type.isAnnotationPresent(Entity.class)
                || type.isAnnotationPresent(Table.class);
    }

    /** Get data from annotation */
    public static <D> TableIdentifier of(Class<D> beanClass) {
        var tableAnnotation = beanClass.getAnnotation(Table.class);
        var table = getFromTable(tableAnnotation, Table::name,
                toSnakeCase(beanClass.getSimpleName()));
        var schema = getFromTable(tableAnnotation, Table::schema, "");
        var catalog = getFromTable(tableAnnotation, Table::catalog, "");
        return new TableIdentifier(table, schema, catalog);
    }
}