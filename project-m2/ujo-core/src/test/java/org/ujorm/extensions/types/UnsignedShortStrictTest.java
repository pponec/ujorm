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

import java.util.function.Consumer;
import org.junit.Test;
import org.ujorm.extensions.ValueWrapper;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Pavel Ponec
 */
public class UnsignedShortStrictTest {

    /**
     * Test of toUShort method, of class UnsignedShortTools.
     */
    @Test
    public void testUShort() {
        int value;
        System.out.println("UShort converters");

        value = -1;
        assertEx(true, value, (v) -> convert(v));
        assertEx(true, value, (v) -> UnsignedShortStrict.of(v).readPersistentValue().shortValue());

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
        assertEx(true, value, (v) -> convert(v));
        assertEx(true, value, (v) -> UnsignedShortStrict.of(v).readPersistentValue().shortValue());

        value = UnsignedShort.MAX_VALUE + 2;
        assertEx(true, value, (v) -> convert(v));
        assertEx(true, value, (v) -> UnsignedShortStrict.of(v).readPersistentValue().shortValue());

        value = UnsignedShort.MAX_VALUE + 3;
        assertEx(true, value, (v) -> convert(v));
        assertEx(true, value, (v) -> UnsignedShortStrict.of(v).readPersistentValue().shortValue());

        value = Integer.MAX_VALUE;
        assertEx(true, value, (v) -> convert(v));
        assertEx(true, value, (v) -> UnsignedShortStrict.of(v).readPersistentValue().shortValue());
    }

    /** Double Conversion Assistance Method. */
    private int convert(int value) {
        UnsignedShort unsignedShort = UnsignedShortStrict.of(value);
        Short databaseValue = unsignedShort.readPersistentValue();
        UnsignedShort result = new UnsignedShort(databaseValue);
        return result.get();
    }

    /**
     * Test of getInstance method, of class UnsignedShortStrict.
     */
    @Test
    public void testGetInstance_1() throws Exception {
        System.out.println("getInstance 1");
        ValueWrapper result = UnsignedShortStrict.getInstance();

        assertEquals(Short.class, result.readPersistentClass());
        assertNotNull(result.get());
        assertNotNull(result.readPersistentValue());
    }

    /**
     * Test of getInstance method, of class UnsignedShortStrict.
     */
    @Test
    public void testPlusMinus() {
        System.out.println("testPlusMinus");
        final UnsignedShort value20 = UnsignedShortStrict.of(20);

        assertEx(true , () -> value20.minus(30).readPersistentValue().intValue());
        assertEx(false, () -> value20.minus(20).readPersistentValue().intValue());
        assertEx(false, () -> value20.minus(10).readPersistentValue().intValue());
        assertEx(false, () -> value20.minus(0).readPersistentValue().intValue());
        assertEx(false, () -> value20.plus(0).readPersistentValue().intValue());
        assertEx(false, () -> value20.plus(10).readPersistentValue().intValue());
        assertEx(false, () -> value20.plus(20).readPersistentValue().intValue());
        assertEx(true , () -> value20.plus(UnsignedShortStrict.MAX_VALUE).readPersistentValue().intValue());
    }

    /**
     * Test of getInstance method, of class UnsignedShortStrict.
     */
    @Test
    public void testGetInstance_2() throws Exception {
        System.out.println("getInstance 2");
        ValueWrapper result = ValueWrapper.getInstance(UnsignedShortStrict.class);

        assertEquals(Short.class, result.readPersistentClass());
        assertNotNull(result.get());
        assertNotNull(result.readPersistentValue());
    }

    /**
     * Test of getInstance method, of class UnsignedShortStrict.
     */
    @Test
    public void testGetInstance_3() throws Exception {
        System.out.println("getInstance 3");
        Integer expValue = 77;
        ValueWrapper result = ValueWrapper.getInstance(UnsignedShortStrict.class, (short) (Short.MIN_VALUE + expValue));

        assertEquals(Short.class, result.readPersistentClass());
        assertEquals(expValue, result.get());
    }

    /**
     * Test of getInstance method, of class UnsignedShortStrict.
     */
    @Test
    public void testOfComparation() throws Exception {
        System.out.println("testOfComparation");
        UnsignedShort v1 = UnsignedShortStrict.of(1);
        UnsignedShort v2 = UnsignedShortStrict.of(2);
        UnsignedShort v3 = UnsignedShortStrict.of(3);
        UnsignedShort v1x = UnsignedShortStrict.of(1);
        UnsignedShort v2x = UnsignedShortStrict.of(2);
        UnsignedShort v3x = UnsignedShortStrict.of(3);

        assertTrue(v1.equals(v1x));
        assertTrue(v2.equals(v2x));
        assertTrue(v3.equals(v3x));
        assertFalse(v1.equals(v2x));
        assertFalse(v2.equals(v3x));
        assertFalse(v3.equals(v1x));

        assertTrue(v1.hashCode() == v1x.hashCode());
        assertTrue(v2.hashCode() == v2x.hashCode());
        assertTrue(v3.hashCode() == v3x.hashCode());
        assertFalse(v1.hashCode() == v2x.hashCode());
        assertFalse(v2.hashCode() == v3x.hashCode());
        assertFalse(v3.hashCode() == v1x.hashCode());

        assertTrue(v1.compareTo(v1x) == 0);
        assertTrue(v2.compareTo(v2x) == 0);
        assertTrue(v3.compareTo(v3x) == 0);
        assertTrue(v1.compareTo(v3x) < 0);
        assertTrue(v2.compareTo(v1x) > 0);
        assertTrue(v3.compareTo(v2x) > 0);

        assertEquals("1", v1.toString());
        assertEquals("2", v2.toString());
        assertEquals("3", v3.toString());
    }

    /** The run() must throw an Exception */
    private void assertEx(boolean exceptionExpected, Runnable batch) {
        try {
            batch.run();
            assertFalse("Code must throw an exception", exceptionExpected);
        } catch (RuntimeException e) {
            assertTrue("Code must throw an exception", exceptionExpected);
        }
    }

    /** The run() must throw an Exception */
    private void assertEx(boolean exceptionExpected, Integer value, Consumer<Integer> batch) {
        try {
            batch.accept(value);
            assertFalse("Code must throws an exception", exceptionExpected);
        } catch (RuntimeException e) {
            assertTrue("Code must NOT throw an exception", exceptionExpected);
        }
    }


}
