/*
 * Copyright 2017 pavel.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.xml.bind.DatatypeConverter;
import org.junit.Test;
import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Pavel Ponec
 */
public class ValueFormatterTest {
    
    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testDemo() {
        assertEquals("TE'S'T"      , ValueFormatter.formatSql("TE?T", "S"));
        assertEquals("TE, 'S', 'T'", ValueFormatter.formatSql("TE", "S", "T"));
        assertEquals("TE'S'?"      , ValueFormatter.formatSql("TE??", "S"));
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat0() {
        String template = "?";
        Object[] arguments = {"A"};
        String expResult = "'A'";
        String result = ValueFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat1() {
        String template = "'a'-'?'-'?'.";
        Object[] arguments = {"b","c"};
        String expResult = "'a'-''b''-''c''.";
        String result = ValueFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat2() {
        String template = "abc";
        Object[] arguments = {"d","e","f"};
        String expResult = "abc, 'd', 'e', 'f'";
        String result = ValueFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat3() {
        String template = "abc-?-?";
        Object[] arguments = {"d","e","f"};
        String expResult = "abc-'d'-'e', 'f'";
        String result = ValueFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat4() {
        String template = "abc-?-?-?.";
        Object[] arguments = {"d","e"};
        String expResult = "abc-'d'-'e'-?.";
        String result = ValueFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat5() {
        String template = "???";
        Object[] arguments = {"a","b","c"};
        String expResult = "'a''b''c'";
        String result = ValueFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat6() {
        String template = "";
        Object[] arguments = {"a","b","c"};
        String expResult = ", 'a', 'b', 'c'";
        String result = ValueFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat7() {
        String template = "?";
        Object[] arguments = {null, null, null};
        String expResult = "null, null, null";
        String result = ValueFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat8() {
        String template = "";
        Object[] arguments = null;
        String expResult = "";
        String result = ValueFormatter.formatSql(template, arguments);
        assertSame(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat9() {
        String template = null;
        Object[] arguments = {"a","b","c"};
        String expResult = "null, 'a', 'b', 'c'";
        String result = ValueFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat10() {
        String template = "?";
        Object[] arguments = {new IllegalStateException("TEST")};
        String expResult = "IllegalStateException:TEST";
        String result = ValueFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat11() {
        String template = "?";
        String value = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String expResult = "'ABCDEFGHIJKLMN…52…MNOPQRSTUVWXYZ'";
        String result = ValueFormatter.formatSql(template, value);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat11b() {
        String template = "?";
        String value = "01234567890123456789";
        String expResult = "'" + value + "'";
        String result = ValueFormatter.formatSql(template, value);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat12() {
        String template = "?";
        String value = "2017-11-24";
        String expResult = "'" + value + "'";
        Object[] arguments = {java.sql.Date.valueOf(LocalDate.parse(value))};
        String result = ValueFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat13() {
        String template = "?";
        String value = "2017-01-24T12:57:00.000";
        String expResult = "'" + value + "'";
        LocalDateTime ldt = LocalDateTime.parse(value);
        Object[] arguments = {java.util.Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant())};
        String result = ValueFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat14a() {
        String template = "?";
        byte[] value = "SMALL_TEST".getBytes(StandardCharsets.UTF_8);
        String expResult = "'534D414C4C5F54455354'";
        Object[] arguments = {value};
        String result = ValueFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
        assertEquals("'" + DatatypeConverter.printHexBinary(value) + "'", result);
    }
    

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat14b() {
        String template = "?";
        byte[] value = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes(StandardCharsets.UTF_8); // 25 bytes
        String expResult = "'414243444546…26…55565758595A'";
        String result = ValueFormatter.formatSql(template, value);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat14c() {
        String template = "?";
        byte[] value = "ABCDEFGHIJKLMNOP".getBytes(StandardCharsets.UTF_8); // 16 bytes
        String expResult = "'4142434445464748494A4B4C4D4E4F50'";
        String result = ValueFormatter.formatSql(template, value);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat14d() {
        String template = "?";
        byte[] value = "ABCDEFGHIJKLMNOP+".getBytes(StandardCharsets.UTF_8); // 17 bytes
        String expResult = "'414243444546…17…4C4D4E4F502B'";
        String result = ValueFormatter.formatSql(template, value);
        assertEquals(expResult, result);
    }

}
