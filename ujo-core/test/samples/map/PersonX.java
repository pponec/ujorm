/*
 * Person.java
 *
 * Created on 9. June 2007, 22:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.map;

import java.util.Date;
import org.ujoframework.UjoProperty;

public class PersonX extends Person {
    
    public static final UjoProperty<PersonX,Date>    BIRTH  = newProperty("Birth" , Date.class);
    public static final UjoProperty<PersonX,String>  CITY   = newProperty("City"  , String.class);
    public static final UjoProperty<PersonX,String>  STREET = newProperty("Street", String.class);
    public static final UjoProperty<PersonX,Integer> ZIP    = newProperty("ZIP"   , Integer.class);
    
  /**
     * Creates a new instance of UsePerson
     */
    public static PersonX createPersonX() {
        
        PersonX person = new PersonX();
        
        // Writing:
        NAME.setValue(person, "Paul Ponec");
        MALE.setValue(person, true);
        AGE.setValue(person, 31);        
        BIRTH.setValue(person, new Date() );
        STREET.setValue(person, "ABC street 321");
        CITY.setValue(person, "BigCity");
        ZIP.setValue(person, 3211);
        
        return person;
        
    }
        

}
