package org.ujorm.mapper.jdbc;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.Key;
import org.ujorm.core.generator.ClassName;
import org.ujorm.core.generator.DomainModel;
import org.ujorm.core.generator.JavaSourceGenerator;
import org.ujorm.tools.jdbc.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class ResultSetTreeMappeColumnTest {

    private final boolean printResult = false;

    @Test @Order(200)
    void testConvertResultSetToDomain_1() throws SQLException {

        // 1. Prepare the mock ResultSet, MetaData and the Columns helper
        var rs = Mockito.mock(ResultSet.class);
        var metaData = Mockito.mock(ResultSetMetaData.class);
        var c = new Columns();

        // Setup the mock to return exactly one row and provide metadata
        when(rs.next()).thenReturn(true, false);
        when(rs.getMetaData()).thenReturn(metaData);

        // Setup mock responses and record column aliases transparently on one line
        when(rs.getObject(c.add("id"), Integer.class)).thenReturn(10);
        when(rs.getObject(c.add("name"), String.class)).thenReturn("Jan Novak");
        when(rs.getObject(c.add("city.id"), Integer.class)).thenReturn(100);
        when(rs.getObject(c.add("city.name"), String.class)).thenReturn("Prague");
        when(rs.getObject(c.add("city.country.id"), Integer.class)).thenReturn(1000);
        when(rs.getObject(c.add("city.country.name"), String.class)).thenReturn("Czechia");
        when(rs.getObject(c.add("boss.id"), Integer.class)).thenReturn(20);
        when(rs.getObject(c.add("boss.name"), String.class)).thenReturn("Petr Boss");
        when(rs.getObject(c.add("boss.boss.id"), Integer.class)).thenReturn(null);
        when(rs.getObject(c.add("boss.boss.name"), String.class)).thenReturn(null);

        // 2. Retrieve the synchronized column aliases and mock column count
        var aliases = c.toArray();
        when(metaData.getColumnCount()).thenReturn(aliases.length);

        // 3. Prepare the DomainHandlerService
        var service = DomainHandlerProvider.provider();

        // 4. Initialize the mapper using the requested factory method of()
        var mapper = ResultSetTreeMapper.of(Employee.class, service);

        // 5. Execute the mapping
        var result = mapper.convert(rs, aliases).findFirst().get();

        // 6. Verify the mapped values
        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals("Jan Novak", result.getName());

        assertNotNull(result.getCity());
        assertEquals(100, result.getCity().getId());
        assertEquals("Prague", result.getCity().getName());

        assertNotNull(result.getCity().getCountry());
        assertEquals(1000, result.getCity().getCountry().getId());
        assertEquals("Czechia", result.getCity().getCountry().getName());

        assertNotNull(result.getBoss());
        assertEquals(20, result.getBoss().getId());
        assertEquals("Petr Boss", result.getBoss().getName());

        assertNotNull(result.getBoss().getBoss());
        assertNull(result.getBoss().getBoss().getId());
        assertNull(result.getBoss().getBoss().getName());
    }

    /** Aliases by the Key object */
    @Test @Order(201)
    void testConvertResultSetToDomain_2() throws SQLException {

        // 1. Prepare the mock ResultSet, MetaData and the Columns helper
        var rs = Mockito.mock(ResultSet.class);
        var metaData = Mockito.mock(ResultSetMetaData.class);
        var c = new Columns();

        // Setup the mock to return exactly one row and provide metadata
        when(rs.next()).thenReturn(true, false);
        when(rs.getMetaData()).thenReturn(metaData);

        // Setup mock responses and record column aliases transparently on one line
        when(rs.getObject(c.add("id"), Integer.class)).thenReturn(10);
        when(rs.getObject(c.add("name"), String.class)).thenReturn("Jan Novak");

        // 2. Retrieve the synchronized column aliases and mock column count
        var aliases = c.toArray();
        when(metaData.getColumnCount()).thenReturn(aliases.length);

        // 3. Prepare the DomainHandlerService
        var service = DomainHandlerProvider.provider();

        // 4. Initialize the mapper using the requested factory method of()
        var mapper = ResultSetTreeMapper.of(Employee.class, service);

        // 5. Execute the mapping by the keys
        var result = mapper.convertFlat(JdbcUtils.stream(rs)
                , Employee.keyId
                , Employee.keyName)
                .findFirst().get();

        // 6. Verify the mapped values
        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals("Jan Novak", result.getName());
    }


    // --- HELP classes ---

    /** Helper class for managing column aliases */
    private static class Columns {
        private final List<String> names = new ArrayList<>();

        /** Adds a column name and returns its 1-based index */
        public int add(String name) {
            names.add(name);
            return names.size();
        }

        /** Returns the accumulated column aliases as an array */
        public String[] toArray() {
            return names.toArray(new String[0]);
        }
    }

    // --- Domain Classes & Helpers ---

    /** Represents an employee entity */
    @Getter
    @Setter
    public static class Employee {
        private Integer id;
        private String name;
        private City city;
        private Employee boss;

        // Optional keys:
        static final DomainHandler<Employee> dh = DomainHandlerProvider.getHandler(Employee.class);
        public static final Key<Employee, Integer> keyId = dh.getKey("id", Integer.class);
        public static final Key<Employee, String> keyName = dh.getKey("name", String.class);
        public static final Key<Employee, City> keyCity = dh.getKey("city", City.class);
        public static final Key<Employee, Employee> keyBoss = dh.getKey("boss", Employee.class);
    }

    /** Represents a city entity */
    @Getter
    @Setter
    public static class City {
        private Integer id;
        private String name;
        private Country country;

        // Optional keys:
        static final DomainHandler<City> dh = DomainHandlerProvider.getHandler(City.class);
        public static final Key<City, Integer> keyId = dh.getKey("id", Integer.class);
        public static final Key<City, String> keyName = dh.getKey("name", String.class);
        public static final Key<City, Country> keyCountry = dh.getKey("country", Country.class);
    }

    /** Represents a country entity */
    @Getter
    @Setter
    public static class Country {
        private Integer id;
        private String name;

        // Optional keys:
        static final DomainHandler<Country> dh = DomainHandlerProvider.getHandler(Country.class);
        public static final Key<Country, Integer> keyId = dh.getKey("id", Integer.class);
        public static final Key<Country, String> keyName = dh.getKey("name", String.class);
    }
}