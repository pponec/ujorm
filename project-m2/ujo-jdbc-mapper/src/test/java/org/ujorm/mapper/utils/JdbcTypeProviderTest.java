package org.ujorm.mapper.utils;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.sql.JDBCType;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Test of the JdbcTypeProvider class */
class JdbcTypeProviderTest {

    @Test
    void isSupported() {
        var provider = new JdbcTypeProvider();

        // Object types
        assertTrue(provider.isSupported(String.class));
        assertTrue(provider.isSupported(Integer.class));
        assertTrue(provider.isSupported(Long.class));
        assertTrue(provider.isSupported(BigDecimal.class));
        assertTrue(provider.isSupported(UUID.class));
        assertTrue(provider.isSupported(byte[].class));

        // Primitive types (should be supported via Primitive.wrapPrimitive)
        assertTrue(provider.isSupported(int.class));
        assertTrue(provider.isSupported(long.class));
        assertTrue(provider.isSupported(double.class));
        assertTrue(provider.isSupported(boolean.class));

        // Date and Time API
        assertTrue(provider.isSupported(LocalDate.class));
        assertTrue(provider.isSupported(OffsetDateTime.class));
        assertTrue(provider.isSupported(java.sql.Timestamp.class));

        // Unsupported types
        assertFalse(provider.isSupported(Object.class));
        assertFalse(provider.isSupported(JdbcTypeProviderTest.class));
        assertFalse(provider.isSupported(null));
    }

    @Test
    void findJdbcType() {
        var provider = new JdbcTypeProvider();

        assertEquals(JDBCType.VARCHAR, provider.findJdbcType(String.class));
        assertEquals(JDBCType.INTEGER, provider.findJdbcType(int.class));
        assertEquals(JDBCType.INTEGER, provider.findJdbcType(Integer.class));
        assertEquals(JDBCType.BIGINT, provider.findJdbcType(long.class));
        assertEquals(JDBCType.TIMESTAMP, provider.findJdbcType(java.util.Date.class));
        assertEquals(JDBCType.OTHER, provider.findJdbcType(UUID.class));
        assertNull(provider.findJdbcType(Object.class));
    }

    @Test
    void testNullArgument() {
        var provider = new JdbcTypeProvider();
        assertThrows(IllegalArgumentException.class, () -> provider.findJdbcType(null));
    }

    @Test
    void findJdbcTypeWithProvider() {
        var provider = new JdbcTypeProvider();
        var result = provider.findJdbcType(String.class, () -> "Error");
        assertEquals(JDBCType.VARCHAR, result);
    }

    @Test
    void findJdbcTypeWithProviderException() {
        var provider = new JdbcTypeProvider();
        var msg = "Unsupported type: " + Object.class;
        var result = assertThrows(IllegalArgumentException.class, () ->
                provider.findJdbcType(Object.class, () -> msg));
        assertEquals(msg, result.getMessage());
    }

    @Test
    void findJdbcTypeWithNullProvider() {
        var provider = new JdbcTypeProvider();
        var clazz = Object.class;
        var result = assertThrows(IllegalArgumentException.class, () ->
                provider.findJdbcType(clazz, null));
        assertTrue(result.getMessage().contains(clazz.getSimpleName()));
    }
}