package org.ujorm.orm.tutorial;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.ujorm.orm.SqlQuery;
import org.ujorm.orm.core.EntityManager;
import org.ujorm.orm.tutorial.domains.City;
import org.ujorm.orm.tutorial.domains.Employee;
import org.ujorm.orm.tutorial.domains.MetaCity;
import org.ujorm.orm.tutorial.domains.MetaEmployee;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Base test class for all database integration tests */
@SpringBootTest(classes = AbstractTutorialIT.TestConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractTutorialIT {

    protected EntityManager<Employee, Long> employeeEm;
    protected EntityManager<City, Long> cityEm;
    private Connection dbConnection;

    @Autowired
    private DataSource dataSource;

    /** Inner class for specific Spring configuration if needed */
    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestConfig { }

    @BeforeAll
    void setupDatabase() throws SQLException {
        employeeEm = EntityManager.of(Employee.class);
        cityEm = EntityManager.of(City.class);

        dbConnection = dataSource.getConnection();
        dbConnection.setAutoCommit(false);
        init();
        dbConnection.commit();
    }

    /** Commit the transaction after each test method to keep changes for the next test. */
    @AfterEach
    void commitTransaction() throws SQLException {
        if (dbConnection != null && !dbConnection.isClosed()) {
            dbConnection.commit();
        }
    }

    /** Close the shared connection at the very end. */
    @AfterAll
    void tearDown() throws SQLException {
        if (dbConnection != null) {
            dbConnection.close();
        }
    }

    /** Abstract method to initialize database schema */
    abstract void init();

    protected Connection connection() {
        try {
            if (dbConnection == null || dbConnection.isClosed()) {
                dbConnection = dataSource.getConnection();
                dbConnection.setAutoCommit(false);
            }
            return dbConnection;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get database connection", e);
        }
    }

    @Test
    @Order(100)
    void insert() {
        var employeeCrud = employeeEm.crud(connection());
        var cityCrud = cityEm.crud(connection());

        // City is an immutable Record, Employee is a mutable JavaBean
        var cityOttawa = cityCrud.insert(new City(null, "Ottawa", "CA"));
        var emplIngrid = Employee.of("Ingrid", cityOttawa, null);
        var emplDave = Employee.of("Dave", cityOttawa, emplIngrid);
        var emplCarol = Employee.of("Carol", cityOttawa, emplIngrid);

        employeeCrud.insert(emplIngrid);
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

        var employees = SqlQuery.run(connection(), query -> query
                .sql(sql)
                .column("e.id", MetaEmployee.id)
                .column("e.name", MetaEmployee.name)
                .column("c.name", MetaEmployee.city, MetaCity.name)
                .column("c.country_code", MetaEmployee.city, MetaCity.countryCode)
                .column("b.name", MetaEmployee.boss, MetaEmployee.name)
                .bind("employeeId", 0L)
                .log(Level.INFO, true)
                .streamMap(employeeEm.mapper())
                .toList());

        assertEquals(3, employees.size());
        assertEquals("Dave", employees.get(1).getName());
        assertEquals("Ingrid", employees.get(1).getBoss().getName());
    }

    /** Note the last argument of the update() method specifying the modified attribute. */
    @Test
    @Order(300)
    void update() {
        var employeeCrud = employeeEm.crud(connection());

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
        var employeeCrud = employeeEm.crud(connection());

        var allEmployees = employeeCrud
                .selectWhere("id > :id", query -> query
                        .bind("id", 0L)
                        .streamMap(employeeEm.mapper())
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
}