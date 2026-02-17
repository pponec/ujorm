package org.ujorm.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.mapper.demo.Employee;

class JavaSourceGeneratorTest {

    private final boolean printResult = false;

    @Test
    void getSourceCode() {
        var meta = DomainModel.of(Employee.class);
        var src = new JavaSourceGenerator().getSourceCode(meta);

        if (printResult) System.out.println(src);
        assertContains("package org.ujorm.gen_.org.ujorm.mapper.demo;", src);
        assertContains("public class Employee_ extends AbstractMetaModel<org.ujorm.mapper.demo.Employee> {", src);
        assertContains("super( new Key_id(0)", src);
        assertContains(", new Key_name(1)", src);
        assertContains("static final class Key_id extends AbstractKey<org.ujorm.mapper.demo.Employee, java.lang.Long> {", src);
        assertContains("super(order, \"id\", java.lang.Long.class, \"id\", true, true);", src);
        assertContains("public void setValue(@NotNull final org.ujorm.mapper.demo.Employee bean, @Nullable final java.lang.Long value) {", src);
        assertContains("bean.setId(value != null ? value : defaultValue);", src);
        assertContains("public java.lang.Long getValue(@NotNull final org.ujorm.mapper.demo.Employee bean) {", src);
        assertContains("return bean.getId();", src);
        assertContains("public @NotNull Class<org.ujorm.mapper.demo.Employee> getDomainType() {", src);

        Class clazz = new ClassGenerator().createClass(src);
    }

    private void assertContains(String code, String src) {
        Assertions.assertTrue(src.contains(code), "Expected: " + code);
    }

}