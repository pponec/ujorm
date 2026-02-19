package org.ujorm.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.core.demo.Employee;

class DomainHandlerServiceTest {

    @Test
    void getHandler() {
        var service = new DomainHandlerService();
        var handler = service.getHandler(Employee.class);
        var key = handler.getKey("id", Long.class);
        Assertions.assertNotNull(key);
    }
}