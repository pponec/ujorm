package org.ujorm.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.mapper.demo.Employee;
import org.ujorm.mapper.demo.generated.Employee_;

import static org.junit.jupiter.api.Assertions.*;

class DomainHandlerServiceTest {

    @Test
    void getHandler() {
        var service = new DomainHandlerService();
        var handler = service.getHandler(Employee.class);
        var key = handler.getKey("id", Long.class);
        Assertions.assertNotNull(key);
    }
}