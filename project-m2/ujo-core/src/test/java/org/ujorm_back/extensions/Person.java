/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujorm_back.extensions;

import org.ujorm.extensions.ListProperty;
import org.ujorm.extensions.Property;
import org.ujorm.implementation.map.MapUjo;

/**
 *
 * @author Pavel Ponec
 */
public class Person extends MapUjo {
    
    public static final Property<Person, Integer> ID = newProperty("id", Integer.class);
    public static final ListProperty<Person, Person> PERS = newListProperty("person", Person.class);
    
    public Person(Integer id) {
        ID.setValue(this, id);
    }

}
