/*
 * Person.java
 *
 * Created on 9. June 2007, 22:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.map;

import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.UjoManager;
import org.ujorm.core.UjoTools;
import org.ujorm.implementation.map.MapUjo;

public class Person extends MapUjo {

    public static final Key<Person, String>  NAME = newKey("Name");
    public static final Key<Person, Boolean> MALE = newKey("Male");
    public static final Key<Person, Integer> AGE  = newKey("Age");

  static {
     init(Person.class );
  }

    /** Equals */
    public boolean equals(Object obj) {
        return UjoTools.equalsUjo(this, (Ujo)obj );
    }

    /** Equals */
    public Object clone(int depth) {
        return UjoManager.clone(this, depth, null);
    }

    /**
     * Creates a new instance of UsePerson
     */
    public static Person createPerson() {

        Person person = new Person();

        // Writing:
        Person.NAME.setValue(person, "Pavel Ponec");
        Person.MALE.setValue(person, true);
        Person.AGE.setValue(person, 31);

        // Reading:
        String  name = Person.NAME.of(person);
        Boolean male = Person.MALE.of(person);
        Integer age  = Person.AGE .of(person);

        return person;

    }

}
