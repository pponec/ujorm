package org.ujorm.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ujorm.core.criterion.Criterion;
import org.ujorm.core.criterion.Operator;
import org.ujorm.core.criterion.ProxyValue;
import org.ujorm.tools.common.Array;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for methods of the Key interface */
class KeyTest {

    private Key<Employee, String> nameKey;
    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        nameKey = new EmployeeNameKey();
    }

    @Test
    void testName() {
        assertEquals("firstName", nameKey.name());
    }

    @Test
    void testType() {
        assertEquals(String.class, nameKey.type());
    }

    @Test
    void testDomainClass() {
        assertEquals(Employee.class, nameKey.domainClass());
    }

    @Test
    void testSetValueAndGetValue() {
        nameKey.setValue(employee, "Alice");
        assertEquals("Alice", nameKey.getValue(employee));
    }

    @Test
    void testGetDefaultValue() {
        assertEquals("N/A", nameKey.getDefaultValue());
    }

    @Test
    void testIndex() {
        assertEquals((short) 10, nameKey.index());
    }

    @Test
    void testInfo() {
        assertNotNull(nameKey.info());
    }

    @Test
    void testIsTypeOf() {
        assertTrue(nameKey.isTypeOf(String.class));
        assertTrue(nameKey.isTypeOf(CharSequence.class));
        assertTrue(nameKey.isTypeOf(Object.class));
        assertFalse(nameKey.isTypeOf(Integer.class));
    }

    @Test
    void testIsInstanceOf() {
        assertTrue(nameKey.isInstanceOf("Alice"));
        assertFalse(nameKey.isInstanceOf(new StringBuilder("Bob")));
        assertFalse(nameKey.isInstanceOf(123));
        assertFalse(nameKey.isInstanceOf(null));
    }

    @Test
    void testIsDomainOf() {
        assertTrue(nameKey.isDomainOf(Employee.class));
        assertTrue(nameKey.isDomainOf(Object.class));
        assertFalse(nameKey.isDomainOf(String.class));
    }

    // --- SUPPORTING DUMMY CLASSES ---

    /** Dummy domain class */
    static class Employee {
        private String firstName;
    }

    /** Dummy implementation of the Key interface */
    static class EmployeeNameKey implements Key<Employee, String> {

        @Override
        public String name() {
            return "firstName";
        }

        @Override
        public Class<String> type() {
            return String.class;
        }

        @Override
        public Class<Employee> domainClass() {
            return Employee.class;
        }

        @Override
        public void setValue(Employee bean, String value) throws UnsupportedOperationException {
            bean.firstName = value;
        }

        @Override
        public String getValue(Employee bean) {
            return bean.firstName;
        }

        @Override
        public String getDefaultValue() {
            return "N/A";
        }

        @Override
        public short index() {
            return 10;
        }

        @Override
        public KeyInfo info() {
            return new KeyInfo() {

                @Override
                public @NotNull String columnLabel() {
                    return "";
                }

                @Override
                public boolean required() {
                    return false;
                }

                @Override
                public boolean primaryKey() {
                    return false;
                }

                @Override
                public boolean foreignKey() {
                    return false;
                }

                @Override
                public boolean mapEnumByOrdinal() {
                    return false;
                }

                @Override
                public String toString() {
                    return "DummyKeyInfo";
                }
            };
        }

        @Override
        public @NotNull Criterion where(@NotNull Operator operator, @Nullable String s) {
            return null;
        }

        @Override
        public @NotNull Criterion where(@NotNull Operator operator, @NotNull ProxyValue<String> proxyValue) {
            return null;
        }

        @Override
        public @NotNull Criterion where(@NotNull Operator operator, @NotNull Key<?, String> value) {
            return null;
        }

        @Override
        public @NotNull Criterion whereIn(@NotNull Array<String> array) {
            return null;
        }

        @Override
        public @NotNull Criterion whereNotIn(@NotNull Array<String> array) {
            return null;
        }

        @Override
        public @NotNull Criterion whereTrue() {
            return null;
        }

        @Override
        public @NotNull Criterion whereFalse() {
            return null;
        }

        @Override
        public @NotNull Criterion whereSql(@NotNull String template, String... values) {
            return null;
        }
    }
}