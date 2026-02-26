package org.ujorm.mapper.jdbc;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.generator.ClassName;
import org.ujorm.core.generator.DomainModel;
import org.ujorm.core.generator.JavaSourceGenerator;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class TreeResultSetMapperTest {

    private final boolean printResult = false;

    @Test @Order(100)
    void codeGen() {
        var meta = DomainModel.of(Employee.class);
        var className = ClassName.ofGenerated(meta);
        var src = new JavaSourceGenerator().getSourceCode(meta, className);
        if (printResult) System.out.println(src);

        assertTrue(src.contains("static final class Key_city extends AbstractKey<Employee, org.ujorm.mapper.jdbc.TreeResultSetMapperTest.City> {"));
        assertTrue(src.contains("public org.ujorm.mapper.jdbc.TreeResultSetMapperTest.City getValue(@NotNull final Employee bean) {"));
    }

    @Test @Order(200)
    void testConvertResultSetToDomain() throws SQLException {

        // 1. Prepare the mock ResultSet
        var rs = Mockito.mock(ResultSet.class);
        when(rs.getObject("id", Integer.class)).thenReturn(10);
        when(rs.getObject("name", String.class)).thenReturn("Jan Novak");
        when(rs.getObject("city.id", Integer.class)).thenReturn(100);
        when(rs.getObject("city.name", String.class)).thenReturn("Prague");
        when(rs.getObject("city.country.id", Integer.class)).thenReturn(1000);
        when(rs.getObject("city.country.name", String.class)).thenReturn("Czechia");
        when(rs.getObject("boss.id", Integer.class)).thenReturn(20);
        when(rs.getObject("boss.name", String.class)).thenReturn("Petr Boss");
        when(rs.getObject("boss.boss.id", Integer.class)).thenReturn(null);
        when(rs.getObject("boss.boss.name", String.class)).thenReturn(null);

        // 2. Prepare the DomainHandlerService
        var service = DomainHandlerProvider.provider();

        // 3. Define the column aliases we expect from the SQL SELECT
        var aliases = new String[]{
                "id",
                "name",
                "city.id",
                "city.name",
                "city.country.id",
                "city.country.name",
                "boss.id",
                "boss.name",
                "boss.boss.id",
                "boss.boss.name"
        };

        // 4. Initialize the mapper using the requested factory method of()
        var mapper = TreeResultSetMapper.of(Employee.class, service, aliases);

        // 5. Execute the mapping
        var result = mapper.convert(rs);

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

    // --- Domain Classes ---


    /** Represents an employee entity */
    @Getter
    @Setter
    public static class Employee {
        private Integer id;
        private String name;
        private City city;
        private Employee boss;
    }


    /** Represents a city entity */
    @Getter
    @Setter
    public static class City {
        private Integer id;
        private String name;
        private Country country;
    }


    /** Represents a country entity */
    @Getter
    @Setter
    public static class Country {
        private Integer id;
        private String name;
    }

}