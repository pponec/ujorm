package org.ujorm.tools.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ujorm.Key;

import java.sql.Connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SqlQueryTest {

    private Connection connectionMock;

    @BeforeEach
    void setUp() {
        // A real DB connection is not needed, a mock is sufficient to pass the constructor
        connectionMock = mock(Connection.class);
    }

    @Test
    void testSingleLabel() {
        try (var query = new SqlQuery(connectionMock)) {
            query.sql("""
                    SELECT a AS ${col1} FROM table""");
            query.label("col1", mockKey("first_name"));

            assertEquals("""
                    SELECT a AS "first_name" FROM table""", query.toStringLine());
        }
    }

    @Test
    void testTwoLabels() {
        try (var query = new SqlQuery(connectionMock)) {
            query.sql("""
                    SELECT a AS ${col1}, b AS ${col2} FROM table""");
            query.label("col1", mockKey("user"), mockKey("name"))
                    .label("col2", mockKey("user"), mockKey("age"));

            assertEquals("""
                    SELECT a AS "user.name", b AS "user.age" FROM table""", query.toStringLine());
        }
    }

    @Test
    void testMultipleLabelsVarargs() {
        try (var query = new SqlQuery(connectionMock)) {
            query.sql("""
                    SELECT c AS ${complex_col} FROM table""");
            query.label("complex_col",
                    mockKey("db"), mockKey("schema"), mockKey("table"),
                    mockKey("user"), mockKey("address"), mockKey("zip"));

            assertEquals("""
                    SELECT c AS "db.schema.table.user.address.zip" FROM table""", query.toStringLine());
        }
    }

    @Test
    void testLabelsWithBoundParameters() {
        try (var query = new SqlQuery(connectionMock)) {
            // Combination of labels and standard parameters
            // We can now comfortably format the SQL on multiple lines
            query.sql("""
                    SELECT a AS ${lbl}
                    FROM table
                    WHERE id = :id AND status = :status""");
            query.label("lbl", mockKey("employee"), mockKey("id"))
                    .bind("id", 42)
                    .bind("status", "ACTIVE");

            // The toStringLine() method replaces \n with spaces, matching the expected one-liner below
            assertEquals("""
                    SELECT a AS "employee.id" FROM table WHERE id = [42] AND status = [ACTIVE]""", query.toStringLine());
        }
    }

    /**
     * A helper method for quick creation of a mock Key object.
     * Raw types are used to bypass strict generic type checks during varargs mocking.
     *
     * @param name The string representation of the key
     * @return A mocked Key instance
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Key mockKey(String name) {
        Key key = mock(Key.class);

        // Standard toString() for the + operator or String.join
        when(key.toString()).thenReturn(name);

        // Mocking CharSequence methods for StringBuilder appending
        when(key.length()).thenReturn(name.length());
        when(key.charAt(anyInt())).thenAnswer(inv -> name.charAt(inv.<Integer>getArgument(0)));
        when(key.subSequence(anyInt(), anyInt())).thenAnswer(inv ->
                name.subSequence(inv.getArgument(0), inv.getArgument(1)));

        return key;
    }

    @Test
    void testDynamicColumns() {
        try (var query = new SqlQuery(connectionMock)) {
            query.sql("SELECT ${COLUMNS} FROM employee t");
            query.column("t.id", mockKey("id"))
                    .column("t.name", mockKey("name"))
                    .column("t.created", mockKey("created"));

            var expected = """
                    SELECT t.id AS "id",
                           t.name AS "name",
                           t.created AS "created" FROM employee t""";
            assertEquals(expected, query.toString());
        }
    }

    @Test
    void testColumnLabelMismatch() {
        try (var query = new SqlQuery(connectionMock)) {
            query.sql("SELECT ${COLUMNS} FROM table");
            // Should throw exception because label() is used with ${COLUMNS} placeholder
            assertThrows(IllegalStateException.class, () -> query.label("any", mockKey("id")));
        }

        try (var query = new SqlQuery(connectionMock)) {
            query.sql("SELECT a AS ${lbl} FROM table");
            // Should throw exception because column() is used without ${COLUMNS} placeholder
            assertThrows(IllegalStateException.class, () -> query.column("a", mockKey("id")));
        }
    }

    @Test
    void testDuplicateKey() {
        try (var query = new SqlQuery(connectionMock)) {
            query.sql("SELECT ${COLUMNS} FROM table")
                    .column("a", mockKey("id"));
            assertThrows(IllegalArgumentException.class, () ->
                    query.column("a", mockKey("id")));
        }
    }

    @Test
    void testStaticRun() {
        var result = SqlQuery.run(connectionMock, query -> {
            query.sql("SELECT a AS ${lbl} FROM table").label("lbl", mockKey("name"));
            return query.toStringLine();
        });
        assertEquals("SELECT a AS \"name\" FROM table", result);
    }
}