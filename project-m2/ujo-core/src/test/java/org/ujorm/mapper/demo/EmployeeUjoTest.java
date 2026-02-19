package org.ujorm.mapper.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class EmployeeUjoTest {

    //@Test
    void readWrite() {
        var employee = new EmployeeUjo();
        employee.setValue(EmployeeUjo.keyId, 1L);
        var id = employee.getValue(EmployeeUjo.keyId);

        Assertions.assertEquals(1L, id);
        Assertions.assertEquals(1L, employee.array()[0]);
    }

}