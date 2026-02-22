package org.ujorm.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.core.demo.City;
import org.ujorm.core.demo.Employee;

public class DomainHandlerServiceTest {

    @Test
    void getHandler() {
        var service = new DomainHandlerService();
        var handler = service.getHandler(Employee.class);
        var keyId = handler.getKey("id", Long.class);
        var keyCity = handler.getKey("city", City.class);
        var keySuperior = handler.getKey("superior", Employee.class);

        Assertions.assertNotNull(keyId);
        Assertions.assertEquals(Long.class, keyId.getType());
        Assertions.assertEquals(City.class, keyCity.getType());
        Assertions.assertEquals(Employee.class, keySuperior.getType());
    }

    @Test
    void getHandlerOfInnerClass() {
        var service = new DomainHandlerService();
        var handler = service.getHandler(CityInner.class);
        var key = handler.getKey("name", String.class);
        Assertions.assertNotNull(key);
    }

    /**
     * Simple Java Record representing a City.
     */
    public record CityInner(String name) {}
}