/*
 *  Copyright 2024-2024 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ujorm.tools.sql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ujorm.tools.jdbc.AbstractJdbcConnector;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testing the SqlParamBuilder class
 * @author Pavel Ponec
 */
public class SqlParamBuilderTest extends AbstractJdbcConnector {

    private final String newLine = "\n";

    /** Some testing date */
    private final LocalDate someDate = LocalDate.parse("2018-09-12");

    @Test
    public void testShowUsage() throws Exception {
        try (Connection dbConnection = createDbConnection())  {
            runSqlStatements(dbConnection);
        }
    }

    @Test
    public void testRegexMatches() {
        Pattern SQL_MARK = Pattern.compile(":(\\w+)");

        // Test pro :hello
        Matcher matcher = SQL_MARK.matcher(":hello");
        matcher.find();
        assertEquals("hello", matcher.group(1));

        // Test pro :abc123
        matcher = SQL_MARK.matcher(":abc123");
        matcher.find();
        assertEquals("abc123", matcher.group(1));

        // Test pro :test_
        matcher = SQL_MARK.matcher(":test_");
        matcher.find();
        assertEquals("test_", matcher.group(1));

        // Test pro text bez shody
        matcher = SQL_MARK.matcher("hello");
        matcher.find();
        assertEquals(false, matcher.find());

        // Test pro text s dvojtečkou, ale bez \w+
        matcher = SQL_MARK.matcher(":");
        matcher.find();
        assertEquals(false, matcher.find());
    }

    @Test
    public void regexpTest() {
        // Test pro :hello
        Matcher matcher = SqlParamBuilder.SQL_MARK.matcher(":hello");
        assertEquals("hello", matcher.find() ? matcher.group(1) : "");

        // Test pro :abc123
        matcher = SqlParamBuilder.SQL_MARK.matcher(":abc123");
        assertEquals("abc123", matcher.find() ? matcher.group(1) : "");

        // Test pro :test_
        matcher = SqlParamBuilder.SQL_MARK.matcher(":test_");
        assertEquals("test_", matcher.find() ? matcher.group(1) : "");

        // Test pro :test%
        matcher = SqlParamBuilder.SQL_MARK.matcher(":test%");
        assertEquals("test", matcher.find() ? matcher.group(1) : "");

        // Test pro text bez shody
        matcher = SqlParamBuilder.SQL_MARK.matcher("hello");
        assertEquals("", matcher.find() ? matcher.group(1) : "");

        // Test pro text s dvojtečkou, ale bez \w+
        matcher = SqlParamBuilder.SQL_MARK.matcher(":");
        assertEquals("", matcher.find() ? matcher.group(1) : "");
    }

    /** Example of SQL statement INSERT. */
    public void runSqlStatements(Connection dbConnection) throws SQLException {

        try (SqlParamBuilder builder = new SqlParamBuilder(dbConnection)) {
            System.out.println("CREATE TABLE");
            builder.sql("CREATE TABLE employee",
                            "( id INTEGER PRIMARY KEY AUTO_INCREMENT",
                            ", name VARCHAR(256) DEFAULT 'test'",
                            ", code VARCHAR(1)",
                            ", created DATE NOT NULL",
                            ")")
                    .execute();

            System.out.println("SINGLE INSERT");
            builder.sql(String.join(newLine,
                            "INSERT INTO employee",
                            "( code, created ) VALUES",
                            "( :code, :created )"))
                    .bind("code", "T")
                    .bind("created", someDate)
                    .executeInsert();

            var id = builder.generatedKeys(rs -> rs.getInt(1)).findFirst();
            var id2 = builder.generatedKeys(rs -> rs.getInt(1)).findFirst();
            //Assertions.assertEquals(1, id.get());

            System.out.println("MULTI INSERT");
            builder.sql("INSERT INTO employee",
                            "(code,created) VALUES",
                            "(:code,:created),",
                            "(:code,:created)")
                    .bind("code", "M")
                    .bind("created", someDate.plusDays(1))
                    .executeInsert();
            System.out.println("Previous statement with modified parameter(s)");
            builder.bind("code", "X")
                    .execute();

            System.out.println("SELECT 1");
            List<Employee> employees = builder.sql("SELECT t.id, t.name, t.created",
                            "FROM employee t",
                            "WHERE t.id < :id",
                            "  AND t.code IN (:code)",
                            "ORDER BY t.id")
                    .bind("id", 10)
                    .bind("code", "T", "M")
                    .streamMap(rs -> new Employee(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getObject("created", LocalDate.class)))
                    .collect(Collectors.toList());
            employees.stream().forEach(e -> System.out.println("> " + e));
            Assertions.assertEquals(3, employees.size());
            Assertions.assertEquals(1, employees.get(0).id);
            Assertions.assertEquals("test", employees.get(0).name);
            Assertions.assertEquals(someDate, employees.get(0).created);

            System.out.println("SELECT 2 (reuse the previous SELECT)");
            List<Employee> employees2 = builder
                    .bind("id", 100)
                    .streamMap(rs -> new Employee(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getObject("created", LocalDate.class)))
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, employees2.size());

            System.out.println("SELECT 2 (forEach");
            builder.bind("id", 1000).forEach(rs -> {
                        var idValue = rs.getInt(1);
                        System.out.printf("\tid = %s%n", idValue);
                    });
            Assertions.assertEquals(3, employees2.size());
            runSqlStatementsLike(builder);
        }
    }

    private void runSqlStatementsLike(SqlParamBuilder builder) {
        System.out.println("SELECT 3a");
        List<Employee> employees = builder.sql("SELECT t.id, t.name, t.created",
                        "FROM employee t",
                        "WHERE t.id = :id",
                        "  AND t.name LIKE :name") // AND t.name LIKE :name%
                .bind("id", 1)
                .bind("name", "test")
                .streamMap(rs -> new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getObject("created", LocalDate.class)))
                .collect(Collectors.toList());
        employees.stream().forEach(e -> System.out.println("> " + e));
        Assertions.assertEquals(1, employees.size());

        System.out.println("SELECT 3b");
        employees = builder.sql("SELECT t.id, t.name, t.created",
                        "FROM employee t",
                        "WHERE t.id = :id",
                        "  AND t.name LIKE :name")
                .bind("id", 1)
                .bind("name", "t%")
                .streamMap(rs -> new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getObject("created", LocalDate.class)))
                .collect(Collectors.toList());
        employees.stream().forEach(e -> System.out.println("> " + e));
        Assertions.assertEquals(1, employees.size());
    }

    @Test
    public void loggingSql() throws SQLException {
        final Connection dbConnection = Mockito.mock(Connection.class);
        try (SqlParamBuilder builder = new SqlParamBuilder(dbConnection)) {

            System.out.println("MISSING PARAMS");
            builder.sql("SELECT t.id, t.name",
                    "FROM employee t",
                    "WHERE t.id > :id",
                    "  AND t.code = :code",
                    "ORDER BY t.id");
            Assertions.assertEquals(builder.sqlTemplate(), builder.toString());

            IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                builder.streamMap(t -> t).count();
            });
            assertEquals("Missing SQL parameter: [code, id]", ex.getMessage());

            System.out.println("ASSIGNED PARAMS");
            builder.bind("id", 10);
            builder.bind("code", "w");
            String expected = String.join(newLine,
                    "SELECT t.id, t.name",
                    "FROM employee t",
                    "WHERE t.id > [10]",
                    "  AND t.code = [w]",
                    "ORDER BY t.id");
            assertEquals(expected, builder.toString());
        }
    }

    public record Employee (int id, String name, LocalDate created) {};

    /** Check that autoclosing works correctly also on NULL objects. */
    @Test
    public void autoCloseTest() {
        try (SqlParamBuilder builder = null) {
        }
    }

}
