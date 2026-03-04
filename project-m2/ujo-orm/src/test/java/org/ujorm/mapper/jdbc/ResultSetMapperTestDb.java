package org.ujorm.mapper.jdbc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.mapper.core.AbstractDaoTest;
import org.ujorm.mapper.core.EntityManager;
import org.ujorm.mapper.demo.City;
import org.ujorm.mapper.demo.Employee;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/** Tests a logic of column aliases processing in ResultSetMapper */
public class ResultSetMapperTestDb extends AbstractDaoTest {

    City cityOriginal = null;
    Employee employeeOriginal = null;

    /** Set up the database connection and initialize tables before each test. */
    @BeforeEach
    void setUp() throws SQLException {
        var cityDao = EntityManager.of(City.class, Long.class).crud(dbConnection);
        var emplDao = EntityManager.of(Employee.class, Long.class).crud(dbConnection);
        cityOriginal = cityDao.insert(new City(2L, "California", "US", 36.7783, -119.4179));
        employeeOriginal = emplDao.insert(createEmployee(1L, "EmplA", cityOriginal));
    }

    @Test
    void readEmployeeTest_explicitId() throws SQLException {
        var sql = """
            SELECT
              id AS "id"
            , name AS "name"
            , city_id AS "city.id"
            , is_active AS "active"
            , contract_day AS "contractDay"
            , superior_id AS "superior.id"
             FROM employee 
             WHERE id = ?
            """;
        var resultSet = getEmployeeResultSet(sql,1L);
        var service = DomainHandlerProvider.provider();
        var mapper = ResultSetMapper.of(Employee.class, service);
        var employee = mapper.convert(resultSet).findFirst();
        Assertions.assertTrue(employee.isPresent());
        Assertions.assertEquals(1L, employee.get().getId());
        Assertions.assertEquals(2L, employee.get().getCity().id());
    }

    @Test
    void readEmployeeTest_implicitId() throws SQLException {
        var sql = """
            SELECT
              id AS "id"
            , city_id AS "city"
            , superior_id AS "superior"
             FROM employee 
             WHERE id = ?
            """;
        var resultSet = getEmployeeResultSet(sql,1L);
        var service = DomainHandlerProvider.provider();
        var mapper = ResultSetMapper.of(Employee.class, service);
        var employee = mapper.convert(resultSet).findFirst();
        Assertions.assertTrue(employee.isPresent());
        Assertions.assertEquals(1L, employee.get().getId());
        Assertions.assertEquals(2L, employee.get().getCity().id());
    }

    /** Gets the ResultSet for the employee with ID 1 */
    private ResultSet getEmployeeResultSet(String sql, Long id) throws SQLException {
        var statement = dbConnection.prepareStatement(sql);
        statement.setLong(1, id);
        return statement.executeQuery();
    }

    /** Create a new Employee with ID */
    public Employee createEmployee(Long id, String name, City city) {
        return Employee.of(id, name, null, city, LocalDate.of(2020, 1, 1), true);
    }
}