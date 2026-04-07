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
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.DomainHandlerService;
import org.ujorm.core.criterion.Criterion;
import org.ujorm.orm.core.EntityManager;
import org.ujorm.orm.model.QuotePair;
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

    /** Handler service */
    private final DomainHandlerService handlerService;

    /** Columns */
    private final DslQueryBuilder builder;

    /** Column quotes */
    private final QuotePair q;

    /** Sql Tail */
    @NotNull
    private CharSequence[] sqlTail;

    /**
     * Constructor with a database connection
     * @param dbConnection A database connection
     */
    public DslQuery(@NotNull Connection dbConnection,
                    @NotNull QuotePair quote,
                    @NotNull DomainHandlerService handlerService
    ) {
        super(dbConnection);
        this.handlerService = handlerService;
        this.q = quote;
        this.builder = new DslQueryBuilder(new Writer());
    }

    /**
     * Constructor with a database connection
     * @param dbConnection A database connection
     */
    public DslQuery(@NotNull Connection dbConnection,
                    @NotNull QuotePair quote
    ) {
        this(dbConnection, quote, DomainHandlerProvider.provider());
    }

    @Override
    public DslQuery<D> sql(@NotNull String... sqlItems) {
        initWriter();
        // TODO:
        var sql = String.join(" ", sqlItems);
        super.sql(sql);
        return self();
    }

    /** Write a SQL condition. */
    public DslQuery<D> where (@NotNull Criterion criterion) {
        this.builder.where(criterion);
        return self();
    }

    /** Append an optional rest of the SQL statement */
    public DslQuery<D> tail(@NotNull CharSequence... sqlTail) {
        this.sqlTail = sqlTail;
        return self();
    }

    // ------- COLUMNS -------

    /**
     * Add a SQL column definition dynamically to replace the {@code ${COLUMNS} } placeholder.
     * The provided metamodel attributes define a type-safe path to the specific property
     * (e.g., entity -> relation -> property) and are concatenated to form the final SQL column label.
     */
    public DslQuery<D> column(@NotNull Key<?,?> attr) {
        return putColumn(EMPTY, attr);
    }

    /**
     * Add a SQL column definition dynamically to replace the {@code ${COLUMNS} } placeholder.
     * The provided metamodel attributes define a type-safe path to the specific property
     * (e.g., entity -> relation -> property) and are concatenated to form the final SQL column label.
     */
    public <V1> DslQuery<D> column(@NotNull Key<D,V1> attr1,
                                   @NotNull Key<V1,?> attr2) {
        return putColumn(EMPTY, attr1, attr2);
    }

    /**
     * Add a SQL column definition dynamically to replace the {@code ${COLUMNS} } placeholder.
     * The provided metamodel attributes define a type-safe path to the specific property
     * (e.g., entity -> relation -> property) and are concatenated to form the final SQL column label.
     */
    public <V1,V2> DslQuery<D> column(@NotNull Key<D,V1> attr1,
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
    public final <V1,V2,V3> DslQuery<D> column(@NotNull Key<D,V1> attr1,
                                               @NotNull Key<V1,V2> attr2,
                                               @NotNull Key<V2,V3> attr3,
                                               @NotNull Key<V3,?> attr4,
                                               @NotNull Key<?,?>... attrs) {
        return putColumn(attrs, attr1, attr2, attr3, attr4);
    }

    /** Validates and adds an expression to the internal map */
    DslQuery<D> putColumn(
            @NotNull Key<?,?>[] attrs,
            @NotNull Key<?,?>... keys
    ) {
        var result = new Key<?,?>[keys.length + attrs.length];
        System.arraycopy(keys, 0, result, 0, keys.length);
        if (attrs != EMPTY) System.arraycopy(attrs, 0, result, keys.length, attrs.length);
        this.builder.column(result);
        return self();
    }

    public final class Writer implements DslQueryWriter {

        final StringBuilder writer = initWriter();

        /** Write database table name. */
        @Override
        public void writeTableName(@NotNull String tableAlias, @NotNull Class<?> entityClass) {
            writer.append(q.open())
                    .append(entityClass.getSimpleName())
                    .append(q.close())
                    .append(' ').append(tableAlias);
        }

        /** Write database column name. */
        @Override
        public void writeColumnName(@NotNull String tableAlias, @NotNull Key<?,?> column, Key<?,?>... labels) {
            writer.append(q.open()).append(tableAlias).append('.').append(column.name()).append(q.close());

            var printLabel = labels.length > 0;
            if (printLabel) {
                writer.append(" AS ");
                writer.append(q.open());
                for (var i = 0; i < labels.length; i++) {
                    if (i > 0) writer.append('.');
                    writer.append(labels[i].name());
                }
                writer.append(q.close());
            }
        }

        @Override
        public StringBuilder append(String str) {
            return writer.append(str);
        }

        @Override
        public StringBuilder append(char str) {
            return writer.append(str);
        }
    }

    /** Run a query statement */
    public static <D, R> R run(Connection connection, QuotePair quote, DomainHandlerService service, SqlFunction<DslQuery<D>, R> fun) {
        try (var query = new DslQuery<D>(connection, quote, service)) {
            return fun.applyFunction(query);
        } catch (Exception ex) {
            throw (ex instanceof RuntimeException re) ? re : new SqlException(ex);
        }
    }

    /** Run a query statement */
    public static <D, R> R run(Connection connection, QuotePair quote, SqlFunction<DslQuery<D>, R> fun) {
        return run(connection, quote, DomainHandlerProvider.provider(), fun);
    }

    /** Run a query statement */
    public static <D, V, R> R run(Connection connection, EntityManager<D, V> em, SqlFunction<DslQuery<D>, R> fun) {
        return run(connection, em.tableModel(connection).jdbc().quotes(), DomainHandlerProvider.provider(), fun);
    }
}