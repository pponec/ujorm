package org.ujorm.mapper.utils;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultiMapTest {

    /** Tests adding and retrieving elements */
    @Test
    void put() {
        var map = new MultiMap<String, String>();

        map.put("key1", "value1");
        map.put("key1", "value2");
        map.put("key2", "value3");

        assertEquals(List.of("value1", "value2"), map.get("key1"));
        assertEquals(List.of("value3"), map.get("key2"));

        // Verifies that a missing key returns an empty list, not null
        assertTrue(map.get("missingKey").isEmpty());
    }

}