package org.ujorm.core.generator;

import org.junit.jupiter.api.Test;
import org.ujorm.annotation.*;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.converter.DbConverter;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A domain class annotated by the Ujorm annotations only works without the Jakarta Persistence API.
 * The classes of Ujorm are loaded by a class loader which hides the {@code jakarta.persistence} package.
 */
public class JakartaFreeTest {

    @UTable(name = "town")
    public record City(@UId Long id, @UColumn(name = "town_name", nullable = false) String name) {}

    public record Person(
            @UId Long id,
            @UManyToOne @UJoinColumn(name = "town_id", nullable = false) City city,
            @UConvert(converter = YesNoConverter.class) Boolean active,
            @UEnumerated Color color,
            @UTransient String note) {
    }

    public enum Color { RED, GREEN }

    public static class YesNoConverter implements DbConverter<Boolean, String> {
        @Override public Class<String> dbType() { return String.class; }
        @Override public String toDb(Boolean value) { return value ? "Y" : "N"; }
        @Override public Boolean fromDb(String value) { return "Y".equals(value); }
    }

    /** Runs in the isolated class loader, it returns the Jakarta availability, the model and its hash. */
    public static class Probe implements Function<Class<?>, List<String>> {
        @Override
        public List<String> apply(Class<?> domain) {
            var model = DomainModel.of(domain);
            var hash = new JavaSourceGenerator().getModelHash(model, ClassName.ofGenerated(domain));
            return List.of(String.valueOf(MappingAnnotations.JAKARTA_AVAILABLE), model.toString(), hash);
        }
    }

    @Test
    void withoutJakarta() throws Exception {
        var loader = new HidingClassLoader(getClass().getClassLoader());
        assertThrows(ClassNotFoundException.class, () -> Class.forName("jakarta.persistence.Column", false, loader));

        for (var domain : List.of(City.class, Person.class)) {
            var isolated = run(loader, domain);
            var regular = new Probe().apply(domain);

            assertEquals("false", isolated.get(0), "Jakarta is hidden");
            assertEquals("true", regular.get(0), "Jakarta is available");
            assertEquals(regular.get(1), isolated.get(1), "The same model");
            assertEquals(regular.get(2), isolated.get(2), "The same MODEL_HASH");
        }
    }

    /** The handler compiled at runtime by the regular class loader. */
    @Test
    void handler() {
        var handler = DomainHandlerProvider.getHandler(Person.class);
        assertEquals("town_id", handler.getKey("city").info().columnLabel());
        assertTrue(handler.getKey("city").info().foreignKey());
        assertEquals(YesNoConverter.class, handler.getKey("active").info().converter().getClass());
        assertEquals(4, handler.count(), "The note is transient");
    }

    @SuppressWarnings("unchecked")
    private static List<String> run(ClassLoader loader, Class<?> domain) throws Exception {
        var probe = (Function<Class<?>, List<String>>) loader.loadClass(Probe.class.getName())
                .getConstructor().newInstance();
        return probe.apply(loader.loadClass(domain.getName()));
    }

    /** Defines the Ujorm classes itself and hides the Jakarta Persistence API. */
    static final class HidingClassLoader extends ClassLoader {

        HidingClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.startsWith("jakarta.persistence.")) {
                throw new ClassNotFoundException(name);
            }
            if (!name.startsWith("org.ujorm.")) {
                return super.loadClass(name, resolve);
            }
            synchronized (getClassLoadingLock(name)) {
                var result = findLoadedClass(name);
                if (result == null) {
                    try (var input = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                        if (input == null) {
                            throw new ClassNotFoundException(name);
                        }
                        var bytes = input.readAllBytes();
                        result = defineClass(name, bytes, 0, bytes.length);
                    } catch (IOException e) {
                        throw new ClassNotFoundException(name, e);
                    }
                }
                return result;
            }
        }
    }
}
