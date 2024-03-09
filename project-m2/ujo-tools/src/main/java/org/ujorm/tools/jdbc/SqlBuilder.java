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
public class SqlBuilder implements AutoCloseable {

    private static final Pattern PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

    @NotNull
    protected final String sqlTemplate;
    @NotNull
    protected final Map<String, Object> params;
    @NotNull
    protected final Connection connection;

     private PreparedStatement preparedStatement = null;

    public SqlBuilder(
            @NotNull CharSequence sqlTemplate,
            @NotNull Map<String, Object> params,
            @NotNull Connection connection) {
        this.sqlTemplate = sqlTemplate.toString();
        this.params = params;
        this.connection = connection;
    }

    public SqlBuilder(@NotNull CharSequence sqlTemplate, @NotNull Connection connection) {
        this(sqlTemplate, new HashMap<>(), connection);
    }

    @NotNull
    public PreparedStatementWrapper executeSelect() throws IllegalStateException, SQLException {
        return new PreparedStatementWrapper(prepareStatement());
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
    public void close() throws Exception {
       if (preparedStatement != null) {
           preparedStatement.close();
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
        final Matcher matcher = PATTERN.matcher(sqlTemplate);
        final Set<String> missingKeys = new HashSet<>();
        final StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            final String key = matcher.group(1);
            final Object value = params.get(key);
            if (value != null) {
                matcher.appendReplacement(result, "");
                result.append(Matcher.quoteReplacement(toLog ? "[" + value + "]" : "?"));
                sqlValues.add(value);
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement("${" + key + "}"));
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
    public SqlBuilder set(String key, Object value) {
        this.params.put(key, value);
        return this;
    }

    @Override
    @NotNull
    public String toString() {
        return buildSql(new ArrayList<>(), true);
    }

    /** Based on the @{link RowIterator} class */
    public static class PreparedStatementWrapper implements Iterable<ResultSet>, Iterator<ResultSet>, Closeable {

        /** Prepared Statement */
        @NotNull
        private final PreparedStatement ps;
        /** ResultSet */
        @Nullable
        private ResultSet rs;
        /** It the cursor ready for reading? After a row reading the value will be set to false */
        private boolean cursorReady = false;
        /** Has a resultset a next row? */
        private boolean hasNext = false;

        public PreparedStatementWrapper(@NotNull final PreparedStatement ps) {
            this.ps = ps;
        }

        @NotNull
        @Override
        public Iterator<ResultSet> iterator() {
            return this;
        }

        @Override
        public Spliterator<ResultSet> spliterator() {
            throw new UnsupportedOperationException("todo");
        }

        /** The last checking closes all resources. */
        @Override
        public boolean hasNext() throws IllegalStateException {
            if (!cursorReady) try {
                if (rs == null) {
                    rs = ps.executeQuery();
                }
                hasNext = rs.next();
                if (!hasNext) {
                    close();
                }
                cursorReady = true;
            } catch (SQLException | IOException e) {
                throw new IllegalStateException(e);
            }
            return hasNext;
        }

        @Override
        public ResultSet next() {
            if (hasNext()) {
                cursorReady = false;
                return rs;
            }
            throw new NoSuchElementException();
        }

        /** Close all resources */
        @Override
        public void close() throws IOException {
            if (rs != null) {
                try (ResultSet tempRs = rs) {
                    cursorReady = true;
                    hasNext = false;
                } catch (SQLException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }

}