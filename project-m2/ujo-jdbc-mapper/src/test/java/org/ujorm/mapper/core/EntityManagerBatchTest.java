package org.ujorm.mapper.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.mapper.demo.City;
import org.ujorm.mapper.demo.Employee;

import java.time.LocalDate;


/**
 * Tests for batch operations (insert, delete) in the EntityManager.
 */
class EntityManagerBatchTest extends AbstractDaoTest {

    private final Class<Long> pkType = Long.class;

    @Test
    void testBatchInsertWithAndWithoutIds() {
        var cityDao = EntityManager.of(City.class, pkType).crud(dbConnection);

        // Mix of cities with and without predefined IDs
        var city1 = new City(null, "Prague", "CZ", 50.0755, 14.4378);
        var city2 = new City(1001L, "Brno", "CZ", 49.1951, 16.6068);
        var city3 = new City(null, "Ostrava", "CZ", 49.8209, 18.2625);
        var city4 = new City(1002L, "Plzen", "CZ", 49.7384, 13.3736);

        // Execute batch insert
        cityDao.insertBatch(city1, city2, city3, city4);

        // Verify entities without IDs got them generated
        Assertions.assertNotNull(city1.id());
        Assertions.assertNotNull(city3.id());
        Assertions.assertTrue(city1.id() > 0);
        Assertions.assertTrue(city3.id() > 0);

        // Verify entities with predefined IDs kept them
        Assertions.assertEquals(1001L, city2.id());
        Assertions.assertEquals(1002L, city4.id());

        // Verify they are actually in the DB
        Assertions.assertTrue(cityDao.read(city1.id()).isPresent());
        Assertions.assertTrue(cityDao.read(city2.id()).isPresent());
    }

    @Test
    void testBatchInsertExceedingChunkLimit() {
        var cityDao = EntityManager.of(City.class, pkType).crud(dbConnection);

        // Create a large number of cities to force batch chunking
        // Assuming default chunk limit is 500, we create 1200 items.
        int totalCities = 1200;
        var cities = new City[totalCities];

        for (int i = 0; i < totalCities; i++) {
            cities[i] = new City(null, "City-" + i, "XY", 0.0, 0.0);
        }

        // Execute huge batch insert
        cityDao.insertBatch(cities);

        // Verify all entities received an ID
        for (int i = 0; i < totalCities; i++) {
            Assertions.assertNotNull(cities[i].id(), "City at index " + i + " should have an ID");
        }

        // Spot check a few from DB
        Assertions.assertTrue(cityDao.read(cities[0].id()).isPresent());
        Assertions.assertTrue(cityDao.read(cities[totalCities / 2].id()).isPresent());
        Assertions.assertTrue(cityDao.read(cities[totalCities - 1].id()).isPresent());
    }

    @Test
    void testBatchDelete() {
        var cityDao = EntityManager.of(City.class, pkType).crud(dbConnection);
        var cityInp = new City(null, "California", "US", 36.7783, -119.4179);
        var city = cityDao.insert(cityInp);

        var emplDao = EntityManager.of(Employee.class, pkType).crud(dbConnection);

        // Prepare employees
        var emp1 = emplDao.insert(createEmployee("Emp-A", city));
        var emp2 = emplDao.insert(createEmployee("Emp-B", city));
        var emp3 = emplDao.insert(createEmployee("Emp-C", city));

        // Ensure they exist
        Assertions.assertTrue(emplDao.read(emp1.getId()).isPresent());
        Assertions.assertTrue(emplDao.read(emp2.getId()).isPresent());
        Assertions.assertTrue(emplDao.read(emp3.getId()).isPresent());

        // Execute batch delete for two of them
        int deletedCount = emplDao.deleteBatch(emp1, emp3);
        Assertions.assertEquals(2, deletedCount);

        // Verify DB state
        Assertions.assertFalse(emplDao.read(emp1.getId()).isPresent());
        Assertions.assertTrue(emplDao.read(emp2.getId()).isPresent()); // Emp2 should still exist
        Assertions.assertFalse(emplDao.read(emp3.getId()).isPresent());
    }

    @Test
    void testBatchDeleteExceedingChunkLimit() {
        var cityDao = EntityManager.of(City.class, pkType).crud(dbConnection);
        var city = cityDao.insert(new City(null, "California", "US", 36.7783, -119.4179));
        var emplDao = EntityManager.of(Employee.class, pkType).crud(dbConnection);

        // Create 1100 employees
        int totalEmployees = 1100;
        var employees = new Employee[totalEmployees];
        for (int i = 0; i < totalEmployees; i++) {
            employees[i] = createEmployee(null, "Emp-" + i, city);
        }

        // Insert them all
        emplDao.insertBatch(employees);

        // Now delete them all in a batch
        int deletedCount = emplDao.deleteBatch(employees);

        Assertions.assertEquals(totalEmployees, deletedCount);
        Assertions.assertFalse(emplDao.read(employees[0].getId()).isPresent());
        Assertions.assertFalse(emplDao.read(employees[totalEmployees - 1].getId()).isPresent());
    }

    @Test
    void testEmptyAndNullBatches() {
        var cityDao = EntityManager.of(City.class, pkType).crud(dbConnection);

        // Empty array
        cityDao.insertBatch(new City[0]);

        // Array with nulls
        City nullCity = null;
        cityDao.insert(nullCity);

        // Empty delete
        int deleted = cityDao.deleteBatch(new City[0]);
        Assertions.assertEquals(0, deleted);
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