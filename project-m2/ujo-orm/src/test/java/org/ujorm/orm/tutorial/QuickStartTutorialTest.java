package org.ujorm.orm.tutorial;

import org.junit.jupiter.api.*;
import org.ujorm.orm.SqlQuery;
import org.ujorm.orm.core.EntityManager;
import org.ujorm.orm.dsl.SelectQuery;
import org.ujorm.orm.jdbc.ResultSetMapper;
import org.ujorm.orm.tutorial.domains.City;
import org.ujorm.orm.tutorial.domains.Employee;
import org.ujorm.orm.tutorial.domains.MetaCity;
import org.ujorm.orm.tutorial.domains.MetaEmployee;
import org.ujorm.orm.utils.EntityContext;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ujorm3: Lightweight, fast, and transparent ORM.
 * Entities are either standard JavaBeans or Records. No magic, pure speed.
 * Note: These tests run sequentially to demonstrate an entity lifecycle.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuickStartTutorialTest extends AbstractDemo {

    private static final EntityContext CTX = EntityContext.ofSqlInfo();
    private static final EntityManager<City, Long> CITY_EM = CTX.entityManager(City.class);
    private static final ResultSetMapper<Employee> EMPLOYEE_MAPPER = ResultSetMapper.of(Employee.class);

    @Test
    @Order(100)
    void insert() {
        // City is an immutable Recor
        var cityOttawa = new City(1L, "Ottawa", "CA");
        var cityBarcelone = new City(2L, "Barcelona", "ES");

        try (var query = new SqlQuery(connection())) {
            var sql = """
                    INSERT INTO
                    
                    """;

        }
    }

    /** Select employees and map columns by type-safe generated Meta classes. */
    @Test
    @Order(200)
    void select() {
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
                 ORDER BY e.id
                 """;

        var employees = SqlQuery.run(connection(), query -> query
                .sql(sql)
                .label("e.id", MetaEmployee.id)
                .label("e.name", MetaEmployee.name)
                .label("c.name", MetaEmployee.city, MetaCity.name)
                .label("c.country_code", MetaEmployee.city, MetaCity.countryCode)
                .label("b.name", MetaEmployee.boss, MetaEmployee.name)
                .bind("employeeId", 0L)
                .streamMap(EMPLOYEE_MAPPER.mapper())
                .toList());

        assertEquals(3, employees.size());
        assertEquals("Dave", employees.get(1).getName());
        assertEquals("Ingrid", employees.get(1).getBoss().getName());
    }

    /** Simplified select by the column method. */
    @Test
    @Order(210)
    void select_by_columns() {
        var sql = """
                 SELECT ${COLUMNS}
                 FROM employee e
                 JOIN city c ON c.id = e.city_id
                 LEFT JOIN employee b ON b.id = e.boss_id
                 WHERE e.id > :employeeId
                 ORDER BY e.id
                 """;

        var employees = SqlQuery.run(connection(), query -> query
                .sql(sql)
                .column("e.id", MetaEmployee.id)
                .column("e.name", MetaEmployee.name)
                .column("c.name", MetaEmployee.city, MetaCity.name)
                .column("c.country_code", MetaEmployee.city, MetaCity.countryCode)
                .column("b.name", MetaEmployee.boss, MetaEmployee.name)
                .bind("employeeId", 0L)
                .streamMap(EMPLOYEE_MAPPER.mapper())
                .toList());

        assertEquals(3, employees.size());
        assertEquals("Dave", employees.get(1).getName());
        assertEquals("Ingrid", employees.get(1).getBoss().getName());
    }


    /** Note the last argument of the update() method specifying the modified attribute. */
    @Test
    @Order(300)
    void update() {
        var sql = """
               """;
    }

    /** Note the returned row count at the end of the method. */
    @Test
    @Order(400)
    void delete() {
       var sql = """
               """;
    }

    /** Create all database tables first. */
    @Override
    void init() {
        try (var query = new SqlQuery(connection())) {
            query.sql("""
                    CREATE TABLE city
                    ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                    , name VARCHAR(50) NOT NULL
                    , country_code VARCHAR(2) NOT NULL
                    )
                    """).execute();
            query.sql("""
                    CREATE TABLE employee
                    ( id BIGINT AUTO_INCREMENT PRIMARY KEY
                    , name VARCHAR(50) NOT NULL
                    , boss_id BIGINT NULL
                    , city_id BIGINT NOT NULL
                    )
                    """).execute();
            query.sql("""
                    ALTER TABLE employee ADD CONSTRAINT fk_employee_boss_id__id
                    FOREIGN KEY (boss_id)
                    REFERENCES employee(id)
                    ON DELETE RESTRICT ON UPDATE RESTRICT;
                    """).execute();
            query.sql("""
                    ALTER TABLE employee ADD CONSTRAINT fk_employee_city_id__id
                    FOREIGN KEY (city_id)
                    REFERENCES city(id)
                    ON DELETE CASCADE ON UPDATE RESTRICT;
                    """).execute();
        }
    }
}