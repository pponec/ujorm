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
package org.ujorm.orm.dsl;

import org.ujorm.core.Key;
import org.ujorm.core.criterion.Criterion;
import org.ujorm.core.criterion.ValueCriterion;
import org.ujorm.tools.jdbc.SQLException;

import java.util.ArrayList;
import java.util.List;

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
public class DslBuilder {

    /** Columns */
    private final List<Key<?,?>[]> columns = new ArrayList<>();


    private final Criterion criterion;

    /** Writer */
    private StringBuilder writer = new StringBuilder(256);


    public DslBuilder(Criterion criterion) {
        this.criterion = criterion;
    }

    public void column(Key<?,?>[] column) {
        columns.add(column);
    }

    boolean isRequired(Key<?,?> column) {
        return true; // todo
    }

    /** Get Database column name */
    String databaseColumnName(Key<?,?> column, String defaultTableAlias) {
        var tableAlias =  (column instanceof AliasedKey akey)
                ? akey.tableAlias()
                : defaultTableAlias;
        return tableAlias + "." + column.name();
    }

    public void build() {
        writer.append("SELECT ");
        buildColumns();
        buildTable();
        buildJoins();
    }

    public void buildColumns() {
        writer.append("SELECT ");
    }

    public void buildTable() {
        var firstKey = columns.get(0)[0];
        var tableName = firstKey.domainClass().getSimpleName();
        writer.append(" FROM ").append(tableName).append(" ");
    }

    /**  Inner/outer joins according to {@link #isRequired } method  */
    public void buildJoins() {

    }

    /** TODO */
    public void buildWhere() {
        if (this.criterion instanceof ValueCriterion<?> valCrn) {
            // TODO ....
        } else {
            // TODO ...
        }
    }

}