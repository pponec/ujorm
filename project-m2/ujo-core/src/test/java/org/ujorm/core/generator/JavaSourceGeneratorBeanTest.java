package org.ujorm.core.generator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.core.demo.Employee;

import java.util.List;

/**
 * A JUnit 5 test needs no {@code public} modifier, but this one hosts the nested {@link BeanInner}
 * class, whose metamodel is generated into the {@code org.ujorm.gen_.*} package. A nested public
 * class of a package-private outer class is unreachable from there, so the generated source code
 * fails to compile. Hence the modifier and the suppression of the "no public test class" rule.
 */
@SuppressWarnings("java:S5786")
public class JavaSourceGeneratorBeanTest {

    private final boolean printResult = false;

    @Test
    void getSourceCode() {
        var meta = DomainModel.of(Employee.class);
        var className = ClassName.ofGenerated(meta);
        var src = new JavaSourceGenerator().getSourceCode(meta, className);

        if (printResult) System.out.println(src);
        assertContains("package org.ujorm.gen_.org.ujorm.core.demo;", src);
        assertContains("public final class Employee_ extends AbstractDomainHandler<Employee> {", src);
        assertContains("super( new Key_id(0)", src);
        assertContains(", new Key_name(1)", src);

        assertContains("public Employee newDomain( Object... values) {", src);
        assertContains("final var result = new Employee();", src);
        assertContains("final var key = (AbstractKey<Employee, Object>) keyList.get(i)", src);

        assertContains("static final class Key_id extends", src);
        assertContains("super(order, \"id\", java.lang.Long.class, \"id\", true, false, true, false);", src);
        assertContains("public void setValue(@NotNull final Employee bean, @Nullable final java.lang.Long value) {", src);
        assertContains("bean.setId(value != null ? value : defaultValue);", src);
        assertContains("public java.lang.Long getValue(@NotNull final Employee bean) {", src);
        assertContains("return bean.getId();", src);
        assertContains("public @NotNull Class<Employee> domainClass() {", src);

        var clazz = new ClassGenerator().createClass(src, className);
        Assertions.assertNotNull(clazz);
    }

    /** A bean property with a getter but no setter must not break the generated source code. */
    @Test
    void getSourceCodeForPropertyWithoutSetter() {
        var meta = DomainModel.of(BeanInner.class);
        var className = ClassName.ofGenerated(meta);
        var src = new JavaSourceGenerator().getSourceCode(meta, className);

        if (printResult) System.out.println(src);
        assertContains("bean.setName(value != null ? value : defaultValue);", src);
        assertContains("throw unsupportedSetter(this);", src);
        assertContains("return bean.isWebRelease();", src);
        Assertions.assertFalse(src.contains("${"), "No template placeholder can survive");

        var clazz = new ClassGenerator().createClass(src, className);
        Assertions.assertNotNull(clazz);
    }

    /**
     * The metamodel keeps a property without a setter, so a non-persistent domain object
     * can read it. Assigning the value fails to keep an application error loud.
     */
    @Test
    void keyOfPropertyWithoutSetterIsReadable() throws Exception {
        var meta = DomainModel.of(BeanInner.class);
        var className = ClassName.ofGenerated(meta);
        var src = new JavaSourceGenerator().getSourceCode(meta, className);
        var clazz = new ClassGenerator().createClass(src, className);
        var handler = (DomainHandler<BeanInner>) clazz.getDeclaredConstructor().newInstance();

        Key<BeanInner, String> nameKey = handler.getKey("name");
        Key<BeanInner, Boolean> webReleaseKey = handler.getKey("webRelease");

        Assertions.assertTrue(nameKey.info().writable(), "The name has a setter");
        Assertions.assertFalse(webReleaseKey.info().writable(), "The webRelease has no setter");

        var domain = new BeanInner();
        nameKey.setValue(domain, "Ann");
        Assertions.assertEquals("Ann", nameKey.getValue(domain), "A writable value is assigned");
        Assertions.assertEquals(Boolean.FALSE, webReleaseKey.getValue(domain), "A read-only value is available");

        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> webReleaseKey.setValue(domain, true), "The setter is missing");
    }

    /**
     * The positional factory refuses a value belonging to a property without a setter, rather
     * than dropping it in silence. The loop reaches the position only when the caller supplies
     * it, so a shorter array is accepted.
     */
    @Test
    @SuppressWarnings("unchecked")
    void newDomainRefusesAValueOfThePropertyWithoutSetter() throws Exception {
        var meta = DomainModel.of(BeanInner.class);
        var className = ClassName.ofGenerated(meta);
        var src = new JavaSourceGenerator().getSourceCode(meta, className);
        var clazz = new ClassGenerator().createClass(src, className);
        var handler = (DomainHandler<BeanInner>) clazz.getDeclaredConstructor().newInstance();

        Assertions.assertEquals(List.of("name", "webRelease"),
                handler.getKeyList().stream().map(Key::name).toList(),
                "The read-only property is modelled, hence it occupies a position");

        var result = Assertions.assertThrows(UnsupportedOperationException.class,
                () -> handler.newDomain("Ann", Boolean.TRUE),
                "A value of the read-only position is refused");
        Assertions.assertTrue(result.getMessage().contains("BeanInner.webRelease"), result.getMessage());

        var domain = handler.newDomain("Ann");
        Assertions.assertEquals("Ann", domain.getName(), "A shorter array assigns the writable prefix");
        Assertions.assertNotNull(handler.newDomain(), "An empty factory call is supported");
    }

    private void assertContains(String code, String src) {
        code = code
                .replace("@NotNull", "")
                .replace("@Nullable", "");
        Assertions.assertTrue(src.contains(code), "Expected: " + code);
    }

    public static class BeanInner {
        private String name;
        /** A property with a getter only */
        private boolean webRelease;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isWebRelease() {
            return webRelease;
        }
    }

}
