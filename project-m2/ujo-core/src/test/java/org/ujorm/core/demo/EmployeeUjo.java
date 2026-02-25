package org.ujorm.core.demo;

import org.ujorm.core.DomainHandlerProvider;
import org.ujorm.core.DomainHandler;
import org.ujorm.core.Key;
import org.ujorm.core.Ujo;
import org.ujorm.core.demo.generated.Employee_;

import java.time.LocalDate;

/**
 * Example demonstration of a type-safe attribute container based on the Object array.
 *
 * <p>This implementation guarantees <b>type-safe write and read operations</b> at compile time.
 * By using {@link Key} as a descriptor, it ensures that the type of the value being stored
 * strictly matches the type of the value being retrieved, eliminating the need for
 * manual casting or runtime type checks.</p>
 *
 * <p>Unlike a standard {@code Map<String, Object>}, the association between a key and
 * its value type is statically defined and enforced.</p>
 *
 * @see Key
 */
public class EmployeeUjo implements Ujo<Employee> {
    /** For implementation see the {@link Employee_} class. */
    public static final DomainHandler<Employee> meta = DomainHandlerProvider.getHandler(Employee.class);
    public static final Key<Employee, Long> keyId = meta.getKey("id", Long.class);
    public static final Key<Employee, String> keyName = meta.getKey("name", String.class);
    public static final Key<Employee, Employee> keySuperior = meta.getKey("superior", Employee.class);
    public static final Key<Employee, City> keyCity = meta.getKey("city", City.class);
    public static final Key<Employee, LocalDate> keyContractDay = meta.getKey("contractDay", LocalDate.class);
    public static final Key<Employee, Boolean> keyActive = meta.getKey("active", Boolean.class);
    private final Object[] array = new Object[meta.count()];

    /** Access by a key index */
    @Override
    public <V> void setValue(Key<Employee,V> key, V value) {
        array[key.getIndex()] = value;
    }

    /** Access by a key index */
    @Override
    public <V> V getValue(Key<Employee,V> key) {
        return (V) array[key.getIndex()];
    }

    @Override
    public DomainHandler<Employee> domainHandler() {
        return meta;
    }

    /** Create a new domain object for the current values */
    public Employee newDomain() {
        return meta.newDomain(array);
    }

    /** Clone internal array for tests */
    public Object[] array() {
        return array.clone();
    }
}
