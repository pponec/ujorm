/*
 * Usage.java
 *
 * Created on 10. June 2007, 10:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.array;

import java.util.Date;

/**
 * Sample of usage
 * @author Pavel Ponec
 */
public class Usage {
    
    /** A sample of usage. */
    public void run() {
Person person = new Person();

// Writing:
person.writeValue(Person.NAME, "John Smith");
person.writeValue(Person.MALE, Boolean.TRUE);
person.writeValue(Person.BIRTH, new Date());

// Reading:
String  name = (String ) person.readValue(Person.NAME);
Boolean male = (Boolean) person.readValue(Person.MALE);
Date   birth = (Date   ) person.readValue(Person.NAME);
    }
    
}
