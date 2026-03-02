package org.ujorm.mapper.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.mapper.demo.City;
import org.ujorm.mapper.demo.Employee;

import java.time.LocalDate;

class EntityManagerTest extends AbstractDaoTest {

    private final Class<Long> pkType = Long.class;

    @Test
    void crud() {
        var cityDao = EntityManager.of(City.class, dbConnection, pkType);
        var cityInp = new City(null, "California", "US", 36.7783, -119.4179);
        var cityOut = cityDao.insert(cityInp);
        Assertions.assertNotNull(cityOut.id());
        Assertions.assertEquals(1L, cityOut.id());
        Assertions.assertNotSame(cityInp, cityOut);

        // City including ID
        cityInp = new City(101L, "Ottawa", "CA", 45.4215, 75.6972);
        cityOut = cityDao.insert(cityInp);
        Assertions.assertNotNull(cityOut.id());
        Assertions.assertEquals(101L, cityOut.id());
        Assertions.assertSame(cityInp, cityOut);

        // Employee A
        var emplDao = EntityManager.of(Employee.class, dbConnection, pkType);
        var employeeInp = createEmployee("EmplA", cityInp);
        var employeeOut = emplDao.insert(employeeInp);
        Assertions.assertNotNull(employeeOut.getId());
        Assertions.assertEquals(1L, employeeOut.getId());
        Assertions.assertSame(employeeInp, employeeOut);

        // Employee B
        employeeInp = createEmployee(101L, "EmplB", cityOut);
        employeeOut = emplDao.insert(employeeInp);
        Assertions.assertNotNull(employeeOut.getId());
        Assertions.assertEquals(101L, employeeOut.getId());
        Assertions.assertSame(employeeInp, employeeOut);

        // Update-1
        employeeOut.setName("EmplC");
        long count = emplDao.update(employeeOut, "name");
        Assertions.assertEquals(1, count);

        // Update-2
        employeeOut.setName("EmplC");
        count = emplDao.update(employeeOut, "city", "name");
        Assertions.assertEquals(1, count);

        // Delete the last employee
        count = emplDao.delete(employeeOut);
        Assertions.assertEquals(1, count);
    }

    // @Test // TODO:pop
    void readRecord() {
        var cityDao = EntityManager.of(City.class, dbConnection, pkType);
        var cityInp = new City(null, "California", "US", 36.7783, -119.4179);
        var cityOut = cityDao.insert(cityInp);
        Assertions.assertNotNull(cityOut.id());
        Assertions.assertEquals(1L, cityOut.id());


        // Select Record:
        var cityReloaded = cityDao.read(cityOut.id());
        Assertions.assertNotNull(cityReloaded);
    }

    public Employee createEmployee(String name, City city) {
        return createEmployee(null, name, city);
    }

    public Employee createEmployee(Long id, String name, City city) {
        return Employee.of(id, name, null , city, LocalDate.of(2020,1,1), true);
    }
}