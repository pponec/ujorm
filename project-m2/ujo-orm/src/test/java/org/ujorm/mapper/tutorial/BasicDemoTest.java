package org.ujorm.mapper.tutorial;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.ujorm.mapper.Crud;
import org.ujorm.mapper.UjormServiceProvider;
import org.ujorm.mapper.core.EntityManager;
import org.ujorm.mapper.tutorial.domains.City;
import org.ujorm.mapper.tutorial.domains.Employee;
import org.ujorm.tools.jdbc.SqlParamBuilder;

import java.sql.SQLException;

public class BasicDemoTest extends AbstractDemo {

    /** ??? */
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
    void init1() throws SQLException {
        employeeCrud = EMPLOYEE_EM.crud(connection());
        cityCrud = CITY_EM.crud(connection());
    }
    @BeforeAll
    void init2() throws SQLException {
        employeeCrud = UjormServiceProvider.crud(Employee.class, connection());
        cityCrud = UjormServiceProvider.crud(City.class, connection());
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
                    , superior_id BIGINT NULL
                    , city_id BIGINT NOT NULL
                    )
                    """).execute();
            builder.sql("""
                    ALTER TABLE employee ADD CONSTRAINT fk_employee_superior_id__id
                    FOREIGN KEY (superior_id)
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
        var city = new City(null, "Ottawa", "CA");


    }

    @Test
    @Order(100)
    void select() {

    }

    @Test
    @Order(100)
    void update() {

    }

    @Test
    @Order(100)
    void delete() {

    }

}
