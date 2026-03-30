package org.ujorm.orm.utils;

import org.junit.jupiter.api.Test;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatementCacheTest {

    /** Tests basic put, get and size operations. */
    @Test
    public void testPutAndGet() throws SQLException {
        var statementCache = new StatementCache<Integer>(5, 100);
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
        var statementCache = new StatementCache<Integer>(2, 100);

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
        var result = statementCache.put(changes3, statement3);

        assertEquals(3L, result); // 2 + 1 rows from statement1 and statement2
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
        var statementCache = new StatementCache<Integer>(2, 100);

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
        var statementCache = new StatementCache<Integer>(5, 100);
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

        try (var cache = new StatementCache<Integer>(5, 100)) {
            cache.put(changes1, statement1);
        }

        // Verify that leaving the try-with-resources block triggered flush and close
        verify(statement1, times(1)).executeBatch();
        verify(statement1, times(1)).close();
    }

    /** Tests active ID tracking and flush on collision. */
    @Test
    public void testFlushOnCollision() throws SQLException {
        var statementCache = new StatementCache<Integer>(5, 100);
        var changes1 = BitSet.of(10);
        var statement1 = mock(PreparedStatement.class);

        when(statement1.executeBatch()).thenReturn(new int[]{ 2 });

        statementCache.put(changes1, statement1);

        // No collision yet
        var result = statementCache.flushOnCollision(1);
        assertEquals(0L, result);

        // Add ID to active set
        statementCache.addId(1);

        // Different ID should not trigger flush
        result = statementCache.flushOnCollision(2);
        assertEquals(0L, result);
        assertEquals(1, statementCache.size());

        // Same ID should trigger flush
        result = statementCache.flushOnCollision(1);
        assertEquals(2L, result);
        assertEquals(0, statementCache.size());
        verify(statement1, times(1)).executeBatch();
        verify(statement1, times(1)).close();
    }
}