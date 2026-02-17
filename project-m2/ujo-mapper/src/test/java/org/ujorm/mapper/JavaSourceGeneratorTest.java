package org.ujorm.mapper;

import org.junit.jupiter.api.Test;
import org.ujorm.mapper.demo.Employee;

class JavaSourceGeneratorTest {

    @Test
    void getSourceCode() {

        DomainModel.of(Employee.class);
        var meta = "";

    }
}