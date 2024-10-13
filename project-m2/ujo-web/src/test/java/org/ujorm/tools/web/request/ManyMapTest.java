package org.ujorm.tools.web.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManyMapTest {

    @Test
    void get() {

        ManyMap map = new ManyMap();
        map.put("p1", "v1");
        map.put("p2", "v2a");
        map.put("p2", "v2b");

        assertEquals(0, map.get("p0").length);
        assertEquals(1, map.get("p1").length);
        assertEquals(2, map.get("p2").length);

        assertEquals("v1", map.get("p1")[0]);
        assertEquals("v2b", map.get("p2")[1]);
    }
}