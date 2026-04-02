package org.ujorm.core.demo.generated;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.core.demo.City;
import org.ujorm.core.demo.Employee;
import org.ujorm.core.demo.EmployeeUjo;
import java.time.LocalDate;

class Employee_Test {

    @Test
    void managementTest() {
        final var instance = new Employee_();
        Assertions.assertEquals(Employee.class, instance.getDomainClass());
        Assertions.assertEquals(6, instance.count());

        var employee = instance.newDomain();
        EmployeeUjo.keyId.setValue(employee, 10L);
        EmployeeUjo.keyName.setValue(employee, "Joe");
        EmployeeUjo.keyCity.setValue(employee, City.of(9L, "California"));
        EmployeeUjo.keyActive.setValue(employee, true);

        Assertions.assertEquals(10, employee.getId());
        Assertions.assertEquals("Joe", employee.getName());
        Assertions.assertEquals(true, employee.isActive());
        Assertions.assertEquals("California", employee.getCity().name());

        Assertions.assertEquals(10, EmployeeUjo.keyId.getValue(employee));
        Assertions.assertEquals("Joe", EmployeeUjo.keyName.getValue(employee));
        Assertions.assertEquals(true, EmployeeUjo.keyActive.getValue(employee));
        Assertions.assertEquals("California", EmployeeUjo.keyCity.getValue(employee).name());

        Assertions.assertNull(EmployeeUjo.keyCity.getDefaultValue());
        Assertions.assertFalse(EmployeeUjo.keyActive.getDefaultValue());
    }

    /** Create new instance */
    @Test
    void testNewInstance_basic() {
        var e = new EmployeeUjo();
        e.setValue(EmployeeUjo.keyId, 10L);
        e.setValue(EmployeeUjo.keyName, "Test");
        e.setValue(EmployeeUjo.keyBoss, null);
        e.setValue(EmployeeUjo.keyCity, City.of(1L, "California"));
        e.setValue(EmployeeUjo.keyContractDay, LocalDate.of(2026, 02, 17));
        e.setValue(EmployeeUjo.keyActive, true);
        var employee = e.newDomain();

        Assertions.assertEquals(10, employee.getId());
        Assertions.assertEquals("Test", employee.getName());
        Assertions.assertNull(employee.getBoss());
        Assertions.assertEquals("California", employee.getCity().name());
        Assertions.assertEquals(2026, employee.getContractDay().getYear());
        Assertions.assertTrue(employee.isActive());
    }

    /** Create new instance */
    @Test
    void testNewInstance_null() {
        var e = new EmployeeUjo();
        var employee = e.newDomain();

        Assertions.assertNull(employee.getId());
        Assertions.assertNull(employee.getName());
        Assertions.assertNull(employee.getCity());
        Assertions.assertNull(employee.getBoss());
        Assertions.assertNull(employee.getContractDay());
        Assertions.assertFalse(employee.isActive());
    }

}