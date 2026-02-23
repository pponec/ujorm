package org.ujorm.mapper.utils;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

public class Tools {

    /**
     * Splits an array into batches using Object[][] for a concise implementation
     * without reflection. Assumes batchSize is always greater than zero.
     *
     * @param batchSize The maximum size of each resulting batch.
     * @param ids       The original array to be partitioned.
     * @param <V>       The generic type of the input array elements.
     * @return A 2D Object array containing the individual batches.
     */
    @SafeVarargs
    public static <V> Object[][] splitIntoBatches(int batchSize, @NotNull V... ids) {
        if (ids.length <= batchSize) {
            return new Object[][]{ids};
        }

        var chunksCount = (ids.length + batchSize - 1) / batchSize;
        var result = new Object[chunksCount][];

        for (var i = 0; i < chunksCount; i++) {
            var start = i * batchSize;
            var end = Math.min(start + batchSize, ids.length);
            result[i] = Arrays.copyOfRange(ids, start, end);
        }
        return result;
    }

    /** Quote Identifier of the current database */
    public static String getQuoteIdentifier(Connection connection) {
        try {
            return connection.getMetaData().getIdentifierQuoteString();
        } catch (SQLException ex) {
            throw org.ujorm.tools.jdbc.SQLException.of(ex);
        }
    }
}
