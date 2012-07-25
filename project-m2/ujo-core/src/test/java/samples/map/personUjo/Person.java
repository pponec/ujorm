/**
 * Sample of implementation
 */
package samples.map.personUjo;

import java.util.HashMap;
import org.ujorm.*;
import org.ujorm.core.UjoManager;
import org.ujorm.KeyList;
import org.ujorm.extensions.Property;
import org.ujorm.UjoAction;
import org.ujorm.core.UjoPropertyListImpl;

public class Person implements Ujo {

    public static final Key<Person, String> NAME = Property.newInstance("Name", String.class);
    public static final Key<Person, Boolean> MALE = Property.newInstance("Male", Boolean.class);
    public static final Key<Person, Double> CASH = Property.newInstance("Cash", 0d);
    // --- The begin of the Ujo implementation ---
    private HashMap map = new HashMap();

    public Object readValue(Key property) {
        return map.get(property);
    }

    @SuppressWarnings("unchecked")
    public void writeValue(Key property, Object value) {
        map.put(property, value);
    }

    public KeyList readKeys() {
        return UjoManager.getInstance().readProperties(getClass());
    }

    public UjoPropertyList readProperties() {
        return new UjoPropertyListImpl(readKeys());
    }

    public boolean readAuthorization(UjoAction action, Key property, Object value) {
        return true;
    }
    // --- The end of UJO implementation --

    public void addCash(double cash) {
        double newPrice = CASH.of(this) + cash;
        CASH.setValue(this, newPrice);
    }
}