package org.ujorm.mapper.tutorial;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.ujorm.mapper.Crud;
import org.ujorm.mapper.core.EntityManager;
import org.ujorm.mapper.tutorial.domains.City;
import org.ujorm.mapper.tutorial.domains.Employee;
import org.ujorm.tools.jdbc.SqlParamBuilder;

import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class BasicDemoTest extends AbstractDemo {

    private static final EntityManager<City, Long> CITY_EM = EntityManager.of(City.class);
    private static final EntityManager<Employee, Long> EMPLOYEE_EM = EntityManager.of(Employee.class);

    private Crud<Employee, Long> employeeCrud;
    private Crud<City, Long> cityCrud;

    /**
     * Initializes the CRUD (Create, Read, Update, Delete) service objects for the City and Employee entities.
     * These objects are bound to a shared database connection.
     * <p>
     * Note that the transaction and connection lifecycles are managed by the parent abstract class:
     * a database commit is automatically performed after each individual test method finishes,
     * and the shared connection is safely closed once all tests in the class are completed.
     */
    @BeforeAll
    void init() {
        employeeCrud = EMPLOYEE_EM.crud(connection());
        cityCrud = CITY_EM.crud(connection());
    }

    @Test @Order(100)
    void createTable() {
        try (var builder = new SqlParamBuilder(connection())) {
            builder.sql("""
                    CREATE TABLE city
                    ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                    , name VARCHAR(50) NOT NULL
                    , country_code VARCHAR(2) NOT NULL
                    )
                    """).execute();
            builder.sql("""
                    CREATE TABLE employee
                    ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                    , name VARCHAR(50) NOT NULL
                    , boss_id BIGINT NULL
                    , city_id BIGINT NOT NULL
                    )
                    """).execute();
            builder.sql("""
                    ALTER TABLE employee ADD CONSTRAINT fk_employee_boss_id__id
                    FOREIGN KEY (boss_id)
                    REFERENCES employee(id)
                    ON DELETE RESTRICT ON UPDATE RESTRICT;
                    """).execute();
            builder.sql("""
                    ALTER TABLE employee ADD CONSTRAINT fk_employee_city_id__id
                    FOREIGN KEY (city_id)
                    REFERENCES city(id)
                    ON DELETE CASCADE ON UPDATE RESTRICT;
                    """).execute();
        }
    }

    @Test
    @Order(100)
    void insert() {
        var cityOttawa = cityCrud.insert(new City(null, "Ottawa", "CA"));
        var emplIngird = Employee.of("Ingrid", cityOttawa, null);
        var emplDave = Employee.of("Dave", cityOttawa, emplIngird);
        var emplCarol = Employee.of("Carol", cityOttawa, emplIngird);

        employeeCrud.insertBatch(emplIngird, emplDave, emplCarol);
    }

    @Test
    @Order(100)
    void select() {
        try (var builder = new SqlParamBuilder(connection())) {
            builder.sql("""
                    SELECT e.id
                    , e.name
                    , c.name AS "city.name"
                    , c.country_code AS "city.countryCode"
                    , b.name AS "boss.name"
                    FROM employee e
                    JOIN city c ON c.id = e.id
                    OUTER JOIN boss b ON b.id = e.boss_id
                    WHERE e.id > :employeeId
                    ORDER BY e.id
                    """)
                    .bind("employeeId", 1L);
            var employees = builder.streamMap(EMPLOYEE_EM::map).toList();

            // Test employee names
            assertEquals("Ingrid", employees.get(0).getName());
            assertEquals("Dave", employees.get(1).getName());
            assertEquals("Carol", employees.get(2).getName());

            // Test boss names:
            assertNull(employees.get(0).getBoss().getName());
            assertNull("Ingrid", employees.get(1).getBoss().getName());
            assertNull("Ingrid", employees.get(2).getBoss().getName());

            // Test ID
            assertEquals(1L, employees.get(0).getId()); // Ingrid
            assertEquals(2L, employees.get(1).getId()); // Dave
            assertEquals(3L, employees.get(2).getId()); // Carol
        }
    }

    @Test
    @Order(100)
    void update() {
        var emplIngird = employeeCrud.findByIdNullable(1L);
        var emplDave = employeeCrud.findByIdNullable(2L);
        var emplCarol = employeeCrud.findByIdNullable(3L);
        var newBoss = emplDave;

        emplIngird.setBoss(newBoss);
        emplDave.setBoss(null);
        emplCarol.setBoss(newBoss);
        employeeCrud.updateBatch(Stream.of(emplIngird, emplDave, emplCarol), "boss");

        assertNull(employeeCrud.findByIdNullable(2L).getBoss(), "The new boss is Dave");
    }

    @Test
    @Order(100)
    void delete() {
        var allEmployees = employeeCrud
                .select("id > :id")
                .bind("id", 0L)
                .streamMap(EMPLOYEE_EM::map)
                .sorted(Comparator.comparing(e -> e.getBoss() == null)) // The boss is the last
                .toList();

        assertEquals(3, allEmployees);
        employeeCrud.deleteBatch(allEmployees.stream());

        var count =  employeeCrud
                .select("1 = 1")
                .streamMap(e -> e)
                .count();

        assertEquals(0L, count);
    }

}
