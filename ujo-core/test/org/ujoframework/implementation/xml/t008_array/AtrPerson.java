/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml.t008_array;

import org.ujoframework.extensions.ListProperty;
import org.ujoframework.extensions.Property;
import org.ujoframework.implementation.map.MapUjo;


/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class AtrPerson extends MapUjo  {
    
    public static final Property<AtrPerson, String>  NAME = newProperty("Name", String.class );
    public static final Property<AtrPerson, Boolean> MALE = newProperty("Male", Boolean.class);
    public static final Property<AtrPerson, java.sql.Date>   BIRTH = newProperty("Birth", java.sql.Date.class  );
    public static final ListProperty<AtrPerson, Integer> NUMBERS = newListProperty("Child", Integer.class);
     
}
