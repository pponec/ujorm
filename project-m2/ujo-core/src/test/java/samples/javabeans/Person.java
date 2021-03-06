/*
 * Person.java
 *
 * Created on 9. June 2007, 22:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.javabeans;

import org.ujorm.Key;
import org.ujorm.implementation.map.*;
public class Person extends MapUjo {
    
    public static final Key<Person, String>  NAME = newKey("Name");
    public static final Key<Person, Integer> AGE  = newKey("Age");
    
    public void setName(String name) {
        NAME.setValue(this, name);
    }
    
    public String getName() {
        return NAME.of(this);
    }
    
    public void setAge(Integer age) {
        AGE.setValue(this, age);
    }
    
    public Integer getAge() {
        return AGE.of(this);
    }
}
