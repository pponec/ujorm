/*
 * Copyright 2024-2024 Pavel Ponec
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
package org.ujorm.tools.jdbc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A tool for building an SQL query with parameters.
 * Inspired by the original class {@link JdbcBuilder}.
 *
 * <h3>Usage:</h3>
 * <pre>
 *     TODO...
 * <pre/>
 *
 * @author Pavel Ponec
 */
public class SqlParamBuilder implements Closeable {

    /** SQL parameter mark type of {@code :param} */
    private static final Pattern SQL_MARK = Pattern.compile(":(\\w+)");

    @NotNull
    protected final Connection dbConnection;
    @Nullable
    protected String sqlTemplate;
    @NotNull
    protected final Map<String, Object> params = new HashMap<>();
    @Nullable
    private PreparedStatement preparedStatement = null;

    public SqlParamBuilder(@NotNull Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    /** Close an old statement (if any) and assign the new SQL template */
    public SqlParamBuilder sql(@NotNull String... sqlLines) {
        close();
        this.params.clear();
        this.sqlTemplate = sqlLines.length == 1 ? sqlLines[0] : String.join("\n", sqlLines);
        return this;
    }

    /** Assign a SQL value */
    public SqlParamBuilder bind(@NotNull String key, @NotNull Object value) {
        this.params.put(key, value);
        return this;
    }

    /** Assign more SQL values, separated by comma {@code ,} */
    public SqlParamBuilder bind(@NotNull String key, @NotNull Object... value) {
        return bind (key, Arrays.asList(value));
    }

    public int execute() throws IllegalStateException, SQLException {
        return prepareStatement().executeUpdate();
    }

    /** Execute: INSERT, UPDATE, DELETE, DDL statements */
    @NotNull
    private ResultSet executeSelect() throws IllegalStateException {
        try {
            return prepareStatement().executeQuery();
        } catch (Exception ex) {
            throw (ex instanceof RuntimeException) ? (RuntimeException) ex : new IllegalStateException(ex);
        }
    }

    /** Use the  {@link #streamMap(SqlFunction)} or {@link #forEach(SqlConsumer)} methods rather */
    @NotNull
    private Stream<ResultSet> stream() {
        final ResultSet resultSet = executeSelect();
        final Iterator<ResultSet> iterator = new Iterator<ResultSet>() {
            @Override
            public boolean hasNext() {
                try {
                    return resultSet.next();
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                }
            }
            @Override
            public ResultSet next() {
                return resultSet;
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }

    /** Iterate executed select */
    public void forEach(@NotNull SqlConsumer consumer) throws IllegalStateException, SQLException  {
        stream().forEach(consumer);
    }

    @NotNull
    public <R> Stream<R> streamMap(SqlFunction<ResultSet, ? extends R> mapper ) {
        return stream().map(mapper);
    }

    public Connection getConnection() {
        return dbConnection;
    }

    /** The method closes a PreparedStatement object with related objects, not the database connection. */
    @Override
    public void close() {
        try (PreparedStatement c2 = preparedStatement) {
        } catch (Exception e) {
            throw new IllegalStateException("Closing fails", e);
        } finally {
            preparedStatement = null;
        }
    }

    /** Build (or reuse) a PreparedStatement object with SQL arguments */
    @NotNull
    public PreparedStatement prepareStatement() throws SQLException {
        final ArrayList<Object> sqlValues = new ArrayList<>(params.size());
        final String sql = buildSql(sqlValues, false);
        final PreparedStatement result = preparedStatement != null
                ? preparedStatement
                : dbConnection.prepareStatement(sql);
        for (int i = 0, max = sqlValues.size(); i < max; i++) {
            result.setObject(i + 1, sqlValues.get(i));
        }
        this.preparedStatement = result;
        return result;
    }

    @NotNull
    protected String buildSql(@NotNull List<Object> sqlValues, boolean toLog) {
        final StringBuffer result = new StringBuffer(256);
        final Matcher matcher = SQL_MARK.matcher(sqlTemplate);
        final Set<String> missingKeys = new HashSet<>();
        final Object[] singleValue = new Object[1];
        while (matcher.find()) {
            final String key = matcher.group(1);
            final Object value = params.get(key);
            if (value != null) {
                matcher.appendReplacement(result, "");
                singleValue[0] = value;
                final Object[] values = value instanceof List ? ((List<?>) value).toArray() : singleValue;
                for (int i = 0; i < values.length; i++) {
                    if (i > 0) result.append(',');
                    result.append(Matcher.quoteReplacement(toLog ? "[" + values[i] + "]" : "?"));
                    sqlValues.add(values[i]);
                }
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement(":" + key));
                missingKeys.add(key);
            }
        }
        if (! toLog && !missingKeys.isEmpty()) {
            throw new IllegalArgumentException("Missing value of the keys: " + missingKeys);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /** Set a SQL parameter value */
    @NotNull
    public SqlParamBuilder setParam(String key, Object value) {
        this.params.put(key, value);
        return this;
    }

    @NotNull
    public String sqlTemplate() {
        return sqlTemplate;
    }

    @NotNull
    @Override
    public String toString() {
        return buildSql(new ArrayList<>(), true);
    }
}