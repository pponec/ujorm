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
import java.io.IOException;
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
    private static final Pattern SQL_MARK = Pattern.compile(":(\\w+)(?=[\\s,;\\]\\)]|$)");

    @NotNull
    protected final String sqlTemplate;
    @NotNull
    protected final Map<String, Object> params;
    @NotNull
    protected final Connection dbConnection;
    @Nullable
    private PreparedStatement preparedStatement = null;
    @Nullable
    private ResultSetWrapper rsWrapper = null;

    public SqlParamBuilder(
            @NotNull CharSequence sqlTemplate,
            @NotNull Map<String, ?> params,
            @NotNull Connection dbConnection) {
        this.sqlTemplate = sqlTemplate.toString();
        this.params = new HashMap<>(params);
        this.dbConnection = dbConnection;
    }

    public SqlParamBuilder(@NotNull CharSequence sqlTemplate, @NotNull Connection dbConnection) {
        this(sqlTemplate, new HashMap<>(), dbConnection);
    }

    @NotNull
    public Iterable<ResultSet> executeSelect() throws IllegalStateException, SQLException {
        try (Closeable rs = rsWrapper) {
        } catch (IOException e) {
            throw new IllegalStateException("Closing fails", e);
        }
        rsWrapper = new ResultSetWrapper(prepareStatement().executeQuery());
        return rsWrapper;
    }

    /** Iterate select */
    public void forEach(RsConsumer consumer) throws IllegalStateException, SQLException  {
        for (ResultSet rs : executeSelect()) {
            consumer.accept(rs);
        }
    }

    public <R> Stream<R> streamMap(JdbcFunction<ResultSet, ? extends R> mapper ) throws SQLException {
        return StreamSupport.stream(executeSelect().spliterator(), false).map(mapper);
    }

    @NotNull
    public int execute() throws IllegalStateException, SQLException {
        return prepareStatement().executeUpdate();
    }

    @NotNull
    public Connection getDbConnection() {
        return dbConnection;
    }

    /** The method closes a PreparedStatement object with related objects, not the database connection. */
    @Override
    public void close() {
        try (Closeable c1 = rsWrapper; PreparedStatement c2 = preparedStatement) {
        } catch (Exception e) {
            throw new IllegalStateException("Closing fails", e);
        } finally {
            rsWrapper = null;
            preparedStatement = null;
        }
    }

    /**
     * Build (or reuse) a PreparedStatement object with SQL arguments
     */
    @NotNull
    public PreparedStatement prepareStatement() throws SQLException {
        final List<Object> sqlValues = new ArrayList<>();
        final String sql = buildSql(sqlValues, false);
        if (preparedStatement == null) {
            preparedStatement = dbConnection.prepareStatement(sql);
        }
        for (int i = 0, max = sqlValues.size(); i < max; i++) {
            preparedStatement.setObject(i + 1, sqlValues.get(i));
        }
        return preparedStatement;
    }

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
    public SqlParamBuilder setParam(String key, Object value) {
        this.params.put(key, value);
        return this;
    }

    @Override
    @NotNull
    public String toString() {
        return buildSql(new ArrayList<>(), true);
    }

    /** Based on the {@code RowIterator} class of Ujorm framework. */
    static final class ResultSetWrapper implements Iterable<ResultSet>, Iterator<ResultSet>, Closeable {
        @NotNull
        private ResultSet resultSet;
        /** It the cursor ready for reading? After a row reading the value will be set to false */
        private boolean cursorReady = false;
        /** Has a resultset a next row? */
        private boolean hasNext = false;

        public ResultSetWrapper(@NotNull final ResultSet resultSet) {
            this.resultSet = resultSet;
        }

        @NotNull
        @Override
        public Iterator<ResultSet> iterator() {
            return this;
        }

        /** The last checking closes all resources. */
        @Override
        public boolean hasNext() throws IllegalStateException {
            if (!cursorReady) try {
                hasNext = resultSet.next();
                if (!hasNext) {
                    close();
                }
                cursorReady = true;
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
            return hasNext;
        }

        @Override
        public ResultSet next() {
            if (hasNext()) {
                cursorReady = false;
                return resultSet;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void close() {
            try (ResultSet rs = resultSet) {
                cursorReady = true;
                hasNext = false;
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}