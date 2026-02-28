package org.ujorm.mapper.jdbc;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/** Tests a logic of column aliases processing in ResultSetTreeMapper */
public class ResultSetMapperColumnTest {

    /** Tests mapping by property name when label and name differ. */
    @Test @Order(100)
    void testLabelDiffersFromName_mapsByProperty() throws SQLException {
        var rs = Mockito.mock(ResultSet.class);
        var metaData = Mockito.mock(ResultSetMetaData.class);

        when(rs.next()).thenReturn(true, false);
        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);

        setupColumn(rs, metaData, 1, "x1", "id", 10, Integer.class);
        setupColumn(rs, metaData, 2, "x2", "name", "Jan", String.class);

        var service = DomainHandlerProvider.provider();
        var mapper = ResultSetMapper.of(Employee.class, service);
        var result = mapper.convert(rs).findFirst().get();

        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals("Jan", result.getName());
    }

    /** Tests mapping by DB column name when label and name match with annotations. */
    @Test @Order(200)
    void testLabelEqualsName_mapsByDbColumn() throws SQLException {
        var rs = Mockito.mock(ResultSet.class);
        var metaData = Mockito.mock(ResultSetMetaData.class);

        when(rs.next()).thenReturn(true, false);
        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);

        setupColumn(rs, metaData, 1, "db_id", "db_id", 20, Integer.class);
        setupColumn(rs, metaData, 2, "db_name", "db_name", "Petr", String.class);

        var service = DomainHandlerProvider.provider();
        var mapper = ResultSetMapper.of(Employee.class, service);
        var result = mapper.convert(rs).findFirst().get();

        assertNotNull(result);
        assertEquals(20, result.getId());
        assertEquals("Petr", result.getName());
    }

    /** Tests case-insensitive mapping by DB column name when label and name match. */
    @Test @Order(210)
    void testLabelEqualsName_mapsByDbColumnCaseInsensitive() throws SQLException {
        var rs = Mockito.mock(ResultSet.class);
        var metaData = Mockito.mock(ResultSetMetaData.class);

        when(rs.next()).thenReturn(true, false);
        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);

        setupColumn(rs, metaData, 1, "DB_ID", "DB_ID", 25, Integer.class);
        setupColumn(rs, metaData, 2, "Db_NaMe", "Db_NaMe", "Lucie", String.class);

        var service = DomainHandlerProvider.provider();
        var mapper = ResultSetMapper.of(Employee.class, service);
        var result = mapper.convert(rs).findFirst().get();

        assertNotNull(result);
        assertEquals(25, result.getId());
        assertEquals("Lucie", result.getName());
    }

    /** Tests fallback to property mapping if DB column match fails. */
    @Test @Order(300)
    void testLabelEqualsName_fallbackToProperty() throws SQLException {
        var rs = Mockito.mock(ResultSet.class);
        var metaData = Mockito.mock(ResultSetMetaData.class);

        when(rs.next()).thenReturn(true, false);
        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);

        setupColumn(rs, metaData, 1, "id", "id", 30, Integer.class);
        setupColumn(rs, metaData, 2, "name", "name", "Karel", String.class);

        var service = DomainHandlerProvider.provider();
        var mapper = ResultSetMapper.of(Employee.class, service);
        var result = mapper.convert(rs).findFirst().get();

        assertNotNull(result);
        assertEquals(30, result.getId());
        assertEquals("Karel", result.getName());
    }

    /** Tests that explicit aliases bypass ResultSet metadata logic. */
    @Test @Order(400)
    void testExplicitAliases_behaviorUnchanged() throws SQLException {
        var rs = Mockito.mock(ResultSet.class);
        var metaData = Mockito.mock(ResultSetMetaData.class);

        when(rs.next()).thenReturn(true, false);
        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(2);

        setupColumn(rs, metaData, 1, "db_id", "x1", 40, Integer.class);
        setupColumn(rs, metaData, 2, "db_name", "x2", "Eva", String.class);

        var service = DomainHandlerProvider.provider();
        var mapper = ResultSetMapper.of(Employee.class, service);
        var result = mapper.convert(rs, "id", "name").findFirst().get();

        assertNotNull(result);
        assertEquals(40, result.getId());
        assertEquals("Eva", result.getName());
    }

    /** Tests failure when column matches neither DB annotation nor property name. */
    @Test @Order(500)
    void testUnknownColumn_throwsException() throws SQLException {
        var rs = Mockito.mock(ResultSet.class);
        var metaData = Mockito.mock(ResultSetMetaData.class);

        when(rs.next()).thenReturn(true, false);
        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);

        setupColumn(rs, metaData, 1, "xx1", "yy1", 50, Integer.class);

        var service = DomainHandlerProvider.provider();
        var mapper = ResultSetMapper.of(Employee.class, service);

        assertThrows(NoSuchElementException.class, () -> {
            mapper.convert(rs).findFirst();
        });
    }

    /** Helper to easily mock ResultSet metadata and values */
    <T> void setupColumn(ResultSet rs
            , ResultSetMetaData meta
            , int index
            , String columnName
            , String columnLabel
            , T columnValue
            , Class<T> type) throws SQLException {
        when(meta.getColumnLabel(index)).thenReturn(columnLabel);
        when(meta.getColumnName(index)).thenReturn(columnName);
        when(rs.getObject(index, type)).thenReturn(columnValue);
    }

    /** Represents a simplified employee entity with explicitly defined DB columns */
    @Getter
    @Setter
    public static class Employee {

        @Column(name = "db_id")
        private Integer id;

        @Column(name = "db_name")
        private String name;

        static final DomainHandler<Employee> dh = DomainHandlerProvider.getHandler(Employee.class);
        public static final Key<Employee, Integer> employee_id = dh.getKey("id", Integer.class);
        public static final Key<Employee, String> employee_name = dh.getKey("name", String.class);
    }
}