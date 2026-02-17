package org.ujorm.mapper.generated;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.ujorm.core.Key;
import org.ujorm.mapper.demo.City;
import org.ujorm.mapper.demo.Employee;
import java.time.LocalDate;

class Employee_Test {

    @Test
    void managementTest() {
        final var instance = new Employee_();
        Assertions.assertEquals(Employee.class, instance.getDomainType());
        Assertions.assertEquals(6, instance.count());

        var employee = instance.newDomain();
        EmplyeeUjo.keyId.setValue(employee, 10L);
        EmplyeeUjo.keyName.setValue(employee, "Joe");
        EmplyeeUjo.keyCity.setValue(employee, City.of(9L, "California"));
        EmplyeeUjo.keyActive.setValue(employee, true);

        Assertions.assertEquals(10, employee.getId());
        Assertions.assertEquals("Joe", employee.getName());
        Assertions.assertEquals(true, employee.isActive());
        Assertions.assertEquals("California", employee.getCity().name());

        Assertions.assertEquals(10, EmplyeeUjo.keyId.getValue(employee));
        Assertions.assertEquals("Joe", EmplyeeUjo.keyName.getValue(employee));
        Assertions.assertEquals(true, EmplyeeUjo.keyActive.getValue(employee));
        Assertions.assertEquals("California", EmplyeeUjo.keyCity.getValue(employee).name());

        Assertions.assertNull(EmplyeeUjo.keyCity.getDefaultValue());
        Assertions.assertFalse(EmplyeeUjo.keyCity.isDefault(employee));
        Assertions.assertFalse(EmplyeeUjo.keyActive.getDefaultValue());
        Assertions.assertFalse(EmplyeeUjo.keyActive.isDefault(employee));

    }

    /** Create new instance */
    @Test
    void testNewInstance_basic() {
        var e = new EmplyeeUjo();
        e.setValue(EmplyeeUjo.keyId, 10L);
        e.setValue(EmplyeeUjo.keyName, "Test");
        e.setValue(EmplyeeUjo.keySuperior, null);
        e.setValue(EmplyeeUjo.keyCity, City.of(1L, "California"));
        e.setValue(EmplyeeUjo.keyContractDay, LocalDate.of(2026, 02, 17));
        e.setValue(EmplyeeUjo.keyActive, true);
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
        var e = new EmplyeeUjo();
        var employee = e.newInstance();

        Assertions.assertNull(employee.getId());
        Assertions.assertNull(employee.getName());
        Assertions.assertNull(employee.getCity());
        Assertions.assertNull(employee.getSuperior());
        Assertions.assertNull(employee.getContractDay());
        Assertions.assertEquals(false, employee.isActive());
    }

    public class EmplyeeUjo {
        static final Employee_ meta = new Employee_();
        static final Key<Employee, Long> keyId = meta.getKey("id", Long.class);
        static final Key<Employee, String> keyName = meta.getKey("name", String.class);
        static final Key<Employee, Employee> keySuperior = meta.getKey("superior", Employee.class);
        static final Key<Employee, City> keyCity = meta.getKey("city", City.class);
        static final Key<Employee, LocalDate> keyContractDay = meta.getKey("contractDay", LocalDate.class);
        static final Key<Employee, Boolean> keyActive = meta.getKey("active", boolean.class);
        final Object[] array = new Object[meta.count()];

        public <V> void setValue(Key<Employee,V> key, V value) {
            array[key.getIndex()] = value;
        }

        public Employee newInstance() {
            return meta.newDomain(array);
        }
    }
}