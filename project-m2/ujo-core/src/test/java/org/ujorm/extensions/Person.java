/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujorm.extensions;

import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.implementation.map.MapUjo;

/**
 *
 * @author Pavel Ponec
 */
public class Person extends MapUjo {
    
    public static final Key<Person, Integer> ID = newProperty("id", Integer.class);
    public static final ListKey<Person, Person> PERS = newListProperty("person", Person.class);
    
    public Person(Integer id) {
        ID.setValue(this, id);
    }

}
