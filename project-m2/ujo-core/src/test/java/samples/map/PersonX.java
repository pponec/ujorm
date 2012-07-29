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
import org.ujorm.Key;

public class PersonX extends Person {
    
    public static final Key<PersonX,Date>    BIRTH  = newKey("Birth");
    public static final Key<PersonX,String>  CITY   = newKey("City");
    public static final Key<PersonX,String>  STREET = newKey("Street");
    public static final Key<PersonX,Integer> ZIP    = newKey("ZIP");

  static {
     init(PersonX.class );
  }
    
  /**
     * Creates a new instance of UsePerson
     */
    public static PersonX createPersonX() {
        
        PersonX person = new PersonX();
        
        // Writing:
        NAME.setValue(person, "Pavel Ponec");
        MALE.setValue(person, true);
        AGE.setValue(person, 31);        
        BIRTH.setValue(person, new Date() );
        STREET.setValue(person, "ABC street 321");
        CITY.setValue(person, "BigCity");
        ZIP.setValue(person, 3211);
        
        return person;
        
    }
        

}
