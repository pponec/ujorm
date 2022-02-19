/*
 *  Copyright 2018-2022 Pavel Ponec
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Compare using in the {@link JdbcBuilderTest} with native JDBC implementations here.
 * @author Pavel Ponec
 */
public class NativeJdbclTest extends AbstractJdbcConnector {

    /** Some testing date */
    private final LocalDate someDate = LocalDate.parse("2018-09-12");

    @Test
    public void testShowUsage() throws ClassNotFoundException, SQLException {
        try (Connection dbConnection = createTable(createDbConnection())) {
            showInsert(dbConnection);
            showSelect(dbConnection);
            showUpdate(dbConnection);
        }
    }

    /** Test SQL INSERT using raw JDBC. */
    public void showInsert(Connection dbConnection) throws SQLException {
        System.out.println("INSERT");
        int count = 0;
        String sql = "INSERT INTO testTable"
                + " ( id, name, created )"
                + " VALUES"
                + " ( ?, ?, ? )"
                ;
        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setInt(1, 10);
            ps.setObject(2, "A name");
            ps.setObject(3, someDate);
            count = ps.executeUpdate();
        }

        dbConnection.commit();
        String expResult1 = "INSERT INTO testTable ( id, name, created ) VALUES ( ?, ?, ? )";
        assertEquals(expResult1, sql);
        assertEquals(1, count);
    }

    /** Test SQL SELECT using raw JDBC. */
    public void showSelect(Connection dbConnection) throws SQLException {
        System.out.println("Show INSERT");
        String sql = "SELECT"
                + " t.id,"
                + " t.name"
                + " FROM testTable t"
                + " WHERE t.name = ?"
                + " AND t.created >= ?"
                ;
        ResultSet tempRs = null;
        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setString(1, "A name");
            ps.setObject(2, someDate);

            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    int id = rs.getInt(1);
                    String name = rs.getString(2);

                    assertEquals(10, id);
                    assertEquals("A name", name);
                    tempRs = rs;
                }
            }
        }

        String expResult1 = "SELECT t.id, t.name FROM testTable t WHERE t.name = ? AND t.created >= ?";
        assertEquals(expResult1, sql);
        assertTrue(tempRs.isClosed());
    }

    /** How to UPDATE using raw JDBC. */
    public void showUpdate(Connection dbConnection) throws SQLException {
        System.out.println("UPDATE");
        int count = 0;

        String sql = "UPDATE testTable"
                + " SET name = ?"
                + ", created = ?"
                + " WHERE id IN ( ?, ?, ? )"
                + " AND created BETWEEN ? AND ?"
                + " AND name IS NOT NULL"
                ;
        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.setObject(1, "Test");
            ps.setObject(2, someDate);
            ps.setObject(3, 10);
            ps.setObject(4, 20);
            ps.setObject(5, 30);
            ps.setObject(6, someDate);
            ps.setObject(7, someDate.plusMonths(1));

            count = ps.executeUpdate();
        }

        String expResult1 = "UPDATE testTable"
                + " SET name = ?"
                + ", created = ?"
                + " WHERE id IN ( ?, ?, ? )"
                + " AND created BETWEEN ? AND ?"
                + " AND name IS NOT NULL"
                ;
        assertEquals(expResult1, sql);
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
