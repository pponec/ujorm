package org.ujorm.core.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.core.Key;


class EmployeeUjoTest {

    @Test
    void readWrite() {
        var employee = new EmployeeUjo();
        var keyId = EmployeeUjo.keyId;
        employee.setValue(EmployeeUjo.keyId, 1L);
        var id = employee.getValue(EmployeeUjo.keyId);

        Assertions.assertEquals("id", keyId.getName());
        Assertions.assertEquals("id", keyId.toString());
        Assertions.assertEquals(Long.class, keyId.getType());
        Assertions.assertEquals(Employee.class, keyId.getDomainClass());
        Assertions.assertEquals(1L, id);
        Assertions.assertEquals(1L, employee.array()[0]);
    }

    @Test
    void joinKeys() {
        Key<Employee, City> userCity = EmployeeUjo.keyCity;
        Key<Employee, String> userName = EmployeeUjo.keyName;
        Key<Employee, Employee> userBoss = EmployeeUjo.keySuperior;
        Key<City, String> cityCountry = CityUjo.keyCountryCode;
        Key<City, Double> cityLatitude = CityUjo.keyLatitude;

        Assertions.assertEquals("name", userName.getName());
        Assertions.assertEquals("superior.name", userBoss.join(userName));
        Assertions.assertEquals("superior.city", userBoss.join(userCity));
        Assertions.assertEquals("superior.city.countryCode", userBoss.join(userCity, cityCountry));
        Assertions.assertEquals("countryCode", cityCountry.toString());
        Assertions.assertEquals("latitude", cityLatitude.toString());
        // Non type safe method:
        Assertions.assertEquals("superior.superior.superior.name.latitude", userBoss.join(userBoss, userBoss, userName, cityLatitude));

        /* Wrong compilation!
        Assertions.assertEquals("superior.latitude", userBoss.join(cityLatitude));
        Assertions.assertEquals("country.name", cityCountry.join(userName));
        */
    }

}