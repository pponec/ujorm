package org.ujorm.core.generator;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Tests of the converter declarations */
public class ConverterResolverTest {

    // --- Converters ---

    public static class YesNoConverter implements AttributeConverter<Boolean, String> {
        @Override public String convertToDatabaseColumn(Boolean value) { return value ? "Y" : "N"; }
        @Override public Boolean convertToEntityAttribute(String dbValue) { return "Y".equals(dbValue); }
    }

    public abstract static class ListConverter<T> implements AttributeConverter<List<T>, String> {
        @Override public String convertToDatabaseColumn(List<T> value) { return value.toString(); }
        @Override public List<T> convertToEntityAttribute(String dbValue) { return List.of(); }
    }

    public static class StringListConverter extends ListConverter<String> {}

    /** The type argument is a type variable */
    public static class RawConverter<T> implements AttributeConverter<T, String> {
        @Override public String convertToDatabaseColumn(T value) { return ""; }
        @Override public T convertToEntityAttribute(String dbValue) { return null; }
    }

    static class HiddenConverter extends YesNoConverter {}

    public static class NoDefaultConstructor extends YesNoConverter {
        public NoDefaultConstructor(String arg) {}
    }

    public enum Color { RED, GREEN }

    // --- Domains ---

    record Valid(@Convert(converter = YesNoConverter.class) boolean active,
                 @Convert(converter = StringListConverter.class) List<String> tags,
                 Color color,
                 @Enumerated(EnumType.ORDINAL) Color colorOrdinal,
                 @Convert(disableConversion = true) Color colorDisabled,
                 String name) {}

    record OnId(@Id @Convert(converter = YesNoConverter.class) Boolean id) {}
    record OnRelation(@JoinColumn(name = "x_id") @Convert(converter = YesNoConverter.class) Boolean relation) {}
    record WithEnumerated(@Enumerated @Convert(converter = YesNoConverter.class) Boolean value) {}
    record TypeMismatch(@Convert(converter = YesNoConverter.class) String value) {}
    record Unresolved(@Convert(converter = RawConverter.class) String value) {}
    record NotConverter(@Convert(converter = String.class) String value) {}
    record Hidden(@Convert(converter = HiddenConverter.class) Boolean value) {}
    record NoConstructor(@Convert(converter = NoDefaultConstructor.class) Boolean value) {}
    record AttributeName(@Convert(converter = YesNoConverter.class, attributeName = "key") Boolean value) {}

    @Test
    void validConverters() {
        var props = DomainPropertyModel.of(Valid.class);
        var prefix = "org.ujorm.core.converter.";
        assertEquals(prefix + "JpaConverter.of(new org.ujorm.core.generator.ConverterResolverTest.YesNoConverter(),"
                + " java.lang.String.class)", props.get(0).converter());
        assertEquals(prefix + "JpaConverter.of(new org.ujorm.core.generator.ConverterResolverTest.StringListConverter(),"
                + " java.lang.String.class)", props.get(1).converter());
        assertEquals(prefix + "EnumNameConverter.of(org.ujorm.core.generator.ConverterResolverTest.Color.class)",
                props.get(2).converter());
        assertEquals(prefix + "EnumOrdinalConverter.of(org.ujorm.core.generator.ConverterResolverTest.Color.class)",
                props.get(3).converter());
        assertEquals(prefix + "EnumNameConverter.of(org.ujorm.core.generator.ConverterResolverTest.Color.class)",
                props.get(4).converter());
        assertNull(props.get(5).converter());
    }

    @Test
    void invalidConverters() {
        assertError(OnId.class, "OnId.id: a primary key can't have a converter");
        assertError(OnRelation.class, "OnRelation.relation: a relation can't have a converter");
        assertError(WithEnumerated.class, "the @Convert and @Enumerated annotations can't be combined");
        assertError(TypeMismatch.class, "converts the type java.lang.Boolean rather than java.lang.String");
        assertError(Unresolved.class, "the type arguments of the converter");
        assertError(NotConverter.class, "doesn't implement the AttributeConverter");
        assertError(Hidden.class, "must be a public top-level or static nested class");
        assertError(NoConstructor.class, "with a public constructor without arguments");
        assertError(AttributeName.class, "the attribute name of the @Convert annotation is not supported");
    }

    private void assertError(Class<?> domain, String expectedPart) {
        var e = assertThrows(IllegalArgumentException.class, () -> DomainPropertyModel.of(domain));
        assertTrue(e.getMessage().contains(expectedPart), e.getMessage());
    }
}
