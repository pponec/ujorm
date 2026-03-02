package org.ujorm.mapper.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.mapper.demo.City;
import org.ujorm.mapper.demo.Employee;

import java.time.LocalDate;

/** Tests for partial updates using SnapshotProvider */
class EntityManagerUpdateTest extends AbstractDaoTest {

    /** Test partial updates of one or multiple entities */
    //@Test // TODO:pop
    void partialUpdate() {
        var cityDao = EntityManager.of(City.class, dbConnection, Long.class);
        var city = cityDao.insert(new City(null, "California", "US", 36.7783, -119.4179));

        var emplDao = EntityManager.of(Employee.class, dbConnection, Long.class);
        var employee1 = emplDao.insert(createEmployee("EmplA", city));
        var employee2 = emplDao.insert(createEmployee(101L, "EmplB", city));

        // Update 1: Single entity partial update using fluent chaining
        employee1.saveSnapshot().setName("EmplA-Updated");
        Assertions.assertEquals(1L, emplDao.updateChanged(employee1));
        Assertions.assertEquals("EmplA-Updated", employee1.getName());

        // Update 2: Multiple entities partial update using fluent chaining
        employee1.saveSnapshot().setActive(false);
        employee2.saveSnapshot().setName("EmplB-Updated");

        Assertions.assertEquals(2L, emplDao.updateChanged(employee1, employee2));
        Assertions.assertEquals(employee1.toString(), emplDao.read(employee1.getId()));
        Assertions.assertEquals(employee2.toString(), emplDao.read(employee2.getId()));

        // Update 3: Verify that an entity without changes doesn't increment the update count
        employee1.saveSnapshot();
        Assertions.assertEquals(0L, emplDao.updateChanged(employee1));

        // Update 4: Change two columns:
        employee1.saveSnapshot();
        employee1.setActive(!employee1.isActive());
        employee1.setName("EmplX-Updated");
        Assertions.assertEquals(1L, emplDao.updateChanged(employee1));
    }

    /** Test error scenarios for partial updates */
    @Test
    void errorScenarios() {
        var cityDao = EntityManager.of(City.class, dbConnection, Long.class);
        var city = cityDao.insert(new City(null, "California", "US", 36.7783, -119.4179));

        var emplDao = EntityManager.of(Employee.class, dbConnection, Long.class);
        var employee = emplDao.insert(createEmployee("EmplA", city));

        // Error 1: Missing snapshot (expected IllegalStateException)
        employee.setName("EmplA-Updated");
        Assertions.assertThrows(IllegalStateException.class, () -> emplDao.updateChanged(employee));

        // Error 2: Null entity in varargs array (expected IllegalArgumentException)
        employee.saveSnapshot();
        var ex = Assertions.assertThrows(IllegalArgumentException.class, () ->
                emplDao.updateChanged(employee, null));
        Assertions.assertEquals("The entity at index 1 must not be null.", ex.getMessage());
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