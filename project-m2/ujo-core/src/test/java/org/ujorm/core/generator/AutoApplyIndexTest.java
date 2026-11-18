package org.ujorm.core.generator;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.ujorm.core.converter.JpaConverter;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests of the index of the auto-applied converters.
 * The index is written to a temporary directory, because the converters below are invalid on purpose.
 */
public class AutoApplyIndexTest {

    @TempDir
    Path indexDir;

    public record Phone(String value) {}

    public enum Color { RED }

    @Converter(autoApply = true)
    public static class PhoneConverter implements AttributeConverter<Phone, String> {
        @Override public String convertToDatabaseColumn(Phone value) { return value.value(); }
        @Override public Phone convertToEntityAttribute(String dbValue) { return new Phone(dbValue); }
    }

    @Converter(autoApply = true)
    public static class OtherPhoneConverter extends PhoneConverter {}

    /** The annotation was removed after the index has been written */
    public static class StalePhoneConverter extends PhoneConverter {}

    @Converter(autoApply = true)
    public static class BooleanConverter implements AttributeConverter<Boolean, String> {
        @Override public String convertToDatabaseColumn(Boolean value) { return value ? "Y" : "N"; }
        @Override public Boolean convertToEntityAttribute(String dbValue) { return "Y".equals(dbValue); }
    }

    @Converter(autoApply = true)
    public static class ColorConverter implements AttributeConverter<Color, String> {
        @Override public String convertToDatabaseColumn(Color value) { return "R"; }
        @Override public Color convertToEntityAttribute(String dbValue) { return Color.RED; }
    }

    @Converter(autoApply = true)
    static class HiddenConverter extends PhoneConverter {}

    @Test
    void validIndex() throws Exception {
        var result = load("# comment", "", PhoneConverter.class.getName(),
                PhoneConverter.class.getName(), // a duplicate of the same class is ignored
                StalePhoneConverter.class.getName(),
                "org.ujorm.MissingConverter");
        assertEquals(Map.of(Phone.class, PhoneConverter.class), result);
    }

    @Test
    void invalidIndex() {
        assertError("is auto-applied to the type", PhoneConverter.class, OtherPhoneConverter.class);
        assertError("not allowed for the type java.lang.Boolean", BooleanConverter.class);
        assertError("not allowed for the type " + Color.class.getName(), ColorConverter.class);
        assertError("must be a public top-level or static nested class", HiddenConverter.class);
    }

    private Map<Class<?>, Class<?>> load(String... lines) throws Exception {
        var index = indexDir.resolve(JpaConverter.CONVERTER_INDEX);
        Files.createDirectories(index.getParent());
        Files.write(index, List.of(lines));
        try (var loader = new URLClassLoader(new URL[] {indexDir.toUri().toURL()}, getClass().getClassLoader())) {
            return new ConverterResolver().loadAutoApplied(loader);
        }
    }

    private void assertError(String expectedPart, Class<?>... converters) {
        var names = java.util.Arrays.stream(converters).map(Class::getName).toArray(String[]::new);
        var e = assertThrows(IllegalArgumentException.class, () -> load(names));
        assertTrue(e.getMessage().startsWith("Invalid auto-applied converter "), e.getMessage());
        assertTrue(e.getMessage().contains(expectedPart), e.getMessage());
    }
}
