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
 * Less than 320 lines long class to simplify work with JDBC.
 * Original source: <a href="https://github.com/pponec/PPScriptsForJava/blob/development/src/main/java/net/ponec/script/SqlExecutor.java">GitHub</a>
 * Licence: Apache License, Version 2.0
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

    public SqlParamBuilder(@NotNull Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /** Close an old statement (if any) and assign the new SQL template */
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

    /** Bind Ingegers */
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

    /** For INSERT operations used before calling method {@code #generatedKeysRs}. */
    public int executeInsert() {
        try {
            return prepareStatement(Statement.RETURN_GENERATED_KEYS).executeUpdate();
        } catch (SQLException e) {
            throw sqlException(e);
        }
    }

    /** Execute: INSERT, UPDATE, DELETE, DDL statements.
     * The ResultSet object is automatically closed when the Statement object that generated it is closed,
     * re-executed, or used to retrieve the next result from a sequence of multiple results. */
    private ResultSet executeSelect() {
        try {
            return prepareStatement(Statement.NO_GENERATED_KEYS).executeQuery();
        } catch (SQLException e) {
            throw sqlException(e);
        }
    }

    /**
     * Returns a Stream over the given ResultSet. Closing the Stream also closes the ResultSet.
     * Prefer {@link #streamMap(SqlFunction)} or {@link #forEach(SqlConsumer)}.
     * @param resultSet the ResultSet to stream over
     */
    @NotNull
    private Stream<ResultSet> stream(final ResultSet resultSet) {
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
                return resultSet;
            }
        };
        // NOTE: The last ResultSet from a PreparedStatement is closed automatically when the statement is closed.
        // For multiple ResultSets or other creation methods, must be closed explicitly.
        final var spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false).onClose(() -> {
            try {
                resultSet.close();
            } catch (SQLException e) {
                sqlException(e);
            }
        });
    }

    /** Iterate executed select */
    public void forEach(@NotNull SqlConsumer consumer) throws SQLException {
        stream(executeSelect()).forEach(consumer);
    }

    @NotNull
    public <R> Stream<R> streamMap(SqlFunction<ResultSet, ? extends R> mapper) {
        return stream(executeSelect()).map(mapper);
    }

    /** The method closes a PreparedStatement object with related objects, not the database connection. */
    @Override
    public void close() {
        try (AutoCloseable c2 = preparedStatement) {
        } catch (Exception e) {
            throw sqlException("Closing fails", e);
        } finally {
            preparedStatement = null;
            params.clear();
        }
    }

    /** Build (or reuse) a PreparedStatement object with SQL arguments
     * @param autoGeneratedKeys For example: {@code Statement.RETURN_GENERATED_KEYS} */
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
        } catch (SQLException ex) {
            throw sqlException(ex);
        }
    }

    @Nullable
    protected ResultSet generatedKeysRs() {
        try {
            return preparedStatement != null ? preparedStatement.getGeneratedKeys() : null;
        } catch (SQLException e) {
            throw org.ujorm.tools.sql.SQLException.of(e);
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
            throw sqlException("Missing SQL parameter: " + missingKeys, null);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    @NotNull
    public String sqlTemplate() {
        return sqlTemplate;
    }

    protected static org.ujorm.tools.sql.SQLException sqlException(@Nullable final SQLException ex) {
        return new org.ujorm.tools.sql.SQLException(ex);
    }

    protected static org.ujorm.tools.sql.SQLException sqlException(@NotNull String messages, @Nullable final Exception ex) {
        return new org.ujorm.tools.sql.SQLException(ex, messages);
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