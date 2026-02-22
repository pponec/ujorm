package org.ujorm.mapper.core;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.mapper.demo.City;
import org.ujorm.mapper.demo.Employee;

import java.sql.SQLException;
import java.time.LocalDate;

class CrudManagerTest extends AbstractDaoTest {

    @Test
    void crud() {
        var cityDao = new CrudManager<City, Long>(City.class, dbConnection);
        var city1 = new City(null, "California", "US", 36.7783, -119.4179);
        var city2 = cityDao.insert(city1);
        Assertions.assertNotNull(city2.id());
        Assertions.assertEquals(1L, city2.id());

        // City including ID
        city1 = new City(100L, "Ottawa", "CA", 45.4215, 75.6972);
        city2 = cityDao.insert(city1);
        Assertions.assertNotNull(city2.id());
        Assertions.assertEquals(100L, city2.id());

        // Employee A
        var emplDao = new CrudManager<Employee, Long>(Employee.class, dbConnection);
        var employee = createEmployee("TestA", city1);
        var employee2 = emplDao.insert(employee);
        Assertions.assertNotNull(employee2.getId());
        Assertions.assertEquals(1L, city2.id());

        // Employee B
        employee = createEmployee("TestB", city2);
        employee2 = emplDao.insert(employee);
        Assertions.assertNotNull(employee2.getId());
        Assertions.assertEquals(1L, employee2.getId());
    }

    public Employee createEmployee(String name, City city) {
        return new Employee(null, name, null , city, LocalDate.of(2020,1,1), true);
    }
}