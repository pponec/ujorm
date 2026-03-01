package org.ujorm.core;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SnapshotableTest {

    @Test
    void saveSnapshot() {
        var employee = new Employee();
        employee.setId(1);
        employee.setName("John");
        employee.setActive(true);
        employee.setCreated(LocalDateTime.now());
        employee.saveSnapshot(); // SAVE A SNAPSHOT

        // Change original values:
        employee.setName("Modified");
        employee.setActive(false);

        assertNotNull(employee.readSnapshot());
        assertEquals("John", employee.readSnapshot().getName());
        assertEquals("Modified", employee.getName());
        assertTrue(employee.readSnapshot().isActive());
        assertFalse(employee.isActive());
    }

    @Test
    void readSnapshot() {
        var employee = new Employee();
        assertNull(employee.readSnapshot());

        employee.saveSnapshot();
        assertNotNull(employee.readSnapshot());
    }

    @Getter @Setter @ToString
    public static class Employee implements Snapshotable<Employee> {
        private transient Employee _snapshot;

        private Integer id;
        private String name;
        private boolean active;
        private LocalDateTime created;


        /** Save a shallow copy of the current state internally */
        @Override
        public Employee saveSnapshot() {
            this._snapshot = clone();
            return this;
        }

        /** Get the previously saved snapshot of the object */
        @Override
        public Employee readSnapshot() {
            return _snapshot;
        }

        /** Creates a shallow copy of the object */
        @Override
        public Employee clone() {
            try {
                return (Employee) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}