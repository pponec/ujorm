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
package org.ujorm.tools;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test of the Check class
 * @author Pavel Ponec
 */
public class CheckTest {

    /**
     * Test of format method, of class MsgFormatter.
     */
    @Test
    public void testDemo() {
        boolean resultTrue1,resultTrue2,resultTrue3,resultTrue4;
        boolean resultTrue5,resultTrue6,resultTrue7,resultTrue8;

        resultTrue1 = Check.hasLength("ABC");
        resultTrue2 = Check.hasLength(new char[]{'A', 'B', 'C'});
        resultTrue3 = Check.hasLength(new StringBuilder().append("ABC"));
        resultTrue4 = Check.hasLength(Arrays.asList("A", "B", "C"));

        resultTrue5 = Check.isEmpty("");
        resultTrue6 = Check.isEmpty(new char[0]);
        resultTrue7 = Check.isEmpty(new StringBuilder());
        resultTrue8 = Check.isEmpty((List) null);

        assertTrue(resultTrue1);
        assertTrue(resultTrue2);
        assertTrue(resultTrue3);
        assertTrue(resultTrue4);
        assertTrue(resultTrue5);
        assertTrue(resultTrue6);
        assertTrue(resultTrue7);
        assertTrue(resultTrue8);
    }

    /**
     * Test of hasLength method, of class Check.
     */
    @Test
    public void testHasLength_byteArr() {
        System.out.println("hasLength");

        assertFalse(Check.hasLength((byte[]) null));
        assertFalse(Check.hasLength(new byte[0]));
        assertTrue(Check.hasLength(new byte[1]));
    }

    /**
     * Test of hasLength method, of class Check.
     */
    @Test
    public void testHasLength_charArr() {
        System.out.println("hasLength");

        assertFalse(Check.hasLength((char[]) null));
        assertFalse(Check.hasLength(new char[0]));
        assertTrue(Check.hasLength(new char[1]));
    }

    /**
     * Test of hasLength method, of class Check.
     */
    @Test
    public void testHasLength_ObjectArr() {
        System.out.println("hasLength");

        assertFalse(Check.hasLength((Object[]) null));
        assertFalse(Check.hasLength(new Object[0]));
        assertTrue(Check.hasLength(new Object[1]));

    }

    /**
     * Test of hasLength method, of class Check.
     */
    @Test
    public void testHasLength_Collection() {
        System.out.println("hasLength");

        assertFalse(Check.hasLength((Collection) null));
        assertFalse(Check.hasLength(Arrays.asList()));
        assertTrue(Check.hasLength(Arrays.asList("A", "B", "C")));

    }

    /**
     * Test of hasLength method, of class Check.
     */
    @Test
    public void testHasLength_CharSequence() {
        System.out.println("hasLength");

        assertFalse(Check.hasLength((CharSequence) null));
        assertFalse(Check.hasLength(""));
        assertTrue(Check.hasLength("ABC"));

    }

    /**
     * Test of isEmpty method, of class Check.
     */
    @Test
    public void testIsEmpty_byteArr() {
        System.out.println("isEmpty");

        assertTrue(Check.isEmpty((byte[]) null));
        assertTrue(Check.isEmpty(new byte[0]));
        assertFalse(Check.isEmpty(new byte[1]));
    }

    /**
     * Test of isEmpty method, of class Check.
     */
    @Test
    public void testIsEmpty_charArr() {
        System.out.println("isEmpty");
        assertTrue(Check.isEmpty((char[]) null));
        assertTrue(Check.isEmpty(new char[0]));
        assertFalse(Check.isEmpty(new char[1]));
    }

    /**
     * Test of isEmpty method, of class Check.
     */
    @Test
    public void testIsEmpty_ObjectArr() {
        System.out.println("isEmpty");

        assertTrue(Check.isEmpty((Object[]) null));
        assertTrue(Check.isEmpty(new Object[0]));
        assertFalse(Check.isEmpty(new Object[1]));

    }

    /**
     * Test of isEmpty method, of class Check.
     */
    @Test
    public void testIsEmpty_Collection() {
        System.out.println("isEmpty");

        assertTrue(Check.isEmpty((Collection) null));
        assertTrue(Check.isEmpty(Arrays.asList()));
        assertFalse(Check.isEmpty(Arrays.asList("A", "B", "C")));
    }

    /**
     * Test of isEmpty method, of class Check.
     */
    @Test
    public void testIsEmpty_CharSequence() {
        System.out.println("isEmpty");
        assertTrue(Check.isEmpty((CharSequence) null));
        assertTrue(Check.isEmpty(""));
        assertFalse(Check.isEmpty("ABC"));
    }

}
