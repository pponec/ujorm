package org.ujorm.mapper;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.mapper.demo.City;
import org.ujorm.mapper.demo.Employee;

class JavaSourceGeneratorRecordTest {

    private final boolean printResult = false;

    @Test
    void getSourceCode() {
        var meta = DomainModel.of(City.class);
        var src = new JavaSourceGenerator().getSourceCode(meta);

        if (printResult) System.out.println(src);
        assertContains("package org.ujorm.gen_.org.ujorm.mapper.demo", src);
        assertContains("public class City_ extends AbstractMetaModel<org.ujorm.mapper.demo.City> {", src);
        assertContains("super( new Key_id(0)", src);
        assertContains(", new Key_countryCode(2)", src);

        assertContains("public org.ujorm.mapper.demo.City newDomain(@NotNull Object... values) {", src);
        assertContains("values = normalizePrimitives(values);", src);
        assertContains("return new org.ujorm.mapper.demo.City", src);
        assertContains(", (java.lang.String) values[1]", src);

        assertContains("static final class Key_id extends AbstractKey<org.ujorm.mapper.demo.City, java.lang.Long> {", src);
        assertContains("super(order, \"id\", java.lang.Long.class, \"id\", true, true);", src);
        assertContains("throw unsupportedSetter(this);", src);

        var clazz = new ClassGenerator().createClass(src);
        Assertions.assertNotNull(clazz);
    }

    private void assertContains(String code, String src) {
        Assertions.assertTrue(src.contains(code), "Expected: " + code);
    }

}