package org.ujorm.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.core.demo.Employee;

public class DomainHandlerServiceTest {

    @Test
    void getHandler() {
        var service = new DomainHandlerService();
        var handler = service.getHandler(Employee.class);
        var key = handler.getKey("id", Long.class);
        Assertions.assertNotNull(key);
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