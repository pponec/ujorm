/*
 * Copyright 2024-2026 Pavel Ponec
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/jdbc/SqlBuilder.java
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
package org.ujorm.orm;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.Key;
import org.ujorm.tools.common.StringUtils;
import org.ujorm.tools.jdbc.AbstractSqlQuery;
import org.ujorm.tools.msg.MessageService;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A fluent wrapper over {@link java.sql.PreparedStatement}
 * that manages named parameters and ensures automatic resource cleanup
 * of both statements and result sets.
 * Apart from annotations and {@link java.sql.SQLException}, this class depends only on its abstract parent.
 *
 * <h4>Sample of usage</h4>
 * <pre>
 *  List&lt;Employee&gt; employees = SqlQuery.run(dbConnection, build -> build.sql("""
 *         SELECT t.id, t.name, t.created
 *         FROM employee t
 *         WHERE t.id > :id
 *           AND t.code IN (:code)
 *         ORDER BY t.id
 *         """)
 *         .bind("id", 10)
 *         .bind("code", "T", "V")
 *         .streamMap(rs -> new Employee(
 *                 rs.getInt("id"),
 *                 rs.getString("name"),
 *                 rs.getObject("created", LocalDate.class)))
 *         .toList()
 * }
 * </pre>
 * Licence: Apache License, Version 2.0
 * Original source: <a href="https://github.com/pponec/PPScriptsForJava/blob/development/src/main/java/net/ponec/script/SqlExecutor.java">GitHub</a>
 * @author Pavel Ponec, https://github.com/pponec
 * @since 2.26
 */
public class SqlQuery extends AbstractSqlQuery<SqlQuery> {

    /** SQL parameter mark type of {@code :param} */
    static final String COLUMNS_MARK = "COLUMNS";

    @Nullable
    private Map<String, Object> columnLabels;
    private boolean hasColumnsMode = false;
    /** Label quoter */
    private final char q;

    public SqlQuery(@NotNull Connection dbConnection) {
        this(dbConnection, '"');
    }

    public SqlQuery(@NotNull Connection dbConnection, char quoter) {
        super(dbConnection);
        this.q = quoter;
    }

    @Override
    public SqlQuery sql(@NotNull String... sqlLines) {
        super.sql(sqlLines);
        this.hasColumnsMode = this.sqlTemplate != null && this.sqlTemplate.contains("${" + COLUMNS_MARK + "}");
        return this;
    }

    @Override
    public void close() {
        super.close();
        if (columnLabels != null) {
            columnLabels.clear();
        }
    }

    @NotNull
    @Override
    protected String buildColumns(@NotNull String sql) {
        if (hasColumnsMode) {
            if (columnLabels == null || columnLabels.isEmpty()) {
                var msg = "Placeholder ${%s} found but no columns defined.".formatted(COLUMNS_MARK);
                throw new IllegalStateException(msg);
            }
            var columnsStr = columnLabels.values().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("\n, "));

            return sql.replace("${" + COLUMNS_MARK + "}", columnsStr);
        } else if (columnLabels != null && !columnLabels.isEmpty()) {
            return MessageService.formatMsg(sql, columnLabels);
        }
        return sql;
    }

    // ------- COMMON PUT METHOD -------

    /** Validates and adds an expression to the internal map */
    protected SqlQuery putColumnOrLabel(boolean isColumn, String placeholder, CharSequence... attrs) {
        if (hasColumnsMode != isColumn) {
            var msg = (isColumn
                    ? "Method column() requires ${%s} placeholder in SQL."
                    : "Cannot mix label() method with ${%s} placeholder.")
                    .formatted(COLUMNS_MARK);
            throw new IllegalStateException(msg);
        }
        if (StringUtils.isEmpty(placeholder)) {
            throw new IllegalArgumentException("Key placeholder is required");
        }
        if (columnLabels == null) {
            columnLabels = new LinkedHashMap<>();
        }

        var builder = initBuilder();
        int labelOffset = 0;

        if (isColumn) {
            builder.append(placeholder).append(" AS ");
            labelOffset = builder.length();
        }

        builder.append(q);
        for (int i = 0; i < attrs.length; i++) {
            if (i > 0) builder.append('.');
            builder.append(attrs[i]);
        }
        builder.append(q);

        var mapValue = builder.toString();
        var labelStr = labelOffset == 0 ? mapValue : builder.substring(labelOffset);
        checkLabel(labelStr);

        if (columnLabels.putIfAbsent(placeholder, mapValue) != null) {
            throw new IllegalArgumentException("Duplicate key: " + placeholder);
        }
        return self();
    }

    /** A hook method to validate a column label.
     * By default, this method only checks for a non-null value to avoid performance overhead
     * during query building. The responsibility for correct mapping lies with the developer.
     */
    protected void checkLabel(@NotNull String labelStr) {
        Objects.requireNonNull(labelStr);
    }

    // ------- LABELS -------

    /** Add a column label to the placeholder in the format {@code ${placeholder} }.
     * The provided metamodel attributes define a type-safe path to the specific property
     * (e.g., entity -> relation -> property) and are concatenated to form the final SQL column label.
     */
    public SqlQuery label(@NotNull String placeholder, @NotNull Key<?,?> attr) {
        return putColumnOrLabel(false, placeholder, attr);
    }

    /** Add a column label to the placeholder in the format {@code ${placeholder} }.
     * The provided metamodel attributes define a type-safe path to the specific property
     * (e.g., entity -> relation -> property) and are concatenated to form the final SQL column label.
     */
    public <V1> SqlQuery label(@NotNull String placeholder,
                               @NotNull Key<?,V1> attr1,
                               @NotNull Key<V1,?> attr2) {
        return putColumnOrLabel(false, placeholder, attr1, attr2);
    }

    /** Add a column label to the placeholder in the format {@code ${placeholder} }.
     * The provided metamodel attributes define a type-safe path to the specific property
     * (e.g., entity -> relation -> property) and are concatenated to form the final SQL column label.
     */
    public <V1,V2> SqlQuery label(@NotNull String placeholder,
                                  @NotNull Key<?,V1> attr1,
                                  @NotNull Key<V1,V2> attr2,
                                  @NotNull Key<V2,?> attr3) {
        return putColumnOrLabel(false, placeholder, attr1, attr2, attr3);
    }

    /** Add a column label to the placeholder in the format {@code ${placeholder} }.
     * The provided metamodel attributes define a type-safe path to the specific property
     * (e.g., entity -> relation -> property) and are concatenated to form the final SQL column label.
     */
    @SafeVarargs
    public final <V1,V2,V3> SqlQuery label(@NotNull String placeholder,
                                           @NotNull Key<?,V1> attr1,
                                           @NotNull Key<V1,V2> attr2,
                                           @NotNull Key<V2,V3> attr3,
                                           @NotNull Key<V3,?> attr4,
                                           @NotNull Key<?,?>... attrs) {
        var allAttrs = new CharSequence[4 + attrs.length];
        allAttrs[0] = attr1;
        allAttrs[1] = attr2;
        allAttrs[2] = attr3;
        allAttrs[3] = attr4;
        System.arraycopy(attrs, 0, allAttrs, 4, attrs.length);
        return putColumnOrLabel(false, placeholder, allAttrs);
    }

    // ------- COLUMNS -------

    /** Add a SQL column definition dynamically to replace the {@code ${COLUMNS} } placeholder.
     * The provided metamodel attributes define a type-safe path to the specific property
     * (e.g., entity -> relation -> property) and are concatenated to form the final SQL column label.
     */
    public SqlQuery column(@NotNull String sqlExpression, @NotNull Key<?,?> attr) {
        return putColumnOrLabel(true, sqlExpression, attr);
    }

    /** Add a SQL column definition dynamically to replace the {@code ${COLUMNS} } placeholder.
     * The provided metamodel attributes define a type-safe path to the specific property
     * (e.g., entity -> relation -> property) and are concatenated to form the final SQL column label.
     */
    public <V1> SqlQuery column(@NotNull String sqlExpression,
                                @NotNull Key<?,V1> attr1,
                                @NotNull Key<V1,?> attr2) {
        return putColumnOrLabel(true, sqlExpression, attr1, attr2);
    }

    /** Add a SQL column definition dynamically to replace the {@code ${COLUMNS} } placeholder.
     * The provided metamodel attributes define a type-safe path to the specific property
     * (e.g., entity -> relation -> property) and are concatenated to form the final SQL column label.
     */
    public <V1,V2> SqlQuery column(@NotNull String sqlExpression,
                                   @NotNull Key<?,V1> attr1,
                                   @NotNull Key<V1,V2> attr2,
                                   @NotNull Key<V2,?> attr3) {
        return putColumnOrLabel(true, sqlExpression, attr1, attr2, attr3);
    }

    /** Add a SQL column definition dynamically to replace the {@code ${COLUMNS} } placeholder.
     * The provided metamodel attributes define a type-safe path to the specific property
     * (e.g., entity -> relation -> property) and are concatenated to form the final SQL column label.
     */
    @SafeVarargs
    public final <V1,V2,V3> SqlQuery column(@NotNull String sqlExpression,
                                            @NotNull Key<?,V1> attr1,
                                            @NotNull Key<V1,V2> attr2,
                                            @NotNull Key<V2,V3> attr3,
                                            @NotNull Key<V3,?> attr4,
                                            @NotNull Key<?,?>... attrs) {
        var allAttrs = new CharSequence[4 + attrs.length];
        allAttrs[0] = attr1;
        allAttrs[1] = attr2;
        allAttrs[2] = attr3;
        allAttrs[3] = attr4;
        System.arraycopy(attrs, 0, allAttrs, 4, attrs.length);
        return putColumnOrLabel(true, sqlExpression, allAttrs);
    }

    /** Run a builder statement */
    public static <R> R run(Connection connection, final SqlFunction<SqlQuery, R> fun) {
        try (var query = new SqlQuery(connection)) {
            return fun.applyFunction(query);
        } catch (Exception ex) {
            throw (ex instanceof RuntimeException re) ? re : new SqlException(ex);
        }
    }
}