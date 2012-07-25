/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujorm.extensions;

import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.implementation.map.MapUjoExt;

/**
 *
 * @author Pavel Ponec
 */
public class PersonExt extends MapUjoExt<PersonExt> {
    
    public static final Key<PersonExt, Integer> ID = newProperty("id", Integer.class);
    public static final ListKey<PersonExt, PersonExt> PERS = newListProperty("person", PersonExt.class);
    
    public PersonExt(Integer id) {
        ID.setValue(this, id);
    }

}
