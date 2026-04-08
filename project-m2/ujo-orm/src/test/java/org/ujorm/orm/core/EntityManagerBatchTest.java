package org.ujorm.orm.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.orm.Config;
import org.ujorm.orm.demo.City;
import org.ujorm.orm.demo.Employee;
import org.ujorm.orm.utils.EntityContext;

import java.time.LocalDate;
import java.util.stream.Stream;

/**
 * Tests for batch operations (insert, delete) in the EntityManager.
 */
class EntityManagerBatchTest extends AbstractDaoTest {

    private final EntityContext ctx = EntityContext.ofDefault();
    private final Class<Long> idType = Long.class;

    @Test
    void testBatchInsertWithAndWithoutIds() {
        var cityDao = ctx.entityManager(City.class, idType).crud(dbConnection);

        // Mix of cities with and without predefined IDs
        var city1 = new City(null, "Prague", "CZ", 50.0755, 14.4378);
        var city2 = new City(1001L, "Brno", "CZ", 49.1951, 16.6068);
        var city3 = new City(null, "Ostrava", "CZ", 49.8209, 18.2625);
        var city4 = new City(1002L, "Plzen", "CZ", 49.7384, 13.3736);

        // Execute batch insert and fetch the updated instances (Returns the same array)
        var insertedCities = cityDao.insert(city1, city2, city3, city4);
        city1 = insertedCities[0];
        city2 = insertedCities[1];
        city3 = insertedCities[2];
        city4 = insertedCities[3];

        // Verify entities without IDs got them generated
        Assertions.assertNotNull(city1.id());
        Assertions.assertNotNull(city3.id());
        Assertions.assertTrue(city1.id() > 0);
        Assertions.assertTrue(city3.id() > 0);

        // Verify entities with predefined IDs kept them
        Assertions.assertEquals(1001L, city2.id());
        Assertions.assertEquals(1002L, city4.id());

        // Verify they are actually in the DB
        Assertions.assertTrue(cityDao.findById(city1.id()).isPresent());
        Assertions.assertTrue(cityDao.findById(city2.id()).isPresent());
    }

    @Test
    void testBatchInsertExceedingChunkLimit() {
        var cityDao = ctx.entityManager(City.class, idType).crud(dbConnection);

        // Create a large number of cities to force batch chunking
        int totalCities = 1200;
        var cities = new City[totalCities];

        for (int i = 0; i < totalCities; i++) {
            cities[i] = new City(null, "City-" + i, "XY", 0.0, 0.0);
        }

        // Execute huge batch insert (updates the array in-place)
        cities = cityDao.insert(cities);

        // Verify all entities received an ID
        for (int i = 0; i < totalCities; i++) {
            Assertions.assertNotNull(cities[i].id(), "City at index " + i + " should have an ID");
        }

        // Spot check a few from DB
        Assertions.assertTrue(cityDao.findById(cities[0].id()).isPresent());
        Assertions.assertTrue(cityDao.findById(cities[totalCities / 2].id()).isPresent());
        Assertions.assertTrue(cityDao.findById(cities[totalCities - 1].id()).isPresent());
    }

    @Test
    void testBatchDelete() {
        var cityDao = ctx.entityManager(City.class, idType).crud(dbConnection);
        var cityInp = new City(null, "California", "US", 36.7783, -119.4179);
        var city = cityDao.insert(cityInp);

        var emplDao = ctx.entityManager(Employee.class, idType).crud(dbConnection);

        // Prepare employees
        var emp1 = emplDao.insert(createEmployee("Emp-A", city));
        var emp2 = emplDao.insert(createEmployee("Emp-B", city));
        var emp3 = emplDao.insert(createEmployee("Emp-C", city));

        // Ensure they exist
        Assertions.assertTrue(emplDao.findById(emp1.getId()).isPresent());
        Assertions.assertTrue(emplDao.findById(emp2.getId()).isPresent());
        Assertions.assertTrue(emplDao.findById(emp3.getId()).isPresent());

        // Execute batch delete for two of them
        int deletedCount = emplDao.deleteBatchEntities(emp1, emp3);
        Assertions.assertEquals(2, deletedCount);

        // Verify DB state
        Assertions.assertFalse(emplDao.findById(emp1.getId()).isPresent());
        Assertions.assertTrue(emplDao.findById(emp2.getId()).isPresent());
        Assertions.assertFalse(emplDao.findById(emp3.getId()).isPresent());
    }

    @Test
    void testBatchDeleteExceedingChunkLimit() {
        var cityDao = ctx.entityManager(City.class, idType).crud(dbConnection);
        var city = cityDao.insert(new City(null, "California", "US", 36.7783, -119.4179));
        var emplDao = ctx.entityManager(Employee.class, idType).crud(dbConnection);

        // Create 1100 employees
        int totalEmployees = 1100;
        var employees = new Employee[totalEmployees];
        for (int i = 0; i < totalEmployees; i++) {
            employees[i] = createEmployee(null, "Emp-" + i, city);
        }

        // Insert them all
        employees = emplDao.insert(employees);

        // Now delete them all in a batch
        int deletedCount = emplDao.delete(Stream.of(employees));

        Assertions.assertEquals(totalEmployees, deletedCount);
        Assertions.assertFalse(emplDao.findById(employees[0].getId()).isPresent());
        Assertions.assertFalse(emplDao.findById(employees[totalEmployees - 1].getId()).isPresent());
    }

    @Test
    void testEmptyAndNullBatches() {
        var cityDao = ctx.entityManager(City.class, idType).crud(dbConnection);

        // Empty array
        var result1 = cityDao.insert(new City[0]);
        Assertions.assertEquals(0, result1.length);

        // Explicit nulls
        City nullCity = null;
        var result2 = cityDao.insert(nullCity, nullCity);
        Assertions.assertEquals(2, result2.length);
        Assertions.assertNull(result2[0]);

        // Empty delete
        int deleted = cityDao.delete(Stream.of(new City[0]));
        Assertions.assertEquals(0, deleted);
    }

    /** Tests batch delete operation using a stream that ends with a null value. */
    @Test
    void testBatchDeleteWithTrailingNull() {
        var cityDao = ctx.entityManager(City.class, idType).crud(dbConnection);
        var city1 = cityDao.insert(new City(null, "Prague", "CZ", 50.0755, 14.4378));
        var city2 = cityDao.insert(new City(null, "Brno", "CZ", 49.1951, 16.6068));

        // Create a stream that ends with a null element
        var stream = Stream.of(city1, city2, null);
        var deletedCount = cityDao.delete(stream);

        Assertions.assertEquals(2, deletedCount);
        Assertions.assertFalse(cityDao.findById(city1.id()).isPresent());
        Assertions.assertFalse(cityDao.findById(city2.id()).isPresent());
    }

    /** Create a new Employee without ID */
    public Employee createEmployee(String name, City city) {
        return createEmployee(null, name, city);
    }

    /** Create a new Employee with ID */
    public Employee createEmployee(Long id, String name, City city) {
        return Employee.of(id, name, null, city, LocalDate.of(2020, 1, 1), true);
    }

    /**
     * Tests processing of the remaining batch elements with a custom small batch limit.
     */
    @Test
    void testBatchRemainderFlushWithSmallLimit() {
        // 1. Get the default configuration instance
        var defaultConfig = Config.ofDefault();

        // 2. Create a partial mock (Spy) to override ONLY the batch size
        var customConfig = org.mockito.Mockito.spy(defaultConfig);
        org.mockito.Mockito.when(customConfig.getBatchSize()).thenReturn(3);

        // 3. Create a Context with this mocked config
        var ctx = EntityContext.of(customConfig);


        // 4. Initialize managers for both City and Employee
        var cityDao = ctx.crud(City.class, idType, dbConnection);
        var employeeDao = ctx.crud(Employee.class, idType, dbConnection);

        // --- Execution ---

        // Insert a valid City first to satisfy the foreign key constraint
        var city = cityDao.insert(new City(null, "Prague", "CZ", 50.0755, 14.4378));

        // Create 8 items (with a limit of 3, this creates chunks: 3, 3, and a remainder of 2)
        var totalItems = 8;
        var employees = new Employee[totalItems];
        for (var i = 0; i < totalItems; i++) {
            // Use the full builder to pass the newly created, valid city
            employees[i] = Employee.of(null, "Emp-Batch-" + i, null, city, java.time.LocalDate.now(), true);
        }

        // Test Insert: Verify that elements from the remainder chunk received an ID
        employeeDao.insert(employees);
        Assertions.assertNotNull(employees[totalItems - 1].getId(), "The last element of the remainder chunk did not receive an ID.");

        // Test Update: Verify that the number of updated rows matches exactly
        var updateStream = java.util.Arrays.stream(employees).peek(e -> e.setName(e.getName() + "-Updated"));
        var updatedCount = employeeDao.update(updateStream, "name");
        Assertions.assertEquals(totalItems, updatedCount);

        // Test Delete: Verify that the number of deleted rows matches exactly
        var deletedCount = employeeDao.delete(java.util.Arrays.stream(employees));
        Assertions.assertEquals(totalItems, deletedCount);
    }
}