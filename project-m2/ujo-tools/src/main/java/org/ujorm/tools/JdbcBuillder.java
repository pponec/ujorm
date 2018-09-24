/*
 *  Copyright 2018 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ujorm.tools;

import java.io.CharArrayWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * PrepareStatement builder support
 * @author Pavel Ponec
 * @sa.date 2015-12-08T09:03:39+0100
 */
public final class JdbcBuillder {

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

    protected boolean insertMode = false;

    /** Default constructor */
    public JdbcBuillder() {
        this(new StringBuilder(32));
    }

    /** StringBuilder construcor */
    public JdbcBuillder(@Nonnull final StringBuilder sql) {
        this.sql = sql;
    }

    /** If buffer is an empty, than the space is introduced */
    @Nonnull
    public JdbcBuillder write(@Nonnull final CharSequence sqlFragment) {
        if (emptySql) {
            emptySql = false;
        } else {
            sql.append(SPACE);
        }
        sql.append(sqlFragment);
        return this;
    }

    /** Write argument with no spaces */
    @Nonnull
    public JdbcBuillder rowWrite(@Nonnull final CharSequence sqlFragment) {
        if (emptySql) {
            emptySql = false;
        }
        sql.append(sqlFragment);
        return this;
    }


    /** Add new column */
    @Nonnull
    public JdbcBuillder column(@Nonnull final CharSequence column) {
        if (columnCounter++ > 0) {
            sql.append(ITEM_SEPARATOR);
        }
        sql.append(SPACE);
        sql.append(column);
        return this;
    }

    /** Set new value to column by template {@code name = ? */
    @Nonnull
    public JdbcBuillder columnUpdate(@Nonnull final CharSequence column, @Nonnull final Object value) {
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
    public JdbcBuillder columnInsert(@Nonnull final CharSequence column, @Nonnull final Object value) {
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
    public JdbcBuillder value(@Nonnull Object param) {
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
    public JdbcBuillder andCondition(@Nonnull CharSequence sqlCondition, @Nullable Object value) {
        return andCondition(sqlCondition, null, value);
    }

    /**
     * Add an equals condition for a valid <strong>argument</strong> joined by AND operator
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param operator An optional operator is followed by the {@link #VALUE_MARKER} automatically
     * @param value The value od the condition (a replacement for the question character)
     */
    @Nonnull
    public JdbcBuillder andCondition(@Nonnull CharSequence sqlCondition, @Nullable String operator, @Nullable Object value) {
        return condition(sqlCondition, operator, value, true);
    }

    /**
     * Add an condition for a valid <strong>argument</strong> joined by OR operator
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param value The value od the condition (a replacement for the question character)
     */
    @Nonnull
    public JdbcBuillder orCondition(@Nonnull CharSequence sqlCondition, @Nullable Object value) {
        return orCondition(sqlCondition, null, value);
    }

    /**
     * Add an equals condition for a valid <strong>argument</strong> joined by OR operator
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param operator An optional operator is followed by the {@link #VALUE_MARKER} automatically
     * @param value The value od the condition (a replacement for the question character)
     */
    @Nonnull
    public JdbcBuillder orCondition(@Nonnull CharSequence sqlCondition, @Nullable String operator, @Nullable Object value) {
        return condition(sqlCondition, operator, value, false);
    }

   /**
     * Add an condition for an <strong>argument</strong> with length
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param value The value od the condition (a replacement for the question character)
     */
    @Nonnull
    public JdbcBuillder condition(@Nonnull CharSequence sqlCondition, @Nullable Object value, final @Nullable Boolean andOperator) {
        return condition(sqlCondition, null, value, andOperator);
    }

    /**
     * Add an condition for an <strong>argument</strong> with length
     * @param sqlCondition A condition in the SQL format like the next: {@code "table.id = ?"}
     * @param operator An optional operator is followed by the {@link #VALUE_MARKER} automatically
     * @param value The value od the condition (a replacement for the question character)
     */
    @Nonnull
    public JdbcBuillder condition(@Nonnull CharSequence sqlCondition, @Nullable String operator, @Nullable Object value, final @Nullable Boolean andOperator) {
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
     * @return Pole argumentů */
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
    public <T> T uniqueValue(@Nonnull Class<T> resultType, @Nonnull final Connection connection) throws IllegalStateException {
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




