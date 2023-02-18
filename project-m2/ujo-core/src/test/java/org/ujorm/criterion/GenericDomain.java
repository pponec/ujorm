package org.ujorm.criterion;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

/**
 * Generic domain
 * @author Pavel Ponec
 */
public class GenericDomain<U extends GenericDomain> {

    public static final Key<GenericDomain, Integer> ID = new Key("id");
    public static final Key<GenericDomain, String> NAME = new Key("name");
    protected HashMap<String, Object> map = new HashMap();

    public <VALUE> void set(Key<? super U, VALUE> key, VALUE value) {
        key.setValue((U) this, value);
    }

    public <VALUE> VALUE get(Key<? super U, VALUE> key) {
        return key.getValue((U) this);
    }

    @Test
    public void test1() {
        // GenericDomain domain = this;
        GenericDomain<U> domain = this; // The Workarround
        domain.set(GenericDomain.ID, 10);
        domain.set(GenericDomain.NAME, "MyName");

        Integer id = domain.get(ID);   // Compilator fails!
        String name = domain.get(NAME); // Compilator fails!

        assert id == 10;
        assert name == "MyName";
    }

    @Test
    public void test2() {
        GenericDomain domain = this;
        domain.set(ID, 10);
        domain.set(NAME, "MyName");

        Integer id = ID.getValue(domain);   // Compilator OK
        String name = NAME.getValue(domain); // Compilator OK

        assert id == 10;
        assert name == "MyName";
    }

    /** Immutable Key */
    public static class Key<D extends GenericDomain, V> {
        private final String name;

        public Key(String name) {
            this.name = name;
        }

        public void setValue(D domain, V value) {
            domain.map.put(name, value);
        }

        public V getValue(D domain) {
            return (V) domain.map.get(name);
        }
    }
}
