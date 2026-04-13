package org.ujorm.orm.jdbc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.orm.core.AbstractDaoTest;
import org.ujorm.orm.demo.City;
import org.ujorm.orm.demo.Employee;
import org.ujorm.orm.utils.EntityContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/** Tests a logic of column aliases processing in ResultSetMapper */
class ResultSetMapperTestDb extends AbstractDaoTest {

    private final EntityContext ctx = EntityContext.ofDefault();
    private final Class<Long> idType = Long.class;

    City cityOriginal = null;
    Employee employeeOriginal = null;

    /** Set up the database connection and initialize tables before each test. */
    @BeforeEach
    void setUp() throws SQLException {
        var cityDao = ctx.entityManager(City.class, idType).crud(dbConnection);
        var emplDao = ctx.entityManager(Employee.class, idType).crud(dbConnection);
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

    @Test
    void entityManagerMapTest() throws SQLException {
        var sql = """
            SELECT
              id AS "id"
            , name AS "name"
            , city_id AS "city.id"
             FROM employee 
             WHERE id = ?
            """;
        var resultSet = getEmployeeResultSet(sql, 1L);
        var entityManager = ctx.entityManager(Employee.class, idType);

        Assertions.assertTrue(resultSet.next(), "ResultSet should contain at least one row");
        var employee = entityManager.map(resultSet);

        Assertions.assertNotNull(employee);
        Assertions.assertEquals(1L, employee.getId());
        Assertions.assertEquals("EmplA", employee.getName());
        Assertions.assertEquals(2L, employee.getCity().id());
    }

    @Test
    void entityManagerMapperTest() throws SQLException {
        var sql = """
            SELECT
              id AS "id"
            , name AS "name"
            , city_id AS "city.id"
             FROM employee 
             WHERE id = ?
            """;
        var resultSet = getEmployeeResultSet(sql, 1L);
        var entityManager = ctx.entityManager(Employee.class, idType);
        var mapper = entityManager.mapper();

        Assertions.assertTrue(resultSet.next(), "ResultSet should contain at least one row");
        var employee = mapper.applyFunction(resultSet);

        Assertions.assertNotNull(employee);
        Assertions.assertEquals(1L, employee.getId());
        Assertions.assertEquals("EmplA", employee.getName());
        Assertions.assertEquals(2L, employee.getCity().id());
    }

    /** Tests mapping of a single row using the mapSingle method */
    @Test
    void mapSingleTest() throws SQLException {
        var sql = """
            SELECT
              id AS "id"
            , name AS "name"
            , city_id AS "city.id"
             FROM employee 
             WHERE id = ?
            """;
        var resultSet = getEmployeeResultSet(sql, 1L);
        var service = DomainHandlerProvider.provider();
        var mapper = ResultSetMapper.of(Employee.class, service);

        Assertions.assertTrue(resultSet.next(), "ResultSet should contain at least one row");
        var employee = mapper.map(resultSet);

        Assertions.assertNotNull(employee);
        Assertions.assertEquals(1L, employee.getId());
        Assertions.assertEquals("EmplA", employee.getName());
        Assertions.assertEquals(2L, employee.getCity().id());
    }

    /** Tests mapping of a single row using the mapSingle method with explicit column labels */
    @Test
    void mapSingleWithLabelsTest() throws SQLException {
        var sql = "SELECT id, name, city_id FROM employee WHERE id = ?";
        var resultSet = getEmployeeResultSet(sql, 1L);
        var service = DomainHandlerProvider.provider();
        var mapper = ResultSetMapper.of(Employee.class, service);

        Assertions.assertTrue(resultSet.next(), "ResultSet should contain at least one row");
        // Mapping explicit column labels to match the ResultSet structure
        var employee = mapper.map(resultSet, "id", "name", "city.id");

        Assertions.assertNotNull(employee);
        Assertions.assertEquals(1L, employee.getId());
        Assertions.assertEquals("EmplA", employee.getName());
        Assertions.assertEquals(2L, employee.getCity().id());
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