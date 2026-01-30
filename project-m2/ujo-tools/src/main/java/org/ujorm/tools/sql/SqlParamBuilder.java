/*
 * Copyright 2024-2026 Pavel Ponec
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/jdbc/SqlBuilder.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.sql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.jdbc.SqlConsumer;
import org.ujorm.tools.jdbc.SqlFunction;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A fluent wrapper over {@link PreparedStatement}
 * that manages named parameters and ensures automatic resource cleanup
 * of both statements and result sets.
 * <h4>Sample of usage</h4>
 * <pre>
 * try (var builder = new SqlParamBuilder(dbConnection)) {
      List&lt;Employee&gt; employees = builder.sql("""
                SELECT t.id, t.name, t.created
                FROM employee t
                WHERE t.id &gt; :id
                  AND t.code IN (:code)
                ORDER BY t.id
                """)
        .bind("id", 10)
        .bind("code", "T", "V")
        .streamMap(rs -&gt; new Employee(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getObject("created", LocalDate.class)))
        .toList();
 * }
 * </pre>
 * Licence: Apache License, Version 2.0
 * Original source: <a href="https://github.com/pponec/PPScriptsForJava/blob/development/src/main/java/net/ponec/script/SqlExecutor.java">GitHub</a>
 * @author Pavel Ponec, https://github.com/pponec
 * @since 2.26
 */
public class SqlParamBuilder implements AutoCloseable {

    /** SQL parameter mark type of {@code :param} */
    static final Pattern SQL_MARK = Pattern.compile(":(\\w+)");

    @NotNull
    private final Connection dbConnection;
    @Nullable
    protected String sqlTemplate = "";
    @NotNull
    private final Map<String, ParamValue> params = new HashMap<>();
    @Nullable
    private PreparedStatement preparedStatement = null;
    @Nullable
    private ResultSet resultSet = null;

    public SqlParamBuilder(@NotNull Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /** Sets a new SQL template and resets current parameters.
     * Any existing resources are closed. */
    public SqlParamBuilder sql(@NotNull String... sqlLines) {
        close();
        params.clear();
        sqlTemplate = sqlLines.length == 1 ? sqlLines[0] : String.join("\n", sqlLines);
        return this;
    }

    public SqlParamBuilder bind(@NotNull final String key, final Boolean... values) {
        return bind(true, key, values);
    }
    public SqlParamBuilder bind(final boolean enabled, @NotNull final String key, final Boolean... values) {
        return bindObject(enabled, key, JDBCType.BOOLEAN, values);
    }

    /** Bind Bytes */
    public SqlParamBuilder bind(@NotNull final String key, final Byte... values) {
        return bind(true, key, values);
    }
    public SqlParamBuilder bind(final boolean enabled, @NotNull final String key, final Byte... values) {
        return bindObject(enabled, key, JDBCType.TINYINT, values);
    }

    /** Bind Shorts */
    public SqlParamBuilder bind(@NotNull final String key, final Short... values) {
        return bind(true, key, values);
    }
    public SqlParamBuilder bind(final boolean enabled, @NotNull final String key, final Short... values) {
        return bindObject(enabled, key, JDBCType.SMALLINT, values);
    }

    /** Bind Integers */
    public SqlParamBuilder bind(@NotNull final String key, final Integer... values) {
        return bind(true, key, values);
    }
    public SqlParamBuilder bind(final boolean enabled, @NotNull final String key, final Integer... values) {
        return bindObject(enabled, key, JDBCType.BIGINT, values);
    }

    /** Bind Longs */
    public SqlParamBuilder bind(@NotNull final String key, final Long... values) {
        return bind(true, key, values);
    }
    public SqlParamBuilder bind(final boolean enabled, @NotNull final String key, final Long... values) {
        return bindObject(enabled, key, JDBCType.BIGINT, values);
    }

    /** Bind BigDecimal */
    public SqlParamBuilder bind(@NotNull final String key, final BigDecimal... values) {
        return bind(true, key, values);
    }
    public SqlParamBuilder bind(final boolean enabled, @NotNull final String key, final BigDecimal... values) {
        return bindObject(enabled, key, JDBCType.NUMERIC, values);
    }

    /** Bind String */
    public SqlParamBuilder bind(@NotNull final String key, final String... values) {
        return bind(true, key, values);
    }
    public SqlParamBuilder bind(final boolean enabled, @NotNull final String key, final String... values) {
        return bindObject(enabled, key, JDBCType.VARCHAR, values);
    }

    /** Bind LocalDates */
    public SqlParamBuilder bind(@NotNull final String key, final LocalDate... values) {
        return bind(true, key, values);
    }
    public SqlParamBuilder bind(final boolean enabled, @NotNull final String key, final LocalDate... values) {
        return bindObject(enabled, key, JDBCType.DATE, values);
    }

    /** Bind LocalDateTimes */
    public SqlParamBuilder bind(@NotNull final String key, final LocalDateTime... values) {
        return bind(true, key, values);
    }
    public SqlParamBuilder bind(final boolean enabled, @NotNull final String key, final LocalDateTime... values) {
        return bindObject(enabled, key, JDBCType.TIMESTAMP, values);
    }

    /** Bind Objects */
    public SqlParamBuilder bindObject(@NotNull final String key, final Object... values) {
        return bindObject(true, key, JDBCType.OTHER, values);
    }
    /** Assigns SQL parameter values. If reusing a statement, ensure the same number of parameters is set. */
    public SqlParamBuilder bindObject(final boolean enabled, @NotNull final String key, final JDBCType jdbcType, final Object... values) {
        if (enabled) {
            params.put(key, new ParamValue(jdbcType, values));
        }
        return this;
    }

    public int execute() {
        try {
            return prepareStatement(Statement.NO_GENERATED_KEYS).executeUpdate();
        } catch (SQLException e) {
            throw sqlException(e);
        }
    }

    /** Executes an INSERT statement with the ability to retrieve generated keys. */
    public int executeInsert() {
        try {
            return prepareStatement(Statement.RETURN_GENERATED_KEYS).executeUpdate();
        } catch (SQLException e) {
            throw sqlException(e);
        }
    }

    /** Internal execution of a SELECT query. */
    private ResultSet executeSelect() {
        try {
            return prepareStatement(Statement.NO_GENERATED_KEYS).executeQuery();
        } catch (SQLException e) {
            throw sqlException(e);
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
                    throw sqlException(e);
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
            sqlException(e);
        }
        this.resultSet = rs;
    }

    /** Executes the query and processes each row using the provided consumer. */
    public void forEach(@NotNull SqlConsumer consumer) throws SQLException {
        stream(executeSelect()).forEach(consumer);
    }

    /** Executes the query and returns a Stream of mapped results. */
    @NotNull
    public <R> Stream<R> streamMap(SqlFunction<ResultSet, ? extends R> mapper) {
        return stream(executeSelect()).map(mapper);
    }

    /** Closes the PreparedStatement and any active ResultSet.
     * The database connection remains open. */
    @Override
    public void close() {
        try (var ps = preparedStatement; var rs = resultSet) {
        } catch (Exception e) {
            throw sqlException(e, "Closing resources failed");
        } finally {
            resultSet = null;
            preparedStatement = null;
            params.clear();
        }
    }

    /** Builds or reuses a PreparedStatement and binds current parameters. */
    @NotNull
    public PreparedStatement prepareStatement(int autoGeneratedKeys) {
        try {
            final var sqlValues = new ArrayList<ParamValue>(params.size());
            final var sql = buildSql(sqlValues, false);
            final var result = preparedStatement != null
                    ? preparedStatement
                    : dbConnection.prepareStatement(sql, autoGeneratedKeys);
            for (int i = 0, max = sqlValues.size(); i < max; i++) {
                var sqlValue = sqlValues.get(i);
                result.setObject(i + 1, sqlValue.first(), sqlValue.jdbcType);
            }
            preparedStatement = result;
            return result;
        } catch (SQLException e) {
            throw sqlException(e);
        }
    }

    /** Returns the ResultSet containing generated keys from the last insert. */
    @Nullable
    protected ResultSet generatedKeysRs() {
        try {
            return (preparedStatement != null) ? preparedStatement.getGeneratedKeys() : null;
        } catch (SQLException e) {
            throw sqlException(e);
        }
    }

    /** Method for retrieving the primary keys of an INSERT statement. Only one call per INSERT is allowed. <br>
     * Usage: {@code builder.generatedKeys(rs -> rs.getInt(1)).findFirst()} */
    @NotNull
    public <R> Stream<R> generatedKeys(SqlFunction<ResultSet, ? extends R> mapper) {
        final var generatedKeysRs = generatedKeysRs();
        return generatedKeysRs != null
                ? stream(generatedKeysRs).map(mapper)
                : Stream.of();
    }

    /** Method returns the last inserted key of the last INSERT statement.
     * Only one call per INSERT is allowed. <br>
     * Usage: {@code builder.LastKey(rs -> rs.getInt(1))}
     * @throws NoSuchElementException If no key found */
    @NotNull
    public <R> R generatedLastKey(SqlFunction<ResultSet, ? extends R> mapper) throws NoSuchElementException {
        try (var stream = generatedKeys(mapper)) {
            return stream.reduce((first, second) -> second)
                    .orElseThrow(() -> new NoSuchElementException("No keys"));
        }
    }

    @NotNull
    protected String buildSql(List<ParamValue> sqlValues, boolean toLog) {
        final var result = new StringBuffer(256);
        final var matcher = SQL_MARK.matcher(sqlTemplate);
        final var missingKeys = new HashSet<>();
        while (matcher.find()) {
            final var key = matcher.group(1);
            final var param = params.get(key);
            if (param != null) {
                matcher.appendReplacement(result, "");
                for (int i = 0; i < param.values.length; i++) {
                    if (i > 0) result.append(',');
                    result.append(toLog ? "[" + param.values[i] + "]" : "?");
                    sqlValues.add(i == 0 ? param : new ParamValue(param.jdbcType, param.values[i]));
                }
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
                missingKeys.add(key);
            }
        }
        if (!toLog && !missingKeys.isEmpty()) {
            throw sqlException(null, "Missing SQL parameter: " + missingKeys);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    @NotNull
    public String sqlTemplate() {
        return sqlTemplate;
    }

    protected static org.ujorm.tools.sql.SQLException sqlException(@Nullable final Exception ex, @NotNull String... messages) {
        var msg = (messages.length > 0 || ex == null) ? String.join(" ", messages) : ex.getMessage();
        return new org.ujorm.tools.sql.SQLException(ex, msg);
    }

    record ParamValue(JDBCType jdbcType, Object... values) {
        public Object first() {
            return values.length > 0 ? values[0] : null;
        }
    }

    @NotNull
    @Override
    public String toString() {
        return buildSql(new ArrayList<>(), true);
    }

    public String toStringLine() {
        return toString().replaceAll("\\s*\\R+\\s*", " ");
    }
}