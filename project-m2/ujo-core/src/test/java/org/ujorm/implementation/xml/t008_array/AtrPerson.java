/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.xml.t008_array;

import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.implementation.map.MapUjo;


/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class AtrPerson extends MapUjo  {
    
    public static final Key<AtrPerson, String>  NAME = newProperty("Name", String.class );
    public static final Key<AtrPerson, Boolean> MALE = newProperty("Male", Boolean.class);
    public static final Key<AtrPerson, java.sql.Date>   BIRTH = newProperty("Birth", java.sql.Date.class  );
    public static final ListKey<AtrPerson, Integer> NUMBERS = newListProperty("Child", Integer.class);
     
}
