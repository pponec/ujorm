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

public class ResultSetTreeMapperKeyTest {

    private final boolean printResult = false;

    @Test @Order(100)
    void codeGen() {
        var meta = DomainModel.of(Employee.class);
        var className = ClassName.ofGenerated(meta);
        var src = new JavaSourceGenerator().getSourceCode(meta, className);
        if (printResult) System.out.println(src);

        assertTrue(src.contains("static final class Key_city extends AbstractKey<Employee, org.ujorm.mapper.jdbc.ResultSetTreeMapperKeyTest.City> {"));
        assertTrue(src.contains("public org.ujorm.mapper.jdbc.ResultSetTreeMapperKeyTest.City getValue(@NotNull final Employee bean) {"));
    }

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
                , Employee.employee_id
                , Employee.employee_name)
                .findFirst().get();

        // 6. Verify the mapped values
        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals("Jan Novak", result.getName());
    }

    @Test @Order(300)
    void testColumnCountMismatch_throwsException() throws SQLException {
        // 1. Prepare the mock ResultSet and MetaData
        var rs = Mockito.mock(ResultSet.class);
        var metaData = Mockito.mock(ResultSetMetaData.class);

        // Simulate that the ResultSet has data and reports 5 columns
        when(rs.next()).thenReturn(true);
        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(5);

        // 2. Prepare the DomainHandlerService and initialize the mapper
        var service = DomainHandlerProvider.provider();
        var mapper = ResultSetTreeMapper.of(Employee.class, service);

        // 3. Provide an explicitly invalid number of aliases (e.g., only 2)
        var invalidAliases = new String[]{"id", "name"};

        // 4. Execute and verify the exception
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            mapper.convert(rs, invalidAliases).findFirst();
        });

        assertEquals("Column count mismatch between aliases and ResultSet.", exception.getMessage());
    }

    /** Tests that an empty ResultSet skips metadata validation and returns an empty Stream. */
    @Test @Order(400)
    void testEmptyResultSet_withInvalidAliases() throws SQLException {
        var rs = Mockito.mock(ResultSet.class);
        when(rs.next()).thenReturn(false);

        var service = DomainHandlerProvider.provider();
        var mapper = ResultSetTreeMapper.of(Employee.class, service);
        var invalidAliases = new String[]{"id", "invalid_column", "another_invalid"};
        var result = mapper.convert(rs, invalidAliases);

        assertEquals(0, result.count());
    }

    /** Tests that an invalid property name in the alias hierarchy throws an exception. */
    @Test @Order(500)
    void testInvalidPropertyNameInAlias_throwsException() throws SQLException {
        var rs = Mockito.mock(ResultSet.class);
        var metaData = Mockito.mock(ResultSetMetaData.class);

        when(rs.next()).thenReturn(true);
        when(rs.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(1);

        var service = DomainHandlerProvider.provider();
        var mapper = ResultSetTreeMapper.of(Employee.class, service);
        var invalidAliases = new String[]{"city.invalidProperty"};

        var ex = assertThrows(NoSuchElementException.class, () -> {
            mapper.convert(rs, invalidAliases).findFirst();
        });
        assertEquals("Property not found: City.invalidProperty", ex.getMessage());
    }

    /** Tests automatic extraction of column aliases from ResultSetMetaData. */
    @Test @Order(600)
    void testExtractAliases_fromMetaData() throws SQLException {
        var rs = Mockito.mock(ResultSet.class);
        var metaData = Mockito.mock(ResultSetMetaData.class);
        var c = new Columns();

        when(rs.next()).thenReturn(true, false);
        when(rs.getMetaData()).thenReturn(metaData);
        when(rs.getObject(c.add("id"), Integer.class)).thenReturn(10);
        when(rs.getObject(c.add("city.name"), String.class)).thenReturn("Prague");
        when(rs.getObject(c.add("boss.boss.name"), String.class)).thenReturn(null);

        // Mock metadata to provide column labels automatically
        var aliases = c.toArray();
        when(metaData.getColumnCount()).thenReturn(aliases.length);
        for (var i = 0; i < aliases.length; i++) {
            when(metaData.getColumnLabel(i + 1)).thenReturn(aliases[i]);
        }

        var service = DomainHandlerProvider.provider();
        var mapper = ResultSetTreeMapper.of(Employee.class, service);
        var result = mapper.convert(rs).findFirst().get();
        assertEquals(10, result.getId());
        assertNotNull(result.getCity());
        assertEquals("Prague", result.getCity().getName());
        assertNotNull(result.getBoss());
        assertNotNull(result.getBoss().getBoss());
        assertNull(result.getBoss().getBoss().getName());
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
        public static final Key<Employee, Integer> employee_id = dh.getKey("id", Integer.class);
        public static final Key<Employee, String> employee_name = dh.getKey("name", String.class);
        public static final Key<Employee, City> employee_city = dh.getKey("city", City.class);
        public static final Key<Employee, Employee> employee_boss = dh.getKey("boss", Employee.class);
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
        public static final Key<City, Integer> city_id = dh.getKey("id", Integer.class);
        public static final Key<City, String> city_name = dh.getKey("name", String.class);
        public static final Key<City, Country> city_country = dh.getKey("country", Country.class);
    }

    /** Represents a country entity */
    @Getter
    @Setter
    public static class Country {
        private Integer id;
        private String name;

        // Optional keys:
        static final DomainHandler<Country> dh = DomainHandlerProvider.getHandler(Country.class);
        public static final Key<Country, Integer> country_id = dh.getKey("id", Integer.class);
        public static final Key<Country, String> country_name = dh.getKey("name", String.class);
    }
}