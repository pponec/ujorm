/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujorm_back.extensions;

import org.ujorm.extensions.ListProperty;
import org.ujorm.extensions.Property;
import org.ujorm.implementation.map.MapUjoExt;

/**
 *
 * @author Pavel Ponec
 */
public class PersonExt extends MapUjoExt<PersonExt> {
    
    public static final Property<PersonExt, Integer> ID = newProperty("id", Integer.class);
    public static final ListProperty<PersonExt, PersonExt> PERS = newListProperty("person", PersonExt.class);
    
    public PersonExt(Integer id) {
        ID.setValue(this, id);
    }

}
