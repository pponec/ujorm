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

import jakarta.persistence.Table;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.common.StringUtils;

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
        return StringUtils.isEmpty(original) ? null : real;
    }

    /** Get Table name in the full format: {@code catalog.schema.table} . */
    public String getQualifiedName() {
        return Stream.of(
                        catalog,
                        schema,
                        table)
                .filter(s -> StringUtils.hasLength(s))
                .collect(Collectors.joining("."));
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