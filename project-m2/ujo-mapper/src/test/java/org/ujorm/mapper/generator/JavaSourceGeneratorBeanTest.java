package org.ujorm.mapper.generator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.mapper.demo.Employee;

class JavaSourceGeneratorBeanTest {

    private final boolean printResult = false;

    @Test
    void getSourceCode() {
        var meta = DomainModel.of(Employee.class);
        var src = new JavaSourceGenerator().getSourceCode(meta);

        if (printResult) System.out.println(src);
        assertContains("package org.ujorm.gen_.org.ujorm.mapper.demo;", src);
        assertContains("public final class Employee_ extends AbstractDomainHandler<Employee> {", src);
        assertContains("super( new Key_id(0)", src);
        assertContains(", new Key_name(1)", src);

        assertContains("public Employee newDomain(@NotNull Object... values) {", src);
        assertContains("final var result = new Employee();", src);
        assertContains("final var key = (AbstractKey<Employee, Object>) keyList.get(i)", src);

        assertContains("static final class Key_id extends", src);
        assertContains("super(order, \"id\", java.lang.Long.class, \"id\", true, true);", src);
        assertContains("public void setValue(@NotNull final Employee bean, @Nullable final java.lang.Long value) {", src);
        assertContains("bean.setId(value != null ? value : defaultValue);", src);
        assertContains("public java.lang.Long getValue(@NotNull final Employee bean) {", src);
        assertContains("return bean.getId();", src);
        assertContains("public @NotNull Class<Employee> getDomainClass() {", src);

        var clazz = new ClassGenerator().createClass(src);
        Assertions.assertNotNull(clazz);
    }

    private void assertContains(String code, String src) {
        Assertions.assertTrue(src.contains(code), "Expected: " + code);
    }

}