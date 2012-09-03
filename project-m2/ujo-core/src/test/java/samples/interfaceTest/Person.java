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

    private static final KeyFactory f = KeyFactory.CamelBuilder.get(Person.class);
    public static final Key<Person, String> NAME = f.newKey();
    public static final Key<Person, Boolean> MALE = f.newKey();
    public static final Key<Person, Double> CASH = f.newKeyDefault(0.0);
    // --- The start of the Ujo implementation ---
    private Object[] data = new Object[f.lockAndSize()];

    public Object readValue(Key property) {
        return data[property.getIndex()];
    }

    public void writeValue(Key property, Object value) {
        data[property.getIndex()] = value;
    }

    public KeyList<?> readKeys() {
        return f.getKeys();
    }

    public boolean readAuthorization(UjoAction action, Key property, Object value) {
        return true;
    } // --- The end of the Ujo implementation ---

    /** Add cash in the Ujo implementation */
    public void addCash(double cash) {
        double newPrice = CASH.of(this) + cash;
        CASH.setValue(this, newPrice);
    }

    public UjoPropertyList readProperties() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}