/*
 * Copyright 2024-2026 Pavel Ponec
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/jdbc/SqlBuilder.java
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
package org.ujorm.orm.dsl;

import org.jetbrains.annotations.NotNull;
import org.ujorm.core.criterion.Criterion;
import org.ujorm.tools.jdbc.AbstractSqlQuery;
import org.ujorm.tools.jdbc.SQLException;
import org.ujorm.core.Key;

import java.sql.Connection;

/**
 * A fluent wrapper over {@link java.sql.PreparedStatement}
 * that manages named parameters and ensures automatic resource cleanup
 * of both statements and result sets.
 * This class has no dependencies other than its abstract parent, annotations, and {@link SQLException}.
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
public class DslQuery<D> extends AbstractSqlQuery<DslQuery<D>> {

    /** Empty key array */
    private static final Key<?,?>[] EMPTY = new Key<?,?>[0];

    /** Columns */
    private final DslBuilder builder;

    /**
     * Constructor with a database connection
     * @param dbConnection A database connection
     */
    public DslQuery(@NotNull Connection dbConnection) {
        super(dbConnection);
        builder = new DslBuilder(super.initWriter());
    }

    @Override
    public DslQuery<D> sql(@NotNull String... sqlLines) {
        super.sql(sqlLines);
        _writer.append(super.sqlTemplate);
        return self();
    }

    public <V> DslQuery<D> where (@NotNull Criterion condition) {
        return self();
    }

    public <V> DslQuery<D> append(@NotNull CharSequence... sqlTail) {
        return self();
    }

    // ------- COLUMNS -------

    /**
     * Add a SQL column definition dynamically to replace the {@code ${COLUMNS} } placeholder.
     * The provided metamodel attributes define a type-safe path to the specific property
     * (e.g., entity -> relation -> property) and are concatenated to form the final SQL column label.
     */
    public DslQuery column(@NotNull Key<?,?> attr) {
        return putColumn(EMPTY, attr);
    }

    /**
     * Add a SQL column definition dynamically to replace the {@code ${COLUMNS} } placeholder.
     * The provided metamodel attributes define a type-safe path to the specific property
     * (e.g., entity -> relation -> property) and are concatenated to form the final SQL column label.
     */
    public <V1> DslQuery column(@NotNull Key<D,V1> attr1,
                                @NotNull Key<V1,?> attr2) {
        return putColumn(EMPTY, attr1, attr2);
    }

    /**
     * Add a SQL column definition dynamically to replace the {@code ${COLUMNS} } placeholder.
     * The provided metamodel attributes define a type-safe path to the specific property
     * (e.g., entity -> relation -> property) and are concatenated to form the final SQL column label.
     */
    public <V1,V2> DslQuery column(@NotNull Key<D,V1> attr1,
                                   @NotNull Key<V1,V2> attr2,
                                   @NotNull Key<V2,?> attr3) {
        return putColumn(EMPTY, attr1, attr2, attr3);
    }

    /**
     * Add a SQL column definition dynamically to replace the {@code ${COLUMNS} } placeholder.
     * The provided metamodel attributes define a type-safe path to the specific property
     * (e.g., entity -> relation -> property) and are concatenated to form the final SQL column label.
     */
    @SafeVarargs
    public final <V1,V2,V3> DslQuery column(@NotNull Key<D,V1> attr1,
                                            @NotNull Key<V1,V2> attr2,
                                            @NotNull Key<V2,V3> attr3,
                                            @NotNull Key<V3,?> attr4,
                                            @NotNull Key<?,?>... attrs) {
        return putColumn(attrs, attr1, attr2, attr3, attr4);
    }

    /** Validates and adds an expression to the internal map */
    DslQuery putColumn(
            @NotNull Key<?,?>[] attrs,
            @NotNull Key<?,?>... keys
    ) {
        var result = new Key<?,?>[keys.length + attrs.length];
        System.arraycopy(keys, 0, result, 0, keys.length);
        if (attrs != EMPTY) System.arraycopy(attrs, 0, result, keys.length, attrs.length);
        this.builder.column(result);
        return self();
    }

    /** Run a query statement */
    public static <R> R run(Connection connection, final SqlFunction<DslQuery, R> fun) {
        try (var query = new DslQuery(connection)) {
            return fun.applyFunction(query);
        } catch (Exception ex) {
            throw (ex instanceof RuntimeException re) ? re : new SqlException(ex);
        }
    }
}