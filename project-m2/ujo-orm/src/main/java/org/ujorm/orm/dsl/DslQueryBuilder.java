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
import org.ujorm.core.criterion.AbstractOperator;
import org.ujorm.core.criterion.BinaryCriterion;
import org.ujorm.core.criterion.Criterion;
import org.ujorm.core.criterion.ValueCriterion;
import java.util.*;

/**
 * A fluent wrapper over {@link java.sql.PreparedStatement}
 * that manages named parameters and ensures automatic resource cleanup
 * of both statements and result sets.
 *
 * @since 2.26
 */
public class DslQueryBuilder implements AutoCloseable {

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

    /** Extracted base table alias */
    private String baseTableAlias;

    /** DSL Writer */
    private final DslQueryWriter dslWriter;

    @NotNull
    private Criterion criterion = Criterion.forAll();

    /** Constructor */
    public DslQueryBuilder(@NotNull DslQueryWriter dslWriter) {
        this.dslWriter = dslWriter;
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
    public StringBuilder build() {
        var result = dslWriter.append("");
        if (result.isEmpty()) {
            result.append("SELECT");
        }
        buildColumns();
        buildTable();
        buildJoins();
        buildWhere();

        return result;
    }

    /** Prepare model for JOINs and format SELECT columns */
    public void buildColumns() {
        if (columns.isEmpty()) {
            return;
        }

        var firstKey = columns.get(0)[0];
        var baseClass = firstKey.domainClass();
        this.baseTableAlias = generateAlias(baseClass.getSimpleName());
        this.domainAliases.putIfAbsent(baseClass, this.baseTableAlias);

        for (var colIdx = 0; colIdx < columns.size(); colIdx++) {
            var keyPath = columns.get(colIdx);
            var currentAlias = this.baseTableAlias;
            var keyNameCounts = new HashSet<String>();

            for (var i = 0; i < keyPath.length - 1; i++) {
                var relKey = keyPath[i];
                var subPath = Arrays.asList(keyPath).subList(0, i + 1);
                var relKeyName = relKey.name();

                if (!joinMap.containsKey(subPath)) {
                    var targetClass = relKey.type();
                    var isReq = relKey.info().required();
                    var nextKey = keyPath[i + 1];
                    String targetAlias;

                    if (!nextKey.tableAlias().isEmpty()) {
                        targetAlias = nextKey.tableAlias();
                        usedAliases.add(targetAlias);
                    } else if (!relKey.tableAlias().isEmpty()) {
                        targetAlias = relKey.tableAlias();
                        usedAliases.add(targetAlias);
                    } else {
                        targetAlias = keyNameCounts.contains(relKeyName)
                                ? generateNumberedAlias(relKeyName)
                                : generateAlias(relKeyName);
                    }

                    var joinInfo = new JoinModel(relKey, currentAlias, targetAlias, targetClass, isReq);
                    joinMap.put(subPath, joinInfo);
                    joins.add(joinInfo);
                    domainAliases.putIfAbsent(targetClass, targetAlias);
                }

                keyNameCounts.add(relKeyName);
                currentAlias = joinMap.get(subPath).targetAlias();
            }

            // Centralized logic for column prefixes:
            if (colIdx > 0) {
                dslWriter.append(NEW_LINE).append(", ");
            } else {
                dslWriter.append(SPACE);
            }

            var finalKey = keyPath[keyPath.length - 1];
            var finalAlias = finalKey.tableAlias().isEmpty()
                    ? currentAlias
                    : finalKey.tableAlias();

            dslWriter.writeColumnName(finalAlias, finalKey, keyPath);
        }
    }

    /**
     * Build the FROM clause. Find a basic domain model using:
     * <br/> the first select column or
     * <br/> the first criterion column of
     */
    public void buildTable() {
        var entityClass = columns.isEmpty()
                ? extractTableClassFromCriterion(this.criterion)    // The first criterion column
                : columns.get(0)[0].domainClass();                  // The first select column

        Objects.requireNonNull(entityClass, "Entity class could not be resolved.");

        if (baseTableAlias == null) {
            baseTableAlias = generateAlias(entityClass.getSimpleName());
        }

        dslWriter.append(NEW_LINE).append("FROM ");
        dslWriter.writeTableName(baseTableAlias, entityClass);
    }

    /** Inner/outer joins according to isRequired method */
    public void buildJoins() {
        for (var join : joins) {
            dslWriter.append(NEW_LINE).append(join.required() ? "JOIN " : "LEFT JOIN ");
            dslWriter.writeTableName(join.targetAlias(), join.targetClass());
            dslWriter.append(" ON ");
            dslWriter.writeColumnName(join.targetAlias(), findRelatedPrimaryKey(join.relationKey()));
            dslWriter.append(" = ");
            dslWriter.writeColumnName(join.sourceAlias(), join.relationKey());
        }
    }

    /** Build the WHERE clause traversing the Criterion tree. */
    public void buildWhere() {
        if (this.criterion instanceof ValueCriterion<?> valCrn) {
            switch (valCrn.getOperator()) {
                case ALWAYS_TRUE -> { return; }
                case ALWAYS_FALSE -> {
                    dslWriter.append(NEW_LINE).append("WHERE ")
                            .append(getSqlOperatorText(valCrn.getOperator()));
                    return;
                }
                default -> {}
            }
        }

        dslWriter.append(NEW_LINE).append("WHERE ");
        buildCriterionTree(this.criterion, true);
    }

    /** Recursive evaluation of the criterion tree */
    private void buildCriterionTree(Criterion crn, boolean isRoot) {
        if (crn instanceof ValueCriterion<?> valCrn) {
            var alias = findTableAlias(valCrn.getLeftNode());
            dslWriter.writeCondition(valCrn, alias);
        } else if (crn instanceof BinaryCriterion binCrn) {
            if (!isRoot) {
                dslWriter.append("(");
            }
            buildCriterionTree(binCrn.getLeftNode(), false);
            dslWriter.append(SPACE).append(getSqlOperatorText(binCrn.getOperator())).append(SPACE);
            buildCriterionTree(binCrn.getRightNode(), false);
            if (!isRoot) {
                dslWriter.append(")");
            }
        } else {
            throw new IllegalArgumentException("Unsupported criterion: " + crn);
        }
    }

    /** Resolve table alias from a Key */
    @NotNull
    public String findTableAlias(@NotNull Key<?, ?> key) {
        var result = key.tableAlias().isEmpty()
                ? domainAliases.getOrDefault(key.domainClass(), baseTableAlias)
                : key.tableAlias();
        if (result == null) {
            throw new IllegalStateException("No alias found for the key: " + key.fullName());
        }
        return result;
    }

    /** Extract base table class from criterion */
    @Nullable
    private Class<?> extractTableClassFromCriterion(Criterion crn) {
        Class<?> result = null;
        if (crn instanceof BinaryCriterion binCrn) {
            result = extractTableClassFromCriterion(binCrn.getLeftNode());
            if (result == null) {
                result = extractTableClassFromCriterion(binCrn.getRightNode());
            }
        } else if (crn instanceof ValueCriterion<?> valCrn) {
            result = valCrn.getLeftNode().domainClass();
        }
        return result;
    }

    /** Get SQL operator text. */
    private String getSqlOperatorText(Enum<?> operator) {
        return operator instanceof AbstractOperator op ? op.term() : operator.name();
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

    /** Find a relation key */
    protected Key<?,?> findRelatedPrimaryKey(Key<?,?> foreignKey) {
        var acceptDefaultPk = true;
        return DomainHandlerProvider.getHandler(foreignKey.domainClass()).findPrimaryKey(acceptDefaultPk);
    }

    /** Close the inner states */
    @Override
    public void close() {
        columns.clear();
        usedAliases.clear();
        joinMap.clear();
        joins.clear();
        domainAliases.clear();
        criterion = Criterion.forAll();
        baseTableAlias = null;
    }

    @Override
    public String toString() {
        return build().toString();
    }

    /** Record representing a parsed JOIN relationship. */
    record JoinModel(
            /** Returns relation key */
            Key<?, ?> relationKey,
            /** Returns source alias */
            String sourceAlias,
            /** Returns target alias */
            String targetAlias,
            /** Returns target class */
            Class<?> targetClass,
            /** Returns true if required */
            boolean required
    ) {}

    /** Returns current DSL version */
    public static String getVersion() {
        return "1.0";
    }
}