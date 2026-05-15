package org.ujorm.core.composed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ujorm.core.Key;
import org.ujorm.core.KeyInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for the {@link ComposedKeyImpl} class.
 */
class ComposedKeyImplTest {

    private Key<Person, Address> personAddressKey;
    private Key<Address, String> addressCityKey;
    private Key<Person, String> composedKey;

    @BeforeEach
    void setUp() {
        personAddressKey = new MockKey<>("address", Address.class, Person.class);
        addressCityKey = new MockKey<>("city", String.class, Address.class);
        composedKey = ComposedKeyImpl.of(personAddressKey, addressCityKey);
    }

    @Test
    void testGetValueSuccess() {
        var city = "Prague";
        var person = new Person(new Address(city));

        var result = composedKey.getValue(person);
        assertEquals(city, result);
    }

    @Test
    void testGetValueWithNullIntermediate() {
        var person = new Person(null);

        var result = composedKey.getValue(person);
        assertNull(result, "Should return null if intermediate object is null");
    }

    @Test
    void testSetValueSuccess() {
        var address = new Address("Brno");
        var person = new Person(address);
        var newCity = "Ostrava";

        composedKey.setValue(person, newCity);
        assertEquals(newCity, address.getCity());
    }

    @Test
    void testSetValueThrowsExceptionOnNull() {
        var person = new Person(null);

        var exception = assertThrows(NullPointerException.class, () ->
                composedKey.setValue(person, "Plzeň"));

        assertTrue(exception.getMessage().contains("address"));
    }

    @Test
    void testNameAndFullName() {
        assertEquals("address.city", composedKey.name());
        assertEquals("Person.address.city", composedKey.fullName());
    }

    @Test
    void testDeepComposition() {
        var k1 = personAddressKey;
        var k2 = addressCityKey;

        // Test three-level composition via ofDirtyKeys
        var tripleKey = ComposedKeyImpl.ofDirtyKeys(k1, k2);

        assertEquals(2, ((ComposedKey<?, ?>) tripleKey).pathSize());
        assertEquals("address", ((ComposedKey<?, ?>) tripleKey).pathItem(0).name());
    }

    // --- Helper Domain Classes ---

    static class Person {
        private Address address;
        Person(Address address) { this.address = address; }
        Address getAddress() { return address; }
    }

    static class Address {
        private String city;
        Address(String city) { this.city = city; }
        String getCity() { return city; }
        void setCity(String city) { this.city = city; }
    }

    // --- Minimal Mock Implementation of Key ---

    static class MockKey<D, V> implements Key<D, V> {
        private final String name;
        private final Class<V> type;
        private final Class<D> domain;

        MockKey(String name, Class<V> type, Class<D> domain) {
            this.name = name;
            this.type = type;
            this.domain = domain;
        }

        @Override public @NotNull String name() { return name; }
        @Override public @NotNull Class<V> type() { return type; }
        @Override public @NotNull Class<D> domainClass() { return domain; }

        @Override
        public void setValue(@NotNull D bean, @Nullable V value) {
            if (bean instanceof Address a) a.setCity((String) value);
        }

        @Override
        public V getValue(@NotNull D bean) {
            if (bean instanceof Person p) return (V) p.getAddress();
            if (bean instanceof Address a) return (V) a.getCity();
            return null;
        }

        @Override public @NotNull Key<D, V> self() { return this; }
        @Override public Key<?, ?> pathItem(int index) { return this; }
        @Override public @Nullable V getDefaultValue() { return null; }
        @Override public short index() { return 0; }
        @Override public @NotNull KeyInfo info() { return null; }
        @Override public String toString() { return name; }
    }
}