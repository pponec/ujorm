package org.ujorm.core.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.Key;


class EmployeeUjoTest {

    @Test
    void readWrite() {
        var employee = new EmployeeUjo();
        var keyId = EmployeeUjo.keyId;
        employee.setValue(EmployeeUjo.keyId, 1L);
        var id = employee.getValue(EmployeeUjo.keyId);

        Assertions.assertEquals("id", keyId.name());
        Assertions.assertEquals("id", keyId.toString());
        Assertions.assertEquals(Long.class, keyId.type());
        Assertions.assertEquals(Employee.class, keyId.domainClass());
        Assertions.assertEquals(1L, id);
        Assertions.assertEquals(1L, employee.array()[0]);
    }

    @Test
    void joinKeys() {
        Key<Employee, City> user_city = EmployeeUjo.keyCity;
        Key<Employee, String> user_name = EmployeeUjo.keyName;
        Key<Employee, Employee> user_boss = EmployeeUjo.keyBoss;
        Key<City, String> city_country = CityUjo.keyCountryCode;
        Key<City, Double> city_latitude = CityUjo.keyLatitude;

        Assertions.assertEquals("name", user_name.name());
        Assertions.assertEquals("boss.name", user_boss.join(user_name));
        Assertions.assertEquals("boss.city", user_boss.join(user_city));
        Assertions.assertEquals("boss.city.countryCode", user_boss.join(user_city, city_country));
        Assertions.assertEquals("countryCode", city_country.toString());
        Assertions.assertEquals("latitude", city_latitude.toString());
        // Non type safe method:
        Assertions.assertEquals("boss.boss.boss.name.latitude", user_boss.join(user_boss, user_boss, user_name, city_latitude));

        // Compilation error is expected (!)
        /*
        Assertions.assertEquals("boss.latitude", user_boss.join(city_latitude));
        Assertions.assertEquals("country.name", city_country.join(user_name));
        */


    }

}