/*
 * Person.java
 *
 * Created on 9. June 2007, 22:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.map;

import org.ujoframework.Ujo;
import org.ujoframework.core.UjoManager;
import org.ujoframework.extensions.Property;
import org.ujoframework.implementation.map.MapUjo;

public class Person extends MapUjo {
    
    public static final Property<Person, String>  NAME = newProperty("Name", String.class);
    public static final Property<Person, Boolean> MALE = newProperty("Male", Boolean.class);
    public static final Property<Person, Integer> AGE  = newProperty("Age" , Integer.class);
    
    
    /** Equals */
    public boolean equals(Object obj) {
        return UjoManager.getInstance().equalsUjo(this, (Ujo)obj );
    }
    
    /** Equals */
    public Object clone(int depth) {
        return UjoManager.getInstance().clone(this, depth, null);
    }
    
    /**
     * Creates a new instance of UsePerson
     */
    public static Person createPerson() {
        
        Person person = new Person();
        
        // Writing:
        Person.NAME.setValue(person, "Paul Ponec");
        Person.MALE.setValue(person, true);
        Person.AGE.setValue(person, 31);
        
        // Reading:
        String  name = Person.NAME.getValue(person);
        Boolean male = Person.MALE.getValue(person);
        Integer age  = Person.AGE .getValue(person);
        
        return person;
        
    }    
    
}
