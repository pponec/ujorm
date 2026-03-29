package org.ujorm.orm.jdbc;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/** Tests a logic of Enum mapping in ResultSetMapper */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResultSetMapperTestEnum {

    /** Tests mapping of an Enum property by its String name */
    @Test @Order(100)
    void testMapEnumByName() throws SQLException {
        var rs = Mockito.mock(ResultSet.class);
        var metaData = Mockito.mock(ResultSetMetaData.class);

        when(rs.next()).thenReturn(true, false);
        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(3);

        setupColumn(rs, metaData, 1, "id", "id", 10, Integer.class);
        setupColumn(rs, metaData, 2, "name", "name", "Jan", String.class);
        setupEnumColumn(rs, metaData, 3, "role", "role", "USER");

        var service = DomainHandlerProvider.provider();
        var mapper = ResultSetMapper.of(Employee.class, service);
        var result = mapper.convert(rs).findFirst().get();

        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals(Role.USER, result.getRole());
    }

    /** Tests mapping of an Enum property by its numeric index */
    @Test @Order(200)
    void testMapEnumByIndex() throws SQLException {
        var rs = Mockito.mock(ResultSet.class);
        var metaData = Mockito.mock(ResultSetMetaData.class);

        when(rs.next()).thenReturn(true, false);
        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(3);

        setupColumn(rs, metaData, 1, "id", "id", 20, Integer.class);
        setupColumn(rs, metaData, 2, "name", "name", "Petr", String.class);
        setupEnumColumn(rs, metaData, 3, "role", "role", 0); // 0 = ADMIN

        var service = DomainHandlerProvider.provider();
        var mapper = ResultSetMapper.of(Employee.class, service);
        var result = mapper.convert(rs).findFirst().get();

        assertNotNull(result);
        assertEquals(20, result.getId());
        assertEquals(Role.ADMIN, result.getRole());
    }

    /** Tests mapping of an Enum property when the DB value is null */
    @Test @Order(300)
    void testMapEnumWithNull() throws SQLException {
        var rs = Mockito.mock(ResultSet.class);
        var metaData = Mockito.mock(ResultSetMetaData.class);

        when(rs.next()).thenReturn(true, false);
        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(3);

        setupColumn(rs, metaData, 1, "id", "id", 30, Integer.class);
        setupColumn(rs, metaData, 2, "name", "name", "Karel", String.class);
        setupEnumColumn(rs, metaData, 3, "role", "role", null);

        var service = DomainHandlerProvider.provider();
        var mapper = ResultSetMapper.of(Employee.class, service);
        var result = mapper.convert(rs).findFirst().get();

        assertNotNull(result);
        assertEquals(30, result.getId());
        assertNull(result.getRole());
    }

    /** Helper to easily mock standard ResultSet metadata and values */
    <T> void setupColumn(ResultSet rs, ResultSetMetaData meta, int index, String columnName, String columnLabel, T columnValue, Class<T> type) throws SQLException {
        when(meta.getColumnLabel(index)).thenReturn(columnLabel);
        when(meta.getColumnName(index)).thenReturn(columnName);
        when(rs.getObject(index, type)).thenReturn(columnValue);
    }

    /** Helper to mock Enum column using plain getObject(index) */
    void setupEnumColumn(ResultSet rs, ResultSetMetaData meta, int index, String columnName, String columnLabel, Object columnValue) throws SQLException {
        when(meta.getColumnLabel(index)).thenReturn(columnLabel);
        when(meta.getColumnName(index)).thenReturn(columnName);
        when(rs.getObject(index)).thenReturn(columnValue);
    }

    /** Sample enumeration for testing */
    public enum Role { ADMIN, USER, GUEST }

    /** Represents a simplified employee entity */
    @Getter
    @Setter
    public static class Employee {

        static final DomainHandler<Employee> dh = DomainHandlerProvider.getHandler(Employee.class);
        public static final Key<Employee, Integer> employee_id = dh.getKey("id", Integer.class);
        public static final Key<Employee, String> employee_name = dh.getKey("name", String.class);
        public static final Key<Employee, Role> employee_role = dh.getKey("role", Role.class);

        private Integer id;
        private String name;
        private Role role;
    }
}