/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.extensions;

import org.ujoframework.implementation.map.MapUjo;

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
