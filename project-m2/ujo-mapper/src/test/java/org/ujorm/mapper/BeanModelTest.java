package org.ujorm.mapper;

import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class BeanModelTest {

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
        Method toSnakeCase = BeanModel.class.getDeclaredMethod("toSnakeCase", String.class);
        toSnakeCase.setAccessible(true);

        // Standard CamelCase
        assertEquals("user_profile", toSnakeCase.invoke(null, "UserProfile"));
        assertEquals("customer_order_item", toSnakeCase.invoke(null, "CustomerOrderItem"));

        // Single word
        assertEquals("user", toSnakeCase.invoke(null, "User"));

        // Lowercase input
        assertEquals("simple", toSnakeCase.invoke(null, "simple"));

        // Edge case: Multiple uppercase letters (e.g. acronyms)
        // Based on the regex: (?<!^)(?=[A-Z]) -> splits before every uppercase not at start
        assertEquals("x_m_l_parser", toSnakeCase.invoke(null, "XMLParser"));
    }

    // --- Test Data Classes ---

    @Table(name = "my_custom_table")
    static class AnnotatedEntity {
    }

    static class UserProfile {
    }

    record SimpleRecord(int id) {
    }
}