package org.ujorm.mapper;

import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BeanModelTest {

    /**
     * Test resolving all attributes from the @Table annotation.
     */
    @Test
    void testTableAnnotationAttributes() {
        // Case 1: Fully annotated entity
        var model = BeanModel.of(FullAnnotatedEntity.class);
        assertEquals("custom_table", model.databaseTable());
        assertEquals("my_schema", model.databaseSchema());
        assertEquals("my_catalog", model.databaseCatalog());

        // Case 2: Partially annotated (only name)
        var partialModel = BeanModel.of(PartialAnnotatedEntity.class);
        assertEquals("only_name", partialModel.databaseTable());
        assertNull(partialModel.databaseSchema(), "Schema should be null if not provided");
        assertNull(partialModel.databaseCatalog(), "Catalog should be null if not provided");

        // Case 3: Annotation present but empty strings (JPA default)
        var emptyModel = BeanModel.of(EmptyAnnotatedEntity.class);
        assertEquals("empty_annotated_entity", emptyModel.databaseTable(), "Should fallback to snake_case");
        assertNull(emptyModel.databaseSchema());
        assertNull(emptyModel.databaseCatalog());
    }

    /**
     * Test resolving the table name from @Table annotation or class name fallback.
     */
    @Test
    void getDatabaseTable() {
        // Case 1: Class with explicit @Table annotation
        var modelWithAnnotation = BeanModel.of(AnnotatedEntity.class);
        assertEquals("my_custom_table", modelWithAnnotation.databaseTable());

        // Case 2: Class without annotation (fallback to snake_case)
        var modelWithoutAnnotation = BeanModel.of(UserProfile.class);
        assertEquals("user_profile", modelWithoutAnnotation.databaseTable());

        // Case 3: Record without annotation
        var recordModel = BeanModel.of(SimpleRecord.class);
        assertEquals("simple_record", recordModel.databaseTable());
    }

    /**
     * Test the private static method toSnakeCase via Reflection.
     */
    @Test
    void toSnakeCase() throws Exception {
        var toSnakeCase = BeanModel.class.getDeclaredMethod("toSnakeCase", String.class);
        toSnakeCase.setAccessible(true);

        assertEquals("user_profile", toSnakeCase.invoke(null, "UserProfile"));
        assertEquals("customer_order_item", toSnakeCase.invoke(null, "CustomerOrderItem"));
        assertEquals("user", toSnakeCase.invoke(null, "User"));
        assertEquals("simple", toSnakeCase.invoke(null, "simple"));
        assertEquals("x_m_l_parser", toSnakeCase.invoke(null, "XMLParser"));
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