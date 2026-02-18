package org.ujorm.mapper.generated;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.mapper.demo.CityUjo;

class City_Test {

    private static double DELTA = 0.00_000_000_001;

    /** Create new instance */
    @Test
    void testNewInstance_basic() {
        var e = new CityUjo();
        e.setValue(CityUjo.keyId, 10L);
        e.setValue(CityUjo.keyName, "Ottawa");
        e.setValue(CityUjo.keyCountryCode, "CA");
        e.setValue(CityUjo.keyLatitude, 45.4215);
        e.setValue(CityUjo.keyLongitude, -75.6972);
        var city = e.newInstance();

        Assertions.assertEquals(10, city.id());
        Assertions.assertEquals("Ottawa", city.name());
        Assertions.assertEquals("CA", city.countryCode());
        Assertions.assertEquals(45.4215, city.latitude(), DELTA);
        Assertions.assertEquals(-75.6972, city.longitude(), DELTA);
    }

    /** Create new instance */
    @Test
    void testNewInstance_null() {
        var e = new CityUjo();
        var city = e.newInstance();

        Assertions.assertNull(city.id());
        Assertions.assertNull(city.name());
        Assertions.assertNull(city.countryCode());
        Assertions.assertEquals(0.0, city.longitude(), DELTA);
        Assertions.assertEquals(0.0, city.latitude(), DELTA);
    }

}