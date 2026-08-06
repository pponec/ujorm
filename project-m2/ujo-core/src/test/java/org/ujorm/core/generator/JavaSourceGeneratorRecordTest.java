package org.ujorm.core.generator;

import jakarta.persistence.Transient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.demo.City;

@SuppressWarnings("java:S5786")
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


    /** A transient component keeps its position in the canonical constructor. */
    @Test
    @SuppressWarnings("unchecked")
    void getSourceCodeForTransientComponent() throws Exception {
        var meta = DomainModel.of(CityTransient.class);
        var className = ClassName.ofGenerated(meta);
        var src = new JavaSourceGenerator().getSourceCode(meta, className);

        if (printResult) System.out.println(src);
        Assertions.assertEquals(2, meta.properties().size(), "The note is out of the model");
        assertContains("( (java.lang.Long) values[0]", src);
        assertContains(", (java.lang.String) null", src);
        assertContains(", (java.lang.String) values[1]", src);
        assertContains(", (int) 0", src);
        assertNotContains(", (boolean) 0", src);

        var clazz = new ClassGenerator().createClass(src, className);
        var handler = (DomainHandler<CityTransient>) clazz.getDeclaredConstructor().newInstance();
        var city = handler.newDomain(10L, "Prague");

        Assertions.assertEquals(10L, city.id());
        Assertions.assertEquals("Prague", city.name());
        Assertions.assertNull(city.note(), "A transient component gets the default value");
        Assertions.assertEquals(0, city.code());
        Assertions.assertFalse(city.active());
    }

    private void assertContains(String code, String src) {
        Assertions.assertTrue(src.contains(normalize(code)), "Expected: " + code);
    }

    private void assertNotContains(String code, String src) {
        Assertions.assertFalse(src.contains(normalize(code)), "Unexpected: " + code);
    }

    /** The generated source code has no Jetbrains annotations by default. */
    private String normalize(String code) {
        return code
                .replace("@NotNull", "")
                .replace("@Nullable", "");
    }

    public record CityInner(String name) {}

    public record CityTransient(
            Long id,
            @Transient String note,
            String name,
            @Transient int code,
            @Transient boolean active) {}

}
