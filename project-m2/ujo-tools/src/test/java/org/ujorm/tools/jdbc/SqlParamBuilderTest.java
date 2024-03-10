/*
 *  Copyright 2024-2024 Pavel Ponec
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testing the SqlParamBuilder class
 * @author Pavel Ponec
 */
public class SqlParamBuilderTest extends AbstractJdbcConnector {

    private final String newLine = "\n";

    /** Some testing date */
    private final LocalDate someDate = LocalDate.parse("2018-09-12");

    @Test
    public void testShowUsage() throws Exception {
        try (Connection dbConnection = createTable(createDbConnection()))  {
            runSingleInsert(dbConnection);
            runMultipleInsert(dbConnection);
            runSelect(dbConnection);
            runUpdate(dbConnection);
            missingParam(dbConnection);
        }
    }

    /** Example of SQL statement INSERT. */
    public void runSingleInsert(Connection connection) throws Exception {
        System.out.println("SINGLE INSERT");
        String sql = String.join(newLine,
                "INSERT INTO employee",
                "( id, code, created ) VALUES",
                "( :id, :code, :created )");
        Map<String, Object> params = new HashMap<String, Object>() {{
            put("id", 11);
            put("code", "T");
            put("created", LocalDate.parse("2018-09-12"));
        }};

        try (SqlParamBuilder builder = new SqlParamBuilder(sql, params, connection)) {
            int count = builder.execute();
            Assertions.assertEquals(1, count);

            String toString = builder.toString();
            String expected = String.join(newLine,
                    "INSERT INTO employee",
                    "( id, code, created ) VALUES",
                    "( [11], [T], [2018-09-12] )");
            Assertions.assertEquals(expected, toString);
        }
    }

    /** Example of SQL statement INSERT. */
    public void runMultipleInsert(Connection connection) throws Exception {
        System.out.println("MULTIPLE INSERT");
        String sql = String.join(newLine,
                "INSERT INTO employee",
                "(id,code,created) VALUES",
                "(:id1,:code,:created),",
                "(:id2,:code,:created)"
        );
        Map<String, Object> params = new HashMap<String, Object>() {{
            put("id1", 1);
            put("id2", 2);
            put("code", "T");
            put("created", LocalDate.parse("2018-09-12"));
        }};

        try(SqlParamBuilder builder = new SqlParamBuilder(sql, params, connection)) {
            int count = builder.execute();
            Assertions.assertEquals(2, count);

            String toString = builder.toString();
            String expected = String.join(newLine,
                    "INSERT INTO employee",
                    "(id,code,created) VALUES",
                    "([1],[T],[2018-09-12]),",
                    "([2],[T],[2018-09-12])");
            Assertions.assertEquals(expected, toString);
        }
    }

    public void runSelect(Connection connection) throws SQLException {
        System.out.println("SELECT");
        String sql = String.join(newLine,
                "SELECT t.id, t.name",
                "FROM employee t",
                "WHERE t.id > :id",
                "  AND t.code IN (:code)",
                "ORDER BY t.id");
        Map<String, Object> params = new HashMap<String, Object>() {{
            put("id", 10);
            put("code", Arrays.asList("T", "V"));
        }};

        try (SqlParamBuilder builder = new SqlParamBuilder(sql, params, connection)) {
            AtomicInteger counter = new AtomicInteger();
            for (ResultSet rs : builder.executeSelect()) {
                int id = rs.getInt(1);
                String name = rs.getString(2);

                assertEquals(11, id);
                assertEquals("test", name);
                counter.incrementAndGet();
            }

            String toString = builder.toString();
            String expected = String.join(newLine,
                    "SELECT t.id, t.name",
                    "FROM employee t",
                    "WHERE t.id > [10]",
                    "  AND t.code IN ([T],[V])",
                    "ORDER BY t.id");
            Assertions.assertEquals(expected, toString);
            Assertions.assertEquals(1, counter.get());
        }
    }

    public void runUpdate(Connection connection) throws Exception {
        System.out.println("UPDATE");
        String sql = String.join(newLine, "UPDATE employee t",
                "SET name = :name",
                "WHERE t.id < :id");
        Map<String, Object> params = new HashMap<String, Object>() {{
            put("id", 10);
            put("name", "TEST");
        }};

        try (SqlParamBuilder builder = new SqlParamBuilder(sql, params, connection)) {
            int count = builder.execute();
            assertEquals(2, count);

            // Modify arguments:
            builder.set("id", 100).set("name", "TEXT");
            count = builder.execute();
            assertEquals(3, count);
        }
    }


    public void missingParam(Connection connection) throws SQLException {
        System.out.println("MISSING PARAMS");
        String sql = String.join(newLine,
                "SELECT t.id, t.name",
                "FROM employee t",
                "WHERE t.id > :id",
                "  AND t.code = :code",
                "ORDER BY t.id");

        try (SqlParamBuilder builder = new SqlParamBuilder(sql, connection)) {
            Assertions.assertEquals(sql, builder.toString());

            IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, () -> {
                for (ResultSet rs : builder.executeSelect()) {
                    int id = rs.getInt(1);
                }
            });
            assertEquals("Missing value of the keys: [code, id]", ex.getMessage());
        }
    }

    // --- UTILS ---

    /** Create new DB connection */
    Connection createTable(Connection dbConnection) throws ClassNotFoundException, SQLException {
        String sql = String.join(newLine,
                "CREATE TABLE employee"
                + "( id INTEGER PRIMARY KEY"
                + ", name VARCHAR(256) DEFAULT 'test'"
                + ", code VARCHAR(1)"
                + ", created TIMESTAMP"
                + ")");

        try (PreparedStatement ps = dbConnection.prepareStatement(sql)) {
            ps.execute();
            dbConnection.commit();
        }
        return dbConnection;
    }

}
