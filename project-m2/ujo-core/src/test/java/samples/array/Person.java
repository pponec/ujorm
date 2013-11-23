/*
 * Person.java
 *
 * Created on 9. June 2007, 22:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package samples.array;

import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.UjoManager;
import org.ujorm.implementation.quick.QuickUjo;

public class Person extends QuickUjo {

    /** An Incrementator. Use a new counter for each subclass by sample. */
    private static final KeyFactory<Person> f = newFactory(Person.class);

    public static final Key NAME = f.newKey("name");
    public static final Key MALE = f.newKey("male");
    public static final Key BIRTH = f.newKey("birth");

    static {
        f.lock();
    }

    /** Equals */
    public boolean equals(Object obj) {
        return UjoManager.getInstance().equalsUjo(this, (Ujo) obj);
    }

    /** Equals */
    @Override
    public Object clone(int depth, Object context) {
        return UjoManager.getInstance().clone(this, depth, context);
    }

}
