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
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Pavel Ponec
 */
public class MsgFormatterTest {

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat1() {
        String template = "";
        Object[] arguments = null;
        MsgFormatter instance = new MsgFormatter();
        String expResult = "";
        String result = instance.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat2() {
        String template = "";
        Object[] arguments = null;
        MsgFormatter instance = new MsgFormatter();
        String expResult = "";
        String result = instance.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat3() {
        String template = "";
        Object[] arguments = null;
        MsgFormatter instance = new MsgFormatter();
        String expResult = "";
        String result = instance.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat4() {
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
    public void testFormat5() {
        String template = "abc-{}-{}-{}-x";
        Object[] arguments = {"d","e","f"};
        MsgFormatter instance = new MsgFormatter();
        String expResult = "abc-d-e-f-x";
        String result = instance.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat6() {
        String template = "abc-{}-{}-x";
        Object[] arguments = {"d","e","f"};
        MsgFormatter instance = new MsgFormatter();
        String expResult = "abc-d-e-x, f";
        String result = instance.format(template, arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testFormat7() {
        String template = "abc-{}-{}-{}-x";
        Object[] arguments = {"d","e"};
        MsgFormatter instance = new MsgFormatter();
        String expResult = "abc-d-e-{}-x";
        String result = instance.format(template, arguments);
        assertEquals(expResult, result);
    }

}
