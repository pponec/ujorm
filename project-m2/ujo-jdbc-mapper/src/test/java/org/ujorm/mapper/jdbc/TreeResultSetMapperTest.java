package org.ujorm.mapper.jdbc;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.DomainHandlerService;
import org.ujorm.core.Key;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

public class TreeResultSetMapperTest {

    @Test
    void testConvertResultSetToDomain() throws SQLException {
        // 1. Prepare the mock ResultSet
        var rs = Mockito.mock(ResultSet.class);
        when(rs.getObject("id", Integer.class)).thenReturn(10);
        when(rs.getObject("name", String.class)).thenReturn("Jan Novak");
        when(rs.getObject("city.id", Integer.class)).thenReturn(100);
        when(rs.getObject("city.name", String.class)).thenReturn("Brno");
        when(rs.getObject("city.country.id", Integer.class)).thenReturn(1000);
        when(rs.getObject("city.country.name", String.class)).thenReturn("Czechia");
        when(rs.getObject("boss.id", Integer.class)).thenReturn(20);
        when(rs.getObject("boss.name", String.class)).thenReturn("Petr Boss");

        // 2. Prepare the DomainHandlerService stub
        var service = new StubDomainHandlerService();

        // 3. Define the column aliases we expect from the SQL SELECT
        var aliases = new String[]{
                "id",
                "name",
                "city.id",
                "city.name",
                "city.country.id",
                "city.country.name",
                "boss.id",
                "boss.name"
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
        assertEquals("Brno", result.getCity().getName());

        assertNotNull(result.getCity().getCountry());
        assertEquals(1000, result.getCity().getCountry().getId());
        assertEquals("Czechia", result.getCity().getCountry().getName());

        assertNotNull(result.getBoss());
        assertEquals(20, result.getBoss().getId());
        assertEquals("Petr Boss", result.getBoss().getName());
    }

    // --- Stubs for internal framework interfaces ---

    /**
     * Stub implementation of the DomainHandlerService for testing purposes.
     */
    static class StubDomainHandlerService implements DomainHandlerService {
        @Override
        public <T> DomainHandler<T> getHandler(Class<T> domainType) {
            return new DomainHandler<T>() {
                @Override
                public T newDomain() {
                    return createInstance(domainType);
                }
            };
        }

        @Override
        public <T> T createInstance(Class<T> type) {
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot instantiate " + type.getSimpleName(), e);
            }
        }
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