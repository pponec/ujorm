package org.ujorm.mapper.utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

/** Handles caching, execution and resource cleanup of batched statements. */
public final class StatementCache<V> implements AutoCloseable {

    private final int maxCapacity;
    private final HashMap<BitSet, PreparedStatement> cache = new HashMap<>();
    private final java.util.HashSet<V> activeIds = new java.util.HashSet<>();

    /** Creates a cache with a default capacity of 5. */
    public StatementCache() {
        this(5);
    }

    /** Creates a cache with a specific maximum capacity. */
    public StatementCache(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /** Retrieves an existing statement for the given mask. */
    public PreparedStatement get(BitSet mask) {
        return cache.get(mask);
    }

    /** Caches a statement and flushes if capacity is exceeded. */
    public long put(BitSet mask, PreparedStatement statement) throws SQLException {
        var result = 0L;

        // Flush happens only if the cache is full AND the mask is entirely new
        if (cache.size() >= maxCapacity && !cache.containsKey(mask)) {
            result = flush();
        }

        cache.put(mask, statement);
        return result;
    }

    /** Checks if a statement for the given mask is already cached. */
    public boolean containsKey(BitSet mask) {
        return cache.containsKey(mask);
    }

    /** Returns the number of currently cached statements. */
    public int size() {
        return cache.size();
    }

    /** Flushes the cache if the given ID is already active. */
    public long flushOnCollision(V id) throws SQLException {
        if (activeIds.contains(id)) {
            return flush();
        }
        return 0L;
    }

    /** Adds an ID to the active set. */
    public void addId(V id) {
        activeIds.add(id);
    }

    /** Executes and closes all batched statements in the cache. */
    public long flush() throws SQLException {
        if (cache.isEmpty()) {
            return 0L;
        }

        var result = 0L;
        for (var statement : cache.values()) {
            var counts = statement.executeBatch();
            for (var count : counts) {
                result += count;
            }
            statement.close();
        }
        cache.clear();
        activeIds.clear();
        return result;
    }

    /** Safe cleanup, but any returned update counts here are lost */
    @Override
    public void close() throws SQLException {
        flush();
    }
}