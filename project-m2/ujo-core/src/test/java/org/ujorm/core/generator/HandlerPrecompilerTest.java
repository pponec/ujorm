package org.ujorm.core.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.demo.City;
import org.ujorm.core.demo.Employee;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The handler built at build time must be indistinguishable from the one the runtime
 * would have compiled on the first use of the entity.
 */
class HandlerPrecompilerTest {

    @TempDir
    Path outputDir;

    /** A record with renamed columns, a primary key and primitive components. */
    @Test
    void precompiledRecordHandlerMatchesRuntime() throws Exception {
        assertEquivalent(DomainHandlerProvider.getHandler(City.class), precompile(City.class));
    }

    /** A Lombok bean with an inherited ancestor, a foreign key and a boolean {@code is} getter. */
    @Test
    void precompiledBeanHandlerMatchesRuntime() throws Exception {
        assertEquivalent(DomainHandlerProvider.getHandler(Employee.class), precompile(Employee.class));
    }

    /** The canonical constructor of a record is called with the values in the key order. */
    @Test
    void precompiledRecordBuildsTheDomain() throws Exception {
        var handler = precompile(City.class);
        var city = handler.newDomain(7L, "Brno", "CZ", 49.2, 16.6);

        assertEquals(7L, city.id());
        assertEquals("Brno", city.name());
        assertEquals("CZ", city.countryCode());
        assertEquals(49.2, city.latitude());
        assertEquals(16.6, city.longitude());
    }

    /** The generated keys read and write the bean properties. */
    @Test
    void precompiledBeanKeysAccessTheDomain() throws Exception {
        var handler = precompile(Employee.class);
        var employee = new Employee();

        handler.getKey("name").setValue(employee, "John");
        handler.getKey("contractDay").setValue(employee, LocalDate.of(2020, 1, 1));
        handler.getKey("active").setValue(employee, true);

        assertEquals("John", employee.getName());
        assertEquals(LocalDate.of(2020, 1, 1), employee.getContractDay());
        assertTrue(employee.isActive());
        assertEquals("John", handler.getKey("name").getValue(employee));
    }

    /** The runtime resolves a handler by a calculated name, which a native image can not see. */
    @Test
    void writesGraalVmReflectionMetadata() throws Exception {
        var handlers = new HandlerPrecompiler(getClass().getClassLoader())
                .generate(List.of(City.class.getName(), Employee.class.getName()), null, outputDir);
        var config = outputDir.resolve(HandlerPrecompiler.REFLECT_CONFIG);

        assertTrue(Files.isRegularFile(config), "Missing file: " + config);
        var content = Files.readString(config);
        assertTrue(content.contains(handlers.get(0)), content);
        assertTrue(content.contains(handlers.get(1)), content);
        assertTrue(content.contains("<init>"), content);
    }

    /** A build JDK newer than the runtime one must not emit an unreadable class file. */
    @Test
    void generatedBytecodeTargetsTheReleaseOfTheDomainClass() throws Exception {
        var handlers = new HandlerPrecompiler(getClass().getClassLoader())
                .generate(List.of(City.class.getName()), null, outputDir);
        var handlerFile = outputDir.resolve(handlers.get(0).replace('.', '/') + ".class");
        var expected = HandlerPrecompiler.clampRelease(majorVersionOf(City.class) - 44) + 44;

        assertEquals(expected, majorVersionOf(Files.readAllBytes(handlerFile)),
                "The handler must share the class file version of its domain class");
    }

    /** The generated source code is available for an inspection. */
    @Test
    void writesTheGeneratedSourceCode() throws Exception {
        var javaOutputDir = outputDir.resolve("generated-sources");
        new HandlerPrecompiler(getClass().getClassLoader())
                .generate(List.of(City.class.getName()), javaOutputDir, outputDir);

        var handlerName = ClassName.ofGenerated(City.class);
        var source = javaOutputDir
                .resolve(handlerName.packageName().replace('.', '/'))
                .resolve(handlerName.className() + ".java");

        assertTrue(Files.isRegularFile(source), "Missing file: " + source);
        assertTrue(Files.readString(source).contains("class " + handlerName.className()));
    }

    /** Blank lines and comments of the entity index are skipped. */
    @Test
    void readsTheEntityIndex() throws Exception {
        var index = outputDir.resolve(HandlerPrecompiler.ENTITY_INDEX);
        Files.createDirectories(index.getParent());
        Files.writeString(index, "# A comment\n\n%s\n  %s  \n"
                .formatted(City.class.getName(), Employee.class.getName()));

        assertEquals(List.of(City.class.getName(), Employee.class.getName()),
                HandlerPrecompiler.readEntityIndex(outputDir));
        assertEquals(List.of(), HandlerPrecompiler.readEntityIndex(outputDir.resolve("missing")),
                "A missing index is not an error");
    }

    /** Reads the class file major version of a compiled class. */
    private int majorVersionOf(Class<?> type) throws Exception {
        var resource = type.getName().replace('.', '/') + ".class";
        try (var input = getClass().getClassLoader().getResourceAsStream(resource)) {
            return majorVersionOf(input.readNBytes(8));
        }
    }

    /** Reads the class file major version from the header bytes. */
    private int majorVersionOf(byte[] classFile) {
        return ((classFile[6] & 0xFF) << 8) | (classFile[7] & 0xFF);
    }

    /** Generates the handler of the domain class and loads it from the output directory. */
    @SuppressWarnings("unchecked")
    private <D> DomainHandler<D> precompile(Class<D> domainClass) throws Exception {
        var handlers = new HandlerPrecompiler(getClass().getClassLoader())
                .generate(List.of(domainClass.getName()), null, outputDir);
        assertEquals(1, handlers.size());

        // The parent does not know the generated class, so it is loaded from the output directory:
        var loader = new URLClassLoader(new URL[] {outputDir.toUri().toURL()}, getClass().getClassLoader());
        var handlerClass = Class.forName(handlers.get(0), true, loader);
        return (DomainHandler<D>) handlerClass.getConstructor().newInstance();
    }

    /** Compares the pre-compiled handler with the reference one built by the runtime. */
    private void assertEquivalent(DomainHandler<?> expected, DomainHandler<?> actual) {
        assertNotSame(expected.getClass(), actual.getClass(), "The handlers must be built independently");
        assertEquals(expected.getDomainClass(), actual.getDomainClass());
        assertEquals(expected.getDatabaseTable(), actual.getDatabaseTable());

        var expectedKeys = expected.getKeyList();
        var actualKeys = actual.getKeyList();
        assertEquals(expectedKeys.size(), actualKeys.size(), "Count of the keys");

        for (var i = 0; i < expectedKeys.size(); i++) {
            var e = expectedKeys.get(i);
            var a = actualKeys.get(i);
            var msg = "Key #%s: %s <> %s".formatted(i, e.name(), a.name());

            assertEquals(e.name(), a.name(), msg);
            assertEquals(e.index(), a.index(), msg);
            assertEquals(e.type(), a.type(), msg);
            assertEquals(e.domainClass(), a.domainClass(), msg);
            assertEquals(e.info().columnLabel(), a.info().columnLabel(), msg);
            assertEquals(e.info().primaryKey(), a.info().primaryKey(), msg);
            assertEquals(e.info().foreignKey(), a.info().foreignKey(), msg);
            assertEquals(e.info().required(), a.info().required(), msg);
            assertEquals(e.info().writable(), a.info().writable(), msg);
            assertEquals(e.info().mapEnumByOrdinal(), a.info().mapEnumByOrdinal(), msg);
        }
    }
}
