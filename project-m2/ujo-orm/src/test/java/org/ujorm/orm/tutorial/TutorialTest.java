package org.ujorm.orm.tutorial;

import org.junit.jupiter.api.*;
import org.ujorm.orm.core.EntityManager;
import org.ujorm.orm.dsl.DslQuery;
import org.ujorm.orm.jdbc.ResultSetMapper;
import org.ujorm.orm.tutorial.domains.*;
import org.ujorm.orm.SqlQuery;

import java.util.Comparator;
import java.util.logging.Level;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ujorm3: Lightweight, fast, and transparent ORM.
 * Entities are either standard JavaBeans or Records. No magic, pure speed.
 * Note: These tests run sequentially to demonstrate an entity lifecycle.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TutorialTest extends AbstractDemo {

    private static final ResultSetMapper<Employee> EMPLOYEE_MAPPER = ResultSetMapper.of(Employee.class);
    private static final EntityManager<Employee, Long> EMPLOYEE_EM = EntityManager.of(Employee.class);
    private static final EntityManager<City, Long> CITY_EM = EntityManager.of(City.class);

    @Test
    @Order(100)
    void insert() {
        var employeeCrud = EMPLOYEE_EM.crud(connection());
        var cityCrud = CITY_EM.crud(connection());

        // City is an immutable Record, Employee is a mutable JavaBean
        var cityOttawa = cityCrud.insert(new City(null, "Ottawa", "CA"));
        var emplIngrid = Employee.of("Ingrid", cityOttawa, null);
        var emplDave = Employee.of("Dave", cityOttawa, emplIngrid);
        var emplCarol = Employee.of("Carol", cityOttawa, emplIngrid);

        employeeCrud.insert(emplIngrid);
        employeeCrud.insert(emplDave, emplCarol);

        assertNotNull(emplIngrid.getId());
    }

    /** Select employees and map columns by type-safe generated Meta classes. */
    @Test
    @Order(200)
    void select_by_labels() {
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

    /** Select an Entity by ID. */
    @Test
    @Order(220)
    void selectEntity_by_id() {
        var crud = CITY_EM.crud(connection());
        var barcelonaId = 1L;
        var barcelona = crud.findById(barcelonaId).orElseThrow();
        Assertions.assertNotNull(barcelona.id());
    }


    /** DSL select by the column method. */
    //@Test
    @Order(230)
    void select_by_dsl() {
        var employees = DslQuery.run(connection(), EMPLOYEE_EM, query -> query
                .sql("SELECT")
                .column(MetaEmployee.id)
                .column(MetaEmployee.name)
                .column(MetaEmployee.city, MetaCity.name)
                .column(MetaEmployee.city, MetaCity.countryCode)
                .column(MetaEmployee.boss, MetaEmployee.name)
                .where(MetaEmployee.id.whereLe(1L))
                .tail("ORDER BY", MetaEmployee.id)
                .log(Level.INFO, true) // LOG SQL statement.
                .streamMap(EMPLOYEE_MAPPER.mapper())
                .toList()
        );

        assertEquals(3, employees.size());
        assertEquals("Dave", employees.get(1).getName());
        assertEquals("Ingrid", employees.get(1).getBoss().getName());
    }


    /** Note the last argument of the update() method specifying the modified attribute. */
    @Test
    @Order(300)
    void update() {
        var employeeCrud = EMPLOYEE_EM.crud(connection());

        var emplIngrid = employeeCrud.findById(1L).orElseThrow();
        var emplDave = employeeCrud.findById(2L).orElseThrow();
        var emplCarol = employeeCrud.findById(3L).orElseThrow();

        emplIngrid.setBoss(emplDave);
        emplDave.setBoss(null);
        emplCarol.setBoss(emplDave);

        employeeCrud.update(Stream.of(emplIngrid, emplDave, emplCarol),
                MetaEmployee.boss);

        assertNull(employeeCrud.findByIdNullable(2L).getBoss());
    }

    /** Note the returned row count at the end of the method. */
    @Test
    @Order(400)
    void delete() {
        var employeeCrud = EMPLOYEE_EM.crud(connection());

        var allEmployees = employeeCrud
                .selectWhere("id > :id", query -> query
                        .bind("id", 0L)
                        .streamMap(EMPLOYEE_EM.mapper())
                        .sorted(Comparator.comparing(e -> e.getBoss() == null))
                        .toList());

        employeeCrud.delete(allEmployees.stream());

        var count = SqlQuery.run(connection(), query -> query
                .sql("SELECT COUNT(*) FROM employee WHERE id >= :id")
                .bind("id", 0L)
                .streamMap(rs -> rs.getInt(1))
                .findFirst()
                .orElseThrow());
        assertEquals(0, count);
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