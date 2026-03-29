package org.ujorm.core.generator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.core.demo.City;

public class JavaSourceGeneratorRecordTest {

    private final boolean printResult = false;

    @Test
    void getSourceCode() {
        var meta = DomainModel.of(City.class);
        var className = ClassName.ofGenerated(meta);
        var src = new JavaSourceGenerator().getSourceCode(meta, className);

        if (printResult) System.out.println(src);
        assertContains("package org.ujorm.gen_.org.ujorm.core.demo", src);
        assertContains("public final class City_ extends AbstractDomainHandler<City> {", src);
        assertContains("super( new Key_id(0)", src);
        assertContains(", new Key_countryCode(2)", src);

        assertContains("public City newDomain(@NotNull Object... values) {", src);
        assertContains("values = normalizePrimitives(values);", src);
        assertContains("return new City", src);
        assertContains(", (java.lang.String) values[1]", src);

        assertContains("static final class Key_id extends AbstractKey<City, java.lang.Long> {", src);
        assertContains("super(order, \"id\", java.lang.Long.class, \"db_id\", true, false, true, false);", src);
        assertContains("throw unsupportedSetter(this);", src);

        var clazz = new ClassGenerator().createClass(src, className);
        Assertions.assertNotNull(clazz);
    }

    @Test
    void getSourceCodeForInnerClass() {
        var meta = DomainModel.of(CityInner.class);
        var className = ClassName.ofGenerated(meta);
        var src = new JavaSourceGenerator().getSourceCode(meta, className);

        if (printResult) System.out.println(src);
        assertContains("org.ujorm.gen_.org.ujorm.core.generator.JavaSourceGeneratorRecordTest", src);


        var clazz = new ClassGenerator().createClass(src, className);
        Assertions.assertNotNull(clazz);
    }


    private void assertContains(String code, String src) {
        code = code
                .replace("@NotNull", "")
                .replace("@Nullable", "");
        Assertions.assertTrue(src.contains(code), "Expected: " + code);
    }

    public record CityInner(String name) {}

}