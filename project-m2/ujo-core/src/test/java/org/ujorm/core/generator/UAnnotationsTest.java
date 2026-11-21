package org.ujorm.core.generator;

import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.ujorm.annotation.*;
import org.ujorm.core.converter.DbConverter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** The Ujorm mapping annotations ({@code @UColumn}, ...) versus the Jakarta Persistence ones */
public class UAnnotationsTest {

    // --- Converters ---

    /** A Ujorm converter */
    public static class YesNoDbConverter implements DbConverter<Boolean, String> {
        @Override public @NotNull Class<String> dbType() { return String.class; }
        @Override public String toDb(@NotNull Boolean value) { return value ? "Y" : "N"; }
        @Override public Boolean fromDb(@NotNull String value) { return "Y".equals(value); }
    }

    /** A JPA converter */
    public static class YesNoConverter implements AttributeConverter<Boolean, String> {
        @Override public String convertToDatabaseColumn(Boolean value) { return value ? "Y" : "N"; }
        @Override public Boolean convertToEntityAttribute(String dbValue) { return "Y".equals(dbValue); }
    }

    public enum Color { RED, GREEN }

    // --- The same model by both annotation sets ---

    @UTable(name = "town", schema = "geo", catalog = "cat")
    public record UCity(@UId Long id, @UColumn(name = "town_name", nullable = false) String name) {}

    @Table(name = "town", schema = "geo", catalog = "cat")
    public record JCity(@Id Long id, @Column(name = "town_name", nullable = false) String name) {}

    @UEntity
    public record UPerson(
            @UId Long id,
            @UManyToOne @UJoinColumn(name = "town_id", nullable = false) UCity city,
            @UJoinColumn(name = "raw_town_id") Long rawCity,
            UCity implicitCity,
            @UConvert(converter = YesNoConverter.class) Boolean active,
            @UConvert(disableConversion = true) Color plainColor,
            @UEnumerated Color ordinalColor,
            @UEnumerated(UEnumType.STRING) Color nameColor,
            @UTransient String note,
            String note2) {}

    @Entity
    public record JPerson(
            @Id Long id,
            @ManyToOne @JoinColumn(name = "town_id", nullable = false) JCity city,
            @JoinColumn(name = "raw_town_id") Long rawCity,
            JCity implicitCity,
            @Convert(converter = YesNoConverter.class) Boolean active,
            @Convert(disableConversion = true) Color plainColor,
            @Enumerated Color ordinalColor,
            @Enumerated(EnumType.STRING) Color nameColor,
            @Transient String note,
            String note2) {}

    // --- Mixed annotations ---

    public record SameValues(@UId @Id Long id, @UColumn(name = "a") @Column(name = "a") String value) {}
    public record DifferentColumn(@UColumn(name = "a") @Column(name = "b") String value) {}
    public record DifferentNullable(@UColumn(nullable = false) @Column String value) {}
    public record DifferentJoin(@UJoinColumn(name = "a") @JoinColumn(name = "b") UCity city) {}
    public record DifferentEnum(@UEnumerated(UEnumType.STRING) @Enumerated Color color) {}
    public record DifferentConvert(@UConvert(converter = YesNoDbConverter.class)
                                   @Convert(converter = YesNoConverter.class) Boolean active) {}
    @UTable(name = "a") @Table(name = "b") public record DifferentTable(Long id) {}
    @UTable(name = "a") @Table(name = "a") public record SameTable(Long id) {}

    /** The Jakarta-free converter */
    public record WithDbConverter(@UConvert(converter = YesNoDbConverter.class) Boolean active,
                                  @UConvert(converter = YesNoDbConverter.class) boolean primitive) {}
    public record WithDbConverterMismatch(@UConvert(converter = YesNoDbConverter.class) String active) {}
    public record WithDbConverterOnId(@UId @UConvert(converter = YesNoDbConverter.class) Boolean id) {}
    public record WithDbConverterEnumerated(@UEnumerated @UConvert(converter = YesNoDbConverter.class) Boolean v) {}

    @Test
    void sameModelByBothAnnotationSets() {
        assertEquals(TableIdentifier.of(JCity.class), TableIdentifier.of(UCity.class));
        assertEquals(new TableIdentifier("town", "geo", "cat"), TableIdentifier.of(UCity.class));
        assertEquals(new TableIdentifier("u_person", "", ""), TableIdentifier.of(UPerson.class));
        assertTrue(TableIdentifier.isEntity(UCity.class));
        assertTrue(TableIdentifier.isEntity(UPerson.class));

        assertEquals(describe(JCity.class), describe(UCity.class));
        assertEquals(describe(JPerson.class).replace("JCity", "UCity"), describe(UPerson.class));
    }

    @Test
    void ujormModel() {
        var props = DomainPropertyModel.of(UPerson.class);
        assertEquals(9, props.size(), "The note is transient");
        var city = props.get(1);
        assertEquals("town_id", city.dbColumName());
        assertTrue(city.required());
        assertTrue(city.foreignKey());
        assertTrue(props.get(2).foreignKey(), "A raw foreign key");
        assertFalse(props.get(2).required());
        assertTrue(props.get(3).foreignKey(), "A relation to an entity");
        assertEquals("implicit_city", props.get(3).dbColumName());
        assertTrue(props.get(0).primaryKey());
        assertTrue(props.get(4).converter().contains("JpaConverter.of(new"), props.get(4).converter());
        assertTrue(props.get(5).converter().contains("EnumNameConverter"));
        assertTrue(props.get(6).converter().contains("EnumOrdinalConverter"));
        assertTrue(props.get(7).converter().contains("EnumNameConverter"));
    }

    /**
     * The MODEL_HASH depends on the generated source only, so it doesn't depend on the annotation set.
     * The source of two equivalent classes differs by the class name and the hash only.
     */
    @Test
    void sameModelHash() {
        var generator = new JavaSourceGenerator();
        var className = ClassName.ofGenerated(UCity.class);
        var uSource = generator.getSourceCode(DomainModel.of(UCity.class), className);
        var jSource = generator.getSourceCode(DomainModel.of(JCity.class), className);
        var uHash = generator.getModelHash(DomainModel.of(UCity.class), className);
        var jHash = generator.getModelHash(DomainModel.of(JCity.class), className);
        assertTrue(uSource.contains(uHash));
        assertEquals(uSource.replace(uHash, "#"), jSource
                .replace(jHash, "#")
                .replace(JCity.class.getSimpleName(), UCity.class.getSimpleName()));
    }

    @Test
    void mixedAnnotations() {
        var props = DomainPropertyModel.of(SameValues.class);
        assertEquals("a", props.get(1).dbColumName());
        assertTrue(props.get(0).primaryKey());
        assertEquals("a", TableIdentifier.of(SameTable.class).table());

        assertError(DifferentColumn.class, "DifferentColumn.value has the annotations @UColumn and @Column with different values");
        assertError(DifferentNullable.class, "@UColumn and @Column");
        assertError(DifferentJoin.class, "@UJoinColumn and @JoinColumn");
        assertError(DifferentEnum.class, "@UEnumerated and @Enumerated");
        assertError(DifferentConvert.class, "@UConvert and @Convert");
        var e = assertThrows(IllegalArgumentException.class, () -> TableIdentifier.of(DifferentTable.class));
        assertTrue(e.getMessage().contains("@UTable and @Table"), e.getMessage());
    }

    @Test
    void ujormConverter() {
        var props = DomainPropertyModel.of(WithDbConverter.class);
        var expected = "new " + YesNoDbConverter.class.getCanonicalName() + "()";
        assertEquals(expected, props.get(0).converter());
        assertEquals(expected, props.get(1).converter(), "A primitive type");

        assertError(WithDbConverterMismatch.class, "converts the type java.lang.Boolean rather than java.lang.String");
        assertError(WithDbConverterOnId.class, "a primary key can't have a converter");
        assertError(WithDbConverterEnumerated.class, "can't be combined");
    }

    /** Without the Jakarta annotations the Jakarta converter is refused and its annotations are ignored. */
    @Test
    void jakartaDisabled() {
        var builder = new DomainModelBuilder(new MappingAnnotations(false));
        var props = builder.createPropertyModel(JCity.class);
        assertFalse(props.get(0).primaryKey());
        assertEquals("name", props.get(1).dbColumName());

        var e = assertThrows(IllegalArgumentException.class, () -> builder.createPropertyModel(UPerson.class));
        assertTrue(e.getMessage().contains("the Jakarta Persistence API is not available"), e.getMessage());
        assertEquals("[]", new ConverterResolver(new MappingAnnotations(false))
                .loadAutoApplied(getClass().getClassLoader()).keySet().toString());
    }

    private static String describe(Class<?> domain) {
        return DomainPropertyModel.of(domain).stream()
                .map(p -> List.of(p.name(), p.dbColumName(), p.required(), p.primaryKey(), p.foreignKey(),
                        String.valueOf(p.converter())).toString())
                .toList()
                .toString();
    }

    private static void assertError(Class<?> domain, String expectedPart) {
        var e = assertThrows(IllegalArgumentException.class, () -> DomainPropertyModel.of(domain));
        assertTrue(e.getMessage().contains(expectedPart), e.getMessage());
    }
}
