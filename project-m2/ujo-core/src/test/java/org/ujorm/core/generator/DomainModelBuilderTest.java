package org.ujorm.core.generator;

import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.ujorm.core.demo.City;
import org.ujorm.core.demo.Employee;

import static org.junit.jupiter.api.Assertions.*;

class DomainModelBuilderTest {

    /** Test resolving all attributes from the @Table annotation. */
    @Test
    void testTableAnnotationAttributes() {
        // Case 1: Fully annotated entity
        var builder = new DomainModelBuilder();
        var meta = builder.build(FullAnnotatedEntity.class);
        assertEquals("custom_table", meta.tableIdentifier().table());
        assertEquals("my_schema", meta.tableIdentifier().schema());
        assertEquals("my_catalog", meta.tableIdentifier().catalog());

        // Case 2: Partially annotated (only name)
        var partialMeta = builder.build(PartialAnnotatedEntity.class);
        assertEquals("only_name", partialMeta.tableIdentifier().table());
        assertEquals("", partialMeta.tableIdentifier().schema(), "Schema should be null if not provided");
        assertEquals("", partialMeta.tableIdentifier().catalog(), "Catalog should be null if not provided");

        // Case 3: Annotation present but empty strings (JPA default)
        var emptyMeta = builder.build(EmptyAnnotatedEntity.class);
        assertEquals("empty_annotated_entity", emptyMeta.tableIdentifier().table(), "Should fallback to snake_case");
        assertEquals("", emptyMeta.tableIdentifier().schema());
        assertEquals("", emptyMeta.tableIdentifier().catalog());
    }

    /** Test resolving the database name from @Table annotation or class name fallback. */
    @Test
    void getDatabaseTable() {
        var builder = new DomainModelBuilder();

        // Case 1: Class with explicit @Table annotation
        var modelWithAnnotation = builder.build(AnnotatedEntity.class);
        assertEquals("my_custom_table", modelWithAnnotation.tableIdentifier().table());

        // Case 2: Class without annotation (fallback to snake_case)
        var modelWithoutAnnotation = builder.build(UserProfile.class);
        assertEquals("user_profile", modelWithoutAnnotation.tableIdentifier().table());

        // Case 3: Record without annotation
        var recordModel = builder.build(SimpleRecord.class);
        assertEquals("simple_record", recordModel.tableIdentifier().table());
    }

    /** Tests property mapping and constraints for a Java Record type. */
    @Test
    void testCityRecordProperties() {
        var builder = new DomainModelBuilder();
        var model = builder.build(City.class);
        var properties = model.properties();

        assertEquals(5, properties.size(), "City should have exactly 5 persisted properties");

        var id = properties.stream().filter(p -> "id".equals(p.propertyName())).findFirst().orElseThrow();
        assertTrue(id.primaryKey());
        assertFalse(id.foreignKey());
        assertEquals("db_id", id.dbColumName());

        var name = properties.stream().filter(p -> "name".equals(p.propertyName())).findFirst().orElseThrow();
        assertFalse(name.primaryKey());
        assertFalse(name.foreignKey());
        assertEquals("db_name", name.dbColumName());
        assertTrue(name.required(), "Name is explicitly not nullable");

        var latitude = properties.stream().filter(p -> "latitude".equals(p.propertyName())).findFirst().orElseThrow();
        assertTrue(latitude.required(), "Primitive types must be required");
        assertFalse(latitude.foreignKey());
    }

    /** Tests property mapping, transient fields ignore, and foreign keys for a standard Class. */
    @Test
    void testEmployeeClassProperties() {
        var builder = new DomainModelBuilder();
        var model = builder.build(Employee.class);
        var properties = model.properties();

        assertEquals(6, properties.size(), "Employee should have exactly 6 persisted properties, transient ignored");

        var snapshot = properties.stream().filter(p -> "_snapshot".equals(p.propertyName())).findFirst();
        assertTrue(snapshot.isEmpty(), "Transient field _snapshot must be ignored");

        var id = properties.stream().filter(p -> "id".equals(p.propertyName())).findFirst().orElseThrow();
        assertTrue(id.primaryKey());
        assertFalse(id.foreignKey());

        var boss = properties.stream().filter(p -> "boss".equals(p.propertyName())).findFirst().orElseThrow();
        assertFalse(boss.primaryKey());
        assertTrue(boss.foreignKey(), "Boss must be detected as a foreign key via @ManyToOne and @JoinColumn");
        assertEquals("boss_id", boss.dbColumName());
        assertFalse(boss.required(), "Boss can be null");

        var city = properties.stream().filter(p -> "city".equals(p.propertyName())).findFirst().orElseThrow();
        assertTrue(city.foreignKey(), "City must be detected as a foreign key via target @Table and @JoinColumn");
        assertEquals("city", city.dbColumName());
        assertTrue(city.required(), "City is explicitly not nullable in @JoinColumn");
    }

    // --- Test Data Classes ---

    @Table(name = "custom_table", schema = "my_schema", catalog = "my_catalog")
    static class FullAnnotatedEntity {
    }

    @Table(name = "only_name")
    static class PartialAnnotatedEntity {
    }

    @Table(name = "", schema = "", catalog = "")
    static class EmptyAnnotatedEntity {
    }

    @Table(name = "my_custom_table")
    static class AnnotatedEntity {
    }

    static class UserProfile {
    }

    record SimpleRecord(int id) {
    }
}