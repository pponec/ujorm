package org.ujorm.orm.utils;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.tools.jdbc.SQLExceptionBuilder;

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** Common JDBC Utilities */
public final class JdbcUtils {

    private static final JdbcTypeProvider JDBC = new JdbcTypeProvider();

    private JdbcUtils() {
        // Utility class should not be instantiated
    }

    /** Safely closes the current ResultSet and starts tracking the new one. */
    private static void closeResultSet(@Nullable final ResultSet rs) {
        try (var oldResultSet = rs) {
            // Closes automatically via try-with-resources
        } catch (SQLException e) {
            throw SQLExceptionBuilder.build("Can't close a ResultSet", e);
        }
    }

    /** Creates a Stream from the ResultSet. The Stream ensures the ResultSet is closed when finished. */
    @NotNull
    public static Stream<ResultSet> stream(final ResultSet rs) {
        final var iterator = new Iterator<ResultSet>() {
            @Override
            public boolean hasNext() {
                try {
                    return rs.next();
                } catch (SQLException e) {
                    throw SQLExceptionBuilder.build(e);
                }
            }
            @Override
            public ResultSet next() {
                return rs;
            }
        };

        final var spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
        var result = StreamSupport.stream(spliterator, false).onClose(() -> closeResultSet(rs));

        return result;
    }

    public static @NotNull JDBCType findJdbcType(@NotNull Key<?,?> key) {
        return JDBC.findJdbcType(key);
    }

    /** Build a property change set */
    @NotNull
    public static <D> BitSet findChanges(@NotNull D domain, @NotNull D snapshot, @NotNull DomainHandler<D> handler) {
        if (domain == null || snapshot == null) {
            var msg = "The %s object type of %s is required".formatted(
                    domain == null ? "domain" : "snapshot",
                    handler.getDomainClass().getSimpleName());
            throw new IllegalArgumentException(msg);
        }

        var keys = handler.getKeyList();
        var result = BitSet.of(keys.size());
        for (var key : keys) {
            var v1 = key.getValue(domain);
            var v2 = key.getValue(snapshot);
            if (!Objects.equals(v1, v2)) {
                result.setValue(key.index(), true);
            }
        }
        return result;
    }
}