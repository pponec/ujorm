package org.ujorm.orm.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.orm.demo.City;
import org.ujorm.orm.demo.Employee;

import java.time.LocalDate;

/** Tests for partial updates using SnapshotProvider */
class EntityManagerReadTest extends AbstractDaoTest {

    @Test
    void readTest() {
        var cityDao = EntityManager.of(City.class, Long.class).crud(dbConnection);
        var city = cityDao.insert(new City(null, "California", "US", 36.7783, -119.4179));
        var emplDao = EntityManager.of(Employee.class, Long.class).crud(dbConnection);
        var employee1 = emplDao.insert(createEmployee("EmplA", city));

        var emplReloaded = emplDao.findByIdNullable(employee1.getId());
        Assertions.assertNotNull(emplReloaded);
    }

    /** Create a new Employee without ID */
    public Employee createEmployee(String name, City city) {
        return createEmployee(null, name, city);
    }

    /** Create a new Employee with ID */
    public Employee createEmployee(Long id, String name, City city) {
        return Employee.of(id, name, null, city, LocalDate.of(2020, 1, 1), true);
    }

    /** Tests the generation of SqlQuery using the select method. */
    @Test
    void testSelectBuilder() {
        var cityDao = EntityManager.of(City.class, Long.class).crud(dbConnection);
        var expectedSql = """
                SELECT "ID" AS "id"
                , "NAME" AS "name"
                , "COUNTRY_CODE" AS "countryCode"
                , "LATITUDE" AS "latitude"
                , "LONGITUDE" AS "longitude" FROM "CITY" WHERE name = :name
                """.trim();
        var sql = cityDao.selectWhere("name = :name", b -> b.toString());
        var resultSql = sql.toString().replace(", ", "\n, ");
        Assertions.assertEquals(expectedSql, resultSql);
    }
}