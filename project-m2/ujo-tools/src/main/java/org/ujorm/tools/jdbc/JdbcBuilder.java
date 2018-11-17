/*
 * Copyright 2018-2018 Pavel Ponec
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/jdbc/JdbcBuilder.java
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

import java.io.CharArrayWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.msg.ValuePrinter;
import org.ujorm.tools.set.LoopingIterator;

/**
 * PrepareStatement builder support
 *
 * <h3>How to use a SELECT</h3>
 * <pre class="pre">
 * JdbcBuilder sql = <strong>new</strong> JdbcBuilder()
 *     .write("SELECT")
 *     .column("t.id")
 *     .column("t.name")
 *     .write("FROM testTable t WHERE")
 *     .andCondition("t.name", "=", "Test")
 *     .andCondition("t.created", "&gt;=", someDate);
 * for (ResultSet rs : sql.executeSelect(dbConnection)) {
 *      int id = rs.getInt(1);
 *      String name = rs.getString(2);
 * }
 * </pre>
 *
 * <h3>How to use a INSERT</h3>
 * <pre class="pre">
 * JdbcBuilder sql = <strong>new</strong> JdbcBuilder()
 *     .write("INSERT INTO testTable (")
 *     .columnInsert("id", 10)
 *     .columnInsert("name", "Test")
 *     .columnInsert("date", someDate)
 *     .write(")");
 * sql.executeUpdate(dbConnection);
 * </pre>
 *
 * <h3>How to use a UPDATE</h3>
 * <pre class="pre">
 * JdbcBuilder sql = <strong>new</strong> JdbcBuilder()
 *     .write("UPDATE testTable SET")
 *     .columnUpdate("name", "Test")
 *     .columnUpdate("date", SOME_DATE)
 *     .write("WHERE")
 *     .andCondition("id", "IN", 10, 20, 30)
 *     .andCondition("created BETWEEN ? AND ?", <strong>null</strong>, someDate, someDate.plusMonths(1))
 *     .andCondition("name", "IS NOT NULL")
 * sql.executeUpdate(dbConnection);
 * </pre>
 * For more information see a <a target="_blank"
 * href="https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/test/java/org/ujorm/tools/jdbc/JdbcBuilderTest.java#L33">jUnit</a> test.
 * @author Pavel Ponec
 */
public final class JdbcBuilder implements Serializable {

    /** Separator of database columns */
    public static final SqlEnvelope ITEM_SEPARATOR = new SqlEnvelope(",");

    /** A value marker for SQL */
    protected static final String VALUE_MARKER = "?";

    /** A value marker for SQL */
    protected static final char SPACE = ' ';

    /** SQL string fragments */
    @Nonnull
    protected final List<CharSequence> sql;

    /** Argument list */
    @Nonnull
    protected final List<Object> arguments;

    /** Condition counter */
    protected int conditionCounter = 0;

    /** Column counter */
    protected int columnCounter = 0;

    /** An insert sign for different rendering */
    protected boolean insertMode = false;

    /** Default constructor */
    public JdbcBuilder() {
        this(new ArrayList<>(32), new ArrayList<>());
    }

    /** Default constructor */
    public JdbcBuilder(final @Nonnull List<CharSequence> sql, final @Nonnull List<Object> arguments) {
        this.sql = sql;
        this.arguments = arguments;
    }

    /** Add a another statement to the end of this statement. */
    @Nonnull
    public JdbcBuilder write(@Nonnull final JdbcBuilder builder) {
        this.sql.addAll(builder.sql);
        this.arguments.addAll(builder.arguments);

        return this;
    }

    /** Write a sql fragment including a space before
     * @param sqlFragment An empty or {@code null} value is ignored.
     */
    @Nonnull
    public JdbcBuilder write(@Nullable final CharSequence sqlFragment) {
        if (Check.hasLength(sqlFragment)) {
            sql.add(sqlFragment);
        }
        return this;
    }

    /** Write a sql fragment with no space before
     * @param sqlFragment An empty or null fragment is ignored.
     */
    @Nonnull
    public JdbcBuilder writeNoSpace(@Nonnull final CharSequence sqlFragment) {
        if (Check.hasLength(sqlFragment)) {
            sql.add(new SqlEnvelope(sqlFragment));
        }
        return this;
    }

    /** Write many sql fragments including a space before */
    @Nonnull
    public JdbcBuilder writeMany(@Nonnull CharSequence... sqlFragments) {
        for (CharSequence text : sqlFragments) {
            write(text);
        }
        return this;
    }

    /** Write many sql fragments with no space before */
    @Nonnull
    public JdbcBuilder writeManyNoSpace(@Nonnull final CharSequence ... sqlFragments) {
        for (CharSequence text : sqlFragments) {
            writeNoSpace(text);
        }
        return this;
    }

    /** Add new column */
    @Nonnull
    public JdbcBuilder column(@Nonnull final CharSequence column) {
        sql.add(new SqlEnvelope(column, columnCounter++));
        return this;
    }

    /** Set new value to column by template {@code name = ? */
    @Nonnull
    public JdbcBuilder columnUpdate(@Nonnull final CharSequence column, @Nonnull final Object value) {
        Assert.validState(!insertMode, "An insertion mode has been started.");
        sql.add(new SqlEnvelope(column, columnCounter++));
        sql.add("=");
        addValue(value);

        return this;
    }

    /** Set new value to column by template {@code name = ? */
    @Nonnull
    public JdbcBuilder columnInsert(@Nonnull final CharSequence column, @Nonnull final Object value) {
        insertMode = true;
        sql.add(new SqlEnvelope(column, columnCounter++));
        arguments.add(value);
        return this;
    }

    /**
     * Add a condition for a valid <strong>argument</strong> joined by AND operator
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param operator An optional operator is followed by the {@link #VALUE_MARKER} automatically
     * @param value Add the value to arguments including a markup to the SQL statement. To ignore the value, send a {@code null}.
     */
    @Nonnull
    public JdbcBuilder andCondition(@Nonnull final CharSequence sqlCondition, @Nullable final String operator, @Nullable final Object value) {
        writeOperator(true, conditionCounter++ > 0);
        return condition(sqlCondition, operator, value);
    }

    /**
     * Add a condition for a multivalue <strong>argument</strong> joined by AND operator
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param operator An optional operator is followed by the {@link #VALUE_MARKER} automatically
     * @param values The value of the condition (a replacement for the question character)
     */
    @Nonnull
    public JdbcBuilder andCondition(@Nonnull final CharSequence sqlCondition, @Nonnull final String operator, @Nullable final Object... values) {
        writeOperator(true, conditionCounter++ > 0);
        return Check.hasLength(values)
             ? condition(sqlCondition, operator, values)
             : condition(sqlCondition, operator, null);
    }

    /**
     * Add a condition for a valid <strong>argument</strong> joined by OR operator
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param operator An optional operator is followed by the {@link #VALUE_MARKER} automatically
     * @param value Add the value to arguments including a markup to the SQL statement. To ignore the value, send a {@code null}.
     */
    @Nonnull
    public JdbcBuilder orCondition(@Nonnull final CharSequence sqlCondition, @Nullable final String operator, @Nullable final Object value) {
        writeOperator(false, conditionCounter++ > 0);
        return condition(sqlCondition, operator, value);
    }

    /**
     * Add a condition for a multivalue <strong>argument</strong> joined by OR operator
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param operator An optional operator is followed by the {@link #VALUE_MARKER} automatically
     * @param values The value of the condition (a replacement for the question character)
     */
    @Nonnull
    public JdbcBuilder orCondition(@Nonnull final CharSequence sqlCondition, @Nonnull final String operator, @Nullable final Object... values) {
        writeOperator(false, conditionCounter++ > 0);
        return Check.hasLength(values)
             ? condition(sqlCondition, operator, values)
             : condition(sqlCondition, operator, null);
    }

    /** Add a condition for an <strong>argument</strong> with length
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}. Send a {@code null} value to ignore the method.
     * @param operator An optional operator is followed by the {@link #VALUE_MARKER} automatically
     * @param value Add a value to arguments including a markup to the SQL statement. To ignore the value, send a {@code null}. An array is supported
     */
    @Nonnull
    public JdbcBuilder condition(@Nullable final CharSequence sqlCondition, @Nullable final String operator, @Nonnull final Object value) {
        if (Check.hasLength(sqlCondition)) {
            final boolean multiValue = value instanceof Object[];
            final Object[] values =  multiValue ? (Object[]) value : new Object[]{value};
            if (Check.hasLength(operator)) {
                sql.add(sqlCondition);
                sql.add(operator);
                if (multiValue) {
                    sql.add("(");
                }
                for (int i = 0; i < values.length; i++) {
                    if (i > 0) {
                        sql.add(ITEM_SEPARATOR);
                    }
                    addValue(values[i]);
                }
                if (multiValue) {
                    sql.add(")");
                }
            } else {
                writeNoSpace(String.valueOf(SPACE));
                String cond = String.valueOf(sqlCondition);
                for (Object val : values) {
                    int i = cond.indexOf(VALUE_MARKER);
                    if (i >= 0) {
                        int i2 = i - (i > 0 && cond.charAt(i - 1 ) == SPACE ? 1 : 0); // Remove last space, if any
                        writeNoSpace(cond.subSequence(0, i2));
                        addValue(val);
                    } else {
                        sql.add(cond);
                    }
                    cond = cond.substring(i + VALUE_MARKER.length());
                }
                writeNoSpace(cond);
            }
        }
        return this;
    }

    /**
     *  Write an opetaror AND / OR
     * @param andOperator
     * @param enabled
     */
    protected void writeOperator(@Nullable final boolean andOperator, final boolean enabled) {
        if (enabled) {
            sql.add(andOperator ? "AND" : "OR");
        }
    }

    /** Add an argument value (including a SEPARATOR and a MARKER) for buidling a SQL INSERT statement
     * @see #addArguments(java.lang.Object...)
     */
    @Nonnull
    public JdbcBuilder value(@Nonnull final Object value) {
        if (value != null && !arguments.isEmpty()) {
                sql.add(ITEM_SEPARATOR);
        }
        return addValue(value);
    }

    /** Add a value to SQL (inlucing MARKER)
     * @param value A {@code null} value is ignored
     * @see #addArguments(java.lang.Object...)
     */
    @Nonnull
    protected JdbcBuilder addValue(@Nullable final Object value) {
        if (value != null) {
            sql.add(new MarkerEnvelope(value));
            arguments.add(value);
        }
        return this;
    }

    /** Add argument values with no SAPARATOR and no MARKER (for a common use)
     * @see #value(java.lang.Object)
     * @see #writeNoSpace(java.lang.CharSequence)
     */
    @Nonnull
    public JdbcBuilder addArguments(final @Nonnull Object ... values) {
        final Object[] vals = values.length == 1 && values[0] instanceof Object[] ? (Object[]) values[0] : values;
        for (int i = 0; i < vals.length; i++) {
            arguments.add(values[i]);
        }
        return this;
    }

    /** Returns an array of all JDBC arguments
     * @return Array of arguments */
    @Nonnull
    public Object[] getArguments() {
        return arguments.toArray(new Object[arguments.size()]);
    }

    /** Build the PreparedStatement with arguments */
    @Nonnull
    public PreparedStatement prepareStatement(@Nonnull final Connection connection) throws SQLException {
        final PreparedStatement result = connection.prepareStatement(getSql());
        for (int i = 0, max = arguments.size(); i < max; ++i) {
            result.setObject(i + 1, arguments.get(i));
        }
        return result;
    }

    /** Create an iterator ready to a <strong>loop</strong> statement {@code for ( ; ; )}
     * Supported SQL statements are: INSERT, UPDATE, DELETE .
     */
    public LoopingIterator<ResultSet> executeSelect(@Nonnull final Connection connection) throws IllegalStateException, SQLException {
        return new RowIterator(prepareStatement(connection));
    }
    /** Create statement and call {@link PreparedStatement.executeUpdate() }.
     * Supported SQL statements are: INSERT, UPDATE, DELETE .
     */
    public int executeUpdate(@Nonnull final Connection connection) throws IllegalStateException {
        try (PreparedStatement ps = prepareStatement(connection)) {
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(getSql(), e);
        }
    }

    /** Return the first column value of a unique resultset, else returns {@code null} value */
    public <T> T uniqueValue(@Nonnull Class<T> resultType, @Nonnull final Connection connection) {
        try (PreparedStatement ps = prepareStatement(connection); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                final T result = rs.getObject(1, resultType);
                if (rs.next()) {
                    throw new NoSuchElementException(getSql());
                }
                return result;
            }
            return null;
        } catch (SQLException | NoSuchElementException e) {
            throw new IllegalStateException(getSql(), e);
        }
    }

    /** Returns a SQL text */
    @Nonnull
    public String getSql(final boolean preview) {
        final CharArrayWriter result = new CharArrayWriter(getBufferSizeEstimation(preview));
        final ValuePrinter printer = preview ? createValuePrinter(result) : null;

        for (int i = 0, max = sql.size(); i < max; i++) {
            final CharSequence item = sql.get(i);
            if (item instanceof SqlEnvelope) {
                final SqlEnvelope env = (SqlEnvelope) item;
                if (env.isColumn()) {
                    if (env.getColumnOrder() > 0) {
                        result.append(ITEM_SEPARATOR);
                    }
                    result.append(SPACE);
                }
            } else if (i > 0) {
                result.append(SPACE);
            }
            if (printer != null && item instanceof MarkerEnvelope) {
                printer.appendValue(((MarkerEnvelope) item).getValue());
            } else {
                result.append(item);
            }
        }

        if (insertMode) {
            result.append(" VALUES (");
            for (int i = 0, max = arguments.size(); i < max; i++) {
                result.append(i > 0 ? ITEM_SEPARATOR : "").append(SPACE);
                if (printer != null) {
                    printer.appendValue(arguments.get(i));
                } else {
                    result.append(VALUE_MARKER);
                }
            }
            result.append(" )");
        }
        return result.toString();
    }

    /** Create a value printer */
    @Nonnull
    protected static ValuePrinter createValuePrinter(@Nonnull final CharArrayWriter result) {
        return new ValuePrinter(VALUE_MARKER,  "'",  result);
    }

    /** Estimate a buffer size in characters */
    protected int getBufferSizeEstimation(final boolean preview) {
        final int averageItemSize = 8;
        return (sql.size() + 2 - (insertMode ? 0 : arguments.size())) * averageItemSize + arguments.size() * (preview ? 10 : 3);
    }

    /** Returns a SQL statement */
    @Nonnull
    public String getSql() {
        return getSql(false);
    }

    /** Returns a SQL preview including values */
    @Nonnull
    public String toString() {
        return getSql(true);
    }

    // -------- Inner classes --------

    /** A value marker envelope */
    protected static class MarkerEnvelope extends ProxySequence {
        /** Value argumenent */
        @Nonnull
        private final Object value;

        protected MarkerEnvelope(@Nonnull final Object value) {
            super(VALUE_MARKER);
            this.value = value;
        }

        /** SQL argumenent */
        @Nonnull
        public Object getValue() {
            return value;
        }
    }

    /** A SQL fragment */
    protected static class SqlEnvelope extends ProxySequence {

        /** Is a column description */
        private final short columnOrder;

        /** Default constructor */
        protected SqlEnvelope(@Nonnull final CharSequence sql) {
            this(sql, (short) -1);
        }

        /**
         * Column constructor
         * @param sql
         * @param columnOrder The first position have got zero.
         */
        protected SqlEnvelope(@Nonnull final CharSequence sql, final int columnOrder) {
            super(sql);
            this.columnOrder = (short) columnOrder;
        }

        /** A column sign */
        public boolean isColumn() {
            return columnOrder >= 0;
        }

        public int getColumnOrder() {
            return columnOrder;
        }
    }
}
