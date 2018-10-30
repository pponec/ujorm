/*
 * Copyright 2018 Pavel Ponec
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
    protected static final char ITEM_SEPARATOR = ',';

    /** A value marker for SQL */
    protected static final char VALUE_MARKER = '?';

    /** A value marker for SQL */
    protected static final char SPACE = ' ';

    /** Opening SQL string */
    @Nonnull
    protected final StringBuilder sql;

    /** Seznam argumentů */
    protected final List<Object> arguments = new ArrayList<>();

    /** The SQL buffer has assigned an character */
    protected boolean emptySql = true;

    /** Column counter */
    protected int columnCounter = 0;

    /** Condition counter */
    protected int conditionCounter = 0;

    /** An insert sign for different rendering */
    protected boolean insertMode = false;

    /** Default constructor */
    public JdbcBuilder() {
        this(new StringBuilder(32));
    }

    /** StringBuilder construcor */
    public JdbcBuilder(@Nonnull final StringBuilder sql) {
        this.sql = sql;
    }

    /** Concatenates the specified statement to the end of this statement. */
    @Nonnull
    public JdbcBuilder concat(@Nonnull final JdbcBuilder builder) {
        this.sql.append(builder.getSql());
        this.arguments.add(builder.arguments);
        this.columnCounter += builder.columnCounter;
        this.conditionCounter += builder.conditionCounter;
        this.emptySql = false;

        return this;
    }

    /** If buffer is an empty, than the space is introduced */
    @Nonnull
    public JdbcBuilder write(@Nonnull final CharSequence sqlFragment) {
        if (emptySql) {
            emptySql = false;
        } else {
            sql.append(SPACE);
        }
        sql.append(sqlFragment);
        return this;
    }

    /** If buffer is an empty, than the space is introduced */
    @Nonnull
    public JdbcBuilder rawWrite(@Nonnull final CharSequence sqlFragment) {
        if (emptySql) {
            emptySql = false;
        } else {
            sql.append(SPACE);
        }
        sql.append(sqlFragment);
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
        if (columnCounter++ > 0) {
            sql.append(ITEM_SEPARATOR);
        }
        sql.append(SPACE);
        sql.append(column);
        return this;
    }

    /** Set new value to column by template {@code name = ? */
    @Nonnull
    public JdbcBuilder columnUpdate(@Nonnull final CharSequence column, @Nonnull final Object value) {
        Assert.validState(!insertMode, "The insertion mode has been started.");
        if (!arguments.isEmpty()) {
            sql.append(ITEM_SEPARATOR);
        }
        sql.append(SPACE)
           .append(column)
           .append(SPACE)
           .append('=')
           .append(SPACE)
           .append(VALUE_MARKER);

        arguments.add(value);
        return this;
    }

    /** Set new value to column by template {@code name = ? */
    @Nonnull
    public JdbcBuilder columnInsert(@Nonnull final CharSequence column, @Nonnull final Object value) {
        insertMode = true;
        if (!arguments.isEmpty()) {
            sql.append(ITEM_SEPARATOR);
        }
        sql.append(SPACE).append(column);
        arguments.add(value);
        return this;
    }

    /** Add new value */
    @Nonnull
    public JdbcBuilder value(@Nonnull Object param) {
        if (!arguments.isEmpty()) {
            sql.append(ITEM_SEPARATOR);
        }
        sql.append(SPACE);
        sql.append(VALUE_MARKER);
        arguments.add(param);
        return this;
    }

    /**
     * Add an condition for a valid <strong>argument</strong> joined by AND operator
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param value The value od the condition (a replacement for the question character)
     */
    @Nonnull
    public JdbcBuilder andCondition(@Nonnull CharSequence sqlCondition, @Nullable Object value) {
        return andCondition(sqlCondition, null, value);
    }

    /**
     * Add an equals condition for a valid <strong>argument</strong> joined by AND operator
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param operator An optional operator is followed by the {@link #VALUE_MARKER} automatically
     * @param value The value od the condition (a replacement for the question character)
     */
    @Nonnull
    public JdbcBuilder andCondition(@Nonnull CharSequence sqlCondition, @Nullable String operator, @Nullable Object value) {
        return condition(sqlCondition, operator, value, true);
    }

    /**
     * Add an condition for a valid <strong>argument</strong> joined by OR operator
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param value The value od the condition (a replacement for the question character)
     */
    @Nonnull
    public JdbcBuilder orCondition(@Nonnull CharSequence sqlCondition, @Nullable Object value) {
        return orCondition(sqlCondition, null, value);
    }

    /**
     * Add an equals condition for a valid <strong>argument</strong> joined by OR operator
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param operator An optional operator is followed by the {@link #VALUE_MARKER} automatically
     * @param value The value od the condition (a replacement for the question character)
     */
    @Nonnull
    public JdbcBuilder orCondition(@Nonnull CharSequence sqlCondition, @Nullable String operator, @Nullable Object value) {
        return condition(sqlCondition, operator, value, false);
    }

   /**
     * Add an condition for an <strong>argument</strong> with length
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param value The value od the condition (a replacement for the question character)
     */
    @Nonnull
    public JdbcBuilder condition(@Nonnull CharSequence sqlCondition, @Nullable Object value, final @Nullable Boolean andOperator) {
        return condition(sqlCondition, null, value, andOperator);
    }

    /**
     * Add an condition for an <strong>argument</strong> with length
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param operator An optional operator is followed by the {@link #VALUE_MARKER} automatically
     * @param value The value od the condition (a replacement for the question character)
     */
    @Nonnull
    public JdbcBuilder condition(@Nonnull CharSequence sqlCondition, @Nullable String operator, @Nullable Object value, final @Nullable Boolean andOperator) {
        if (Check.hasLength(sqlCondition)) {
            if (conditionCounter++ > 0 && andOperator != null) {
                sql.append(andOperator ? " AND " : " OR ");
            } else {
                sql.append(SPACE);
            }
            sql.append(sqlCondition);
            if (Check.hasLength(operator)) {
                sql.append(SPACE);
                sql.append(operator);
                sql.append(SPACE);
                sql.append(VALUE_MARKER);
            }
            arguments.add(value);
        }
        return this;
    }

    /** Array of JDBC argumets
     * @return Array of arguments */
    @Nonnull
    public Object[] getArguments() {
        return arguments.toArray(new Object[arguments.size()]);
    }

    /** Add raw arguments for special use
     * @see #rawWrite(java.lang.CharSequence)
     */
    @Nonnull
    public JdbcBuilder rawArguments(final @Nonnull Object ... values) {
        final Object[] vals = values.length == 1 && values[0] instanceof Object[] ? (Object[]) values[0] : values;
        for (int i = 0; i < vals.length; i++) {
            arguments.add(values[i]);
        }
        return this;
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
        if (insertMode) {
            final String valuesBeg = " VALUES (";
            final String valuesEnd = " )";
            final StringBuilder result = new StringBuilder(sql.length() + valuesBeg.length() + arguments.size() * 3 + valuesEnd.length());
            result.append(sql);
            result.append(valuesBeg);
            for (int i = 0, max = arguments.size(); i < max; i++) {
                result.append(i > 0 ? "" + ITEM_SEPARATOR : "").append(SPACE).append(VALUE_MARKER);
            }
            result.append(valuesEnd);
            return result.toString();
        }
        else {
            return sql.toString();
        }
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

}




