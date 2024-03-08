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
import org.ujorm.tools.set.LoopingIterator;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
public class SqlBuilder {
    private final static Logger logger = Logger.getLogger(SqlBuilder.class.getName());

    protected char paramMark = '?';
    protected String newLine = "\n";
    protected char newLineCh = newLine.charAt(0);
    protected final String space = "\t";
    protected final List<CharSequence> sql;

    public SqlBuilder() {
        this(new ArrayList<>(), 0);
    }

    private SqlBuilder(List<CharSequence> sql, int offset) {
        this.sql = sql;
    }

    @NotNull
    public SqlBuilder add(@NotNull CharSequence... items) throws IllegalArgumentException {
        assertNotNull(items, "Argument is required");
        for (int i = 0, max = items.length; i < max; i++) {
            final CharSequence item = items[i];
            if (item == null) {
                throw new IllegalArgumentException(String.format("Item[%s] is required", i));
            } else if (item instanceof SqlParam || item.length() > 0) {
                if (item != newLine && !beginningLine()) {
                    sql.add(" ");
                }
                sql.add(item);
            }
        }
        return this;
    }

    @NotNull
    public SqlBuilder comma() {
        add(",");
        return this;
    }

    @NotNull
    public SqlBuilder line(@NotNull CharSequence... items) throws IllegalArgumentException {
        if (!beginningLine()) {
            add(newLine);
        }
        add(items);
        return this;
    }

    @NotNull
    public SqlBuilder line(@NotNull SqlBuilder subquery) throws IllegalArgumentException {
        assertNotNull(subquery, "Argument is required");
        line();
        for (CharSequence sqlItem : subquery.sql) {
            this.add(sqlItem);
        }
        return this;
    }

    public SqlBuilder addIf(boolean condition, SqlBuilder.CheckedConsumer<SqlBuilder> consumer) throws IllegalArgumentException {
        if (condition) {
            consumer.accept(this);
        }
        return this;
    }

    public SqlBuilder lineIf(boolean condition, SqlBuilder.CheckedConsumer<SqlBuilder> consumer) throws IllegalArgumentException {
        if (condition) {
            line();
            consumer.accept(this);
        }
        return this;
    }

    /**
     * Add a quoted text
     */
    public SqlBuilder quoted(@NotNull CharSequence item) throws IllegalArgumentException {
        add("\'");
        add(item).add("'");
        return this;
    }

    /**
     * Insert a body into the envelope
     */
    public <T extends SqlBuilder> T envelope(
            @NotNull String header,
            @NotNull String footer,
            @NotNull CheckedConsumer<T> body)
            throws IllegalArgumentException {
        final T result = (T) this;
        add(header);
        body.accept(result);
        add(footer);
        return result;
    }

    @NotNull
    public SqlBuilder param(@NotNull Object value) throws IllegalArgumentException {
        this.assertNotNull(value, "Argument", "value", "is required");
        this.sql.add(new SqlParam(value));
        return this;
    }

    @NotNull
    public SqlBuilder params(@NotNull Object... values) throws IllegalArgumentException {
        for (int i = 0, max = values.length; i < max; i++) {
            if (i > 0) {
                this.sql.add(",");
            }
            param(values[i]);
        }
        return this;
    }

    public SqlBuilder param(@NotNull Object value, @NotNull SQLType jdbcType, int range) throws IllegalArgumentException {
        this.assertNotNull(value, "Argument", "value", "is required");
        this.sql.add(new SqlParam(value, jdbcType, range));
        return this;
    }

    @NotNull
    protected Object convertValue(@NotNull Object value) {
        final Object result = convertValueInternal(value);
        if (result != value) {
            logger.log(Level.WARNING, () ->
                    String.format("The original value of '%s' has been converted to '%s'.", value, result));
        }
        return result;
    }

    @NotNull
    protected Object convertValueInternal(@NotNull Object value) {
        if (value instanceof Number) {
            return value;
        }
        if (value instanceof Map) {
            final Map box = (Map) value;
            for (Object k : box.keySet()) {
                final Object o1 = box.get(k);
                final Object o2 = convertValue(o1);
                if (o1 != o2) box.put(k, o2);
            }
            return value;
        }
        if (value instanceof List) {
            List<Object> box = (List) value;
            for (int k = box.size() - 1; k >= 0; k--) {
                final Object o1 = box.get(k);
                final Object o2 = convertValue(o1);
                if (o1 != o2) box.set(k, o2);
            }
            return value;
        }
        if (value instanceof Collection) {
            return value;
        }
        if (value instanceof OffsetDateTime) {
            return ((OffsetDateTime) value).toInstant().toEpochMilli();
        }
        if (value instanceof Object[]) {
            final Object[] array = (Object[]) value;
            final ArrayList<Object> list = new ArrayList(array.length);
            for (Object item : array) {
                list.add(item);
            }
            return convertValueInternal(list);
        }
        return value.toString();
    }

    protected boolean beginningLine() {
        if (sql.isEmpty()) {
            return true;
        } else {
            final CharSequence lastItem = sql.get(sql.size() - 1);
            return lastItem instanceof SqlBuilder ? false : (lastItem.charAt(lastItem.length() - 1) == newLineCh);
        }
    }

    public String getQuery() {
        return sql.stream().collect(Collectors.joining());
    }

    protected void assertNotNull(Object data, String... message) throws IllegalArgumentException {
        if (data == null) {
            throw new IllegalArgumentException(String.join(" ", message));
        }
    }

    @NotNull
    public LoopingIterator<ResultSet> executeSelect(@NotNull final Connection connection) throws IllegalStateException, SQLException {
        return new RowIterator(prepareStatement(connection));
    }

    /**
     * Build the PreparedStatement with arguments
     */
    @NotNull
    public PreparedStatement prepareStatement(@NotNull final Connection connection) throws SQLException {
        final StringBuilder statement = new StringBuilder();
        final ArrayList<SqlParam> params = new ArrayList<>();
        for (int i = 0, max = sql.size(); i < max; i++) {
            final CharSequence item = sql.get(i);
            if (item instanceof SqlParam) {
                statement.append(paramMark);
                params.add((SqlParam) item);
            } else {
                statement.append(item);
            }
        }
        final PreparedStatement result = connection.prepareStatement(statement.toString());
        for (int i = 0, max = params.size(); i < max; ++i) {
            final SqlParam value = params.get(i);
            result.setObject(i + 1, value.value, value.jdbcType, value.scaleOrLength);
        }
        return result;
    }

    @Override
    @NotNull
    public String toString() {
        final StringBuilder result = new StringBuilder(256);
        final int max = 20;
        for (CharSequence item : sql) {
            if (item instanceof SqlParam) {
                final String val = String.valueOf(((SqlParam) item).value);
                result.append(" [").append(val.length() > max ? val.substring(max) + '…' : val).append(']');
            } else {
                result.append(item);
            }
        }
        return result.toString();
    }


    @FunctionalInterface
    public interface CheckedConsumer<T> extends Consumer<T> {
        void accept(T builder) throws IllegalArgumentException;
    }

    public static class SqlParam implements CharSequence {
        public static int DEFAULT_SCALE = -1;

        private final Object value;

        private final SQLType jdbcType;

        private final int scaleOrLength;

        public SqlParam(@Nullable Object value, @Nullable SQLType jdbcType, int scaleOrLength) {
            this.value = value;
            this.jdbcType = jdbcType != null ? jdbcType : findJdbc(value);
            this.scaleOrLength = scaleOrLength;
        }

        public SqlParam(@Nullable Object value, @Nullable SQLType jdbcType) {
            this(value, jdbcType, DEFAULT_SCALE);
        }

        public SqlParam(@Nullable Object value) {
            this(value, null);
        }

        private static JDBCType findJdbc(Object value) {
            if (value == null) {
                return JDBCType.NULL;
            }
            if (value instanceof String) {
                return JDBCType.NVARCHAR;
            }
            if (value instanceof Short) {
                return JDBCType.SMALLINT;
            }
            if (value instanceof BigInteger) {
                return JDBCType.BIGINT;
            }
            if (value instanceof BigDecimal) {
                return JDBCType.NUMERIC;
            }
            if (value instanceof Boolean) {
                return JDBCType.BOOLEAN;
            }
            return JDBCType.NVARCHAR;

        }

        // ---- CharSequence implementations ----

        @Override
        public int length() {
            return toString().length();
        }

        @Override
        public char charAt(int index) {
            return toString().charAt(index);
        }

        @NotNull
        @Override
        public CharSequence subSequence(int start, int end) {
            return toString().subSequence(start, end);
        }

        @Nullable
        @Override
        public String toString() {
            return value != null ? value.toString() : null;
        }
    }

}