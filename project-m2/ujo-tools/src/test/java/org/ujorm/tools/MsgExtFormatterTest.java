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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.junit.Test;
import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Pavel Ponec
 */
public class MsgExtFormatterTest {
    
    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testDemo() {
        assertEquals("TEST"    , MsgExtFormatter.formatSql("TE?T", "S"));
        assertEquals("TE, S, T", MsgExtFormatter.formatSql("TE", "S", "T"));
        assertEquals("TES?"   , MsgExtFormatter.formatSql("TE??", "S"));
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat0() {
        String template = "?";
        Object[] arguments = {"A"};
        String expResult = "A";
        String result = MsgExtFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat1() {
        String template = "'a'-'?'-'?'.";
        Object[] arguments = {"b","c"};
        String expResult = "'a'-'b'-'c'.";
        String result = MsgExtFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat2() {
        String template = "abc";
        Object[] arguments = {"d","e","f"};
        String expResult = "abc, d, e, f";
        String result = MsgExtFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat3() {
        String template = "abc-?-?";
        Object[] arguments = {"d","e","f"};
        String expResult = "abc-d-e, f";
        String result = MsgExtFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat4() {
        String template = "abc-?-?-?.";
        Object[] arguments = {"d","e"};
        String expResult = "abc-d-e-?.";
        String result = MsgExtFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat5() {
        String template = "???";
        Object[] arguments = {"a","b","c"};
        String expResult = "abc";
        String result = MsgExtFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat6() {
        String template = "";
        Object[] arguments = {"a","b","c"};
        String expResult = ", a, b, c";
        String result = MsgExtFormatter.formatSql(template, arguments);
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
        String result = MsgExtFormatter.formatSql(template, arguments);
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
        String result = MsgExtFormatter.formatSql(template, arguments);
        assertSame(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat9() {
        String template = null;
        Object[] arguments = {"a","b","c"};
        String expResult = "null, a, b, c";
        String result = MsgExtFormatter.formatSql(template, arguments);
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
        String result = MsgExtFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat11() {
        String template = "?";
        Object[] arguments = {"0123456789012345678901234567890123456789"};
        String expResult = "01234567890123…40…67890123456789";
        String result = MsgExtFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat11b() {
        String template = "?";
        String expResult = "01234567890123456789";
        Object[] arguments = {expResult};
        String result = MsgExtFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat12() {
        String template = "?";
        String expResult = "2017-11-24";
        Object[] arguments = {java.sql.Date.valueOf(LocalDate.parse(expResult))};
        String result = MsgExtFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat13() {
        String template = "?";
        String expResult = "2017-01-24T12:57:00";
        LocalDateTime ldt = LocalDateTime.parse(expResult);
        Object[] arguments = {java.util.Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant())};
        String result = MsgExtFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

     /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat14() {
        String template = "?";
        String expResult = "eHh4eHh4eHh4eH…36…h4eHh4eHh4eA==";
        Object[] arguments = {"xxxxxxxxxxxxxxxxxxxxxxxxx".getBytes()};
        String result = MsgExtFormatter.formatSql(template, arguments);
        assertEquals(expResult, result);
    }

}
