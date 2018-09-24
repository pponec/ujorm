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

package org.ujorm.tools;


import java.time.LocalDate;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testing the JdbcBuillder class
 * @author Test Ponec
 */
public class JdbcBuillderTest {

    /** Some testing date */
    private static final LocalDate SOME_DATE = LocalDate.parse("2018-09-12");

    /** Test SQL SELECT of class JdbcBuillder. */
    @Test
    public void testSelect() {
        System.out.println("SELECT");
        JdbcBuillder sql = new JdbcBuillder()
            .write("SELECT")
            .column("t.id")
            .column("t.name")
            .write("FROM testTable t WHERE")
            .andCondition("t.name", "=", "Test")
            .andCondition("t.date", ">", SOME_DATE)
            ;
        String expResult1 = "SELECT t.id, t.name FROM testTable t WHERE t.name = ? AND t.date > ?";
        String expResult2 = "SELECT t.id, t.name FROM testTable t WHERE t.name = 'Test' AND t.date > 2018-09-12";

        assertEquals(expResult1, sql.getSql());
        assertEquals(expResult2, sql.toString());
        assertEquals(2, sql.getArguments().length);
        assertEquals("Test", sql.getArguments()[0]);
    }

    /** Test SQL INSERT of class JdbcBuillder. */
    @Test
    public void testInsert() {
        System.out.println("INSERT INTO");
        JdbcBuillder sql = new JdbcBuillder()
            .write("INSERT INTO testTable (")
            .columnInsert("id", 10)
            .columnInsert("name", "Test")
            .columnInsert("date", SOME_DATE)
            .write(")");
            ;
        String expResult1 = "INSERT INTO testTable ( id, name, date ) VALUES ( ?, ?, ? )";
        String expResult2 = "INSERT INTO testTable ( id, name, date ) VALUES ( 10, 'Test', 2018-09-12 )";

        assertEquals(expResult1, sql.getSql());
        assertEquals(expResult2, sql.toString());
        assertEquals(3, sql.getArguments().length);
        assertEquals(10, sql.getArguments()[0]);
    }

    /** Test raw SQL INSERT of class JdbcBuillder for a better performace and general use. */
    @Test
    public void testInsertRaw() {
        System.out.println("INSERT INTO");
        JdbcBuillder sql = new JdbcBuillder()
            .write("INSERT INTO testTable (")
            .column("id")
            .column("name")
            .column("date")
            .write(") VALUES (")
            .value(10)
            .value("Test")
            .value(SOME_DATE)
            .write(")");
            ;
        String expResult1 = "INSERT INTO testTable ( id, name, date ) VALUES ( ?, ?, ? )";
        String expResult2 = "INSERT INTO testTable ( id, name, date ) VALUES ( 10, 'Test', 2018-09-12 )";

        assertEquals(expResult1, sql.getSql());
        assertEquals(expResult2, sql.toString());
        assertEquals(3, sql.getArguments().length);
        assertEquals(10, sql.getArguments()[0]);
    }

    /** Test SQL INSERT of class JdbcBuillder. */
    @Test
    public void testUpdate() {
        System.out.println("UPDATE");
        JdbcBuillder sql = new JdbcBuillder()
            .write("UPDATE testTable SET")
            .columnUpdate("name", "Test")
            .columnUpdate("date", SOME_DATE)
            .write("WHERE")
            .andCondition("id", ">", 10)
            .andCondition("id", "<", 20)
            ;
        String expResult1 = "UPDATE testTable SET name = ?, date = ? WHERE id > ? AND id < ?";
        String expResult2 = "UPDATE testTable SET name = 'Test', date = 2018-09-12 WHERE id > 10 AND id < 20";

        assertEquals(expResult1, sql.getSql());
        assertEquals(expResult2, sql.toString());
        assertEquals(4, sql.getArguments().length);
        assertEquals("Test", sql.getArguments()[0]);
        assertEquals(SOME_DATE, sql.getArguments()[1]);
        assertEquals(10, sql.getArguments()[2]);
        assertEquals(20, sql.getArguments()[3]);
    }

}
