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
import org.ujorm.tools.msg.SimpleValuePrinter;
import org.ujorm.tools.set.LoopingIterator;

/**
 * PrepareStatement builder support
 *
 * <h3>How to use a SELECT</h3>
 * <pre class="pre">
 * JdbcBuilder sql = new JdbcBuilder()
 *     .write("SELECT")
 *     .column("t.id")
 *     .column("t.name")
 *     .write("FROM testTable t WHERE")
 *     .write("WHERE")
 *     .andCondition("t.name", "=", "Test")
 *     .andCondition("t.date", "&gt;=", SOME_DATE);
 *
 * for (ResultSet rs : sql.executeSelect(dbConnection)) {
 *      int id = rs.getInt(1);
 *      String name = rs.getString(2);
 * }
 * </pre>
 *
 * <h3>How to use a INSERT</h3>
 * <pre class="pre">
 * JdbcBuilder sql = new JdbcBuilder()
 *     .write("INSERT INTO testTable (")
 *     .columnInsert("id", 10)
 *     .columnInsert("name", "Test")
 *     .columnInsert("date", SOME_DATE)
 *     .write(")");
 * sql.executeUpdate(dbConnection);
 * </pre>
 *
 * <h3>How to use a UPDATE</h3>
 * <pre class="pre">
 * JdbcBuilder sql = new JdbcBuilder()
 *     .write("UPDATE testTable SET")
 *     .columnUpdate("name", "Test")
 *     .columnUpdate("date", SOME_DATE)
 *     .write("WHERE")
 *     .andCondition("id", "&gt;", 10)
 *     .andCondition("id", "&lt;", 20);
 * sql.executeUpdate(dbConnection);
 * </pre>
 * For more information see a <a target="_blank"
 * href="https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/test/java/org/ujorm/tools/jdbc/JdbcBuilderTest.java#L33">jUnit</a> test.
 * @author Pavel Ponec
 */
public final class JdbcBuilder implements Serializable {

    /** Separator of database columns */
    public static final Envelope ITEM_SEPARATOR = new Envelope(",");

    /** A value marker for SQL */
    protected static final String VALUE_MARKER = "?";

    /** A value marker for SQL */
    protected static final char SPACE = ' ';

    /** SQL string fragments */
    @Nonnull
    protected final List<CharSequence> sql;

    /** Argument list */
    protected final List<Object> arguments;

    /** Condition counter */
    protected int conditionCounter = 0;

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

    /** Concatenates the specified statement to the end of this statement. */
    @Nonnull
    public JdbcBuilder concat(@Nonnull final JdbcBuilder builder) {
        this.sql.addAll(builder.sql);
        this.arguments.addAll(builder.arguments);
        this.conditionCounter += builder.conditionCounter;

        return this;
    }

    /** Write a sql fragment */
    @Nonnull
    public JdbcBuilder write(@Nullable final CharSequence sqlFragment) {
        if (Check.hasLength(sqlFragment)) {
            sql.add(sqlFragment);
        }
        return this;
    }

    /** If buffer is an empty, than the space is introduced */
    @Nonnull
    public JdbcBuilder rawWrite(@Nonnull final CharSequence sqlFragment) {
        if (Check.hasLength(sqlFragment)) {
            sql.add(new Envelope(sqlFragment));
        }
        return this;
    }

    /** If buffer is an empty, than the space is introduced */
    @Nonnull
    public JdbcBuilder writeMany(@Nonnull CharSequence... sqlFragments) {
        for (CharSequence text : sqlFragments) {
            write(text);
        }
        return this;
    }

    /** Write argument with no space */
    @Nonnull
    public JdbcBuilder rawWriteMany(@Nonnull final CharSequence ... sqlFragments) {
        for (CharSequence text : sqlFragments) {
            rawWrite(text);
        }
        return this;
    }

    /** Add new column */
    @Nonnull
    public JdbcBuilder column(@Nonnull final CharSequence column) {
        sql.add(new Envelope(column, true));
        return this;
    }

    /** Set new value to column by template {@code name = ? */
    @Nonnull
    public JdbcBuilder columnUpdate(@Nonnull final CharSequence column, @Nonnull final Object value) {
        Assert.validState(!insertMode, "An insertion mode has been started.");
        sql.add(new Envelope(column, true));
        sql.add("=");
        sql.add(VALUE_MARKER);

        arguments.add(value);
        return this;
    }

    /** Set new value to column by template {@code name = ? */
    @Nonnull
    public JdbcBuilder columnInsert(@Nonnull final CharSequence column, @Nonnull final Object value) {
        insertMode = true;
        sql.add(new Envelope(column, true));
        arguments.add(value);
        return this;
    }

    /** Add a condition for a valid <strong>argument</strong> joined with AND operator
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param value Add the value to arguments including a markup to the SQL statement. To ignore the value, send a {@code null}.
     */
    @Nonnull
    public JdbcBuilder andCondition(@Nonnull final CharSequence sqlCondition, @Nullable final Object value) {
        return andCondition(sqlCondition, null, value);
    }

    /**
     * Add a condition for a valid <strong>argument</strong> joined by AND operator
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param operator An optional operator is followed by the {@link #VALUE_MARKER} automatically
     * @param value Add the value to arguments including a markup to the SQL statement. To ignore the value, send a {@code null}.
     */
    @Nonnull
    public JdbcBuilder andCondition(@Nonnull final CharSequence sqlCondition, @Nullable final String operator, @Nullable final Object value) {
        return condition(true, sqlCondition, operator, value);
    }

    /**
     * Add a condition for a multivalue <strong>argument</strong> joined by AND operator
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param operator An optional operator is followed by the {@link #VALUE_MARKER} automatically
     * @param values The value of the condition (a replacement for the question character)
     */
    @Nonnull
    public JdbcBuilder andCondition(@Nonnull final CharSequence sqlCondition, @Nonnull final String operator, @Nullable final Object... values) {
        return condition(true, sqlCondition, operator, values);
    }

    /**
     * Add a condition for a valid <strong>argument</strong> joined by OR operator
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param value Add the value to arguments including a markup to the SQL statement. To ignore the value, send a {@code null}.
     */
    @Nonnull
    public JdbcBuilder orCondition(@Nonnull final CharSequence sqlCondition, @Nullable final Object value) {
        return orCondition(sqlCondition, null, value);
    }

    /**
     * Add a condition for a valid <strong>argument</strong> joined by OR operator
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param operator An optional operator is followed by the {@link #VALUE_MARKER} automatically
     * @param value Add the value to arguments including a markup to the SQL statement. To ignore the value, send a {@code null}.
     */
    @Nonnull
    public JdbcBuilder orCondition(@Nonnull final CharSequence sqlCondition, @Nullable final String operator, @Nullable final Object value) {
        return condition(false, sqlCondition, operator, value);
    }

    /**
     * Add a condition for a multivalue <strong>argument</strong> joined by OR operator
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param operator An optional operator is followed by the {@link #VALUE_MARKER} automatically
     * @param values The value of the condition (a replacement for the question character)
     */
    @Nonnull
    public JdbcBuilder orCondition(@Nonnull final CharSequence sqlCondition, @Nonnull final String operator, @Nullable final Object... values) {
        return condition(false, sqlCondition, operator, values);
    }

   /**
     * Add a condition for an <strong>argument</strong> with length
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}. To ignore the method, send a {@code null} value.
     * @param value Add the value to arguments including a markup to the SQL statement. To ignore the value, send a {@code null}.
     */
    @Nonnull
    public JdbcBuilder condition(@Nonnull final CharSequence sqlCondition, @Nullable final Object value, final @Nullable Boolean andOperator) {
        return condition(andOperator, sqlCondition, null, value);
    }

    /** Add a condition for an <strong>argument</strong> with length
     * @param andOperator Print a join operator, or nothing for {@code null} value
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}. To ignore the method, send a {@code null} value.
     * @param operator An optional operator is followed by the {@link #VALUE_MARKER} automatically
     * @param value Add the value to arguments including a markup to the SQL statement. To ignore the value, send a {@code null}.
     */
    @Nonnull
    public JdbcBuilder condition(@Nullable final Boolean andOperator, @Nonnull final CharSequence sqlCondition, @Nullable final String operator, @Nullable final Object value) {
        if (Check.hasLength(sqlCondition)) {
            writeOperator(andOperator, conditionCounter++ > 0);
            sql.add(sqlCondition);
            if (Check.hasLength(operator)) {
                sql.add(operator);
                if (value != null) {
                    sql.add(VALUE_MARKER);
                }
            }
            if (value != null) {
               arguments.add(value);
            }
        }
        return this;
    }

    /**
     *  Write an opetaror AND / OR
     * @param andOperator
     * @param enabled
     */
    protected void writeOperator(@Nullable final Boolean andOperator, final boolean enabled) {
        if (andOperator != null && enabled) {
            sql.add(andOperator ? "AND" : "OR");
        }
    }

    /**
     * Add a condition for an <strong>argument</strong> with length
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param operator An optional operator is followed by the {@link #VALUE_MARKER} automatically
     * @param values Values of the condition (a replacement for the question character). To ignore the method, send a {@code null} value.
     */
    @Nonnull
    public JdbcBuilder condition(@Nullable final Boolean andOperator, @Nonnull final CharSequence sqlCondition, @Nonnull final String operator, @Nullable Object... values) {
        if (Check.hasLength(sqlCondition) && Check.hasLength(values)) {
            assert values != null;
            writeOperator(andOperator, conditionCounter++ > 0);
            sql.add(sqlCondition);
            sql.add(operator);
            sql.add("(");
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    sql.add(ITEM_SEPARATOR);
                }
                sql.add(VALUE_MARKER);
                arguments.add(values[i]);
            }
            sql.add(")");
        }
        return this;
    }

    /** Add an argument value (including a SEPARATOR and a MARKER) for buidling a SQL INSERT statement
     * @see #addArguments(java.lang.Object...)
     */
    @Nonnull
    public JdbcBuilder value(@Nonnull final Object param) {
        if (!arguments.isEmpty()) {
            sql.add(ITEM_SEPARATOR);
        }
        sql.add(VALUE_MARKER);
        arguments.add(param);
        return this;
    }

    /** Add argument values with no SAPARATOR and no MARKER (for a common use)
     * @see #value(java.lang.Object)
     * @see #rawWrite(java.lang.CharSequence)
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
    public String getSql() {
        final StringBuilder result = new StringBuilder(getBufferSizeEstimation());
        int columnCounter = 0;

        for (int i = 0, max = sql.size(); i < max; i++) {
            final CharSequence item = sql.get(i);
            if (item instanceof Envelope) {
                if (((Envelope) item).isColumn()) {
                    if (columnCounter++ > 0) {
                        result.append(ITEM_SEPARATOR);
                    }
                    result.append(SPACE);
                }
            } else if (i > 0) {
                result.append(SPACE);
            }
            result.append(item);
        }

        if (insertMode) {
            result.append(" VALUES (");
            for (int i = 0, max = arguments.size(); i < max; i++) {
                result.append(i > 0 ? ITEM_SEPARATOR : "")
                      .append(SPACE)
                      .append(VALUE_MARKER);
            }
            result.append(" )");
        }
        return result.toString();
    }

    /** Estimate a buffer size in characters */
    protected int getBufferSizeEstimation() {
        final int averageItemSize = 8;
        return (sql.size() + 2 - (insertMode ? 0 : arguments.size())) * averageItemSize + arguments.size() * 3;
    }

    /** Returns a SQL including values */
    @Override @Nonnull
    public String toString() {
        return new SimpleValuePrinter
            ( String.valueOf(VALUE_MARKER)
            , "'"
            , new CharArrayWriter(64))
            .formatMsg(getSql(), getArguments());
    }

    // -------- Inner class --------

    /** Raw XML code envelope */
    protected static final class Envelope implements CharSequence {
        /** SQL content */
        private final CharSequence body;

        private final boolean column;

        public Envelope(@Nonnull final CharSequence body) {
            this(body, false);
        }

        public Envelope(@Nonnull final CharSequence body, final boolean column) {
            this.body = body;
            this.column = column;
        }

        /** A column sign */
        public boolean isColumn() {
            return column;
        }

        @Override
        public int length() {
            return body.length();
        }

        @Override
        public char charAt(final int index) {
            return body.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return body.subSequence(start, end);
        }

        @Override
        public String toString() {
            return body.toString();
        }
    }
}

