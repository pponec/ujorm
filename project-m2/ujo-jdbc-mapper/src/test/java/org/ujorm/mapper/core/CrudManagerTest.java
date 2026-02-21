package org.ujorm.mapper.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.mapper.demo.City;
import org.ujorm.mapper.demo.Employee;

class CrudManagerTest extends AbstractDaoTest {

    @Test
    void crud() {
        var cityDao = new CrudManager<City, Long>(City.class, dbConnection);
        var emplDao = new CrudManager<Employee, Long>(Employee.class, dbConnection);

        var city = new City(null, "California", "US", 36.7783, -119.4179);
        var city2 = cityDao.insert(city);
        Assertions.assertNotNull(city2.id());


//            var employee = new Employee();
//            {
//                employee.setId(null);
//                // ....
//            }
//            var employee2 = emplDao.insert(employee);
//            Assertions.assertNotNull(employee2.getId());

    }
}