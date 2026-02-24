package org.ujorm.core.generator;

import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DomainModelTest {

    /**
     * Test resolving all attributes from the @Table annotation.
     */
    @Test
    void testTableAnnotationAttributes() {
        // Case 1: Fully annotated entity
        var meta = DomainModel.of(FullAnnotatedEntity.class);
        assertEquals("custom_table", meta.database().table());
        assertEquals("my_schema", meta.database().schema());
        assertEquals("my_catalog", meta.database().catalog());

        // Case 2: Partially annotated (only name)
        var partialMeta = DomainModel.of(PartialAnnotatedEntity.class);
        assertEquals("only_name", partialMeta.database().table());
        assertEquals("", partialMeta.database().schema(), "Schema should be null if not provided");
        assertEquals("", partialMeta.database().catalog(), "Catalog should be null if not provided");

        // Case 3: Annotation present but empty strings (JPA default)
        var emptyMeta = DomainModel.of(EmptyAnnotatedEntity.class);
        assertEquals("empty_annotated_entity", emptyMeta.database().table(), "Should fallback to snake_case");
        assertEquals("", emptyMeta.database().schema());
        assertEquals("", emptyMeta.database().catalog());
    }

    /**
     * Test resolving the database name from @Table annotation or class name fallback.
     */
    @Test
    void getDatabaseTable() {
        // Case 1: Class with explicit @Table annotation
        var modelWithAnnotation = DomainModel.of(AnnotatedEntity.class);
        assertEquals("my_custom_table", modelWithAnnotation.database().table());

        // Case 2: Class without annotation (fallback to snake_case)
        var modelWithoutAnnotation = DomainModel.of(UserProfile.class);
        assertEquals("user_profile", modelWithoutAnnotation.database().table());

        // Case 3: Record without annotation
        var recordModel = DomainModel.of(SimpleRecord.class);
        assertEquals("simple_record", recordModel.database().table());
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