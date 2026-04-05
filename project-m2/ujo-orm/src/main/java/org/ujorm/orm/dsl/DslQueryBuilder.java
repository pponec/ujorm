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
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;
import org.ujorm.core.criterion.BinaryCriterion;
import org.ujorm.core.criterion.Criterion;
import org.ujorm.core.criterion.FunctionCriterion;
import org.ujorm.core.criterion.ValueCriterion;
import org.ujorm.orm.Config;
import org.ujorm.orm.model.QuotePair;
import org.ujorm.tools.jdbc.SQLException;

import java.util.*;

/**
 * A fluent wrapper over {@link java.sql.PreparedStatement}
 * that manages named parameters and ensures automatic resource cleanup
 * of both statements and result sets.
 * This class has no dependencies other than its abstract parent, annotations, and {@link SQLException}.
 *
 * @since 2.26
 */
public class DslQueryBuilder {

    /** New Line Character */
    private static final char NEW_LINE = '\n';

    /** Space Character */
    private static final char SPACE = ' ';

    /** Columns */
    private final List<Key<?, ?>[]> columns = new ArrayList<>();

    /** Used table aliases */
    private final Set<String> usedAliases = new HashSet<>();

    /** Mapped relations to avoid duplicate JOIN clauses */
    private final Map<List<Key<?, ?>>, JoinModel> joinMap = new HashMap<>(5);

    /** Ordered list of JOIN clauses */
    private final List<JoinModel> joins = new ArrayList<>();

    /** Default aliases for domain classes */
    private final Map<Class<?>, String> domainAliases = new HashMap<>();

    /** Quoters */
    private final QuotePair q;

    /** Extracted base table alias */
    private String baseTableAlias;

    @NotNull
    private Criterion criterion = Criterion.forAll();

    /** Writer */
    @Nullable
    private StringBuilder writer;

    public DslQueryBuilder(QuotePair quotePair) {
        this.q = quotePair;
    }

    public DslQueryBuilder() {
        this(QuotePair.ofDefault());
    }

    /** Adds a description of a single column composed of sequentially linked components. */
    public void column(Key<?, ?>... column) {
        columns.add(column);
    }

    /** Set criterion */
    public void where(@Nullable Criterion criterion) {
        this.criterion = criterion != null ? criterion : Criterion.forAll();
    }

    /** Build the query */
    public StringBuilder build(@NotNull StringBuilder writer) {
        this.writer = Objects.requireNonNull(writer, "writer");
        if (writer.isEmpty()) {
            writer.append("SELECT ");
        }
        buildColumns();
        buildTable();
        buildJoins();
        buildWhere();

        this.writer = null;
        return writer;
    }

    /** Prepare model for JOINs and format SELECT columns */
    public void buildColumns() {
        if (columns.isEmpty()) {
            return;
        }

        var firstKey = columns.get(0)[0];
        var baseTable = firstKey.domainClass().getSimpleName();
        this.baseTableAlias = generateAlias(baseTable);
        this.domainAliases.putIfAbsent(firstKey.domainClass(), this.baseTableAlias);

        for (var colIdx = 0; colIdx < columns.size(); colIdx++) {
            var keyPath = columns.get(colIdx);
            var currentAlias = this.baseTableAlias;
            var keyNameCounts = new HashSet<String>();

            for (var i = 0; i < keyPath.length - 1; i++) {
                var relKey = keyPath[i];
                var subPath = Arrays.asList(keyPath).subList(0, i + 1);
                var relKeyName = relKey.name();

                if (!joinMap.containsKey(subPath)) {
                    var targetTable = relKey.type().getSimpleName();
                    var isReq = relKey.info().required();
                    String targetAlias;
                    var nextKey = keyPath[i + 1];

                    var aliasedKey = nextKey instanceof AliasedKey ak
                            ? ak
                            : (relKey instanceof AliasedKey akRel ? akRel : null);

                    if (aliasedKey != null) {
                        targetAlias = aliasedKey.tableAlias();
                        usedAliases.add(targetAlias);
                    } else {
                        targetAlias = keyNameCounts.contains(relKeyName)
                                ? generateNumberedAlias(relKeyName)
                                : generateAlias(relKeyName);
                    }

                    var joinInfo = new JoinModel(relKey, currentAlias, targetAlias, targetTable, isReq);
                    joinMap.put(subPath, joinInfo);
                    joins.add(joinInfo);
                    domainAliases.putIfAbsent(relKey.type(), targetAlias);
                }

                keyNameCounts.add(relKeyName);
                currentAlias = joinMap.get(subPath).targetAlias();
            }

            if (colIdx > 0) {
                writer.append(NEW_LINE).append(", ");
            }

            var finalKey = keyPath[keyPath.length - 1];
            var finalAlias = finalKey instanceof AliasedKey akey
                    ? akey.tableAlias()
                    : currentAlias;

            writeColumn(finalAlias, finalKey, keyPath);
        }
    }

    /** Build the FROM clause. Find a basic domain model using:
     * <br/> the first select column or
     * <br/> the first criterion column of
     */
    public void buildTable() {
        var tableName = columns.isEmpty()
                ? extractTableNameFromCriterion(this.criterion)    // The first criterion column
                : columns.get(0)[0].domainClass().getSimpleName(); // The first select column

        if (baseTableAlias == null) {
            baseTableAlias = generateAlias(tableName.isEmpty() ? "t" : tableName);
        }

        writer.append(NEW_LINE).append("FROM ")
                .append(tableName.isEmpty() ? "?" : tableName)
                .append(SPACE).append(baseTableAlias);
    }

    /** Extract base table name from criterion */
    private String extractTableNameFromCriterion(Criterion crn) {
        var result = "";
        if (crn instanceof BinaryCriterion binCrn) {
            result = extractTableNameFromCriterion(binCrn.getLeftNode());
            if (result.isEmpty()) {
                result = extractTableNameFromCriterion(binCrn.getRightNode());
            }
        } else if (crn instanceof ValueCriterion<?> valCrn) {
            result = valCrn.getLeftNode().domainClass().getSimpleName();
        }
        return result;
    }

    /** Inner/outer joins according to isRequired method */
    public void buildJoins() {
        for (var join : joins) {
            writer.append(NEW_LINE).append(join.required() ? "INNER JOIN " : "LEFT OUTER JOIN ")
                    .append(join.targetTable()).append(SPACE).append(join.targetAlias())
                    .append(" ON ");
            writeColumn(join.targetAlias(), findRelatedPrimaryKey(join.relationKey()));
            writer.append(" = ");
            writeColumn(join.sourceAlias(), join.relationKey());
        }
    }

    /** Build the WHERE clause traversing the Criterion tree */
    public void buildWhere() {
        if (this.criterion instanceof ValueCriterion<?> valCrn && valCrn.isConstant()) {
            if (Boolean.FALSE.equals(valCrn.getRightNode())) {
                writer.append(NEW_LINE).append("WHERE 1=0");
            }
            return;
        }

        writer.append(NEW_LINE).append("WHERE ");
        buildCriterionTree(this.criterion, true);
    }

    /** Recursive evaluation of the criterion tree */
    private void buildCriterionTree(Criterion crn, boolean isRoot) {
        if (crn instanceof BinaryCriterion binCrn) {
            if (!isRoot) {
                writer.append("(");
            }
            buildCriterionTree(binCrn.getLeftNode(), false);
            writer.append(SPACE).append(binCrn.getOperator().name()).append(SPACE);
            buildCriterionTree(binCrn.getRightNode(), false);
            if (!isRoot) {
                writer.append(")");
            }
        } else if (crn instanceof FunctionCriterion<?, ?> funCrn) {
            appendSimpleCriterion(funCrn.getLeftNode(), funCrn.getOperator(), funCrn.getRightNode());
        } else if (crn instanceof ValueCriterion<?> valCrn) {
            appendSimpleCriterion(valCrn.getLeftNode(), valCrn.getOperator(), valCrn.getRightNode());
        }
    }

    /** Append simple criterion to writer */
    private void appendSimpleCriterion(Object leftNode, Enum<?> operator, Object rightNode) {
        var key = (Key<?, ?>) leftNode;
        appendDatabaseColumnName(key, baseTableAlias);
        writer.append(SPACE).append(operator.name()).append(SPACE);
        formatValue(rightNode);
    }

    /** Append database column name directly to writer */
    private void appendDatabaseColumnName(Key<?, ?> column, String defaultTableAlias) {
        var resolvedAlias = column instanceof AliasedKey akey
                ? akey.tableAlias()
                : domainAliases.getOrDefault(column.domainClass(), defaultTableAlias);
        writeColumn(resolvedAlias, column);
    }

    /** Generate unique table alias from free characters */
    private String generateAlias(String name) {
        for (var c : name.toLowerCase().toCharArray()) {
            var candidate = String.valueOf(c);
            if (usedAliases.add(candidate)) {
                return candidate;
            }
        }
        return generateNumberedAlias(name);
    }

    /** Generate sequentially numbered alias */
    private String generateNumberedAlias(String name) {
        var prefix = String.valueOf(name.toLowerCase().charAt(0));
        var counter = 1;
        while (true) {
            var candidate = prefix + counter++;
            if (usedAliases.add(candidate)) {
                return candidate;
            }
        }
    }

    /** Format scalar value or arrays directly to writer */
    private void formatValue(Object value) {
        if (value == null) {
            writer.append("NULL");
        } else if (value instanceof String str) {
            writer.append("'").append(str).append("'");
        } else if (value instanceof Object[] arr) {
            formatIterable(Arrays.asList(arr));
        } else if (value instanceof Iterable<?> it) {
            formatIterable(it);
        } else {
            writer.append(value);
        }
    }

    /** Helper to format iterables directly to writer */
    private void formatIterable(Iterable<?> it) {
        writer.append("(");
        var first = true;
        for (var item : it) {
            if (!first) {
                writer.append(", ");
            }
            formatValue(item);
            first = false;
        }
        writer.append(")");
    }

    /** Write database column. */
    protected void writeColumn(@NotNull String tableAlias, @NotNull Key<?,?> column, Key<?,?>... labels) {
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

    /** Find a relation key */
    protected Key<?,?> findRelatedPrimaryKey(Key<?,?> foreignKey) {
        var acceptDefaultPk = Config.ofDefault().acceptDefaultPk(); // TODO
        return DomainHandlerProvider.getHandler(foreignKey.domainClass()).findPrimaryKey(acceptDefaultPk);
    }

    @Override
    public String toString() {
        return build(new StringBuilder(256)).toString();
    }

    /** Record representing a parsed JOIN relationship. */
    record JoinModel(
            /** Relation key property */
            Key<?, ?> relationKey,
            /** Source alias property */
            String sourceAlias,
            /** Target alias property */
            String targetAlias,
            /** Target table property */
            String targetTable,
            /** Is required property */
            boolean required
    ) {}

    /** Returns current DSL version */
    public static String getVersion() {
        return "1.0";
    }
}