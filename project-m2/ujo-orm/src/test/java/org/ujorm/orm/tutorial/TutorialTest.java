package org.ujorm.orm.tutorial;

import org.junit.jupiter.api.*;
import org.ujorm.orm.core.EntityManager;
import org.ujorm.orm.tutorial.domains.*;
import org.ujorm.tools.jdbc.SqlQuery;

import java.util.Comparator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Ujorm3: Lightweight, fast, and transparent ORM.
 * Entities are either standard JavaBeans or Records. No magic, pure speed.
 * Note: These tests run sequentially to demonstrate an entity lifecycle.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TutorialTest extends AbstractDemo {

    private static final EntityManager<Employee, Long> EMPLOYEE_EM = EntityManager.of(Employee.class);
    private static final EntityManager<City, Long> CITY_EM = EntityManager.of(City.class);

    @Test
    @Order(100)
    void insert() {
        var employeeCrud = EMPLOYEE_EM.crud(connection());
        var cityCrud = CITY_EM.crud(connection());

        // City is an immutable Record, Employee is a mutable JavaBean
        var cityOttawa = cityCrud.insert(new City(null, "Ottawa", "CA"));
        var emplIngird = Employee.of("Ingrid", cityOttawa, null);
        var emplDave = Employee.of("Dave", cityOttawa, emplIngird);
        var emplCarol = Employee.of("Carol", cityOttawa, emplIngird);

        employeeCrud.insert(emplIngird);
        employeeCrud.insert(emplDave, emplCarol);
    }

    /** Safe aliasing using generated Meta classes prevents SQL typos */
    @Test
    @Order(200)
    void select() {
        var sql = """
                 SELECT ${COLUMNS}
                 FROM employee e
                 JOIN city c ON c.id = e.city_id
                 LEFT JOIN employee b ON b.id = e.boss_id
                 WHERE e.id > :employeeId
                 """;

        var employees = SqlQuery.run(connection(), builder -> builder
                .sql(sql)
                .column("e.id", MetaEmployee.id)
                .column("e.name", MetaEmployee.name)
                .column("c.name", MetaEmployee.city, MetaCity.name)
                .column("c.country_code", MetaEmployee.city, MetaCity.countryCode)
                .column("b.name", MetaEmployee.boss, MetaEmployee.name)
                .bind("employeeId", 0L)
                .streamMap(EMPLOYEE_EM.mapper())
                .toList());

        assertEquals(3, employees.size());
        assertEquals("Dave", employees.get(1).getName());
        assertEquals("Ingrid", employees.get(1).getBoss().getName());
    }

    @Test
    @Order(300)
    void update() {
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
    void delete() {
        var employeeCrud = EMPLOYEE_EM.crud(connection());

        var allEmployees = employeeCrud
                .selectWhere("id > :employeeId", builder -> builder
                        .bind("employeeId", 0L)
                        .streamMap(EMPLOYEE_EM.mapper())
                        .sorted(Comparator.comparing(e -> e.getBoss() == null))
                        .toList());

        employeeCrud.delete(allEmployees.stream());

        var count = SqlQuery.run(connection(), build -> build
                .sql("SELECT COUNT(*) FROM employee WHERE id >= :employeeId")
                .bind("employeeId", 0L)
                .streamMap(rs -> rs.getInt(1))
                .findFirst()
                .orElseThrow());
        assertEquals(0, count);
    }

    /** Create all database tables first */
    @Override
    void init() {
        try (var builder = new SqlQuery(connection())) {
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