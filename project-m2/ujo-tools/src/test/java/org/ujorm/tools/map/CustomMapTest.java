/*
 * Copyright 2018-2026 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.map;

import org.ujorm.tools.set.CustomMap;
import org.ujorm.tools.set.MapKeyProxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test of UjoMap class where the result is the same like HashMap class.
 * @author Pavel Ponec
 */
public class CustomMapTest {

    /**
     * Test of size method, of class UjoMap.
     */
    @Test
    public void testSize() {
        System.out.println("size");
        CustomMap<Integer,String> instance = createUjoMap(3);
        int expResult = 3;
        int result = instance.size();
        assertEquals(expResult, result);

    }

    /**
     * Test of isEmpty method, of class UjoMap.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        CustomMap<Integer,String> instance = createUjoMap(0);
        boolean expResult = true;
        boolean result = instance.isEmpty();
        assertEquals(expResult, result);
        assertFalse(createUjoMap(1).isEmpty());
    }

    /**
     * Test of containsKey method, of class UjoMap.
     */
    @Test
    public void testContainsKey() {
        System.out.println("containsKey");

        CustomMap<Integer,String> instance = createUjoMap(3);
        assertTrue(instance.containsKey(1));
        assertTrue(instance.containsKey(2));
        assertTrue(instance.containsKey(3));
        assertFalse(instance.containsKey(4));
        assertFalse(instance.containsKey(-1));
    }

    /**
     * Test of containsValue method, of class UjoMap.
     */
    @Test
    public void testContainsValue() {
        System.out.println("containsValue");
        Object value = null;
        CustomMap<Integer,String> instance = createUjoMap(3);
        boolean expResult = false;
        boolean result = instance.containsValue(value);
        assertEquals(expResult, result);
        assertFalse(instance.containsValue("Z"));
    }

    /**
     * Test of get method, of class UjoMap.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        Integer key = 2;
        CustomMap<Integer,String> instance = createUjoMap(3);
        String expResult = "B";
        String result = instance.get(key);
        assertEquals(expResult, result);
        assertNull(instance.get(-1));
    }

    /**
     * Test of put method, of class UjoMap.
     */
    @Test
    public void testPut() {
        System.out.println("put");
        final Integer key = 9;
        String value1 = "Z";
        CustomMap<Integer,String> instance = createUjoMap(3);
        Object expResult = null;
        String result = instance.put(key, value1);
        assertEquals(expResult, result);
        assertEquals(value1, instance.get(key));
        //
        String value2 = "W";
        expResult = value1;
        result = instance.put(key, value2);
        assertEquals(expResult, result);
        assertEquals(value2, instance.get(key));
    }

    /**
     * Test of remove method, of class UjoMap.
     */
    @Test
    public void testRemove() {
        System.out.println("remove");
        Integer key = 2;
        CustomMap<Integer,String> instance = createUjoMap(3);
        String expResult = "B";
        String result = instance.remove(key);
        assertEquals(expResult, result);
        assertEquals(2, instance.size());
        //
        expResult = null;
        result = instance.remove(key);
        assertEquals(expResult, result);
        assertEquals(2, instance.size());
    }

    /**
     * Test of putAll method, of class UjoMap.
     */
    @Test
    public void testPutAll() {
        System.out.println("putAll");
        Map<Integer,String> map = createHashMap(3);
        CustomMap<Integer,String> instance = createUjoMap(0);
        instance.putAll(map);
        assertEquals(map.size(), instance.size());
        assertEquals(3, instance.size());
        assertEquals("A", instance.get(1));
        assertEquals("B", instance.get(2));
        assertEquals("C", instance.get(3));
        assertNull(instance.get(4));
        assertNull(instance.get(-1));
        //
        instance.putAll(createHashMap(9));
        assertEquals(9, instance.size());
        //
        instance.putAll(map);
        assertEquals(9, instance.size());
    }

    /**
     * Test of clear method, of class UjoMap.
     */
    @Test
    public void testClear() {
        System.out.println("clear");
        CustomMap<Integer,String> instance = createUjoMap(3);
        instance.clear();
        assertEquals(0, instance.size());
    }

    /**
     * Test of keySet method, of class UjoMap.
     */
    @Test
    public void testKeySet() {
        System.out.println("keySet");
        CustomMap<Integer,String> instance = createUjoMap(3);
        Set result = instance.keySet();
        assertTrue(result.contains(1));
        assertTrue(result.contains(2));
        assertTrue(result.contains(3));
        assertFalse(result.contains(4));
        assertFalse(result.contains(5));
    }

    /**
     * Test of keySetProxy method, of class UjoMap.
     */
    @Test
    public void testKeySetProxy() {
        System.out.println("keySetProxy");
        CustomMap<Integer,String> instance = createUjoMap(3);
        Set<MapKeyProxy<Integer>> result = instance.keySetProxy();
        assertEquals(3, result.size());
    }

    /**
     * Test of values method, of class UjoMap.
     */
    @Test
    public void testValues() {
        System.out.println("values");
        CustomMap<Integer,String> instance = createUjoMap(3);
        Collection result = instance.values();
        assertFalse(result.isEmpty());
        assertEquals("A", result.iterator().next());
    }


    // --- HELP METHODS ---

    /** Create a test map: 1-A, 2-B, 3-C, ... */
    private CustomMap<Integer, String> createUjoMap(int count) {
        final CustomMap<Integer, String> result = new CustomMap<>();
        for (int i = 0; i < count; i++) {
            result.put(1 + i, String.valueOf((char)('A' + i)));
        }
        return result;
    }

    /** Create a test map: 1-A, 2-B, 3-C, ... */
    private HashMap<Integer, String> createHashMap(int count) {
        final HashMap<Integer, String> result = new HashMap<>();
        for (int i = 0; i < count; i++) {
            result.put(1 + i, String.valueOf((char)('A' + i)));
        }
        return result;
    }
}
