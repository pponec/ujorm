/*
 * Copyright 2018-2022 Pavel Ponec
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

import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.ujorm.tools.Assert;
import org.ujorm.tools.jdbc.JdbcBuilder;

/**
 * PrepareStatement builder support
 *
 * <h4>How to use a SELECT</h4>
 * <pre class="pre">
 * SqlBuilder sql = <strong>new</strong> SqlBuilder()
 *     .select("t.id", "t.name")
 *     .from("testTable t")
 *     .where()
 *     .andCondition("t.name", "=", "Test")
 *     .andCondition("t.created", "&gt;=", someDate);
 * for (ResultSet rs : sql.executeSelect(dbConnection)) {
 *      int id = rs.getInt(1);
 *      String name = rs.getString(2);
 * }
 * </pre>
 *
 * <h4>How to use a INSERT</h4>
 * <pre class="pre">
 * SqlBuilder sql = <strong>new</strong> SqlBuilder()
 *     .insert("testTable")
 *     .write("(")
 *     .columnInsert("id", 10)
 *     .columnInsert("name", "Test")
 *     .columnInsert("date", someDate)
 *     .super.write(")");
 * sql.executeUpdate(dbConnection);
 * </pre>
 *
 * <h4>How to use a UPDATE</h4>
 * <pre class="pre">
 * SqlBuilder sql = <strong>new</strong> SqlBuilder()
 *     .update("testTable")
 *     .columnUpdate("name", "Test")
 *     .columnUpdate("date", SOME_DATE)
 *     .where()
 *     .andCondition("id", "IN", 10, 20, 30)
 *     .andCondition("created BETWEEN ? AND ?", <strong>null</strong>, someDate, someDate.plusMonths(1))
 *     .andCondition("name", "IS NOT NULL")
 * sql.executeUpdate(dbConnection);
 * </pre>
 * For more information see a <a target="_blank"
 * href="https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/test/java/org/ujorm/tools/jdbc/SqlBuilderTest.java#L33">jUnit</a> test.
 * @author Pavel Ponec
 * @deprecated Use the {@link SqlParamBuilder} class rather from the release 2.26.
 */
public class SqlBuilder extends JdbcBuilder {

    public SqlBuilder() {
    }

    public SqlBuilder(List<CharSequence> sql, List<Object> arguments) {
        super(sql, arguments);
    }

    /** Write SELECT columns */
    public SqlBuilder select(@NotNull final CharSequence ... columns) {
        super.write(Sql.SELECT);
        for (CharSequence column : columns) {
            super.column(column);
        }
        return this;
    }

    /** Write FORM tables */
    public SqlBuilder from(@NotNull final CharSequence ... tables) {
        super.write(Sql.FROM);
        for (CharSequence table : tables) {
            super.write(table);
        }
        return this;
    }

    /** Write WHERE */
    public SqlBuilder where() {
        super.write(Sql.WHERE);
        return this;
    }

    /** Write WHERE */
    public SqlBuilder insert(@NotNull final CharSequence table) {
        Assert.hasLength(table, "table");
        super.write(Sql.INSERT_INTO);
        super.write(table);
        return this;
    }

    /** Write WHERE */
    public SqlBuilder update(@NotNull final CharSequence table) {
        Assert.hasLength(table, "table");
        super.write(Sql.UPDATE);
        super.write(table);
        super.write(Sql.SET);
        return this;
    }

    /** Write WHERE */
    public SqlBuilder delete(@NotNull final CharSequence table) {
        Assert.hasLength(table, "table");
        super.write(Sql.DELETE);
        super.write(Sql.FROM);
        super.write(table);
        super.write(Sql.SET);
        return this;
    }

    // ----- original methods -----

    @Override
    public SqlBuilder addArguments(Object... values) {
        return (SqlBuilder) super.addArguments(values);
    }

    @Override
    protected SqlBuilder addValue(Object value) {
        return (SqlBuilder) super.addValue(value);
    }

    @Override
    public SqlBuilder value(Object value) {
        return (SqlBuilder) super.value(value);
    }

    @Override
    public SqlBuilder condition(CharSequence sqlCondition, String operator, Object value) {
        return (SqlBuilder) super.condition(sqlCondition, operator, value);
    }

    @Override
    public SqlBuilder orCondition(CharSequence sqlCondition, String operator, Object... values) {
        return (SqlBuilder) super.orCondition(sqlCondition, operator, values);
    }

    @Override
    public SqlBuilder orCondition(CharSequence sqlCondition, String operator, Object value) {
        return (SqlBuilder) super.orCondition(sqlCondition, operator, value);
    }

    @Override
    public SqlBuilder andCondition(CharSequence sqlCondition, String operator, Object... values) {
        return (SqlBuilder) super.andCondition(sqlCondition, operator, values);
    }

    @Override
    public SqlBuilder andCondition(CharSequence sqlCondition, String operator, Object value) {
        return (SqlBuilder) super.andCondition(sqlCondition, operator, value);
    }

    @Override
    public SqlBuilder columnInsert(CharSequence column, Object value) {
        return (SqlBuilder) super.columnInsert(column, value);
    }

    @Override
    public SqlBuilder columnUpdate(CharSequence column, Object value) {
        return (SqlBuilder) super.columnUpdate(column, value);
    }

    @Override
    public final SqlBuilder column(CharSequence column) {
        return (SqlBuilder) super.column(column);
    }

    @Override
    public final SqlBuilder writeManyNoSpace(CharSequence... sqlFragments) {
        return (SqlBuilder) super.writeManyNoSpace(sqlFragments);
    }

    @Override
    public final SqlBuilder writeMany(CharSequence... sqlFragments) {
        return (SqlBuilder) super.writeMany(sqlFragments);
    }

    @Override
    public final SqlBuilder writeNoSpace(CharSequence sqlFragment) {
        return (SqlBuilder) super.writeNoSpace(sqlFragment);
    }

    @Override
    public final SqlBuilder write(CharSequence sqlFragment) {
        return (SqlBuilder) super.write(sqlFragment);
    }

    public final SqlBuilder write(SqlBuilder builder) {
        return (SqlBuilder) super.write(builder);
    }




}
