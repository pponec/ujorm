package org.ujorm.mapper.generated;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.core.Key;
import org.ujorm.mapper.demo.City;

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

    public class CityUjo {
        static final City_ meta = new City_();
        static final Key<City, Long> keyId = meta.getKey("id", Long.class);
        static final Key<City, String> keyName = meta.getKey("name", String.class);
        static final Key<City, String> keyCountryCode = meta.getKey("countryCode", String.class);
        static final Key<City, Double> keyLatitude = meta.getKey("latitude", Double.class);
        static final Key<City, Double> keyLongitude = meta.getKey("longitude", Double.class);
        final Object[] array = new Object[meta.count()];

        public <V> void setValue(Key<City,V> key, V value) {
            array[key.getIndex()] = value;
        }

        public City newInstance() {
            return meta.newDomain(array);
        }
    }
}