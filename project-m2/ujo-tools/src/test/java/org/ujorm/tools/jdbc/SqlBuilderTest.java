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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testing the SqlBuilder class
 * @author Pavel Ponec
 */
public class SqlBuilderTest extends AbstractJdbcConnector {

    /** DB table name @{code employee} */
    String employee = "employee";

    /** Some testing date */
    private final LocalDate someDate = LocalDate.parse("2018-09-12");

    @Test
    public void testShowUsage() throws ClassNotFoundException, SQLException {
        try (Connection dbConnection = createTable(createDbConnection()))  {
            //showInsert_1(dbConnection);
            showSelect_2(dbConnection);
        }
    }

    /** Example of SQL statement INSERT. */
    public void showInsert_1(Connection connection) throws SQLException {
        System.out.println("INSERT");
        SqlBuilder sql = new SqlBuilder()
                .line("INSERT INTO", employee)
                .line( "( id, name, created) VALUES ")
                .line("(").params(  10, "A name", LocalDate.parse("2018-09-12")).add(")");

        String toString = sql.toString();
        String expected = String.join("\n",
                "INSERT INTO employee",
                "( id, name, created ) VALUES ",
                "( [10], [A name], [2018-09-12] )");
        Assertions.assertEquals(expected, toString);
    }

    public void showSelect_2(Connection connection) throws SQLException {
        System.out.println("SELECT");
        SqlBuilder.SqlValue codeValue = param("T");
        SqlBuilder sql = new SqlBuilder()
                .line("SELECT t.id, t.name FROM", employee, "t")
                .line("WHERE t.code =").param(codeValue)
                .line("ORDER BY t.id");

//        for (ResultSet rs : sql.executeSelect(connection)) {
//            int id = rs.getInt(1);
//            String name = rs.getString(2);
//
//            assertEquals(10, id);
//            assertEquals("A name", name);
//        }

        String toString = sql.toString();
        String expected = String.join("\n",
                "SELECT t.id, t.name FROM employee t",
                "WHERE t.code = [T]",
                "ORDER BY t.id");
        Assertions.assertEquals(expected, toString);
    }

    /** Create new Parameter */
    public SqlBuilder.SqlValue param(Object param) {
        return new SqlBuilder.SqlValue(param);
    }

    // --- UTILS ---

    /** Crete new DB connection */
    Connection createTable(Connection dbConnection) throws ClassNotFoundException, SQLException {
        String sql = "CREATE TABLE " + employee
                + "\n( id INTEGER PRIMARY KEY"
                + "\n, name VARCHAR(256)"
                + "\n, code VARCHAR(1)"
                + "\n, created TIMESTAMP"
                + "\n)";

        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.execute();
            dbConnection.commit();
        }
        return dbConnection;
    }

}
