/*
 * Copyright 2017-2022 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.msg;

import java.util.function.Supplier;
import org.junit.Test;
import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests of the MsgFormatter class
 * @author Pavel Ponec
 */
public class MsgFormatterTest {

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testDemo() {
        assertEquals("TEST"    , MsgFormatter.format("TE{}T", "S"));
        assertEquals("TE S T", MsgFormatter.format("TE", "S", "T"));
        assertEquals("TES{}"   , MsgFormatter.format("TE{}{}", "S"));
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat0() {
        String template = "{}";
        Object[] arguments = {"A"};
        String expResult = "A";
        String result = MsgFormatter.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat1() {
        String template = "'a'-'{}'-'{}'.";
        Object[] arguments = {"b","c"};
        String expResult = "'a'-'b'-'c'.";
        String result = MsgFormatter.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat2() {
        String template = "abc";
        Object[] arguments = {"d","e","f"};
        String expResult = "abc d e f";
        String result = MsgFormatter.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat3() {
        String template = "abc-{}-{}";
        Object[] arguments = {"d","e","f"};
        String expResult = "abc-d-e f";
        String result = MsgFormatter.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat4() {
        String template = "abc-{}-{}-{}.";
        Object[] arguments = {"d","e"};
        String expResult = "abc-d-e-{}.";
        String result = MsgFormatter.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat5() {
        String template = "{}{}{}";
        Object[] arguments = {"a","b","c"};
        String expResult = "abc";
        String result = MsgFormatter.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat6() {
        String template = "";
        Object[] arguments = {"a","b","c"};
        String expResult = " a b c";
        String result = MsgFormatter.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat7() {
        String template = "{}";
        Object[] arguments = {null, null, null};
        String expResult = "null null null";
        String result = MsgFormatter.format(template, arguments);
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
        String result = MsgFormatter.format(template, arguments);
        assertSame(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat9() {
        String template = null;
        Object[] arguments = {"a","b","c"};
        String expResult = "null a b c";
        String result = MsgFormatter.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat10() {
        String template = ">>>";
        Object[] arguments = {new IllegalStateException("TEST")};
        String expResult = ">>>\njava.lang.IllegalStateException: TEST";
        String result = MsgFormatter.format(template, arguments);
        assertTrue(result.startsWith(expResult));
    }

    /**
     * Test of the argument type of {@link Supplier}.
     */
    @Test
    public void testFormat11() {
        String template = "{}{}{}";
        Supplier<Object> sa = () -> "a";
        Supplier<Object> sb = () -> "b";
        Supplier<Object> sc = () -> "c";
        String expResult = "abc";
        String result = MsgFormatter.format(template, sa, sb, sc);
        assertEquals(expResult, result);
    }
}