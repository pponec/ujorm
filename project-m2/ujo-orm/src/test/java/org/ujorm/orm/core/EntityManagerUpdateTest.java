package org.ujorm.orm.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.orm.demo.City;
import org.ujorm.orm.demo.Employee;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

/** Tests for partial updates using SnapshotProvider */
class EntityManagerUpdateTest extends AbstractDaoTest {

    /** Test partial updates of one or multiple entities */
    @Test
    void partialUpdate() {
        var cityDao = EntityManager.of(City.class, Long.class).crud(dbConnection);
        var city = cityDao.insert(new City(null, "California", "US", 36.7783, -119.4179));

        var emplDao = EntityManager.of(Employee.class, Long.class).crud(dbConnection);
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
        var emp1 = emplDao.findByIdNullable(employee1.getId());
        Assertions.assertEquals(employee1.getId(), emp1.getId());
        Assertions.assertEquals(employee1.getName(), emp1.getName());
        Assertions.assertEquals(employee1.getContractDay(), emp1.getContractDay());
        Assertions.assertEquals(employee1.getCity().id(), emp1.getCity().id());
        Assertions.assertNull(emp1.getSuperior());

        // Update 3: Verify that an entity without changes doesn't increment the update count
        employee1.saveSnapshot();
        Assertions.assertEquals(0L, emplDao.updateChanged(employee1));

        // Update 4: Change two columns:
        employee1.saveSnapshot();
        employee1.setActive(!employee1.isActive());
        employee1.setName("EmplX-Updated");
        Assertions.assertEquals(1L, emplDao.updateChanged(employee1));
    }

    /** Test batch update for specific properties */
    @Test
    void updateBatchWithProperties() {
        var cityDao = EntityManager.of(City.class,  Long.class).crud(dbConnection);
        var city = cityDao.insert(new City(null, "California", "US", 36.7783, -119.4179));

        var emplDao = EntityManager.of(Employee.class, Long.class).crud(dbConnection);
        var employee1 = emplDao.insert(createEmployee("EmplA", city));
        var employee2 = emplDao.insert(createEmployee(101L, "EmplB", city));

        // Modify entities in memory
        employee1.setName("EmplA-BatchUpdate");
        employee1.setActive(false);
        employee1.setContractDay(LocalDate.of(2025, 1, 1)); // This should NOT be updated

        employee2.setName("EmplB-BatchUpdate");
        employee2.setActive(false);

        var list = List.of(employee1, employee2);

        // Update only "name" and "active" properties
        var result = emplDao.update(list.stream(), "name", "active");
        Assertions.assertEquals(2L, result);

        // Verify the database state
        var dbEmp1 = emplDao.findById(employee1.getId()).orElseThrow();
        Assertions.assertEquals("EmplA-BatchUpdate", dbEmp1.getName());
        Assertions.assertFalse(dbEmp1.isActive());
        Assertions.assertEquals(LocalDate.of(2020, 1, 1), dbEmp1.getContractDay()); // Remained original

        var dbEmp2 = emplDao.findById(employee2.getId()).orElseThrow();
        Assertions.assertEquals("EmplB-BatchUpdate", dbEmp2.getName());
        Assertions.assertFalse(dbEmp2.isActive());
    }

    /** Test batch update for all properties */
    @Test
    void updateBatchAllProperties() {
        var cityDao = EntityManager.of(City.class, Long.class).crud(dbConnection);
        var city = cityDao.insert(new City(null, "Texas", "US", 31.9686, -99.9018));

        var emplDao = EntityManager.of(Employee.class, Long.class).crud(dbConnection);
        var employee = emplDao.insert(createEmployee("EmplC", city));

        employee.setName("EmplC-FullUpdate");
        employee.setActive(false);
        employee.setContractDay(LocalDate.of(2025, 1, 1));

        // Update all columns (empty properties)
        var result = emplDao.update(Stream.of(employee));
        Assertions.assertEquals(1L, result);

        var dbEmp = emplDao.findById(employee.getId()).orElseThrow();
        Assertions.assertEquals("EmplC-FullUpdate", dbEmp.getName());
        Assertions.assertFalse(dbEmp.isActive());
        Assertions.assertEquals(LocalDate.of(2025, 1, 1), dbEmp.getContractDay());
    }

    /** Test batch update with an empty list */
    @Test
    void updateBatchEmpty() {
        var emplDao = EntityManager.of(Employee.class, Long.class).crud(dbConnection);
        var emptyList = java.util.Collections.<Employee>emptyList();

        var result = emplDao.update(emptyList.stream(), "name");
        Assertions.assertEquals(0L, result);
    }

    /** Test error scenarios for partial updates */
    @Test
    void errorScenarios() {
        var cityDao = EntityManager.of(City.class, Long.class).crud(dbConnection);
        var city = cityDao.insert(new City(null, "California", "US", 36.7783, -119.4179));

        var emplDao = EntityManager.of(Employee.class, Long.class).crud(dbConnection);
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