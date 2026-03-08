package org.ujorm.orm.tutorial;

import org.junit.jupiter.api.*;
import org.ujorm.orm.core.EntityManager;
import org.ujorm.orm.tutorial.domains.*;
import org.ujorm.tools.jdbc.SqlParamBuilder;

import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Ujorm 3: Lightweight, fast, and transparent ORM.
 * Entities are either standard JavaBeans or Records. No magic, pure speed.
 * Note: These tests run sequentially to demonstrate an entity lifecycle.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BasicDemoTest extends AbstractDemo {

    private static final EntityManager<Employee, Long> EMPLOYEE_EM = EntityManager.of(Employee.class);
    private static final EntityManager<City, Long> CITY_EM = EntityManager.of(City.class);

    @Test
    @Order(100)
    void insertEntities() {
        var employeeCrud = EMPLOYEE_EM.crud(connection());
        var cityCrud = CITY_EM.crud(connection());

        // City is an immutable Record, Employee is a mutable JavaBean
        var cityOttawa = cityCrud.insert(new City(null, "Ottawa", "CA"));
        var emplIngird = Employee.of("Ingrid", cityOttawa, null);
        var emplDave = Employee.of("Dave", cityOttawa, emplIngird);
        var emplCarol = Employee.of("Carol", cityOttawa, emplIngird);

        employeeCrud.insert(emplIngird);
        employeeCrud.insertBatch(emplDave, emplCarol);
    }

    @Test
    @Order(200)
    void selectWithTypeSafeLabels() {
        // Safe aliasing using generated Meta classes prevents SQL typos
        var sql = """
                 SELECT e.id      AS ${e.id}
                 , e.name         AS ${e.name}
                 , c.name         AS ${c.name}
                 , c.country_code AS ${c.country_code}
                 , b.name         AS ${b.name}
                 FROM employee e
                 JOIN city c ON c.id = e.city_id
                 LEFT JOIN employee b ON b.id = e.boss_id
                 WHERE e.id > :employeeId
                 """;

        var employees = SqlParamBuilder.run(connection(), builder -> builder
                .sql(sql)
                .label("e.id", MetaEmployee.id)
                .label("e.name", MetaEmployee.name)
                .label("c.name", MetaEmployee.city, MetaCity.name)
                .label("c.country_code", MetaEmployee.city, MetaCity.countryCode)
                .label("b.name", MetaEmployee.boss, MetaEmployee.name)
                .bind("employeeId", 0L)
                .streamMap(EMPLOYEE_EM::map)
                .toList());

        assertEquals(3, employees.size());
        assertEquals("Dave", employees.get(1).getName());
        assertEquals("Ingrid", employees.get(1).getBoss().getName());
    }

    @Test
    @Order(300)
    void partialUpdate() {
        var employeeCrud = EMPLOYEE_EM.crud(connection());

        var emplIngird = employeeCrud.findById(1L).orElseThrow();
        var emplDave = employeeCrud.findById(2L).orElseThrow();
        var emplCarol = employeeCrud.findById(3L).orElseThrow();
        var newBoss = emplDave;

        emplIngird.setBoss(newBoss);
        emplDave.setBoss(null);
        emplCarol.setBoss(newBoss);

        employeeCrud.update(Stream.of(emplIngird, emplDave, emplCarol), MetaEmployee.boss);

        assertNull(employeeCrud.findByIdNullable(2L).getBoss());
    }

    @Test
    @Order(400)
    void deleteWithDependencies() {
        var employeeCrud = EMPLOYEE_EM.crud(connection());

        var allEmployees = employeeCrud
                .selectWhere("id > :id", builder -> builder
                        .bind("id", 0L)
                        .streamMap(EMPLOYEE_EM::map)
                        .sorted(Comparator.comparing(e -> e.getBoss() == null))
                        .toList());

        employeeCrud.delete(allEmployees.stream());

        var count = employeeCrud.selectWhere("1=1", b -> b.streamMap(EMPLOYEE_EM::map).count());
        assertEquals(0L, count);
    }

    /** Create all database tables first */
    @Override
    void init() {
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
}