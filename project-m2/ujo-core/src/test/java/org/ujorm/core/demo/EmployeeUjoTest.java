package org.ujorm.core.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class EmployeeUjoTest {

    @Test
    void readWrite() {
        var employee = new EmployeeUjo();
        var keyId = EmployeeUjo.keyId;
        employee.setValue(EmployeeUjo.keyId, 1L);
        var id = employee.getValue(EmployeeUjo.keyId);

        Assertions.assertEquals("id", keyId.getName());
        Assertions.assertEquals("id", keyId.toString());
        Assertions.assertEquals(Long.class, keyId.getType());
        Assertions.assertEquals(Employee.class, keyId.getDomainClass());
        Assertions.assertEquals(1L, id);
        Assertions.assertEquals(1L, employee.array()[0]);
    }

}