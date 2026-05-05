package org.ujorm.orm.utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * Handles caching, execution and resource cleanup of batched statements.
 *
 * @param <V> Type of the primary key
 */
public final class StatementCache<V> implements AutoCloseable {

    /** Maximum capacity of the cached statements */
    private final int maxStatementCapacity;

    /** Map of currently cached statements */
    private final HashMap<BitSet, PreparedStatement> cache = new HashMap<>();

    /** Set of active entity identifiers to prevent deadlocks */
    private final java.util.HashSet<V> activeIds;

    /** Creates a cache with default capacity of 5 statements and 100 identifiers. */
    public StatementCache() {
        this(5, 100);
    }

    /**
     * Creates a cache with a specific maximum capacity.
     *
     * @param maxStatementCapacity Maximum number of cached statements
     * @param initialIdCapacity Initial capacity for the active IDs set
     */
    public StatementCache(int maxStatementCapacity, int initialIdCapacity) {
        this.maxStatementCapacity = maxStatementCapacity;
        this.activeIds = new java.util.HashSet<>(initialIdCapacity);
    }

    /**
     * Retrieves an existing statement for the given mask.
     *
     * @param mask A bitmask representing the updated columns
     * @return A cached PreparedStatement, or null if not found
     */
    public PreparedStatement get(BitSet mask) {
        return cache.get(mask);
    }

    /**
     * Caches a statement and flushes if capacity is exceeded.
     *
     * @param mask A bitmask representing the updated columns
     * @param statement The PreparedStatement to cache
     * @return Number of executed updates if flush occurred, otherwise 0
     * @throws SQLException If a database access error occurs
     */
    public long put(BitSet mask, PreparedStatement statement) throws SQLException {
        var result = 0L;

        // Flush happens only if the cache is full AND the mask is entirely new
        if (cache.size() >= maxStatementCapacity && !cache.containsKey(mask)) {
            result = flush();
        }

        cache.put(mask, statement);
        return result;
    }

    /**
     * Checks if a statement for the given mask is already cached.
     *
     * @param mask A bitmask representing the updated columns
     * @return True if the statement is cached, otherwise false
     */
    public boolean containsKey(BitSet mask) {
        return cache.containsKey(mask);
    }

    /**
     * Returns the number of currently cached statements.
     *
     * @return The current cache size
     */
    public int size() {
        return cache.size();
    }

    /**
     * Flushes the cache if the given ID is already active.
     *
     * @param id The entity identifier to check
     * @return Number of executed updates if flush occurred, otherwise 0
     * @throws SQLException If a database access error occurs
     */
    public long flushOnCollision(V id) throws SQLException {
        if (activeIds.contains(id)) {
            return flush();
        }
        return 0L;
    }

    /**
     * Adds an ID to the active set.
     *
     * @param id The entity identifier to add
     */
    public void addId(V id) {
        activeIds.add(id);
    }

    /**
     * Executes and closes all batched statements in the cache.
     *
     * @return The total number of executed updates
     * @throws SQLException If a database access error occurs
     */
    public long flush() throws SQLException {
        if (cache.isEmpty()) {
            return 0L;
        }

        var result = 0L;
        for (var statement : cache.values()) {
            var counts = statement.executeBatch();
            result += sumBatchRows(counts);
            statement.close();
        }
        cache.clear();
        activeIds.clear();
        return result;
    }

    private static long sumBatchRows(int[] batchResults) throws SQLException {
        var result = 0L;
        for (var rowCount : batchResults) {
            if (rowCount >= 0) {
                result += rowCount;
            } else if (rowCount == java.sql.Statement.SUCCESS_NO_INFO) {
                result++; // Safe fallback for drivers without exact row count.
            } else if (rowCount == java.sql.Statement.EXECUTE_FAILED) {
                throw new SQLException("Batch execution failed.");
            }
        }
        return result;
    }

    /** Safe cleanup, but any returned update counts here are lost. */
    @Override
    public void close() throws SQLException {
        flush();
    }
}