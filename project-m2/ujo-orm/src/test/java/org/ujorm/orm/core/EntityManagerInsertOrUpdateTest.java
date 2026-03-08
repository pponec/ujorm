/*
 * Copyright 2026-2026 Pavel Ponec
 */
package org.ujorm.orm.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.orm.demo.City;
import org.ujorm.orm.demo.Employee;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.stream.Stream;

/** Tests for stream-based batch inserts and insertOrUpdate operations */
class EntityManagerInsertOrUpdateTest extends AbstractDaoTest {

    // --- TESTS FOR insertBatch (Stream) ---

    @Test
    void insertBatch_Stream() {
        var cityDao = EntityManager.of(City.class, Long.class).crud(dbConnection);
        var city = cityDao.insert(new City(null, "California", "US", 36.7783, -119.4179));
        var emplDao = EntityManager.of(Employee.class, Long.class).crud(dbConnection);

        var newEmployees = Stream.of(
                createEmployee("EmplA", city),
                createEmployee("EmplB", city),
                createEmployee("EmplC", city)
        );

        var insertedList = new ArrayList<Employee>();
        var affectedRows = emplDao.insertBatch(newEmployees, insertedList::add);

        Assertions.assertEquals(3L, affectedRows, "Měly by být vloženy 3 záznamy");
        Assertions.assertEquals(3, insertedList.size(), "Consumer by měl zachytit 3 objekty");

        // Ověření, že Consumer obdržel objekty s vygenerovanými ID
        for (var emp : insertedList) {
            Assertions.assertNotNull(emp.getId(), "Vložená entita musí mít ID");
            var dbEmp = emplDao.findById(emp.getId()).orElseThrow();
            Assertions.assertEquals(emp.getName(), dbEmp.getName());
        }
    }

    // --- TESTS FOR insertOrUpdate (Stream) ---

    @Test
    void insertOrUpdate_AllNew_GoesToInsert() {
        var cityDao = EntityManager.of(City.class, Long.class).crud(dbConnection);
        var city = cityDao.insert(new City(null, "Texas", "US", 31.9686, -99.9018));
        var emplDao = EntityManager.of(Employee.class, Long.class).crud(dbConnection);

        var newEmployees = Stream.of(
                createEmployee("NewEmpl1", city),
                createEmployee("NewEmpl2", city)
        );

        var insertedList = new ArrayList<Employee>();
        var affectedRows = emplDao.insertOrUpdate(newEmployees, insertedList::add);

        Assertions.assertEquals(2L, affectedRows);
        Assertions.assertEquals(2, insertedList.size(), "Všechny záznamy měly projít přes INSERT Consumer");
        Assertions.assertNotNull(insertedList.get(0).getId());
    }

    @Test
    void insertOrUpdate_AllExisting_GoesToUpdate() {
        var cityDao = EntityManager.of(City.class, Long.class).crud(dbConnection);
        var city = cityDao.insert(new City(null, "Nevada", "US", 38.8026, -116.4194));
        var emplDao = EntityManager.of(Employee.class, Long.class).crud(dbConnection);

        // Nejprve záznamy reálně vložíme, abychom získali ID
        var emp1 = emplDao.insert(createEmployee("OldEmpl1", city));
        var emp2 = emplDao.insert(createEmployee("OldEmpl2", city));

        // Změníme data v paměti
        emp1.setName("OldEmpl1-Updated");
        emp2.setName("OldEmpl2-Updated");

        var insertedList = new ArrayList<Employee>();
        var affectedRows = emplDao.insertOrUpdate(Stream.of(emp1, emp2), insertedList::add);

        Assertions.assertEquals(2L, affectedRows, "Měly se updatovat 2 řádky");
        Assertions.assertTrue(insertedList.isEmpty(), "Při UPDATE se Consumer nevolá, nevznikla nová ID");

        // Verifikace v DB
        Assertions.assertEquals("OldEmpl1-Updated", emplDao.findById(emp1.getId()).orElseThrow().getName());
        Assertions.assertEquals("OldEmpl2-Updated", emplDao.findById(emp2.getId()).orElseThrow().getName());
    }

    @Test
    void insertOrUpdate_Mixed_CorrectlyRoutes() {
        var cityDao = EntityManager.of(City.class, Long.class).crud(dbConnection);
        var city = cityDao.insert(new City(null, "Utah", "US", 39.3210, -111.0937));
        var emplDao = EntityManager.of(Employee.class, Long.class).crud(dbConnection);

        // 1 existující záznam (k updatu)
        var existingEmp = emplDao.insert(createEmployee("Mixed-Old", city));
        existingEmp.setName("Mixed-Old-Updated");

        // 2 nové záznamy (k insertu)
        var newEmp1 = createEmployee("Mixed-New1", city);
        var newEmp2 = createEmployee("Mixed-New2", city);

        var insertedList = new ArrayList<Employee>();
        var affectedRows = emplDao.insertOrUpdate(Stream.of(newEmp1, existingEmp, newEmp2), insertedList::add);

        Assertions.assertEquals(3L, affectedRows, "Celkem musí být ovlivněny 3 řádky");
        Assertions.assertEquals(2, insertedList.size(), "Pouze pro 2 nové záznamy se měl zavolat Consumer");

        // Verifikace v DB
        Assertions.assertEquals("Mixed-Old-Updated", emplDao.findById(existingEmp.getId()).orElseThrow().getName());
        Assertions.assertEquals("Mixed-New1", emplDao.findById(insertedList.get(0).getId()).orElseThrow().getName());
    }

    @Test
    void insertOrUpdate_WithSpecificProperties() {
        var cityDao = EntityManager.of(City.class, Long.class).crud(dbConnection);
        var city = cityDao.insert(new City(null, "Oregon", "US", 43.8041, -120.5542));
        var emplDao = EntityManager.of(Employee.class, Long.class).crud(dbConnection);

        var existingEmp = emplDao.insert(createEmployee("PropEmpl", city));

        // Změníme dvě vlastnosti, ale updatovat budeme jen jednu
        existingEmp.setName("PropEmpl-Updated");
        existingEmp.setActive(false);

        // Voláme s filtrem na property "name"
        var affectedRows = emplDao.insertOrUpdate(Stream.of(existingEmp), null, "name");

        Assertions.assertEquals(1L, affectedRows);

        var dbEmp = emplDao.findById(existingEmp.getId()).orElseThrow();
        Assertions.assertEquals("PropEmpl-Updated", dbEmp.getName(), "Jméno mělo být aktualizováno");
        Assertions.assertTrue(dbEmp.isActive(), "Příznak active se neměl změnit, nebyl v properties");
    }

    // --- HELPER METHODS ---

    /** Create a new Employee without ID */
    public Employee createEmployee(String name, City city) {
        return createEmployee(null, name, city);
    }

    /** Create a new Employee with ID */
    public Employee createEmployee(Long id, String name, City city) {
        return Employee.of(id, name, null, city, LocalDate.of(2020, 1, 1), true);
    }
}