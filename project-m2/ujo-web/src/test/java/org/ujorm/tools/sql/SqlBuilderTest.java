/*
 *  Copyright 2019-2019 Pavel Ponec
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

package org.ujorm.tools.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javax.annotation.Nonnull;
import org.junit.Test;
import org.ujorm.tools.jdbc.AbstractJdbcConnector;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Testing the SqlBuillder class
 * @author Pavel Ponec
 */
public class SqlBuilderTest extends AbstractJdbcConnector {

     /** Some testing date */
    private final LocalDate someDate = LocalDate.parse("2018-09-12");

    /** Test SQL SELECT of class JdbcBuillder. */
    @Test
    public void testSelect() {
        System.out.println("SELECT");
        SqlBuilder sql = new SqlBuilder()
            .select("t.id", "t.name")
            .from("testTable t")
            .where()
            .andCondition("t.name", Sql.EQ, "Test")
            .andCondition("t.created", Sql.GE, someDate)
            ;
        String expResult1 = "SELECT t.id, t.name FROM testTable t WHERE t.name = ? AND t.created >= ?";
        String expResult2 = "SELECT t.id, t.name FROM testTable t WHERE t.name = 'Test' AND t.created >= 2018-09-12";

        assertEquals(expResult1, sql.getSql());
        assertEquals(expResult2, sql.toString());
        assertEquals(2, sql.getArguments().length);
        assertEquals("Test", sql.getArguments()[0]);
    }

    /** Test SQL INSERT of class JdbcBuillder. */
    @Test
    public void testInsert() {
        System.out.println("INSERT");
        SqlBuilder sql = new SqlBuilder()
            .insert("testTable")
            .write("(")
            .columnInsert("id", 10)
            .columnInsert("name", "A name")
            .columnInsert("created", someDate)
            .write(")")
            ;
        String expResult1 = "INSERT INTO testTable ( id, name, created ) VALUES ( ?, ?, ? )";
        String expResult2 = "INSERT INTO testTable ( id, name, created ) VALUES ( 10, 'A name', 2018-09-12 )";

        assertEquals(expResult1, sql.getSql());
        assertEquals(expResult2, sql.toString());
        assertEquals(3, sql.getArguments().length);
        assertEquals(10, sql.getArguments()[0]);
    }

    /** Test SQL INSERT By SELECT */
    @Test
    public void testInsertBySelect() {
        System.out.println("INSERT by SELECT");
        SqlBuilder select = new SqlBuilder()
            .select("t.id + 100", "t.name", "t.created")
            .from("testTable t")
            .where()
            .andCondition("t.id", Sql.LT, 100)
            ;
        SqlBuilder sql = new SqlBuilder()
            .insert("testTable")
            .write("(")
            .column("id")
            .column("name")
            .column("created")
            .write(")")
            .write(select)
            ;
        String expResult1 = "INSERT INTO testTable ( id, name, created ) SELECT t.id + 100, t.name, t.created FROM testTable t WHERE t.id < ?";
        String expResult2 = "INSERT INTO testTable ( id, name, created ) SELECT t.id + 100, t.name, t.created FROM testTable t WHERE t.id < 100";

        assertEquals(expResult1, sql.getSql());
        assertEquals(expResult2, sql.toString());
        assertEquals(1, sql.getArguments().length);
    }

    /** Test raw SQL INSERT of class JdbcBuillder for a better performace and general use. */
    @Test @Deprecated
    public void testInsertRaw() {
        System.out.println("INSERT");
        SqlBuilder sql = new SqlBuilder()
            .insert("testTable")
            .write("(")
            .column("id")
            .column("name")
            .column("created")
            .write(")")
            .write(Sql.VALUES)
            .write("(")
            .value(10)
            .value("A test")
            .value(someDate)
            .write(")");
            ;
        String expResult1 = "INSERT INTO testTable ( id, name, created ) VALUES ( ?, ?, ? )";
        String expResult2 = "INSERT INTO testTable ( id, name, created ) VALUES ( 10, 'A test', 2018-09-12 )";

        assertEquals(expResult1, sql.getSql());
        assertEquals(expResult2, sql.toString());
        assertEquals(3, sql.getArguments().length);
        assertEquals(10, sql.getArguments()[0]);
    }

    /** Test SQL UPDATE of class JdbcBuillder. */
    @Test
    public void testUpdate() {
        System.out.println("UPDATE");
        SqlBuilder sql = new SqlBuilder()
            .update("testTable")
            .columnUpdate("name", "Test")
            .columnUpdate("created", someDate)
            .where()
            .andCondition("id", Sql.IN, 10, 20, 30)
            .andCondition("created BETWEEN ? AND ?", Sql.UNDEFINED, someDate, someDate.plusMonths(1))
            .andCondition("name", Sql.IS_NOT_NULL)
            ;
        String expResult1 = "UPDATE testTable"
                + " SET name = ?"
                +    ", created = ?"
                + " WHERE id IN ( ?, ?, ? )"
                + " AND created BETWEEN ? AND ?"
                + " AND name IS NOT NULL";
        String expResult2 = "UPDATE testTable"
                + " SET name = 'Test'"
                +    ", created = 2018-09-12"
                + " WHERE id IN ( 10, 20, 30 )"
                + " AND created BETWEEN 2018-09-12 AND 2018-10-12"
                + " AND name IS NOT NULL";

        assertEquals(expResult1, sql.getSql());
        assertEquals(expResult2, sql.toString());
        assertEquals(7, sql.getArguments().length);
        assertEquals("Test", sql.getArguments()[0]);
        assertEquals(someDate, sql.getArguments()[1]);
        assertEquals(10, sql.getArguments()[2]);
        assertEquals(20, sql.getArguments()[3]);
    }

    /** Test SQL UPDATE of class JdbcBuillder. */
    @Test
    public void testUpdateBySelect() {
        System.out.println("UPDATE by SELECT");

        SqlBuilder select = new SqlBuilder()
            .select("t.id")
            .from("testTable t")
            .where()
            .andCondition("t.created", Sql.GE, someDate)
            ;
        SqlBuilder sql = new SqlBuilder()
            .update("testTable")
            .columnUpdate("name", "Test")
            .columnUpdate("created", someDate.plusDays(11))
            .where()
            .writeMany("id", Sql.IN, "(")
            .write(select)
            .write(")")
            ;

        String expResult1 = "UPDATE testTable"
                + " SET name = ?"
                +    ", created = ?"
                + " WHERE id IN ("
                + " SELECT t.id FROM testTable t WHERE t.created >= ?"
                + " )"
                ;
        String expResult2 = "UPDATE testTable"
                + " SET name = 'Test'"
                +    ", created = 2018-09-23"
                + " WHERE id IN ("
                + " SELECT t.id FROM testTable t WHERE t.created >= 2018-09-12"
                + " )"
                ;
        assertEquals(expResult1, sql.getSql());
        assertEquals(expResult2, sql.toString());
        assertEquals(3, sql.getArguments().length);
    }

    @Test
    public void testShowUsage() throws ClassNotFoundException, SQLException {
        try (Connection dbConnection = createTable(createDbConnection()))  {
            showInsert(dbConnection);
            showSelect_1(dbConnection);
            showSelect_2(dbConnection);
            showSelectForSingleValue(dbConnection);
            showUpdate(dbConnection);
        }
    }

    /** How to INSERT (no commit) */
    public void showInsert(@Nonnull Connection dbConnection) throws SQLException {
        System.out.println("Show INSERT");
        SqlBuilder sql = new SqlBuilder()
            .insert("testTable")
            .write("(")
            .columnInsert("id", 10)
            .columnInsert("name", "A name")
            .columnInsert("created", someDate)
            .write(")")
            ;
        int count = sql.executeUpdate(dbConnection);

        dbConnection.commit();
        assertEquals(1, count);
    }

    /** How to SELECT a list */
    public void showSelect_1(@Nonnull Connection dbConnection) throws IllegalStateException, SQLException {
        System.out.println("Show SELECT");
        SqlBuilder sql = new SqlBuilder()
            .select("t.id", "t.name")
            .from("testTable t")
            .where()
            .andCondition("t.name", Sql.EQ, "A name")
            .andCondition("t.created", Sql.LE, someDate)
            ;
        ResultSet tempRs = null;
        for (ResultSet rs : sql.executeSelect(dbConnection)) {
            int id = rs.getInt(1);
            String name = rs.getString(2);

            assertEquals(10, id);
            assertEquals("A name", name);
            tempRs = rs;
        }
        assertNotNull(tempRs);
        assertTrue(tempRs.isClosed());
    }

   /** How to SELECT a list using a lambda expression */
    public void showSelect_2(@Nonnull Connection dbConnection) throws IllegalStateException, SQLException {
        System.out.println("Show SELECT");
        SqlBuilder sql = new SqlBuilder()
            .select("t.id", "t.name")
            .from("testTable t")
            .where()
            .andCondition("t.name", Sql.EQ, "A name")
            .andCondition("t.created", Sql.LE, someDate)
            ;
        List<Integer> ids = sql.executeSelect(dbConnection, (rs) -> rs.getInt(1));

        assertNotNull(ids);
        assertTrue(!ids.isEmpty());
    }

    /** How to SELECT single value */
    public void showSelectForSingleValue(@Nonnull Connection dbConnection) throws IllegalStateException, SQLException {
        System.out.println("Show SELECT");
        SqlBuilder sql = new SqlBuilder()
            .select("t.name")
            .from("testTable t")
            .where()
            .andCondition("t.id", Sql.EQ, 10)
            ;
        String name = sql.uniqueValue(String.class, dbConnection);
        assertEquals("A name", name);
    }

    /** How to UPDATE single value (no commit) */
    public void showUpdate(@Nonnull Connection dbConnection) {
        System.out.println("Show UPDATE");
        SqlBuilder sql = new SqlBuilder()
            .update("testTable")
            .columnUpdate("created", someDate.plusDays(1))
            .where()
            .andCondition("id", Sql.IN, 10, 20, 30)
            .andCondition("created" + Sql.BETWEEN_X_AND_Y, null, someDate, someDate.plusMonths(1))
            .andCondition("name", Sql.IS_NOT_NULL)
            ;
        int count = sql.executeUpdate(dbConnection);
        assertEquals(1, count);
    }

    /** Crete new DB connection */
    private Connection createTable(Connection dbConnection) throws ClassNotFoundException, SQLException {
        String sql = "CREATE TABLE testTable"
            + "\n( id INTEGER PRIMARY KEY"
            + "\n, name VARCHAR(256)"
            + "\n, xxx VARCHAR(1)" // An unused column
            + "\n, created TIMESTAMP"
            + "\n)";

        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.execute();
            dbConnection.commit();
        }
        return dbConnection;
    }
}
