/**
 * Sample of implementation
 */
package samples.map.personUjo;

import java.util.HashMap;
import org.ujorm.*;
import org.ujorm.core.KeyFactory;

public class Person implements Ujo {
    /** Factory */
    private static final KeyFactory<Person> f = KeyFactory.Builder.get(Person.class);

    public static final Key<Person, String> NAME = f.newKey("Name");
    public static final Key<Person, Boolean> MALE = f.newKey("Male");
    public static final Key<Person, Double> CASH = f.newKey("Cash", 0d);

    static {
        f.lock();
    }

    // --- The begin of the Ujo implementation ---
    private final HashMap map = new HashMap();

    public Object readValue(Key key) {
        return map.get(key);
    }

    @SuppressWarnings("unchecked")
    public void writeValue(Key key, Object value) {
        map.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T extends Ujo> KeyList<T> readKeys() {
        return (KeyList<T>) f.getKeys();
    }

    public boolean readAuthorization(UjoAction action, Key key, Object value) {
        return true;
    }
    // --- The end of UJO implementation --

    public void addCash(double cash) {
        double newPrice = CASH.of(this) + cash;
        CASH.setValue(this, newPrice);
    }
}