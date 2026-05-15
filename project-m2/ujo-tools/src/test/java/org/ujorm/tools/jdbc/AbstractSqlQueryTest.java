package org.ujorm.tools.jdbc;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


/**
 * Test for AbstractSqlQuery using Mockito to simulate JDBC infrastructure.
 */
class AbstractSqlQueryTest {

    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockStatement.getGeneratedKeys()).thenReturn(mockResultSet);
    }

    @Test
    void testSqlTemplateWithParams() throws SQLException {
        var query = new TestSqlQuery(mockConnection)
                .sql("SELECT * FROM users WHERE id = :id AND name = :name")
                .bind("id", 10)
                .bind("name", "Pavel");

        query.execute();

        // Capture the generated SQL
        var sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockConnection).prepareStatement(sqlCaptor.capture(), anyInt());

        assertEquals("SELECT * FROM users WHERE id = ? AND name = ?", sqlCaptor.getValue());
        verify(mockStatement).setObject(eq(1), eq(10), anyInt());
        verify(mockStatement).setObject(eq(2), eq("Pavel"), anyInt());
    }

    @Test
    void testInExpressionBinding() throws SQLException {
        var query = new TestSqlQuery(mockConnection)
                .sql("SELECT * FROM table WHERE code IN (:codes)")
                .bind("codes", "A", "B", "C");

        query.toString(); // Triggers buildSql internally

        var expectedSql = "SELECT * FROM table WHERE code IN ([A],[B],[C])";
        assertEquals(expectedSql, query.toString());
    }

    @Test
    void testToStreamProcessing() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("name")).thenReturn("Alpha", "Beta");

        var query = new TestSqlQuery(mockConnection)
                .sql("SELECT name FROM table");

        List<String> result = query.toStream(rs -> rs.getString("name"))
                .collect(Collectors.toList());

        assertEquals(2, result.size());
        assertEquals("Alpha", result.get(0));
        assertEquals("Beta", result.get(1));
    }

    @Test
    void testGetGeneratedKeys() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getLong(1)).thenReturn(99L);

        var query = new TestSqlQuery(mockConnection)
                .sql("INSERT INTO table (val) VALUES (:val)")
                .bind("val", "data");

        query.executeInsert();
        var key = query.getGeneratedLastKey(rs -> rs.getLong(1));

        assertEquals(99L, key);
    }

    @Test
    void testMissingParameterThrowsException() {
        var query = new TestSqlQuery(mockConnection)
                .sql("SELECT * FROM table WHERE id = :id");
        // No bind for "id"

        assertThrows(AbstractSqlQuery.SqlException.class, query::execute);
    }

    @Test
    void testCloseResources() throws SQLException {
        var query = new TestSqlQuery(mockConnection).sql("SELECT 1");
        query.execute();
        query.close();

        verify(mockStatement).close();
    }

    /** Concrete implementation for testing purposes */
    private static class TestSqlQuery extends AbstractSqlQuery<TestSqlQuery> {
        public TestSqlQuery(@NotNull Connection dbConnection) {
            super(dbConnection);
        }
    }
}