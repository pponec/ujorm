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
import org.ujorm.core.criterion.BinaryCriterion;
import org.ujorm.core.criterion.Criterion;
import org.ujorm.core.criterion.FunctionCriterion;
import org.ujorm.core.criterion.ValueCriterion;
import org.ujorm.tools.jdbc.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A fluent wrapper over {@link java.sql.PreparedStatement}
 * that manages named parameters and ensures automatic resource cleanup
 * of both statements and result sets.
 * This class has no dependencies other than its abstract parent, annotations, and {@link SQLException}.
 *
 * @since 2.26
 */
public class DslBuilder {

    /** New Line Character */
    private static final char NEW_LINE = '\n';
    /** Space Character */
    private static final char SPACE = ' ';

    /** Columns */
    private final List<Key<?, ?>[]> columns = new ArrayList<>();

    /** Writer */
    private final StringBuilder writer = new StringBuilder(256);

    /** Used table aliases */
    private final Set<String> usedAliases = new HashSet<>();

    /** Mapped relations to avoid duplicate JOIN clauses */
    private final Map<List<Key<?, ?>>, JoinModel> joinMap = new HashMap<>();

    /** Ordered list of JOIN clauses */
    private final List<JoinModel> joins = new ArrayList<>();

    /** Default aliases for domain classes */
    private final Map<Class<?>, String> domainAliases = new HashMap<>();

    /** Extracted base table alias */
    private String baseTableAlias;

    @NotNull
    private Criterion criterion = Criterion.forAll();

    /** Add new column */
    public void column(Key<?, ?>... column) {
        columns.add(column);
    }

    /** Add new column with a mandatory first key */
    public void columnChain(Key<?, ?> firstColumn, Key<?, ?>... column) {
        var mergedColumn = new Key[1 + column.length];
        mergedColumn[0] = firstColumn;
        System.arraycopy(column, 0, mergedColumn, 1, column.length);
        column(mergedColumn);
    }

    /** Set criterion */
    public void setCriterion(@Nullable Criterion criterion) {
        this.criterion = criterion != null ? criterion : Criterion.forAll();
    }

    /** Get Database column name */
    String databaseColumnName(Key<?, ?> column, String defaultTableAlias) {
        var resolvedAlias = column instanceof AliasedKey akey
                ? akey.tableAlias()
                : domainAliases.getOrDefault(column.domainClass(), defaultTableAlias);
        return resolvedAlias + "." + column.name();
    }

    /** Build the query */
    public void build() {
        if (columns.isEmpty()) {
            return;
        }
        if (writer.isEmpty()) {
            writer.append("SELECT ");
        }
        buildColumns();
        buildTable();
        buildJoins();
        buildWhere();
    }

    /** Prepare model for JOINs and format SELECT columns */
    public void buildColumns() {
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
                    var targetAlias = keyNameCounts.contains(relKeyName)
                            ? generateNumberedAlias(relKeyName)
                            : generateAlias(relKeyName);

                    var joinInfo = new JoinModel(relKey, currentAlias, targetAlias, targetTable, isReq);
                    joinMap.put(subPath, joinInfo);
                    joins.add(joinInfo);
                    domainAliases.putIfAbsent(relKey.type(), targetAlias);
                }

                keyNameCounts.add(relKeyName);
                currentAlias = joinMap.get(subPath).targetAlias();
            }

            if (colIdx > 0) {
                writer.append(", ");
            }

            var finalKey = keyPath[keyPath.length - 1];
            var finalAlias = finalKey instanceof AliasedKey akey
                    ? akey.tableAlias()
                    : currentAlias;
            writer.append(finalAlias).append(".").append(finalKey.name());
        }
    }

    /** Build the FROM clause */
    public void buildTable() {
        var firstKey = columns.get(0)[0];
        var tableName = firstKey.domainClass().getSimpleName();
        writer.append(NEW_LINE).append("FROM ").append(tableName).append(SPACE).append(baseTableAlias);
    }

    /** Inner/outer joins according to isRequired method */
    public void buildJoins() {
        for (var join : joins) {
            writer.append(NEW_LINE).append(join.required() ? "INNER JOIN " : "LEFT OUTER JOIN ");
            writer.append(join.targetTable()).append(SPACE).append(join.targetAlias());
            writer.append(" ON ").append(join.targetAlias()).append(".id");
            writer.append(" = ").append(join.sourceAlias()).append(".").append(join.relationKey().name());
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
        buildCriterionTree(this.criterion);
    }

    /** Recursive evaluation of the criterion tree */
    private void buildCriterionTree(Criterion crn) {
        if (crn.isBinary() && crn instanceof BinaryCriterion binCrn) {
            writer.append("(");
            buildCriterionTree(binCrn.getLeftNode());
            writer.append(SPACE).append(binCrn.getOperator().name()).append(SPACE);
            buildCriterionTree(binCrn.getRightNode());
            writer.append(")");

        } else if (crn instanceof FunctionCriterion<?, ?> funCrn) {
            var key = (Key<?, ?>) funCrn.getLeftNode();
            writer.append(databaseColumnName(key, baseTableAlias))
                    .append(SPACE).append(funCrn.getOperator().name())
                    .append(SPACE).append(formatValue(funCrn.getRightNode()));

        } else if (crn instanceof ValueCriterion<?> valCrn) {
            var key = (Key<?, ?>) valCrn.getLeftNode();
            writer.append(databaseColumnName(key, baseTableAlias))
                    .append(SPACE).append(valCrn.getOperator().name())
                    .append(SPACE).append(formatValue(valCrn.getRightNode()));
        }
    }

    /** Generate unique table alias from free characters */
    private String generateAlias(String name) {
        var lower = name.toLowerCase();

        for (var i = 0; i < lower.length(); i++) {
            var candidate = String.valueOf(lower.charAt(i));
            if (!usedAliases.contains(candidate)) {
                usedAliases.add(candidate);
                return candidate;
            }
        }
        return generateNumberedAlias(name);
    }

    /** Generate sequentially numbered alias */
    private String generateNumberedAlias(String name) {
        var firstChar = name.toLowerCase().charAt(0);
        var counter = 1;
        while (true) {
            var candidate = firstChar + String.valueOf(counter);
            if (!usedAliases.contains(candidate)) {
                usedAliases.add(candidate);
                return candidate;
            }
            counter++;
        }
    }

    /** Format scalar value */
    private String formatValue(Object value) {
        return value == null
                ? "NULL"
                : (value instanceof String)
                ? "'" + value + "'"
                : String.valueOf(value);
    }

    @Override
    public String toString() {
        return writer.toString();
    }

    /** Record representing a parsed JOIN relationship. */
    record JoinModel(
            /** Relation key */
            Key<?, ?> relationKey,
            /** Source alias */
            String sourceAlias,
            /** Target alias */
            String targetAlias,
            /** Target table */
            String targetTable,
            /** Is required */
            boolean required
    ) {}

    /** Returns current DSL version */
    public static String getVersion() {
        return "1.0";
    }
}