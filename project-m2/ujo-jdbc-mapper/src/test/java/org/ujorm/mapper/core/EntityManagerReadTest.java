package org.ujorm.mapper.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.mapper.demo.City;
import org.ujorm.mapper.demo.Employee;

import java.time.LocalDate;

/** Tests for partial updates using SnapshotProvider */
class EntityManagerReadTest extends AbstractDaoTest {

    @Test
    void readTest() {
        var cityDao = EntityManager.of(City.class, dbConnection, Long.class);
        var city = cityDao.insert(new City(null, "California", "US", 36.7783, -119.4179));
        var emplDao = EntityManager.of(Employee.class, dbConnection, Long.class);
        var employee1 = emplDao.insert(createEmployee("EmplA", city));

        var emplReloaded = emplDao.readNullable(employee1.getId());
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
}