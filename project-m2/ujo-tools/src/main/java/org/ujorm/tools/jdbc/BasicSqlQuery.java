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
package org.ujorm.tools.jdbc;

import org.jetbrains.annotations.NotNull;
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
public class BasicSqlQuery extends AbstractSqlQuery<BasicSqlQuery> {

    /**
     * Constructor with a database connection
     * @param dbConnection A database connection
     */
    public BasicSqlQuery(@NotNull Connection dbConnection) {
        super(dbConnection);
    }

    /** Run a builder statement */
    public static <R> R run(Connection connection, final SqlFunction<BasicSqlQuery, R> fun) {
        try (var builder = new BasicSqlQuery(connection)) {
            return fun.applyFunction(builder);
        } catch (Exception ex) {
            throw (ex instanceof RuntimeException re) ? re : new SqlException(ex);
        }
    }
}