package org.ujorm.mapper;

import org.junit.jupiter.api.Test;
import org.ujorm.mapper.demo.Employee;

class JavaSourceGeneratorTest {

    @Test
    void getSourceCode() {
        var meta = DomainModel.of(Employee.class);
        var src = new JavaSourceGenerator().getSourceCode(meta);

        System.out.println(src);

    }
}