package org.ujorm.core.generator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static org.ujorm.core.generator.DatabaseModel.toSnakeCase;

class DatabaseModelTest {

    /** Test the private static method toSnakeCase via Reflection. */
    @Test
    void toSnakeCaseTest() throws Exception {
        assertEquals("user_profile", toSnakeCase("UserProfile"));
        assertEquals("customer_order_item", toSnakeCase("CustomerOrderItem"));
        assertEquals("user", toSnakeCase("User"));
        assertEquals("simple", toSnakeCase("simple"));
        assertEquals("x_m_l_parser", toSnakeCase("XMLParser"));
    }

}