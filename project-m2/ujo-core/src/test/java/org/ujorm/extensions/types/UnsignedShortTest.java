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
package org.ujorm.extensions.types;

import junit.framework.TestCase;
import org.junit.jupiter.api.Test;
import org.ujorm.extensions.ValueWrapper;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * A test of the UnsignedShort class
 * @author Pavel Ponec
 */
public class UnsignedShortTest {

    /**
     * Test of toUShort method, of class UnsignedShortTools.
     */
    @Test
    public void testUShort() {
        int value;
        System.out.println("UShort converters");

        value = -1;
        assertEquals(0, convert(value));
        assertEquals(Short.MIN_VALUE, UnsignedShort.of(value).readPersistentValue().shortValue());

        value = 0;
        assertEquals(value, convert(value));
        assertEquals((short)(value + Short.MIN_VALUE), UnsignedShort.of(value).readPersistentValue().shortValue());

        value = 1;
        assertEquals(value, convert(value));
        assertEquals((short)(value + Short.MIN_VALUE), UnsignedShort.of(value).readPersistentValue().shortValue());

        value = 2;
        assertEquals(value, convert(value));
        assertEquals((short)(value + Short.MIN_VALUE), UnsignedShort.of(value).readPersistentValue().shortValue());

        value = 3;
        assertEquals(value, convert(value));
        assertEquals((short)(value + Short.MIN_VALUE), UnsignedShort.of(value).readPersistentValue().shortValue());

        value = Short.MAX_VALUE + -1;
        assertEquals(value, convert(value));
        assertEquals((short)(value + Short.MIN_VALUE), UnsignedShort.of(value).readPersistentValue().shortValue());

        value = Short.MAX_VALUE + 0;
        assertEquals(value, convert(value));
        assertEquals((short)(value + Short.MIN_VALUE), UnsignedShort.of(value).readPersistentValue().shortValue());

        value = Short.MAX_VALUE + 1;
        assertEquals(value, convert(value));
        assertEquals((short)(value + Short.MIN_VALUE), UnsignedShort.of(value).readPersistentValue().shortValue());

        value = Short.MAX_VALUE + 2;
        assertEquals(value, convert(value));
        assertEquals((short)(value + Short.MIN_VALUE), UnsignedShort.of(value).readPersistentValue().shortValue());

        value = Short.MAX_VALUE + 3;
        assertEquals(value, convert(value));
        assertEquals((short)(value + Short.MIN_VALUE), UnsignedShort.of(value).readPersistentValue().shortValue());

        value = UnsignedShort.MAX_VALUE + -1;
        assertEquals(value, convert(value));
        assertEquals((short)(value + Short.MIN_VALUE), UnsignedShort.of(value).readPersistentValue().shortValue());

        value = UnsignedShort.MAX_VALUE + 0;
        assertEquals(value, convert(value));
        assertEquals((short)(value + Short.MIN_VALUE), UnsignedShort.of(value).readPersistentValue().shortValue());

        value = UnsignedShort.MAX_VALUE + 1;
        assertEquals(UnsignedShort.MAX_VALUE, convert(value));
        assertEquals(Short.MAX_VALUE, UnsignedShort.of(value).readPersistentValue().shortValue());

        value = UnsignedShort.MAX_VALUE + 2;
        assertEquals(UnsignedShort.MAX_VALUE, convert(value));
        assertEquals(Short.MAX_VALUE, UnsignedShort.of(value).readPersistentValue().shortValue());

        value = UnsignedShort.MAX_VALUE + 3;
        assertEquals(UnsignedShort.MAX_VALUE, convert(value));
        assertEquals(Short.MAX_VALUE, UnsignedShort.of(value).readPersistentValue().shortValue());

        value = Integer.MAX_VALUE;
        assertEquals(UnsignedShort.MAX_VALUE, convert(value));
        assertEquals(Short.MAX_VALUE, UnsignedShort.of(value).readPersistentValue().shortValue());
    }

    /** Double Conversion Assistance Method.  */
    private int convert(int value) {
        UnsignedShort unsignedShort = UnsignedShort.of(value);
        Short databaseValue = unsignedShort.readPersistentValue();
        UnsignedShort result = new UnsignedShort(databaseValue);
        return result.get();
    }

    /**
     * Test of getInstance method, of class UnsignedShort.
     */
    @Test
    public void testGetInstance_1() throws Exception {
        System.out.println("getInstance 1");
        ValueWrapper result = UnsignedShort.getInstance();

        assertEquals(Short.class, result.readPersistentClass());
        assertNotNull(result.get());
        assertNotNull(result.readPersistentValue());
    }

    /**
     * Test of getInstance method, of class UnsignedShort.
     */
    @Test
    public void testPlusMinus() {
        System.out.println("testPlusMinus");
        final UnsignedShort value20 = UnsignedShort.of(20);

        assertEquals(UnsignedShort.DB_ZERO +  0, value20.minus(30).readPersistentValue().intValue());
        assertEquals(UnsignedShort.DB_ZERO +  0, value20.minus(20).readPersistentValue().intValue());
        assertEquals(UnsignedShort.DB_ZERO + 10, value20.minus(10).readPersistentValue().intValue());
        assertEquals(UnsignedShort.DB_ZERO + 20, value20.minus(0).readPersistentValue().intValue());
        assertEquals(UnsignedShort.DB_ZERO + 20, value20.plus( 0).readPersistentValue().intValue());
        assertEquals(UnsignedShort.DB_ZERO + 30, value20.plus(10).readPersistentValue().intValue());
        assertEquals(UnsignedShort.DB_ZERO + 40, value20.plus(20).readPersistentValue().intValue());
        assertEquals(Short.MAX_VALUE, value20.plus(UnsignedShort.MAX_VALUE).readPersistentValue().intValue());
    }

    /**
     * Test of getInstance method, of class UnsignedShort.
     */
    @Test
    public void testGetInstance_2() throws Exception {
        System.out.println("getInstance 2");
        ValueWrapper result = ValueWrapper.getInstance(UnsignedShort.class);

        assertEquals(Short.class, result.readPersistentClass());
        assertNotNull(result.get());
        assertNotNull(result.readPersistentValue());
    }

   /**
     * Test of getInstance method, of class UnsignedShort.
     */
    @Test
    public void testGetInstance_3() throws Exception {
        System.out.println("getInstance 3");
        Integer expValue = 77;
        ValueWrapper result = ValueWrapper.getInstance(UnsignedShort.class, (short)(Short.MIN_VALUE + expValue));

        assertEquals(Short.class, result.readPersistentClass());
        assertEquals(expValue, result.get());
    }

    /**
     * Test of getInstance method, of class UnsignedShort.
     */
    @Test
    public void testOfComparation() throws Exception {
        System.out.println("testOfComparation");
        UnsignedShort v1 = UnsignedShort.of(1);
        UnsignedShort v2 = UnsignedShort.of(2);
        UnsignedShort v3 = UnsignedShort.of(3);
        UnsignedShort v1x = UnsignedShort.of(1);
        UnsignedShort v2x = UnsignedShort.of(2);
        UnsignedShort v3x = UnsignedShort.of(3);

        TestCase.assertEquals(v1, v1x);
        TestCase.assertEquals(v2, v2x);
        TestCase.assertEquals(v3, v3x);
        assertFalse(v1.equals(v2x));
        assertFalse(v2.equals(v3x));
        assertFalse(v3.equals(v1x));

        TestCase.assertEquals(v1.hashCode(), v1x.hashCode());
        TestCase.assertEquals(v2.hashCode(), v2x.hashCode());
        TestCase.assertEquals(v3.hashCode(), v3x.hashCode());
        assertFalse(v1.hashCode() == v2x.hashCode());
        assertFalse(v2.hashCode() == v3x.hashCode());
        assertFalse(v3.hashCode() == v1x.hashCode());

        TestCase.assertEquals(0, v1.compareTo(v1x));
        TestCase.assertEquals(0, v2.compareTo(v2x));
        TestCase.assertEquals(0, v3.compareTo(v3x));
        assertTrue(v1.compareTo(v3x) < 0);
        assertTrue(v2.compareTo(v1x) > 0);
        assertTrue(v3.compareTo(v2x) > 0);

        assertEquals("1", v1.toString());
        assertEquals("2", v2.toString());
        assertEquals("3", v3.toString());
    }
}
