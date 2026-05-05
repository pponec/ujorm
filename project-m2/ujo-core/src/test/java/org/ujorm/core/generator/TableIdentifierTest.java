package org.ujorm.core.generator;

import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static org.ujorm.core.generator.TableIdentifier.toSnakeCase;

class TableIdentifierTest {

    /** Test the private static method toSnakeCase via Reflection. */
    @Test
    void toSnakeCaseTest() throws Exception {
        assertEquals("user_profile", toSnakeCase("UserProfile"));
        assertEquals("customer_order_item", toSnakeCase("CustomerOrderItem"));
        assertEquals("user", toSnakeCase("User"));
        assertEquals("simple", toSnakeCase("simple"));
        assertEquals("x_m_l_parser", toSnakeCase("XMLParser"));
    }

    @Test
    void testMergeWithEmptySchemaAndCatalog() {
        var soft = new TableIdentifier("user_entity", null, "");
        var real = new TableIdentifier("USER_ENTITY", "public", "def_catalog");

        var result = soft.merge(real);

        assertEquals("USER_ENTITY", result.table());
        assertNull(result.schema());
        assertNull(result.catalog());
    }

    @Test
    void testMergeWithDefinedSchemaAndCatalog() {
        var soft = new TableIdentifier("user_entity", "usr_schema", "usr_catalog");
        var real = new TableIdentifier("USER_ENTITY", "USR_SCHEMA", "USR_CATALOG");

        var result = soft.merge(real);

        assertEquals("USER_ENTITY", result.table());
        assertEquals("USR_SCHEMA", result.schema());
        assertEquals("USR_CATALOG", result.catalog());
    }

    @Test
    void testGetQualifiedName() {
        var tableWithAll = new TableIdentifier("users", "public", "my_db");
        assertEquals("my_db.public.users", tableWithAll.getQualifiedName());
        assertEquals("\"my_db\".\"public\".\"users\"", tableWithAll.getQualifiedName("\""));
        assertEquals("[my_db].[public].[users]", tableWithAll.getQualifiedName("[]"));

        var tableOnlyName = new TableIdentifier("users", null, null);
        assertEquals("users", tableOnlyName.getQualifiedName());
        assertEquals("users", tableOnlyName.getQualifiedName(""));

        var tableWithSchema = new TableIdentifier("users", "public", null);
        assertEquals("public.users", tableWithSchema.getQualifiedName());
        assertEquals("\"public\".\"users\"", tableWithSchema.getQualifiedName("\""));
    }

    @Test
    void testOfMethodWithCompleteAnnotation() {
        var result = TableIdentifier.of(DummyEntity.class);

        assertEquals("my_dummy_table", result.table());
        assertEquals("my_schema", result.schema());
        assertEquals("my_catalog", result.catalog());
    }

    @Test
    void testOfMethodWithEmptyAnnotation() {
        var result = TableIdentifier.of(EmptyEntity.class);

        assertEquals("empty_entity", result.table());
        assertEquals("", result.schema());
        assertEquals("", result.catalog());
    }

    @Table(name = "my_dummy_table", schema = "my_schema", catalog = "my_catalog")
    class DummyEntity {
    }

    @Table
    class EmptyEntity {
    }
}