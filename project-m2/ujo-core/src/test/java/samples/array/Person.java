/*
 * Person.java
 *
 * Created on 9. June 2007, 22:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package samples.array;

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.KeyFactory;
import org.ujorm.core.UjoManager;
import org.ujorm.implementation.quick.QuickUjo;

public class Person extends QuickUjo {
    /** Key factory */
    private static final KeyFactory<Person> f = newFactory(Person.class);

    public static final Key<Person,String> NAME = f.newKey("name");
    public static final Key<Person,Boolean> MALE = f.newKey("male");
    public static final Key<Person,Date> BIRTH = f.newKey("birth");

    static { f.lock(); }

}
