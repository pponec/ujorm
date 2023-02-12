/*
 * Person.java
 *
 * Created on 19. October 2007, 20:40
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package samples.interfaceTest;

import org.ujorm.*;
import org.ujorm.core.*;

@SuppressWarnings("unchecked")
public class Person implements Ujo {
    private static final KeyFactory<Person> f = KeyFactory.CamelBuilder.get(Person.class);

    public static final Key<Person, String> NAME = f.newKey();
    public static final Key<Person, Boolean> MALE = f.newKey();
    public static final Key<Person, Double> CASH = f.newKeyDefault(0.0);
    // --- The start of the Ujo implementation ---
    private final Object[] data = new Object[f.lockAndSize()];

    @Override
    public Object readValue(Key key) {
        return data[key.getIndex()];
    }

    @Override
    public void writeValue(Key key, Object value) {
        data[key.getIndex()] = value;
    }

    @Override
    public KeyList readKeys() {
        return f.getKeys();
    }

    @Override
    public boolean readAuthorization(UjoAction action, Key key, Object value) {
        return true;
    } // --- The end of the Ujo implementation ---

    /** Add cash in the Ujo implementation */
    public void addCash(double cash) {
        double newPrice = CASH.of(this) + cash;
        CASH.setValue(this, newPrice);
    }

}