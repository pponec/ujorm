/*
 * Copyright 2017 Pavel Ponec
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
package org.ujorm.logger;

import org.junit.Test;
import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertEquals;

/**
 * Tests of the MsgFormatter class
 * @author Pavel Ponec
 */
public class MsgFormatterTest {

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat1() {
        String template = "abc-{}-{}.";
        Object[] arguments = {"d","e"};
        MsgFormatter instance = new MsgFormatter();
        String expResult = "abc-d-e.";
        String result = instance.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat2() {
        String template = "abc";
        Object[] arguments = {"d","e","f"};
        MsgFormatter instance = new MsgFormatter();
        String expResult = "abc, d, e, f";
        String result = instance.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat3() {
        String template = "abc-{}-{}";
        Object[] arguments = {"d","e","f"};
        MsgFormatter instance = new MsgFormatter();
        String expResult = "abc-d-e, f";
        String result = instance.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat4() {
        String template = "abc-{}-{}-{}.";
        Object[] arguments = {"d","e"};
        MsgFormatter instance = new MsgFormatter();
        String expResult = "abc-d-e-{}.";
        String result = instance.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat5() {
        String template = "{}{}{}";
        Object[] arguments = {"a","b","c"};
        MsgFormatter instance = new MsgFormatter();
        String expResult = "abc";
        String result = instance.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat6() {
        String template = "";
        Object[] arguments = {"a","b","c"};
        MsgFormatter instance = new MsgFormatter();
        String expResult = ", a, b, c";
        String result = instance.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat7() {
        String template = "{}";
        Object[] arguments = {null, null, null};
        MsgFormatter instance = new MsgFormatter();
        String expResult = "null, null, null";
        String result = instance.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat8() {
        String template = "";
        Object[] arguments = null;
        MsgFormatter instance = new MsgFormatter();
        String expResult = template;
        String result = instance.format(template, arguments);
        assertSame(expResult, result);
    }

}