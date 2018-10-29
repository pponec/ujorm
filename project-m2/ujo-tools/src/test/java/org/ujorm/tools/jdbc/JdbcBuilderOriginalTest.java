/*
 *  Copyright 2018-2018 Pavel Ponec
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

package org.ujorm.tools.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.annotation.Nonnull;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testing the JdbcBuillder class
 * @author Pavel Ponec
 */
public class JdbcBuilderOriginalTest {

    /** Some testing date */
    private final LocalDate someDate = LocalDate.parse("2018-09-12");

    /** Test SQL SELECT of class JdbcBuillder. */
    @Test
    public void testSelect() {
        System.out.println("SELECT");
        JdbcBuilder sql = new JdbcBuilder()
            .write("SELECT")
            .column("t.id")
            .column("t.name")
            .write("FROM testTable t WHERE")
            .andCondition("t.name", "=", "Test")
            .andCondition("t.created", ">=", someDate)
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
        JdbcBuilder sql = new JdbcBuilder()
            .write("INSERT INTO testTable (")
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

    /** Test raw SQL INSERT of class JdbcBuillder for a better performace and general use. */
    @Test
    public void testInsertRaw() {
        System.out.println("INSERT");
        JdbcBuilder sql = new JdbcBuilder()
            .write("INSERT INTO testTable (")
            .column("id")
            .column("name")
            .column("created")
            .write(") VALUES (")
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
        JdbcBuilder sql = new JdbcBuilder()
            .write("UPDATE testTable SET")
            .columnUpdate("name", "Test")
            .columnUpdate("created", someDate)
            .write("WHERE")
            .andCondition("id", ">", 10)
            .andCondition("id", "<", 20)
            ;
        String expResult1 = "UPDATE testTable SET name = ?, created = ? WHERE id > ? AND id < ?";
        String expResult2 = "UPDATE testTable SET name = 'Test', created = 2018-09-12 WHERE id > 10 AND id < 20";

        assertEquals(expResult1, sql.getSql());
        assertEquals(expResult2, sql.toString());
        assertEquals(4, sql.getArguments().length);
        assertEquals("Test", sql.getArguments()[0]);
        assertEquals(someDate, sql.getArguments()[1]);
        assertEquals(10, sql.getArguments()[2]);
        assertEquals(20, sql.getArguments()[3]);
    }

    @Test
    public void testShowUsage() throws ClassNotFoundException, SQLException {
        try (Connection dbConnection = createTable(createDbConnection()))  {
            showInsert(dbConnection);
            showSelect(dbConnection);
            showSelectForSingleValue(dbConnection);
            showUpdate(dbConnection);
        }
    }

    /** How to UPDATE single value (no commit) */
    public void showInsert(@Nonnull Connection dbConnection) throws SQLException {
        System.out.println("Show INSERT");
        JdbcBuilder sql = new JdbcBuilder()
            .write("INSERT INTO testTable (")
            .columnInsert("id", 10)
            .columnInsert("name", "A name")
            .columnInsert("created", someDate)
            .write(")")
            ;
        int count = sql.executeUpdate(dbConnection);

        dbConnection.commit();
        assertEquals(1, count);
    }

    /** How to SELECT single value */
    public void showSelect(@Nonnull Connection dbConnection) throws IllegalStateException, SQLException {
        System.out.println("Show SELECT");
        JdbcBuilder sql = new JdbcBuilder()
            .write("SELECT")
            .column("t.id")
            .column("t.name")
            .write("FROM testTable t WHERE")
            .andCondition("t.name", "=", "A name")
            .andCondition("t.created", ">=", someDate)
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

    /** How to SELECT single value */
    public void showSelectForSingleValue(@Nonnull Connection dbConnection) throws IllegalStateException, SQLException {
        System.out.println("Show SELECT");
        JdbcBuilder sql = new JdbcBuilder()
            .write("SELECT")
            .column("t.name")
            .write("FROM testTable t WHERE")
            .andCondition("t.id", "=", 10)
            ;
        String name = sql.uniqueValue(String.class, dbConnection);
        assertEquals("A name", name);
    }

    /** How to UPDATE single value (no commit) */
    public void showUpdate(@Nonnull Connection dbConnection) {
        System.out.println("Show UPDATE");
        JdbcBuilder sql = new JdbcBuilder()
            .write("UPDATE testTable SET")
            .columnUpdate("created", someDate.plusDays(1))
            .write("WHERE")
            .andCondition("id", "=", 10)
            ;
        sql.executeUpdate(dbConnection);
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

    /** Crete a new DB connection */
    private Connection createDbConnection() throws ClassNotFoundException, SQLException {
        Class.forName(org.h2.Driver.class.getName());
        Connection result = DriverManager.getConnection("jdbc:h2:mem:test", "", "");
        result.setAutoCommit(false);
        return result;
    }
}
