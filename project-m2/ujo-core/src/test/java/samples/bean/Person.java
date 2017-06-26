/*
 * Person.java
 *
 * Created on 9. June 2007, 22:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.bean;

import org.ujorm.Key;
import org.ujorm.Ujo;
import org.ujorm.core.UjoManager;
import org.ujorm.implementation.map.MapUjo;

public class Person extends MapUjo {
    
    public static final Key<Person, String>  NAME = newKey("Name");
    public static final Key<Person, Boolean> MALE = newKey("Male");
    public static final Key<Person, Integer> AGE  = newKey("Age");

    // ---- A STANDARD BEAN IMPLEMENTATION ----
    
    private String  name;
    private Boolean male;
    private Integer age ;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getMale() {
        return male;
    }

    public void setMale(Boolean male) {
        this.male = male;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
    
    
    
    // ---- EXTENDED ----
    
    /** Equals */
    @Override
    public boolean equals(Object obj) {
        return UjoManager.equalsUjo(this, (Ujo)obj );
    }
    
    /** Equals */
    @Override
    public Object clone(int depth, Object context) {
        return UjoManager.clone(this, depth, context);
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
