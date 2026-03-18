/*
 *  Copyright 2024-2026 Pavel Ponec
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

package org.ujorm.tools.jdbc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing the SqlQuery class
 * @author Pavel Ponec
 */
public class SqlQueryTest extends AbstractJdbcConnector {

    private final String newLine = "\n";

    /** Some testing date */
    private final LocalDate someDate = LocalDate.parse("2018-09-12");

    @Test
    void testShowUsage() throws Exception {
        try (var dbConnection = createDbConnection())  {
            runSqlStatements(dbConnection);
        }
    }

    @Test
    void testRegexMatches() {
        var sqlMark = Pattern.compile(":(\\w+)");

        // Test pro :hello
        var matcher = sqlMark.matcher(":hello");
        matcher.find();
        assertEquals("hello", matcher.group(1));

        // Test pro :abc123
        matcher = sqlMark.matcher(":abc123");
        matcher.find();
        assertEquals("abc123", matcher.group(1));

        // Test pro :test_
        matcher = sqlMark.matcher(":test_");
        matcher.find();
        assertEquals("test_", matcher.group(1));

        // Test pro text bez shody
        matcher = sqlMark.matcher("hello");
        assertFalse(matcher.find());

        // Test pro text s dvojtečkou, ale bez \w+
        matcher = sqlMark.matcher(":");
        assertFalse(matcher.find());
    }

    @Test
    void regexpTest() {
        // Test pro :hello
        var matcher = SqlQuery.SQL_MARK.matcher(":hello");
        assertEquals("hello", matcher.find() ? matcher.group(1) : "");

        // Test pro :abc123
        matcher = SqlQuery.SQL_MARK.matcher(":abc123");
        assertEquals("abc123", matcher.find() ? matcher.group(1) : "");

        // Test pro :test_
        matcher = SqlQuery.SQL_MARK.matcher(":test_");
        assertEquals("test_", matcher.find() ? matcher.group(1) : "");

        // Test pro :test%
        matcher = SqlQuery.SQL_MARK.matcher(":test%");
        assertEquals("test", matcher.find() ? matcher.group(1) : "");

        // Test pro text bez shody
        matcher = SqlQuery.SQL_MARK.matcher("hello");
        assertEquals("", matcher.find() ? matcher.group(1) : "");

        // Test pro text s dvojtečkou, ale bez \w+
        matcher = SqlQuery.SQL_MARK.matcher(":");
        assertEquals("", matcher.find() ? matcher.group(1) : "");
    }

    /** Example of SQL statement INSERT. */
    void runSqlStatements(Connection dbConnection) throws SQLException {

        try (var builder = new SqlQuery(dbConnection)) {
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

            var id1 = builder.generatedLastKey(rs -> rs.getInt(1));
            Assertions.assertEquals(id1, 1);

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
            var id5 = builder.generatedLastKey(rs -> rs.getInt(1));
            Assertions.assertEquals(id5, 5);

            System.out.println("SELECT 1");
            var employees = builder.sql("SELECT t.id, t.name, t.created",
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

        newDemo(dbConnection);
    }

    private void newDemo(Connection dbConnection) {
        List<Employee> employees = SqlQuery.run(dbConnection, build -> build.sql("""
                SELECT t.id, t.name, t.created
                FROM employee t
                WHERE t.id > :id
                  AND t.code IN (:code)
                ORDER BY t.id
                """)
                .bind("id", 10)
                .bind("code", "T", "V")
                .streamMap(rs -> new Employee(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getObject("created", LocalDate.class)))
                .toList()
        );
    }

    private void runSqlStatementsLike(SqlQuery builder) {
        System.out.println("SELECT 3a");
        var employees = builder.sql("SELECT t.id, t.name, t.created",
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
    void loggingSql() throws SQLException {
        var dbConnection = Mockito.mock(Connection.class);
        try (var builder = new SqlQuery(dbConnection)) {

            System.out.println("MISSING PARAMS");
            builder.sql("SELECT t.id, t.name",
                    "FROM employee t",
                    "WHERE t.id > :id",
                    "  AND t.code = :code",
                    "ORDER BY t.id");
            Assertions.assertEquals(builder.sqlTemplate(), builder.toString());

            var ex = assertThrows(org.ujorm.tools.jdbc.SQLException.class, () -> {
                builder.streamMap(t -> t).count();
            });
            assertEquals("Missing SQL parameter: [code, id]", ex.getMessage());

            System.out.println("ASSIGNED PARAMS");
            builder.bind("id", 10);
            builder.bind("code", "w");
            var expected = String.join(newLine,
                    "SELECT t.id, t.name",
                    "FROM employee t",
                    "WHERE t.id > [10]",
                    "  AND t.code = [w]",
                    "ORDER BY t.id");
            assertEquals(expected, builder.toString());
        }
    }

    /** Test all data type binds and single-line string formatting */
    @Test
    void testAllBindMethodsAndToStringLine() {
        var dbConnection = Mockito.mock(Connection.class);
        try (var builder = new SqlQuery(dbConnection)) {
            builder.sql(
                    "SELECT :b1, :b2, :b3, :b4, :b5,",
                    ":b6, :b7, :b8, :b9, :b10, :b11"
            );

            builder.bind("b1", true);
            builder.bind("b2", (byte) 1);
            builder.bind("b3", (short) 2);
            builder.bind("b4", 3);
            builder.bind("b5", 4L);
            builder.bind("b6", BigDecimal.TEN);
            builder.bind("b7", "hello");
            builder.bind("b8", LocalDate.of(2023, 1, 1));
            builder.bind("b9", LocalDateTime.of(2023, 1, 1, 12, 0));
            builder.bindObject(true, "b10", JDBCType.OTHER, UUID.randomUUID());
            builder.bind(false, "b11", "disabled_param");

            var logLine = builder.toStringLine();

            assertTrue(logLine.contains("[true]"));
            assertTrue(logLine.contains("[1]"));
            assertTrue(logLine.contains("[2]"));
            assertTrue(logLine.contains("[3]"));
            assertTrue(logLine.contains("[4]"));
            assertTrue(logLine.contains("[10]"));
            assertTrue(logLine.contains("[hello]"));
            assertTrue(logLine.contains("[2023-01-01]"));
            assertTrue(logLine.contains("[2023-01-01T12:00]"));
            assertTrue(logLine.contains(":b11")); // Disabled params should remain unresolved

            // Check that multiline template was successfully squashed into a single line
            assertFalse(logLine.contains(newLine));
        }
    }

    /** Test handling of missing generated keys */
    @Test
    void testGeneratedKeysEmpty() throws SQLException {
        var dbConnection = Mockito.mock(Connection.class);
        var preparedStatement = Mockito.mock(PreparedStatement.class);

        Mockito.when(dbConnection.prepareStatement(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.getGeneratedKeys())
                .thenReturn(null);

        try (var builder = new SqlQuery(dbConnection)) {
            builder.sql("INSERT INTO test (id) VALUES (:id)").bind("id", 1);
            builder.prepareStatement(Statement.RETURN_GENERATED_KEYS);

            var stream = builder.generatedKeys(rs -> "dummy");
            assertEquals(0, stream.count());

            assertThrows(NoSuchElementException.class, () -> {
                builder.generatedLastKey(rs -> "dummy");
            });
        }
    }

    public record Employee (int id, String name, LocalDate created) {}

    /** Check that autoclosing works correctly also on NULL objects. */
    @Test
    void autoCloseTest() {
        try (SqlQuery builder = null) {
        }
    }

}