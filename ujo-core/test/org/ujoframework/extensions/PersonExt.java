/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.extensions;

import org.ujoframework.implementation.map.MapProperty;
import org.ujoframework.implementation.map.MapPropertyList;
import org.ujoframework.implementation.map.MapUjoExt;

/**
 *
 * @author pavel
 */
public class PersonExt extends MapUjoExt<PersonExt> {
    
    public static final MapProperty<PersonExt, Integer> ID = newProperty("id", Integer.class);
    public static final MapPropertyList<PersonExt, PersonExt> PERS = newPropertyList("person", PersonExt.class);
    
    public PersonExt(Integer id) {
        ID.setValue(this, id);
    }

}
