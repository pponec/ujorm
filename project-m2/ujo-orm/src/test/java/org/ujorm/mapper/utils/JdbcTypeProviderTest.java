package org.ujorm.mapper.utils;

import org.junit.jupiter.api.Test;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.mapper.demo.Employee;
import org.ujorm.mapper.demo.UserSnapshotable;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Test of the JdbcTypeProvider class */
class JdbcTypeProviderTest {

    private static final DomainHandler<Employee> handler = DomainHandlerProvider.getHandler(Employee.class);

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
    void findJdbcTypeByKey() {
        var provider = new JdbcTypeProvider();

        // Valid keys
        assertEquals(JDBCType.BIGINT, provider.findJdbcType(handler.getKey("id")));
        assertEquals(JDBCType.VARCHAR, provider.findJdbcType(handler.getKey("name")));

        // Keys with unsupported types (relations)
        var exSuperior = assertThrows(IllegalArgumentException.class, () ->
                provider.findJdbcType(handler.getKey("superior")));
        assertTrue(exSuperior.getMessage().contains("Employee.superior"));
        assertEquals("The attribute Employee.superior has an unsupported JDBC type: %s"
                        .formatted(UserSnapshotable.class.getName()), exSuperior.getMessage());

        var exCity = assertThrows(IllegalArgumentException.class, () ->
                provider.findJdbcType(handler.getKey("city")));
        assertTrue(exCity.getMessage().contains("Employee.city"));
        assertEquals("The attribute Employee.superior has an unsupported JDBC type: %s"
                        .formatted(UserSnapshotable.class.getName()), exSuperior.getMessage());
    }
}