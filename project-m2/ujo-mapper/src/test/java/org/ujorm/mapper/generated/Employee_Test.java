package org.ujorm.mapper.generated;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.mapper.generated.demo.City;
import org.ujorm.mapper.generated.demo.Employee;

class Employee_Test {

    @Test
    void managementTest() {
        final var instance = new Employee_();
        Assertions.assertEquals(Employee.class, instance.getDomainType());
        Assertions.assertEquals(5, instance.count());

        var keyId = instance.getKey("id", Long.class);
        var keyName = instance.getKey("name", String.class);
        var keyCity = instance.getKey("city", City.class);
        var employee = instance.newDomain();

        keyId.setValue(employee, 10L);
        keyName.setValue(employee, "Joe");
        keyCity.setValue(employee, City.of(9L, "California"));

        Assertions.assertEquals(10, employee.getId());
        Assertions.assertEquals("Joe", employee.getName());
        Assertions.assertEquals("California", employee.getCity().name());

        Assertions.assertEquals(10, keyId.getValue(employee));
        Assertions.assertEquals("Joe", keyName.getValue(employee));
        Assertions.assertEquals("California", keyCity.getValue(employee).name());
    }
}