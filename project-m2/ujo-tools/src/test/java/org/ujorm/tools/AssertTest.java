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

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Pavel Ponec
 */
public class AssertTest {

    /** Demo message */
    private static final Object[] TEST_MESSAGE = {"TEST MESSAGE"};

    /**
     * Test of isTrue method, of class Assert.
     */
    @Test
    public void testIsTrue_boolean_1() {
        System.out.println("isTrue");
        boolean value = true;
        Assert.isTrue(value);
    }

    /**
     * Test of isTrue method, of class Assert.
     */
    @Test
    public void testIsTrue_boolean_ObjectArr_1() {
        System.out.println("isTrue");
        boolean value = true;
        Assert.isTrue(value, TEST_MESSAGE);
    }

    /**
     * Test of notNull method, of class Assert.
     */
    @Test
    public void testNotNull_Object_1() {
        System.out.println("notNull");
        Object value = new Object();
        Assert.notNull(value);
    }

    /**
     * Test of notNull method, of class Assert.
     */
    @Test
    public void testNotNull_Object_ObjectArr_1() {
        System.out.println("notNull");
        Object value = new Object();
        Assert.notNull(value, TEST_MESSAGE);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test
    public void testHasLength_byteArr_1() {
        System.out.println("hasLength");
        byte[] array = new byte[1];
        Assert.hasLength(array);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test
    public void testHasLength_byteArr_ObjectArr_1() {
        System.out.println("hasLength");
        byte[] array = new byte[1];
        Assert.hasLength(array, TEST_MESSAGE);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test
    public void testHasLength_charArr_1() {
        System.out.println("hasLength");
        char[] array = new char[1];
        Assert.hasLength(array);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test
    public void testHasLength_charArr_ObjectArr_1() {
        System.out.println("hasLength");
        char[] array = new char[1];
        Assert.hasLength(array, TEST_MESSAGE);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test
    public void testHasLength_ObjectArr_1() {
        System.out.println("hasLength");
        Object[] values = new Object[1];
        Assert.hasLength(values);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test
    public void testHasLength_ObjectArr_ObjectArr_1() {
        System.out.println("hasLength");
        Object[] values = new Object[1];
        Assert.hasLength(values, TEST_MESSAGE);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test
    public void testHasLength_Collection_1() {
        System.out.println("hasLength");
        Collection values = Arrays.asList("A", "B", "C");
        Assert.hasLength(values);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test
    public void testHasLength_Collection_ObjectArr_1() {
        System.out.println("hasLength");
        Collection values = Arrays.asList("A", "B", "C");;
        Assert.hasLength(values, TEST_MESSAGE);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test
    public void testHasLength_CharSequence_1() {
        System.out.println("hasLength");
        CharSequence value = "ABC";
        Assert.hasLength(value);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test
    public void testHasLength_CharSequence_ObjectArr_1() {
        System.out.println("hasLength");
        CharSequence value = "ABC";
        Assert.hasLength(value, TEST_MESSAGE);
    }

    /**
     * Test of isFalse method, of class Assert.
     */
    @Test
    public void testIsFalse_boolean_1() {
        System.out.println("isFalse");
        boolean value = false;
        Assert.isFalse(value);
    }

    /**
     * Test of isFalse method, of class Assert.
     */
    @Test
    public void testIsFalse_boolean_ObjectArr_1() {
        System.out.println("isFalse");
        boolean value = false;
        Assert.isFalse(value, TEST_MESSAGE);
    }

    /**
     * Test of isNull method, of class Assert.
     */
    @Test
    public void testIsNull_Object_1() {
        System.out.println("isNull");
        Object value = null;
        Assert.isNull(value);
    }

    /**
     * Test of isNull method, of class Assert.
     */
    @Test
    public void testIsNull_Object_ObjectArr_1() {
        System.out.println("isNull");
        Object value = null;
        Assert.isNull(value, TEST_MESSAGE);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test
    public void testIsEmpty_byteArr_1() {
        System.out.println("isEmpty");
        byte[] array = new byte[0];
        Assert.isEmpty(array);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test
    public void testIsEmpty_byteArr_ObjectArr_1() {
        System.out.println("isEmpty");
        byte[] array = new byte[0];
        Assert.isEmpty(array, TEST_MESSAGE);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test
    public void testIsEmpty_charArr_1() {
        System.out.println("isEmpty");
        char[] array = null;
        Assert.isEmpty(array);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test
    public void testIsEmpty_charArr_ObjectArr_1() {
        System.out.println("isEmpty");
        char[] array = null;
        Assert.isEmpty(array, TEST_MESSAGE);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test
    public void testIsEmpty_ObjectArr_1() {
        System.out.println("isEmpty");
        Object[] values = null;
        Assert.isEmpty(values);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test
    public void testIsEmpty_ObjectArr_ObjectArr_1() {
        System.out.println("isEmpty");
        Object[] values = null;
        Assert.isEmpty(values, TEST_MESSAGE);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test
    public void testIsEmpty_Collection_1() {
        System.out.println("isEmpty");
        Collection values = null;
        Assert.isEmpty(values);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test
    public void testIsEmpty_Collection_ObjectArr_1() {
        System.out.println("isEmpty");
        Collection values = null;
        Assert.isEmpty(values, TEST_MESSAGE);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test
    public void testIsEmpty_CharSequence_1() {
        System.out.println("isEmpty");
        CharSequence value = null;
        Assert.isEmpty(value);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test
    public void testIsEmpty_CharSequence_ObjectArr_1() {
        System.out.println("isEmpty");
        CharSequence value = null;
        Assert.isEmpty(value, TEST_MESSAGE);
    }

    // ------------- EXCEPTION MESSAGE TESTS -------------

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test
    public void testNotNull_CharSequence_ObjectArr_3() {
        try {
            System.out.println("notNull");
            CharSequence value = "";
            Assert.notNull(null, TEST_MESSAGE);
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            assertEquals(TEST_MESSAGE[0], e.getMessage());
            assertTrue(e.getCause() instanceof NullPointerException);
        }
    }

    // ------------- EXCEPTION TESTS -------------

    /**
     * Test of isTrue method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsTrue_boolean_2() {
        System.out.println("isTrue");
        boolean value = false;
        Assert.isTrue(value);
    }

    /**
     * Test of isTrue method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsTrue_boolean_ObjectArr_2() {
        System.out.println("isTrue");
        boolean value = false;
        Assert.isTrue(value, TEST_MESSAGE);
    }

    /**
     * Test of notNull method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNotNull_Object_2() {
        System.out.println("notNull");
        Object value = null;
        Assert.notNull(value);
    }

    /**
     * Test of notNull method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNotNull_Object_ObjectArr_2() {
        System.out.println("notNull");
        Object value = null;

        Assert.notNull(value, TEST_MESSAGE);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHasLength_byteArr_2() {
        System.out.println("hasLength");
        byte[] array = new byte[0];
        Assert.hasLength(array);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHasLength_byteArr_ObjectArr_2() {
        System.out.println("hasLength");
        byte[] array = new byte[0];
        Assert.hasLength(array, TEST_MESSAGE);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHasLength_charArr_2() {
        System.out.println("hasLength");
        char[] array = new char[0];
        Assert.hasLength(array);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHasLength_charArr_ObjectArr_2() {
        System.out.println("hasLength");
        char[] array = new char[0];
        Assert.hasLength(array, TEST_MESSAGE);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHasLength_ObjectArr_2() {
        System.out.println("hasLength");
        Object[] values = null;
        Assert.hasLength(values);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHasLength_ObjectArr_ObjectArr_2() {
        System.out.println("hasLength");
        Object[] values = null;
        Assert.hasLength(values, TEST_MESSAGE);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHasLength_Collection_2() {
        System.out.println("hasLength");
        Collection values = null;
        Assert.hasLength(values);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHasLength_Collection_ObjectArr_2() {
        System.out.println("hasLength");
        Collection values = null;
        Assert.hasLength(values, TEST_MESSAGE);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHasLength_CharSequence_2() {
        System.out.println("hasLength");
        CharSequence value = null;
        Assert.hasLength(value);
    }

    /**
     * Test of hasLength method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHasLength_CharSequence_ObjectArr_2() {
        System.out.println("hasLength");
        CharSequence value = null;
        Assert.hasLength(value, TEST_MESSAGE);
    }

    /**
     * Test of isFalse method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsFalse_boolean_2() {
        System.out.println("isFalse");
        boolean value = true;
        Assert.isFalse(value);
    }

    /**
     * Test of isFalse method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsFalse_boolean_ObjectArr_2() {
        System.out.println("isFalse");
        boolean value = true;
        Assert.isFalse(value, TEST_MESSAGE);
    }

    /**
     * Test of isNull method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsNull_Object_2() {
        System.out.println("isNull");
        Object value = "";
        Assert.isNull(value);
    }

    /**
     * Test of isNull method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsNull_Object_ObjectArr_2() {
        System.out.println("isNull");
        Object value = "";
        Assert.isNull(value, TEST_MESSAGE);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsEmpty_byteArr_2() {
        System.out.println("isEmpty");
        byte[] array = new byte[1];
        Assert.isEmpty(array);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsEmpty_byteArr_ObjectArr_2() {
        System.out.println("isEmpty");
        byte[] array = new byte[1];
        Assert.isEmpty(array, TEST_MESSAGE);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsEmpty_charArr_2() {
        System.out.println("isEmpty");
        char[] array = new char[1];
        Assert.isEmpty(array);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsEmpty_charArr_ObjectArr_2() {
        System.out.println("isEmpty");
        char[] array = new char[1];
        Assert.isEmpty(array, TEST_MESSAGE);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsEmpty_ObjectArr_2() {
        System.out.println("isEmpty");
        Object[] values = {"A"};
        Assert.isEmpty(values);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsEmpty_ObjectArr_ObjectArr_2() {
        System.out.println("isEmpty");
        Object[] values = {"A"};
        Assert.isEmpty(values, TEST_MESSAGE);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsEmpty_Collection_2() {
        System.out.println("isEmpty");
        Collection values = Arrays.asList("A", "B", "C");
        Assert.isEmpty(values);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsEmpty_Collection_ObjectArr_2() {
        System.out.println("isEmpty");
        Collection values = Arrays.asList("A", "B", "C");
        Assert.isEmpty(values, TEST_MESSAGE);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsEmpty_CharSequence_2() {
        System.out.println("isEmpty");
        CharSequence value = "ABC";
        Assert.isEmpty(value);
    }

    /**
     * Test of isEmpty method, of class Assert.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIsEmpty_CharSequence_ObjectArr_2() {
        System.out.println("isEmpty");
        CharSequence value = "ABC";
        Assert.isEmpty(value, TEST_MESSAGE);
    }

}
