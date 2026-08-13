package org.ujorm.core.impl;

import org.junit.jupiter.api.Test;
import org.ujorm.core.Key;
import org.ujorm.core.demo.City;
import org.ujorm.core.demo.Employee;
import org.ujorm.core.demo.generated.City_;
import org.ujorm.core.demo.generated.Employee_;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A hand-written key inherits the default of the {@link AbstractKey#writable()} method,
 * so the default must be safe for a record, where no component has a setter.
 */
class AbstractKeyWritableTest {

    @Test
    @SuppressWarnings("unchecked")
    void handWrittenRecordKeyIsNotWritable() {
        var handler = new City_();

        for (var rawKey : handler.getKeyList()) {
            var key = (Key<City, Object>) rawKey;
            assertFalse(key.info().writable(), "A record component is not writable: " + key.name());
            assertThrows(UnsupportedOperationException.class,
                    () -> key.setValue(City.of(1L, "Prague"), null),
                    "The setter of the key is unsupported: " + key.name());
        }
        assertFalse(handler.getKeyList().isEmpty(), "The handler has keys");
    }

    /**
     * The same default answers true for a bean, where a hand-written key implements the setter.
     * This is the safety net of the EntityManager contract, so the answer must match the reality
     * of the key - see the warning in the JavaDoc of the {@link org.ujorm.core.KeyInfo#writable()}
     * method about a bean key raising the {@code unsupportedSetter()} exception.
     */
    @Test
    @SuppressWarnings("unchecked")
    void handWrittenBeanKeyIsWritable() {
        var handler = new Employee_();

        for (var rawKey : handler.getKeyList()) {
            var key = (Key<Employee, Object>) rawKey;
            assertTrue(key.info().writable(), "A bean property is writable: " + key.name());
        }
        assertFalse(handler.getKeyList().isEmpty(), "The handler has keys");

        var nameKey = (Key<Employee, Object>) (Key<?, ?>) handler.getKey("name");
        var employee = new Employee();
        nameKey.setValue(employee, "Ann");
        assertEquals("Ann", employee.getName(), "The writable key really assigns the value");
    }
}
