package org.ujorm.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SnapshotableTest {

    @Test
    void saveSnapshotBasicProperties() {
        var employee = new Employee();
        employee.setId(1);
        employee.setName("John");
        employee.setActive(true);
        employee.setCreated(LocalDateTime.now());
        employee.saveSnapshot();

        // Change original values
        employee.setName("Modified");
        employee.setActive(false);

        var result = employee.readSnapshot();
        assertNotNull(result);
        assertEquals("John", result.getName());
        assertEquals("Modified", employee.getName());
        assertTrue(result.isActive());
        assertFalse(employee.isActive());
    }

    @Test
    void verifySnapshotReferenceClearing() {
        var employee = new Employee();
        employee.saveSnapshot();

        var firstSnapshot = employee.readSnapshot();
        employee.saveSnapshot(); // Save the next snapshot

        // Verify that chaining does not occur
        assertNull(employee.readSnapshot().readSnapshot());
        assertNull(firstSnapshot.readSnapshot());
    }

    @Test
    void readSnapshotWhenEmpty() {
        var employee = new Employee();
        assertNull(employee.readSnapshot());

        employee.saveSnapshot();
        assertNotNull(employee.readSnapshot());
    }

    @Test
    void verifyMethodChaining() {
        var employee = new Employee();
        var result = employee.saveSnapshot();

        // Verify the returned instance is exactly the original object
        assertSame(employee, result);
    }

    @Test
    void verifyShallowCopyBehavior() {
        var date = LocalDateTime.now();
        var employee = new Employee();
        employee.setCreated(date);
        employee.saveSnapshot();

        var result = employee.readSnapshot();

        // The snapshot must be a different instance
        assertNotSame(employee, result);
        // Object attributes must share the same reference (shallow copy)
        assertSame(date, result.getCreated());
    }

    @Test
    void verifyCloneClearsSnapshot() throws CloneNotSupportedException {
        var employee = new Employee();
        employee.saveSnapshot();

        var result = (Employee) employee.clone();

        // The cloned object must not retain the snapshot of the original object
        assertNull(result.readSnapshot());
        // The original object must still have its snapshot
        assertNotNull(employee.readSnapshot());
    }

    @Getter @Setter @ToString
    public static class Employee extends AbstractSnapshotable<Employee> {
        private int id;
        private String name;
        private boolean active;
        private LocalDateTime created;
    }
}