package org.ujorm.mapper.utils;

import org.junit.jupiter.api.Test;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StatementCacheTest {

    /** Tests basic put, get and size operations. */
    @Test
    public void testPutAndGet() throws SQLException {
        var statementCache = new StatementCache(5);
        var changes1 = BitSet.of(10);
        changes1.setValue(1, true);

        var statement1 = mock(PreparedStatement.class);
        var result = statementCache.put(changes1, statement1);

        assertEquals(0L, result);
        assertEquals(1, statementCache.size());
        assertTrue(statementCache.containsKey(changes1));
        assertEquals(statement1, statementCache.get(changes1));
    }

    /** Tests that flush is triggered when capacity is exceeded with a new changes. */
    @Test
    public void testAutomaticFlushOnCapacityLimit() throws SQLException {
        var statementCache = new StatementCache(2);

        var changes1 = BitSet.of(10); changes1.setValue(1, true);
        var changes2 = BitSet.of(10); changes2.setValue(2, true);
        var changes3 = BitSet.of(10); changes3.setValue(3, true);

        var statement1 = mock(PreparedStatement.class);
        var statement2 = mock(PreparedStatement.class);
        var statement3 = mock(PreparedStatement.class);

        // Mocking executeBatch to return specific update counts
        when(statement1.executeBatch()).thenReturn(new int[]{ 1, 1 }); // 2 rows
        when(statement2.executeBatch()).thenReturn(new int[]{ 1 });    // 1 row

        statementCache.put(changes1, statement1);
        statementCache.put(changes2, statement2);

        assertEquals(2, statementCache.size());

        // Adding 3rd unique changes should trigger flush of statement1 and statement2
        var flushResult = statementCache.put(changes3, statement3);

        assertEquals(3L, flushResult); // 2 + 1 rows from statement1 and statement2
        assertEquals(1, statementCache.size()); // Only statement3 remains in cache

        // Verify that executeBatch and close were called on flushed statements
        verify(statement1, times(1)).executeBatch();
        verify(statement1, times(1)).close();
        verify(statement2, times(1)).executeBatch();
        verify(statement2, times(1)).close();

        // Verify statement3 was NOT flushed
        verify(statement3, never()).executeBatch();
    }

    /** Tests that flush is NOT triggered if capacity is met but changes already exists. */
    @Test
    public void testNoFlushOnExistingKeyWhenFull() throws SQLException {
        var statementCache = new StatementCache(2);

        var changes1 = BitSet.of(10); changes1.setValue(1, true);
        var changes2 = BitSet.of(10); changes2.setValue(2, true);

        var statement1 = mock(PreparedStatement.class);
        var statement2 = mock(PreparedStatement.class);

        statementCache.put(changes1, statement1);
        statementCache.put(changes2, statement2);

        // Put with existing changes1 -> should not flush
        var result = statementCache.put(changes1, statement1);

        assertEquals(0L, result);
        assertEquals(2, statementCache.size());
        verify(statement1, never()).executeBatch();
    }

    /** Tests explicit manual flush and return value calculation. */
    @Test
    public void testManualFlush() throws SQLException {
        var statementCache = new StatementCache(5);
        var changes1 = BitSet.of(10);
        var statement1 = mock(PreparedStatement.class);

        when(statement1.executeBatch()).thenReturn(new int[]{ 5, 5 });

        statementCache.put(changes1, statement1);
        var result = statementCache.flush();

        assertEquals(10L, result);
        assertEquals(0, statementCache.size());
        verify(statement1, times(1)).close();
    }

    /** Tests AutoCloseable interface integration. */
    @Test
    public void testAutoCloseable() throws SQLException {
        var changes1 = BitSet.of(10);
        var statement1 = mock(PreparedStatement.class);
        when(statement1.executeBatch()).thenReturn(new int[0]);

        try (var cache = new StatementCache(5)) {
            cache.put(changes1, statement1);
        }

        // Verify that leaving the try-with-resources block triggered flush and close
        verify(statement1, times(1)).executeBatch();
        verify(statement1, times(1)).close();
    }
}