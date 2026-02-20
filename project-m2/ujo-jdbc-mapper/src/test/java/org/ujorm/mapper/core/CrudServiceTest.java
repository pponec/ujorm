package org.ujorm.mapper.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.mapper.demo.City;
import org.ujorm.mapper.demo.Employee;
import org.ujorm.mapper.impl.Context;
import org.ujorm.tools.Assert;

import static org.junit.jupiter.api.Assertions.*;

class CrudServiceTest extends AbstractDaoTest {

    @Test
    void insert() {
        try (var builder =  sqlBuilder()) {
            var cityDao = new CrudService<City, Long>(City.class, builder, Context.ofDefault());
            var emplDao = new CrudService<Employee, Long>(Employee.class, builder, Context.ofDefault());

            var city = new City(null, "California", "US",  36.7783, -119.4179);
            var city2 = cityDao.insert(city);
            Assertions.assertNotNull(city.id());


            var employee = new Employee();
            {
                employee.setId(null);
                // ....
            }
            var employee2 = emplDao.insert(employee);
            Assertions.assertNotNull(employee2.getId());
        }


    }
}