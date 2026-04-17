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
import static org.ujorm.core.composed.ComposedKeyImpl.*;

/**
 * A fluent wrapper over {@link java.sql.PreparedStatement}
 * that manages named parameters and ensures automatic resource cleanup
 * of both statements and result sets.
 *
 * @since 2.26
 */
public class SelectQueryBuilder implements AutoCloseable {

    /** New Line Character */
    private static final char NEW_LINE = '\n';

    /** Space Character */
    private static final char SPACE = ' ';

    /** Columns */
    private final List<Key<?, ?>> columns = new ArrayList<>();

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
    private final SelectQueryWriter selectWriter;

    @NotNull
    private Criterion criterion = Criterion.forAll();

    /** Constructor */
    public SelectQueryBuilder(@NotNull SelectQueryWriter selectWriter) {
        this.selectWriter = selectWriter;
    }

    /** Adds a description of a single column composed of sequentially linked components. */
    public void column(Key<?, ?> column) {
        columns.add(column);
    }

    /** Adds a SQL condition. Repeated calls append conditions using the AND operator. */
    public void where(@Nullable Criterion criterion) {
        this.criterion = this.criterion.and(Objects.requireNonNull(criterion, "criterion"));
    }

    /** Build the query */
    public StringBuilder build() {
        var result = selectWriter.append("");
        if (result.isEmpty()) {
            result.append("SELECT");
        }

        prepareBaseTableAndJoins();
        buildColumns();
        buildTable();
        buildJoins();
        buildWhere();

        return result;
    }

    /**
     * Prepares base table alias and resolves all JOINs from columns and criteria.
     */
    protected void prepareBaseTableAndJoins() {
        var entityClass = columns.isEmpty()
                ? extractTableClassFromCriterion(this.criterion)
                : columns.get(0).pathItem(0).domainClass();

        if (entityClass != null && baseTableAlias == null) {
            this.baseTableAlias = generateAlias(entityClass.getSimpleName());
            this.domainAliases.putIfAbsent(entityClass, this.baseTableAlias);
        }

        if (this.baseTableAlias != null) {
            for (var keyPath : columns) {
                resolveJoinsAndGetAlias(keyPath);
            }

            var criterionKeys = new ArrayList<Key<?, ?>>();
            collectKeysFromCriterion(this.criterion, criterionKeys);
            for (var keyPath : criterionKeys) {
                resolveJoinsAndGetAlias(keyPath);
            }
        }
    }

    /**
     * Processes the key path, generates necessary JOINs, and returns the final table alias.
     *
     * @param keyPath The key path
     * @return The table alias
     */
    @NotNull
    private String resolveJoinsAndGetAlias(@NotNull Key<?, ?> keyPath) {
        var currentAlias = this.baseTableAlias != null ? this.baseTableAlias : "";
        var keyNameCounts = new HashSet<String>();

        for (var i = 0; i < keyPath.pathSize() - 1; i++) {
            var relKey = keyPath.pathItem(i);
            var subPath = new ArrayList<Key<?, ?>>(i + 1);
            for (var j = 0; j <= i; j++) {
                subPath.add(keyPath.pathItem(j));
            }
            var relKeyName = relKey.name();

            if (!joinMap.containsKey(subPath)) {
                var targetClass = relKey.type();
                var isReq = relKey.info().required();
                var nextKey = keyPath.pathItem(i + 1);
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

        var finalKey = keyPath.pathItem(keyPath.pathSize() - 1);
        return finalKey.tableAlias().isEmpty()
                ? (keyPath.pathSize() == 1 ? domainAliases.getOrDefault(finalKey.domainClass(), currentAlias) : currentAlias)
                : finalKey.tableAlias();
    }

    /**
     * Recursively collects all keys from the criterion tree.
     *
     * @param crn  The criterion
     * @param keys The list of collected keys
     */
    private void collectKeysFromCriterion(Criterion crn, List<Key<?, ?>> keys) {
        if (crn instanceof ValueCriterion<?> valCrn) {
            keys.add(valCrn.getLeftNode());
        } else if (crn instanceof BinaryCriterion binCrn) {
            collectKeysFromCriterion(binCrn.getLeftNode(), keys);
            collectKeysFromCriterion(binCrn.getRightNode(), keys);
        }
    }

    /** Prepare model for JOINs and format SELECT columns */
    public void buildColumns() {
        if (columns.isEmpty()) {
            return;
        }

        for (var colIdx = 0; colIdx < columns.size(); colIdx++) {
            var keyPath = columns.get(colIdx);
            var finalAlias = resolveJoinsAndGetAlias(keyPath);

            if (colIdx > 0) {
                selectWriter.append(NEW_LINE).append(", ");
            } else {
                selectWriter.append(SPACE);
            }

            var finalKey = keyPath.pathItem(keyPath.pathSize() - 1);
            selectWriter.writeColumnName(finalAlias, finalKey, keyPath);
        }
    }

    /**
     * Build the FROM clause. Find a basic domain model using:
     * <br/> the first select column or
     * <br/> the first criterion column of
     */
    public void buildTable() {
        var entityClass = columns.isEmpty()
                ? extractTableClassFromCriterion(this.criterion)
                : columns.get(0).pathItem(0).domainClass();

        Objects.requireNonNull(entityClass, "Entity class could not be resolved.");

        selectWriter.append(NEW_LINE).append("FROM ");
        selectWriter.writeTableName(this.baseTableAlias, entityClass);
    }

    /** Inner/outer joins according to isRequired method */
    public void buildJoins() {
        for (var join : joins) {
            selectWriter.append(NEW_LINE).append(join.required() ? "JOIN " : "LEFT JOIN ");
            selectWriter.writeTableName(join.targetAlias(), join.targetClass());
            selectWriter.append(" ON ");
            selectWriter.writeColumnName(join.targetAlias(), findRelatedPrimaryKey(join.relationKey()), EMPTY_KEY);
            selectWriter.append(" = ");
            selectWriter.writeColumnName(join.sourceAlias(), join.relationKey(), EMPTY_KEY);
        }
    }

    /** Build the WHERE clause traversing the Criterion tree. */
    public void buildWhere() {
        if (this.criterion instanceof ValueCriterion<?> valCrn) {
            switch (valCrn.getOperator()) {
                case ALWAYS_TRUE: return;
                case ALWAYS_FALSE:
                    selectWriter.append(NEW_LINE).append("WHERE ")
                            .append(getSqlOperatorText(valCrn.getOperator()));
                    return;
            }
        }

        selectWriter.append(NEW_LINE).append("WHERE ");
        buildCriterionTree(this.criterion, true);
    }

    /** Recursive evaluation of the criterion tree */
    private void buildCriterionTree(Criterion crn, boolean isRoot) {
        if (crn instanceof ValueCriterion<?> valCrn) {
            var alias = findTableAlias(valCrn.getLeftNode());
            selectWriter.writeCondition(valCrn, alias);
        } else if (crn instanceof BinaryCriterion binCrn) {
            if (!isRoot) {
                selectWriter.append("(");
            }
            buildCriterionTree(binCrn.getLeftNode(), false);
            selectWriter.append(SPACE).append(getSqlOperatorText(binCrn.getOperator())).append(SPACE);
            buildCriterionTree(binCrn.getRightNode(), false);
            if (!isRoot) {
                selectWriter.append(")");
            }
        } else {
            throw new IllegalArgumentException("Unsupported criterion: " + crn);
        }
    }

    /** Resolve table alias from a Key */
    @NotNull
    public String findTableAlias(@NotNull Key<?, ?> key) {
        var result = resolveJoinsAndGetAlias(key);
        if (result.isEmpty()) {
            throw new IllegalStateException("No alias found for the key: " + key.fullName());
        }
        return result;
    }

    /** Extract base table class from criterion */
    @Nullable
    private Class<?> extractTableClassFromCriterion(Criterion crn) {
        if (crn instanceof BinaryCriterion binCrn) {
            var result = extractTableClassFromCriterion(binCrn.getLeftNode());
            return result != null ? result : extractTableClassFromCriterion(binCrn.getRightNode());
        } else if (crn instanceof ValueCriterion<?> valCrn) {
            return valCrn.getLeftNode().domainClass();
        }
        return null;
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
    protected Key<?, ?> findRelatedPrimaryKey(Key<?, ?> foreignKey) {
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
            /** Gets the relation key. */
            Key<?, ?> relationKey,
            /** Gets the source alias. */
            String sourceAlias,
            /** Gets the target alias. */
            String targetAlias,
            /** Gets the target class. */
            Class<?> targetClass,
            /** Gets the required flag. */
            boolean required
    ) {}

    /** Returns current DSL version */
    public static String getVersion() {
        return "1.0";
    }
}