package org.ujorm.mapper.generated;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.core.Key;
import org.ujorm.mapper.generated.demo.City;
import org.ujorm.mapper.generated.demo.Employee;
import java.time.LocalDate;

class Employee_Test {

    @Test
    void managementTest() {
        final var instance = new Employee_();
        Assertions.assertEquals(Employee.class, instance.getDomainType());
        Assertions.assertEquals(6, instance.count());

        var keyId = instance.getKey("id", Long.class);
        var keyName = instance.getKey("name", String.class);
        var keyActive = instance.getKey("active", boolean.class);
        var keyCity = instance.getKey("city", City.class);
        var employee = instance.newDomain();

        keyId.setValue(employee, 10L);
        keyName.setValue(employee, "Joe");
        keyCity.setValue(employee, City.of(9L, "California"));
        keyActive.setValue(employee, true);

        Assertions.assertEquals(10, employee.getId());
        Assertions.assertEquals("Joe", employee.getName());
        Assertions.assertEquals(true, employee.isActive());
        Assertions.assertEquals("California", employee.getCity().name());

        Assertions.assertEquals(10, keyId.getValue(employee));
        Assertions.assertEquals("Joe", keyName.getValue(employee));
        Assertions.assertEquals(true, keyActive.getValue(employee));
        Assertions.assertEquals("California", keyCity.getValue(employee).name());

        Assertions.assertNull(keyCity.getDefaultValue());
        Assertions.assertFalse(keyCity.isDefault(employee));
        Assertions.assertFalse(keyActive.getDefaultValue());
        Assertions.assertFalse(keyActive.isDefault(employee));

    }

    /** Create new instance */
    @Test
    void testNewInstance_basic() {
        var e = new ElementUjo();
        e.setValue(e.keyId, 10L);
        e.setValue(e.keyName, "Test");
        e.setValue(e.keySuperior, null);
        e.setValue(e.keyCity, City.of(1L, "California"));
        e.setValue(e.keyContractDay, LocalDate.of(2026, 02, 17));
        e.setValue(e.keyActive, true);
        var employee = e.newInstance();

        Assertions.assertEquals(10, employee.getId());
        Assertions.assertEquals("Test", employee.getName());
        Assertions.assertEquals(null, employee.getSuperior());
        Assertions.assertEquals("California", employee.getCity().name());
        Assertions.assertEquals(2026, employee.getContractDay().getYear());
        Assertions.assertEquals(true, employee.isActive());
    }

    /** Create new instance */
    @Test
    void testNewInstance_null() {
        var e = new ElementUjo();
        var employee = e.newInstance();

        Assertions.assertNull(employee.getId());
        Assertions.assertNull(employee.getName());
        Assertions.assertNull(employee.getCity());
        Assertions.assertNull(employee.getSuperior());
        Assertions.assertNull(employee.getContractDay());
        Assertions.assertEquals(false, employee.isActive());
    }

    public class ElementUjo {
        final Employee_ meta = new Employee_();
        final Object[] array = new Object[meta.count()];
        final Key<Employee, Long> keyId = meta.getKey("id", Long.class);
        final Key<Employee, String> keyName = meta.getKey("name", String.class);
        final Key<Employee, Employee> keySuperior = meta.getKey("superior", Employee.class);
        final Key<Employee, City> keyCity = meta.getKey("city", City.class);
        final Key<Employee, LocalDate> keyContractDay = meta.getKey("contractDay", LocalDate.class);
        final Key<Employee, Boolean> keyActive = meta.getKey("active", boolean.class);

        public <V> void setValue(Key<Employee,V> key, V value) {
            array[key.getIndex()] = value;
        }

        public Employee newInstance() {
            return meta.newDomain(array);
        }
    }
}