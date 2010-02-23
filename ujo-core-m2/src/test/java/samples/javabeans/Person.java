/*
 * Person.java
 *
 * Created on 9. June 2007, 22:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.javabeans;

import org.ujoframework.extensions.Property;
import org.ujoframework.implementation.map.*;
public class Person extends MapUjo {
    
    public static final Property<Person, String>  NAME = newProperty("Name", String.class);
    public static final Property<Person, Integer> AGE  = newProperty("Age" , Integer.class);
    
    public void setName(String name) {
        NAME.setValue(this, name);
    }
    
    public String getName() {
        return NAME.getValue(this);
    }
    
    public void setAge(Integer age) {
        AGE.setValue(this, age);
    }
    
    public Integer getAge() {
        return AGE.getValue(this);
    }
}
