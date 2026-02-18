package org.ujorm.mapper.generator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.mapper.demo.City;

class JavaSourceGeneratorRecordTest {

    private final boolean printResult = !false;

    @Test
    void getSourceCode() {
        var meta = DomainModel.of(City.class);
        var className = ClassName.forGenerated(meta);
        var src = new JavaSourceGenerator().getSourceCode(meta, className);

        if (printResult) System.out.println(src);
        assertContains("package org.ujorm.gen_.org.ujorm.mapper.demo", src);
        assertContains("public final class City_ extends AbstractDomainHandler<City> {", src);
        assertContains("super( new Key_id(0)", src);
        assertContains(", new Key_countryCode(2)", src);

        assertContains("public City newDomain(@NotNull Object... values) {", src);
        assertContains("values = normalizePrimitives(values);", src);
        assertContains("return new City", src);
        assertContains(", (java.lang.String) values[1]", src);

        assertContains("static final class Key_id extends AbstractKey<City, java.lang.Long> {", src);
        assertContains("super(order, \"id\", java.lang.Long.class, \"id\", true, true);", src);
        assertContains("throw unsupportedSetter(this);", src);

        var clazz = new ClassGenerator().createClass(src, className);
        Assertions.assertNotNull(clazz);
    }

    private void assertContains(String code, String src) {
        Assertions.assertTrue(src.contains(code), "Expected: " + code);
    }

}