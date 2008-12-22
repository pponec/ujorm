/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package samples.criteria;

import org.ujoframework.UjoProperty;
import org.ujoframework.implementation.map.MapUjoExt;

/**
 *
 * @author pavel
 */
public class Person extends MapUjoExt<Person> {
    
    public static final UjoProperty<Person,Person> MOTHER = newProperty("Mother", Person.class);
    public static final UjoProperty<Person,Person> FATHER = newProperty("Father", Person.class);
    public static final UjoProperty<Person,String> NAME   = newProperty("Name", String.class);
    public static final UjoProperty<Person,Double> HIGH   = newProperty("High", Double.class);
    
    public Person(String name, Double high) {
        set(NAME, name);
        set(HIGH, high);
    }

}
