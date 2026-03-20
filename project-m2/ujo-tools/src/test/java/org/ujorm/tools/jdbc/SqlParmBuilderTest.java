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
import java.sql.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing the BasicSqlQuery class
 * @author Pavel Ponec
 */
public class SqlParmBuilderTest extends AbstractJdbcConnector {

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
        var matcher = SqlParmBuilder.SQL_MARK.matcher(":hello");
        assertEquals("hello", matcher.find() ? matcher.group(1) : "");

        // Test pro :abc123
        matcher = SqlParmBuilder.SQL_MARK.matcher(":abc123");
        assertEquals("abc123", matcher.find() ? matcher.group(1) : "");

        // Test pro :test_
        matcher = SqlParmBuilder.SQL_MARK.matcher(":test_");
        assertEquals("test_", matcher.find() ? matcher.group(1) : "");

        // Test pro :test%
        matcher = SqlParmBuilder.SQL_MARK.matcher(":test%");
        assertEquals("test", matcher.find() ? matcher.group(1) : "");

        // Test pro text bez shody
        matcher = SqlParmBuilder.SQL_MARK.matcher("hello");
        assertEquals("", matcher.find() ? matcher.group(1) : "");

        // Test pro text s dvojtečkou, ale bez \w+
        matcher = SqlParmBuilder.SQL_MARK.matcher(":");
        assertEquals("", matcher.find() ? matcher.group(1) : "");
    }

    /** Example of SQL statement INSERT. */
    void runSqlStatements(Connection dbConnection) throws SQLException {

        try (var query = new SqlParmBuilder(dbConnection)) {
            System.out.println("CREATE TABLE");
            query.sql("CREATE TABLE employee",
                            "( id INTEGER PRIMARY KEY AUTO_INCREMENT",
                            ", name VARCHAR(256) DEFAULT 'test'",
                            ", code VARCHAR(1)",
                            ", created DATE NOT NULL",
                            ")")
                    .execute();

            System.out.println("SINGLE INSERT");
            query.sql(String.join(newLine,
                            "INSERT INTO employee",
                            "( code, created ) VALUES",
                            "( :code, :created )"))
                    .bind("code", "T")
                    .bind("created", someDate)
                    .executeInsert();

            var id1 = query.generatedLastKey(rs -> rs.getInt(1));
            Assertions.assertEquals(id1, 1);

            System.out.println("MULTI INSERT");
            query.sql("INSERT INTO employee",
                            "(code,created) VALUES",
                            "(:code,:created),",
                            "(:code,:created)")
                    .bind("code", "M")
                    .bind("created", someDate.plusDays(1))
                    .executeInsert();
            System.out.println("Previous statement with modified parameter(s)");
            query.bind("code", "X")
                    .execute();
            var id5 = query.generatedLastKey(rs -> rs.getInt(1));
            Assertions.assertEquals(id5, 5);

            System.out.println("SELECT 1");
            var employees = query.sql("SELECT t.id, t.name, t.created",
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
            List<Employee> employees2 = query
                    .bind("id", 100)
                    .streamMap(rs -> new Employee(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getObject("created", LocalDate.class)))
                    .collect(Collectors.toList());
            Assertions.assertEquals(3, employees2.size());

            System.out.println("SELECT 2 (forEach");
            query.bind("id", 1000).forEach(rs -> {
                var idValue = rs.getInt(1);
                System.out.printf("\tid = %s%n", idValue);
            });
            Assertions.assertEquals(3, employees2.size());
            runSqlStatementsLike(query);
        }

        newDemo(dbConnection);
    }

    private void newDemo(Connection dbConnection) {
        List<Employee> employees = SqlParmBuilder.run(dbConnection, build -> build.sql("""
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

    private void runSqlStatementsLike(SqlParmBuilder query) {
        System.out.println("SELECT 3a");
        var employees = query.sql("SELECT t.id, t.name, t.created",
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
        employees = query.sql("SELECT t.id, t.name, t.created",
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
        try (var query = new SqlParmBuilder(dbConnection)) {

            System.out.println("MISSING PARAMS");
            query.sql("SELECT t.id, t.name",
                    "FROM employee t",
                    "WHERE t.id > :id",
                    "  AND t.code = :code",
                    "ORDER BY t.id");
            Assertions.assertEquals(query.sqlTemplate(), query.toString());

            var ex = assertThrows(org.ujorm.tools.jdbc.SQLException.class, () -> {
                query.streamMap(t -> t).count();
            });
            assertEquals("Missing SQL parameter: [code, id]", ex.getMessage());

            System.out.println("ASSIGNED PARAMS");
            query.bind("id", 10);
            query.bind("code", "w");
            var expected = String.join(newLine,
                    "SELECT t.id, t.name",
                    "FROM employee t",
                    "WHERE t.id > [10]",
                    "  AND t.code = [w]",
                    "ORDER BY t.id");
            assertEquals(expected, query.toString());
        }
    }

    /** Test all data type binds and single-line string formatting */
    @Test
    void testAllBindMethodsAndToStringLine() {
        var dbConnection = Mockito.mock(Connection.class);
        try (var query = new SqlParmBuilder(dbConnection)) {
            query.sql(
                    "SELECT :b1, :b2, :b3, :b4, :b5,",
                    ":b6, :b7, :b8, :b9, :b10, :b11"
            );

            query.bind("b1", true);
            query.bind("b2", (byte) 1);
            query.bind("b3", (short) 2);
            query.bind("b4", 3);
            query.bind("b5", 4L);
            query.bind("b6", BigDecimal.TEN);
            query.bind("b7", "hello");
            query.bind("b8", LocalDate.of(2023, 1, 1));
            query.bind("b9", LocalDateTime.of(2023, 1, 1, 12, 0));
            query.bindObject(true, "b10", JDBCType.OTHER, UUID.randomUUID());
            query.bind(false, "b11", "disabled_param");

            var logLine = query.toStringLine();

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

        try (var query = new SqlParmBuilder(dbConnection)) {
            query.sql("INSERT INTO test (id) VALUES (:id)").bind("id", 1);
            query.prepareStatement(Statement.RETURN_GENERATED_KEYS);

            var stream = query.generatedKeys(rs -> "dummy");
            assertEquals(0, stream.count());

            assertThrows(NoSuchElementException.class, () -> {
                query.generatedLastKey(rs -> "dummy");
            });
        }
    }

    public record Employee (int id, String name, LocalDate created) {}

    /** Check that autoclosing works correctly also on NULL objects. */
    @Test
    void autoCloseTest() {
        try (SqlParmBuilder query = null) {
        }
    }

    /** Test the static run method */
    @Test
    void testStaticRun() throws Exception {
        try (var dbConnection = createDbConnection()) {
            var result = SqlParmBuilder.run(dbConnection, query -> query
                    .sql("SELECT 1")
                    .streamMap(rs -> rs.getInt(1))
                    .findFirst()
                    .orElse(0));
            assertEquals(1, result);
        }
    }

    /** Test setting the fetch size on the PreparedStatement */
    @Test
    void testFetchSize() throws SQLException {
        var dbConnection = Mockito.mock(Connection.class);
        var preparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(dbConnection.prepareStatement(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(preparedStatement);

        try (var query = new SqlParmBuilder(dbConnection)) {
            query.sql("SELECT * FROM employee").fetchSize(100).prepareStatement(Statement.NO_GENERATED_KEYS);
            Mockito.verify(preparedStatement).setFetchSize(100);
        }
    }

    /** Test binding null values */
    @Test
    void testBindNull() {
        var dbConnection = Mockito.mock(Connection.class);
        try (var query = new SqlParmBuilder(dbConnection)) {
            query.sql("INSERT INTO table (val) VALUES (:val)")
                    .bind("val", (String) null);

            assertTrue(query.toString().contains("[null]"));
        }
    }

    /** Test that stream operations properly close the underlying ResultSet */
    @Test
    void testStreamClosesResultSet() throws SQLException {
        var dbConnection = Mockito.mock(Connection.class);
        var preparedStatement = Mockito.mock(PreparedStatement.class);
        var resultSet = Mockito.mock(ResultSet.class);

        Mockito.when(dbConnection.prepareStatement(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.next())
                .thenReturn(true, false);
        Mockito.when(resultSet.getString(1))
                .thenReturn("test_value");

        try (var query = new SqlParmBuilder(dbConnection)) {
            var stream = query.sql("SELECT name FROM employee")
                    .streamMap(rs -> rs.getString(1));

            // Short-circuit operation
            var result = stream.findFirst().orElse(null);
            Assertions.assertEquals("test_value", result);

            // Closing the stream should trigger switchResultSet(null)
            stream.close();
        }

        // Verify that the ResultSet was actually closed
        Mockito.verify(resultSet, Mockito.atLeastOnce()).close();
    }

    /** Test that JDBC SQLExceptions are properly wrapped in the custom unchecked SqlException */
    @Test
    void testSqlExceptionWrapping() throws SQLException {
        var dbConnection = Mockito.mock(Connection.class);
        var preparedStatement = Mockito.mock(PreparedStatement.class);

        Mockito.when(dbConnection.prepareStatement(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenThrow(new SQLException("Simulated database connection error"));

        try (var query = new SqlParmBuilder(dbConnection)) {
            var ex = assertThrows(SqlParmBuilder.SqlException.class, () -> {
                query.sql("SELECT * FROM non_existing_table").forEach(rs -> {});
            });

            Assertions.assertTrue(ex.getCause() instanceof SQLException);
            Assertions.assertEquals("Simulated database connection error", ex.getCause().getMessage());
        }
    }

    /** Test that setting a new SQL template clears previous parameters and resources */
    @Test
    void testSqlMethodResetsState() throws SQLException {
        var dbConnection = Mockito.mock(Connection.class);
        var preparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(dbConnection.prepareStatement(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(preparedStatement);

        try (var query = new SqlParmBuilder(dbConnection)) {
            // First query setup
            query.sql("SELECT * FROM employee WHERE id = :id")
                    .bind("id", 1)
                    .prepareStatement(Statement.NO_GENERATED_KEYS);

            var sql1 = query.toString();
            Assertions.assertTrue(sql1.contains("[1]"));

            // Setting new SQL should clear bindings and close old statement
            query.sql("SELECT * FROM employee WHERE code = :code");

            // Calling prepareStatement requires all bound parameters. It will trigger validation.
            var ex = assertThrows(SqlParmBuilder.SqlException.class, () -> {
                query.prepareStatement(Statement.NO_GENERATED_KEYS);
            });
            Assertions.assertTrue(ex.getMessage().contains("Missing SQL parameter: [code]"));
        }
    }

    /** Test SQL execution logging functionality */
    @Test
    void testExecutionLogging() throws SQLException {
        var dbConnection = Mockito.mock(Connection.class);
        var preparedStatement = Mockito.mock(PreparedStatement.class);
        Mockito.when(dbConnection.prepareStatement(Mockito.anyString(), Mockito.anyInt()))
                .thenReturn(preparedStatement);

        var logRecords = new ArrayList<LogRecord>();
        var handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                logRecords.add(record);
            }
            @Override
            public void flush() {}
            @Override
            public void close() {}
        };

        var logger = Logger.getLogger(AbstractSqlQuery.class.getName());
        logger.addHandler(handler);
        var originalLevel = logger.getLevel();
        logger.setLevel(Level.INFO);

        try {
            // 1. Test without logging
            try (var query = new SqlParmBuilder(dbConnection)) {
                query.sql("SELECT * FROM employee WHERE id = :id")
                        .bind("id", 1)
                        .prepareStatement(Statement.NO_GENERATED_KEYS);
            }
            Assertions.assertEquals(0, logRecords.size());

            // 2. Test logging without parameters
            try (var query = new SqlParmBuilder(dbConnection)) {
                query.sql("SELECT * FROM employee WHERE id = :id")
                        .bind("id", 2)
                        .log(Level.INFO, false)
                        .prepareStatement(Statement.NO_GENERATED_KEYS);
            }
            Assertions.assertEquals(1, logRecords.size());
            Assertions.assertTrue(logRecords.get(0).getMessage().contains("?"));
            Assertions.assertFalse(logRecords.get(0).getMessage().contains("[2]"));
            logRecords.clear();

            // 3. Test logging with parameters (single line check)
            try (var query = new SqlParmBuilder(dbConnection)) {
                query.sql("SELECT * FROM employee", "WHERE id = :id")
                        .bind("id", 3)
                        .log(Level.INFO, true)
                        .prepareStatement(Statement.NO_GENERATED_KEYS);
            }
            Assertions.assertEquals(1, logRecords.size());
            Assertions.assertTrue(logRecords.get(0).getMessage().contains("[3]"));
            Assertions.assertFalse(logRecords.get(0).getMessage().contains(newLine));
            logRecords.clear();

            // 4. Test turning off logging explicitly
            try (var query = new SqlParmBuilder(dbConnection)) {
                query.sql("SELECT * FROM employee WHERE id = :id")
                        .bind("id", 4)
                        .log(Level.INFO, true)
                        .log(null, true) // Disabled
                        .prepareStatement(Statement.NO_GENERATED_KEYS);
            }
            Assertions.assertEquals(0, logRecords.size());

        } finally {
            logger.removeHandler(handler);
            logger.setLevel(originalLevel);
        }
    }
}