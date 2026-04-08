/*
 * Copyright 2024-2026 Pavel Ponec
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/jdbc/SqlBuilder.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.jdbc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.common.Array;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** Abstract base class for SqlQuery containing internal JDBC execution logic. */
public abstract class AbstractSqlQuery<T extends AbstractSqlQuery<T>> implements AutoCloseable {
    /** SQL parameter mark type of {@code :param} */
    static final Pattern SQL_MARK = Pattern.compile(":(\\w+)");

    /** Class logger */
    private static final Logger LOGGER = Logger.getLogger(AbstractSqlQuery.class.getName());

    @NotNull
    protected final Connection dbConnection;
    @NotNull
    protected String sqlTemplate = "";
    @NotNull
    private final Map<String, ParamValue> params = new HashMap<>();
    @Nullable
    private PreparedStatement preparedStatement = null;
    @Nullable
    private ResultSet resultSet = null;
    @Nullable
    private Integer fetchSize = null;
    @Nullable
    private AbstractSqlQuery.LogRequest logRequest = null;

    @Nullable
    protected StringBuilder _writer = null;

    public AbstractSqlQuery(@NotNull Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /** Returns this instance cast to the generic type T for fluent API. */
    @SuppressWarnings("unchecked")
    protected final T self() {
        return (T) this;
    }

    /** Get builder */
    @NotNull
    protected StringBuilder getWriter(boolean reset) {
        if (_writer == null) {
            _writer = new StringBuilder(64);
        } else if (reset) {
            _writer.setLength(0);
        }
        return _writer;
    }

    /** Sets a new SQL template and resets current parameters. Any existing resources are closed. */
    public T sql(@NotNull CharSequence... sqlLines) {
        close();
        sqlTemplate = sqlLines.length == 1 ? sqlLines[0].toString() : String.join("\n", sqlLines);
        return self();
    }

    /** Sets the fetch size for the prepared statement */
    public T fetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
        return self();
    }

    /** Request for the SQL execution logging */
    public T log(@Nullable Level level, boolean includingParams) {
        logRequest = level != null ? new LogRequest(level, includingParams) : null;
        return self();
    }

    public T bind(@NotNull final String key, final Boolean... values) {
        return bind(true, key, values);
    }

    public T bind(final boolean enabled, @NotNull final String key, final Boolean... values) {
        return bindObject(enabled, key, JDBCType.BOOLEAN, (Object[]) values);
    }

    /** Bind Bytes */
    public T bind(@NotNull final String key, final Byte... values) {
        return bind(true, key, values);
    }

    public T bind(final boolean enabled, @NotNull final String key, final Byte... values) {
        return bindObject(enabled, key, JDBCType.TINYINT, (Object[]) values);
    }

    /** Bind Shorts */
    public T bind(@NotNull final String key, final Short... values) {
        return bind(true, key, values);
    }

    public T bind(final boolean enabled, @NotNull final String key, final Short... values) {
        return bindObject(enabled, key, JDBCType.SMALLINT, (Object[]) values);
    }

    /** Bind Integers */
    public T bind(@NotNull final String key, final Integer... values) {
        return bind(true, key, values);
    }

    public T bind(final boolean enabled, @NotNull final String key, final Integer... values) {
        return bindObject(enabled, key, JDBCType.BIGINT, (Object[]) values);
    }

    /** Bind Longs */
    public T bind(@NotNull final String key, final Long... values) {
        return bind(true, key, values);
    }

    public T bind(final boolean enabled, @NotNull final String key, final Long... values) {
        return bindObject(enabled, key, JDBCType.BIGINT, (Object[]) values);
    }

    /** Bind BigDecimal */
    public T bind(@NotNull final String key, final BigDecimal... values) {
        return bind(true, key, values);
    }

    public T bind(final boolean enabled, @NotNull final String key, final BigDecimal... values) {
        return bindObject(enabled, key, JDBCType.NUMERIC, (Object[]) values);
    }

    /** Bind String */
    public T bind(@NotNull final String key, final String... values) {
        return bind(true, key, values);
    }

    public T bind(final boolean enabled, @NotNull final String key, final String... values) {
        return bindObject(enabled, key, JDBCType.VARCHAR, (Object[]) values);
    }

    /** Bind LocalDates */
    public T bind(@NotNull final String key, final LocalDate... values) {
        return bind(true, key, values);
    }

    public T bind(final boolean enabled, @NotNull final String key, final LocalDate... values) {
        return bindObject(enabled, key, JDBCType.DATE, (Object[]) values);
    }

    /** Bind LocalDateTimes */
    public T bind(@NotNull final String key, final LocalDateTime... values) {
        return bind(true, key, values);
    }

    public T bind(final boolean enabled, @NotNull final String key, final LocalDateTime... values) {
        return bindObject(enabled, key, JDBCType.TIMESTAMP, (Object[]) values);
    }

    /** Bind Objects */
    public T bindObject(@NotNull final String key, final Object... values) {
        return bindObject(true, key, JDBCType.OTHER, values);
    }

    /** Assigns SQL parameter values. If reusing a statement, ensure the same number of parameters is set. */
    public T bindObject(final boolean enabled, @NotNull final String key, final JDBCType jdbcType, @NotNull final Object... values) {
        return bindObject(enabled, key, jdbcType, Array.of(values));
    }

    /** Assigns SQL parameter values. If reusing a statement, ensure the same number of parameters is set. */
    public T bindObject(final boolean enabled, @NotNull final String key, final JDBCType jdbcType, @NotNull final Array<Object> values) {
        if (enabled) {
            params.put(key, new ParamValue(jdbcType, values));
        }
        return self();
    }

    public int execute() {
        try {
            return prepareStatement(Statement.NO_GENERATED_KEYS).executeUpdate();
        } catch (SQLException e) {
            throw new SqlException(e);
        }
    }

    /** Executes an INSERT statement with the ability to retrieve generated keys. */
    public int executeInsert() {
        try {
            return prepareStatement(Statement.RETURN_GENERATED_KEYS).executeUpdate();
        } catch (SQLException e) {
            throw new SqlException(e);
        }
    }

    /** Internal execution of a SELECT query. */
    private ResultSet executeSelect() {
        try {
            return prepareStatement(Statement.NO_GENERATED_KEYS).executeQuery();
        } catch (SQLException e) {
            throw new SqlException(e);
        }
    }

    /** Creates a Stream from the ResultSet. The Stream ensures the ResultSet is closed when finished. */
    @NotNull
    private Stream<ResultSet> stream(final ResultSet rs) {
        switchResultSet(rs);
        final var iterator = new Iterator<ResultSet>() {
            @Override
            public boolean hasNext() {
                try {
                    return resultSet.next();
                } catch (SQLException e) {
                    throw new SqlException(e);
                }
            }
            @Override
            public ResultSet next() {
                return rs;
            }
        };
        final var spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false).onClose(() -> switchResultSet(null));
    }

    /** Safely closes the current ResultSet and starts tracking the new one. */
    private void switchResultSet(@Nullable final ResultSet rs) {
        try (var oldResultSet = this.resultSet) {
        } catch (SQLException e) {
            throw new SqlException(e);
        }
        this.resultSet = rs;
    }

    /** Executes the query and processes each row using the provided consumer. */
    public void forEach(@NotNull SqlConsumer<ResultSet> consumer) throws SQLException {
        stream(executeSelect()).forEach(consumer);
    }

    /** Executes the query and returns a Stream of mapped results. */
    @NotNull
    public <R> Stream<R> streamMap(SqlFunction<ResultSet, ? extends R> mapper) {
        return stream(executeSelect()).map(mapper);
    }

    /**
     * Closes the PreparedStatement and any active ResultSet.
     * External JDBC resources are released, but the internal StringBuilder
     * is intentionally preserved to avoid memory reallocation overhead
     * when the instance is reused for subsequent SQL commands.
     */
    @Override
    public void close() {
        try (var ps = preparedStatement; var rs = resultSet) {
        } catch (Exception e) {
            throw new SqlException(e, "Closing resources failed");
        } finally {
            resultSet = null;
            preparedStatement = null;
            fetchSize = null;
            params.clear();
        }
    }

    /** Builds or reuses a PreparedStatement and binds current parameters. */
    @NotNull
    public PreparedStatement prepareStatement(int autoGeneratedKeys) {
        try {
            final var sqlValues = new ArrayList<ParamValue>(params.size());
            final var sql = buildSql(sqlValues, false);

            if (logRequest != null) {
                var msg = logRequest.params() ? toStringLine() : sql;
                LOGGER.log(logRequest.level(), msg);
            }

            var result = preparedStatement != null
                    ? preparedStatement
                    : dbConnection.prepareStatement(sql, autoGeneratedKeys);

            if (fetchSize != null) {
                result.setFetchSize(fetchSize);
            }
            for (var i = 0; i < sqlValues.size(); i++) {
                var sqlValue = sqlValues.get(i);
                result.setObject(i + 1, sqlValue.first(), sqlValue
                        .jdbcType()
                        .getVendorTypeNumber());
            }
            preparedStatement = result;
            return result;
        } catch (SQLException e) {
            throw new SqlException(e, "prepareStatement()");
        }
    }

    /** Returns the ResultSet containing generated keys from the last insert. */
    @Nullable
    protected ResultSet generatedKeysRs() {
        try {
            return preparedStatement != null ? preparedStatement.getGeneratedKeys() : null;
        } catch (SQLException e) {
            throw new SqlException(e, "generatedKeysRs()");
        }
    }

    /**
     * Method for retrieving the primary keys of an INSERT statement.
     * Only one call per INSERT is allowed.
     */
    @NotNull
    public <R> Stream<R> generatedKeys(SqlFunction<ResultSet, ? extends R> mapper) {
        final var generatedKeysRs = generatedKeysRs();
        return generatedKeysRs != null
                ? stream(generatedKeysRs).map(mapper)
                : Stream.of();
    }

    /** Method returns the last inserted key of the last INSERT statement. */
    @NotNull
    public <R> R generatedLastKey(SqlFunction<ResultSet, ? extends R> mapper) throws NoSuchElementException {
        return generatedKeys(mapper).reduce((first, second) -> second)
                .orElseThrow(() -> new NoSuchElementException("No keys"));
    }

    @NotNull
    protected String buildSql(List<ParamValue> sqlValues, boolean includingValues) {
        final var sqlBuffer = getWriter(true);
        final var matcher = SQL_MARK.matcher(sqlTemplate);
        final var missingKeys = new HashSet<String>();

        while (matcher.find()) {
            final var key = matcher.group(1);
            final var param = params.get(key);
            if (param != null) {
                matcher.appendReplacement(sqlBuffer, "");
                for (int i = 0, max = param.values().size(); i < max; i++) {
                    if (i > 0) sqlBuffer.append(',');
                    sqlBuffer.append(includingValues ? "[" + param.getValue(i) + "]" : "?");
                    sqlValues.add(i == 0 ? param : new ParamValue(param.jdbcType(), Array.of(param.getValue(i))));
                }
            } else {
                matcher.appendReplacement(sqlBuffer, Matcher.quoteReplacement(matcher.group()));
                missingKeys.add(key);
            }
        }
        if (!includingValues && !missingKeys.isEmpty()) {
            throw new SqlException(null, "Missing SQL parameter: " + missingKeys);
        }
        matcher.appendTail(sqlBuffer);

        return buildColumns(sqlBuffer.toString());
    }

    /** Hook for subclasses to append column mappings or formats. */
    @NotNull
    protected String buildColumns(@NotNull String sql) {
        return sql;
    }

    @NotNull
    public String sqlTemplate() {
        return sqlTemplate;
    }

    /** Returns the SQL string include values */
    @NotNull
    @Override
    public String toString() {
        return buildSql(new ArrayList<>(), true);
    }

    /**
     * Converts the text to a single line by collapsing all whitespace sequences into a single space.
     * Note that this formatting applies to the entire string, including spaces inside text literals.
     *
     * @return A single-line text optimized for logging
     */
    public String toStringLine() {
        return toString().replaceAll("\\s+", " ").trim();
    }

    /** A request for logging the SQL. */
    record LogRequest(
            /** Returns the logging level */
            @NotNull Level level,
            /** Returns true if parameters are included */
            boolean params
    ) {}

    /** SQL parameter values */
    protected record ParamValue(
            /** Returns the JDBC Type */
            @NotNull JDBCType jdbcType,
            /** Returns the parameter values */
            @NotNull Array<Object> values
    ) {
        /** Returns the first value from the values array */
        public Object first() {
            return values.getFirstValue(null);
        }

        public Object getValue(int index) {
            return values.getValue(index);
        }
    }

    @FunctionalInterface
    public interface SqlFunction<T, R> extends Function<T, R> {
        @Override
        default R apply(T resultSet) {
            try {
                return applyFunction(resultSet);
            } catch (Exception ex) {
                throw (ex instanceof RuntimeException re) ? re : new SqlException(ex);
            }
        }
        @NotNull
        R applyFunction(T resultSet) throws SQLException;
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
    public static final class SqlException extends org.ujorm.tools.jdbc.SQLException {
        public SqlException(Throwable cause, String... messages) {
            super((messages.length > 0 || cause == null)
                    ? String.join(" ", messages)
                    : cause.getMessage(), cause);
        }
    }
}