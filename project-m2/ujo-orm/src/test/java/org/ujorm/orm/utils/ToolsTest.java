package org.ujorm.orm.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.orm.demo.Employee;

class ToolsTest {

    @Test
    void findChanges() {
        var handler = DomainHandlerProvider.getHandler(Employee.class);
        var domain1 = Employee.of(10L, "Test", true);
        var domain2 = Employee.of(11L, "Demo", false);
        var diff = Tools.findChanges(domain1, domain2, handler).getActive();
        Assertions.assertEquals(3, diff.length);

        domain2 = Employee.of(10L, "Test", true);
        diff = Tools.findChanges(domain1, domain2, handler).getActive();
        Assertions.assertEquals(0, diff.length);
    }
}