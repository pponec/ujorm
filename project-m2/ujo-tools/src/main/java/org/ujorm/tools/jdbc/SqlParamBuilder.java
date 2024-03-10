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
public class SqlParamBuilder implements AutoCloseable {

    /** SQL mark type of {@code :param} */
    private static final Pattern SQL_MARK = Pattern.compile(":(\\w+)(?=[\\s,;\\]\\)]|$)");

    @NotNull
    protected final String sqlTemplate;
    @NotNull
    protected final Map<String, Object> params;
    @NotNull
    protected final Connection connection;
    @Nullable
    private PreparedStatement preparedStatement = null;
    @Nullable
    private ResultSetWrapper rsWrapper = null;

    public SqlParamBuilder(
            @NotNull CharSequence sqlTemplate,
            @NotNull Map<String, Object> params,
            @NotNull Connection connection) {
        this.sqlTemplate = sqlTemplate.toString();
        this.params = params;
        this.connection = connection;
    }

    public SqlParamBuilder(@NotNull CharSequence sqlTemplate, @NotNull Connection connection) {
        this(sqlTemplate, new HashMap<>(), connection);
    }

    @NotNull
    public Iterable<ResultSet> executeSelect() throws IllegalStateException, SQLException {
        if (rsWrapper != null) {
            rsWrapper.close();
        }
        rsWrapper = new ResultSetWrapper(prepareStatement().executeQuery());
        return rsWrapper;
    }

    @NotNull
    public int execute() throws IllegalStateException, SQLException {
        return prepareStatement().executeUpdate();
    }

    @NotNull
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void close() {
        try {
            if (rsWrapper != null) rsWrapper.close();
            if (preparedStatement != null) preparedStatement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
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
            preparedStatement = connection.prepareStatement(sql);
        }
        for (int i = 0, max = sqlValues.size(); i < max; i++) {
            preparedStatement.setObject(i + 1, sqlValues.get(i));
        }
        return preparedStatement;
    }

    protected String buildSql(@NotNull List<Object> sqlValues, boolean toLog) {
        final Matcher matcher = SQL_MARK.matcher(sqlTemplate);
        final Set<String> missingKeys = new HashSet<>();
        final StringBuffer result = new StringBuffer();
        final Object[] singleItem = new Object[1];
        while (matcher.find()) {
            final String key = matcher.group(1);
            final Object value = params.get(key);
            if (value != null) {
                matcher.appendReplacement(result, "");
                singleItem[0] = value;
                final Object[] values = value instanceof List ? ((List<?>) value).toArray() : singleItem;
                for (int i = 0, max = values.length; i < max; i++) {
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

    /** Set a SQL parameter */
    public SqlParamBuilder set(String key, Object value) {
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

        @Override
        public Spliterator<ResultSet> spliterator() {
            throw new UnsupportedOperationException("Unsupported");
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

        /** Close all resources */
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