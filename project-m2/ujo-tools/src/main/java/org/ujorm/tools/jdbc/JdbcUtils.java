package org.ujorm.tools.jdbc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** Common JDBC Utilities */
public final class JdbcUtils {

    private JdbcUtils() {
        // Utility class should not be instantiated
    }

    @FunctionalInterface
    public interface SqlConsumer<T> extends Consumer<T> {
        @Override
        default void accept(final T t) {
            try {
                acceptResultSet(t);
            } catch (Exception ex) {
                throw (ex instanceof RuntimeException re) ? re : new IllegalStateException(ex);
            }
        }
        void acceptResultSet(T t) throws Exception;
    }

    /** A subclass of the undeclared exception class {@link IllegalStateException}. */
    public static final class SqlException extends IllegalStateException {
        private SqlException(Throwable cause, String... messages) {
            super((messages.length > 0 || cause == null)
                    ? String.join(" ", messages)
                    : cause.getMessage(), cause);
        }
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
}