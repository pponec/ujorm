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
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.Key;
import org.ujorm.core.criterion.Criterion;
import org.ujorm.core.criterion.TemplateValue;
import org.ujorm.core.criterion.ValueCriterion;
import org.ujorm.orm.core.EntityManager;
import org.ujorm.orm.model.QuotePair;
import org.ujorm.orm.utils.JdbcUtils;
import org.ujorm.tools.common.Array;
import org.ujorm.tools.jdbc.AbstractSqlQuery;
import org.ujorm.tools.jdbc.SQLException;
import org.ujorm.tools.jdbc.SQLExceptionBuilder;

import java.sql.Connection;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A fluent wrapper over {@link java.sql.PreparedStatement}
 * that manages named parameters and ensures automatic resource cleanup
 * of both statements and result sets.
 * This class has no dependencies other than its abstract parent, annotations, and {@link SQLException}.
 *
 * <h4>Sample of usage</h4>
 * <pre>
 * var employees = SelectQuery.run(connection(), EMPLOYEE_EM, query -> query
 *    .sql("SELECT")
 *    .columnsOfDomain(true)
 *    .column(MetaEmployee.city, MetaCity.name)
 *    .column(MetaEmployee.city, MetaCity.countryCode)
 *    .where(MetaEmployee.id.whereGe(1L))
 *    .tail("ORDER BY", MetaEmployee.id)
 *    .streamMap(EMPLOYEE_MAPPER.mapper())
 *    .toList()
 * );
 * </pre>
 * Licence: Apache License, Version 2.0
 * @author Pavel Ponec, https://github.com/pponec
 * @since 3.0
 */
public class SelectQuery<D> extends AbstractSqlQuery<SelectQuery<D>> {

    /** Empty key array */
    private static final Key<?,?>[] EMPTY = new Key<?,?>[0];

    /** Entity manager */
    private final EntityManager<D,?> entityManager;

    /** Columns */
    private final SelectQueryBuilder builder;

    /** Columns */
    private final SelectWriter dslWriter;

    /** Column quotes */
    @NotNull
    private QuotePair q = QuotePair.ofDefault();

    /** Sql Tail */
    @Nullable
    private CharSequence[] sqlHead;

    /** Sql Tail */
    @Nullable
    private CharSequence[] sqlTail;

    /** Counter of the placeholders */
    private int placeholderCounter;

    /**
     * Constructor with a database connection
     * @param dbConnection A database connection
     */
    public SelectQuery(@NotNull Connection dbConnection, @NotNull EntityManager<D, ?> entityManager) {
        super(dbConnection);
        this.dslWriter = new SelectWriter(getWriter(true));
        this.builder = new SelectQueryBuilder(dslWriter);
        this.entityManager = entityManager;
        var config = entityManager.getConfig();
        this.log(config.getLogSqlLevel(), config.isLogSqlParams());
    }

    @Override
    public void close() {
        super.close();
        sqlHead = null;
        sqlTail = null;
        placeholderCounter = 0;
        builder.close();
    }

    /** Head of the SQL where default is SELECT. */
    @Override
    public SelectQuery<D> sql(@NotNull CharSequence... sqlHead) {
        super.sql("");
        this.sqlHead = sqlHead;
        return self();
    }

    /** Adds a SQL condition. Repeated calls append conditions using the AND operator. */
    public SelectQuery<D> where (@NotNull Criterion criterion) {
        this.builder.where(criterion);
        return self();
    }

    /** Adds many SQL conditions joined by the AND operator */
    public SelectQuery<D> whereAnd (@NotNull Criterion... criterions) {
        for (Criterion crn : criterions) {
            where(crn);
        }
        return self();
    }

    /**
     * Append an optional rest of the SQL statement.
     * The method allows mixing raw SQL fragments (String) with Meta objects
     * to ensure type-safe column names and automatic aliasing.
     * <p>
     * Usage examples:
     * <pre>{@code
     * query.tail("ORDER BY", MetaEmployee.name, "DESC NULLS LAST");
     * query.tail("FOR UPDATE");
     * query.tail("GROUP BY", MetaEmployee.city, "HAVING COUNT(*) > 1");
     * query.tail("LIMIT 10 OFFSET 5");
     * }</pre>
     *
     * @param sqlTail SQL fragments or Meta objects (e.g. {@code Key})
     * @return The same instance
     */
    public SelectQuery<D> tail(@NotNull CharSequence... sqlTail) {
        this.sqlTail = sqlTail;
        return self();
    }

    // ------- COLUMNS -------

    /** Add all properties of the main domain objects */
    public SelectQuery<D> columns(boolean includingForeignKeys) {
        for (Key<D,?> key : entityManager.getDomainHandler().getKeyList()) {
            if (includingForeignKeys || !key.info().foreignKey()) {
                this.builder.column(key);
            }
        }
        return this;
    }

    /**
     * Add a SQL column definition dynamically to replace the {@code ${COLUMNS} } placeholder.
     * The provided metamodel attributes define a type-safe path to the specific property
     * (e.g., entity -> relation -> property) and are concatenated to form the final SQL column label.
     */
    public SelectQuery<D> column(@NotNull Key<?,?> attr) {
        return putColumn(EMPTY, attr);
    }

    /**
     * Add a SQL column definition dynamically to replace the {@code ${COLUMNS} } placeholder.
     * The provided metamodel attributes define a type-safe path to the specific property
     * (e.g., entity -> relation -> property) and are concatenated to form the final SQL column label.
     */
    public <V1> SelectQuery<D> column(@NotNull Key<D,V1> attr1,
                                      @NotNull Key<V1,?> attr2) {
        return putColumn(EMPTY, attr1, attr2);
    }

    /**
     * Add a SQL column definition dynamically to replace the {@code ${COLUMNS} } placeholder.
     * The provided metamodel attributes define a type-safe path to the specific property
     * (e.g., entity -> relation -> property) and are concatenated to form the final SQL column label.
     */
    public <V1,V2> SelectQuery<D> column(@NotNull Key<D,V1> attr1,
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
    public final <V1,V2,V3> SelectQuery<D> column(@NotNull Key<D,V1> attr1,
                                                  @NotNull Key<V1,V2> attr2,
                                                  @NotNull Key<V2,V3> attr3,
                                                  @NotNull Key<V3,?> attr4,
                                                  @NotNull Key<?,?>... attrs) {
        return putColumn(attrs, attr1, attr2, attr3, attr4);
    }

    /** Validates and adds an expression to the internal map */
    SelectQuery<D> putColumn(
            @NotNull Key<?,?>[] attrs,
            @NotNull Key<?,?>... keys
    ) {
        var result = new Key<?,?>[keys.length + attrs.length];
        System.arraycopy(keys, 0, result, 0, keys.length);
        if (attrs != EMPTY) System.arraycopy(attrs, 0, result, keys.length, attrs.length);
        this.builder.column(result);
        return self();
    }

    @NotNull
    @Override
    protected String buildSql(List<ParamValue> sqlValues, boolean includingValues) {
        if (sqlTemplate.isEmpty()) {
            var sqlWriter = getWriter(true);
            var baseEntityClass = entityManager.getDomainHandler().getDomainClass();
            var tableModel = entityManager.getTableModelService().getTableModel(baseEntityClass, dbConnection);
            this.q = tableModel.jdbc().quotes(); // Assign real quotes.

            writeSqlParts(sqlHead);
            builder.build();
            writeSqlParts(sqlTail);
            sqlTemplate = sqlWriter.toString();
        }

        if (sqlValues instanceof ArrayList<ParamValue> array) {
            array.ensureCapacity(10);
        }

        return super.buildSql(sqlValues, includingValues);
    }

    /** Print plain texts, database table or columns */
    void writeSqlParts(CharSequence[] items) {
        if (items == null || items.length == 0) return;
        var writer = dslWriter.append("");
        if (!writer.isEmpty()) writer.append('\n');

        for (var i = 0; i < items.length; i++) {
            var item = items[i];
            if (i > 0) dslWriter.append(' ');
            if (item instanceof Key<?,?> key) {
                var alias = builder.findTableAlias(key);
                dslWriter.writeColumnName(alias, key, EMPTY);
            } else if (item instanceof TableAlias tableAlias) { // Config.DSL_SELECT_ONLY ?
                var tableModel = entityManager.getTableModelService()
                        .getTableModel(tableAlias.domainClass(), dbConnection);
                dslWriter.append(q.open()).append(tableModel.tableName()).append(q.close());
                if (!tableAlias.alias().isEmpty()) {
                    dslWriter.append(' ').append(tableAlias.alias());
                }
            } else {
                dslWriter.append(item);
            }
        }
    }

    /** Executes the query and returns a Stream of mapped results. */
    @NotNull
    public Stream<D> streamMap() {
        return streamMap(entityManager.mapper());
    }

    /** Executes the query and returns a List of mapped results. */
    @NotNull
    public List<D> toList() {
        return streamMap().toList();
    }

    /** Find the first element. */
    @NotNull
    public Optional<D> findFirst() {
        return streamMap().findFirst();
    }

    /**
     * Finds a unique result.
     * If more than one row is found, an exception is thrown.
     * @return An Optional containing the unique result, or empty if no result was found.
     * @throws IllegalStateException If more than one row is found.
     */
    @NotNull
    public Optional<D> findUnique() {
        var resultList = streamMap().limit(2).toList();
        return switch (resultList.size()) {
            case 0 -> Optional.empty();
            case 1 -> Optional.of(resultList.get(0));
            default -> throw new IllegalStateException("Expected one result at most, but found more");
        };
    }

    // --- INNER CLASSES ---

    public final class SelectWriter implements SelectQueryWriter {

        final StringBuilder writer;

        public SelectWriter(StringBuilder writer) {
            this.writer = writer;
        }

        /** Write database table name. */
        @Override
        public void writeTableName(@NotNull String tableAlias, @NotNull Class<?> entityClass) {
            var tableModel = entityManager.getTableModelService().getTableModel(entityClass, dbConnection);
            writer.append(q.open())
                    .append(tableModel.tableName())
                    .append(q.close())
                    .append(' ').append(tableAlias);
        }

        /** Write database column name. If labels are available, append labels for the SQL SELECT statement. */
        @Override
        public void writeColumnName(@NotNull String tableAlias, @NotNull Key<?,?> key, Key<?,?>... labels) {
            var columnModel = entityManager.getTableModelService().getColumnModel(key, dbConnection);
            writer.append(tableAlias).append('.').append(q.open()).append(columnModel.name()).append(q.close());

            if (labels.length > 0) {
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
        public void writeCondition(ValueCriterion<?> criterion, @NotNull String alias) {
            var key = (Key<?, ?>) criterion.getLeftNode();
            var operator = criterion.getOperator();
            var value = criterion.getRightNode();
            var jdbcType = JdbcUtils.findJdbcType(key);

            switch (operator) {
                case ALWAYS_TRUE, ALWAYS_FALSE -> writer.append(operator.term());
                case CUSTOM_SQL -> {
                    if (value instanceof TemplateValue<?> tv) {
                        appendColumn(alias, key, jdbcType, tv);
                    }
                }
                case IN, NOT_IN -> {
                    var placeholder = nextPlaceholder(alias, key);
                    writeColumnName(alias, key, EMPTY);
                    writer.append(' ').append(operator.term()).append(" (:").append(placeholder).append(')');
                    bindObject(true, placeholder, jdbcType, Array.ofObject(value));
                }
                default -> {
                    var placeholder = nextPlaceholder(alias, key);
                    writeColumnName(alias, key, EMPTY);
                    writer.append(' ').append(operator.term()).append(" :").append(placeholder);
                    bindObject(true, placeholder, jdbcType, Array.ofObject(value));
                }
            }
        }

        /** Format custom SQL templates with named parameters */
        private void appendColumn(String alias, Key<?, ?> key, JDBCType jdbcType, TemplateValue<?> templateValue) {
            var template = templateValue.template();
            var last = 0;
            var i = 0;
            while ((i = template.indexOf('{', i)) != -1) {
                if (i + 2 < template.length() && template.charAt(i + 2) == '}') {
                    var mark = template.charAt(i + 1);
                    if (mark == '0' || mark == '1') {
                        writer.append(template, last, i); // Appends without substring allocation!
                        if (mark == '0') {
                            writeColumnName(alias, key, EMPTY);
                        } else {
                            var placeholder = nextPlaceholder(alias, key);
                            writer.append(':').append(placeholder);
                            bindObject(true, placeholder, jdbcType, templateValue.valuesNonNull());
                        }
                        i += 3;
                        last = i;
                        continue;
                    }
                }
                i++;
            }
            writer.append(template, last, template.length());
        }

        /** Generate next unique placeholder parameter name */
        private String nextPlaceholder(String alias, Key<?, ?> key) {
            return alias + '_' + key.name() + '_' + (placeholderCounter++);
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

    // --- STATIC METHODS ---

    /** Run a query statement */
    public static <D, R> R run(Connection connection, EntityManager<D, ?> em, SqlFunction<SelectQuery<D>, R> fun) {
        try (var query = new SelectQuery<D>(connection, em)) {
            return fun.applyFunction(query);
        } catch (Exception ex) {
            throw (ex instanceof RuntimeException re) ? re : SQLExceptionBuilder.build(ex);
        }
    }

}