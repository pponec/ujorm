/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. èerven 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml.t008_array;

import java.util.Date;
import org.ujoframework.implementation.map.MapUjo;
import org.ujoframework.implementation.map.MapProperty;
import org.ujoframework.implementation.map.MapPropertyList;


/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class AtrPerson extends MapUjo  {
    
    public static final MapProperty<AtrPerson, String>  NAME = newProperty("Name", String.class );
    public static final MapProperty<AtrPerson, Boolean> MALE = newProperty("Male", Boolean.class);
    public static final MapProperty<AtrPerson, Date>   BIRTH = newProperty("Birth", Date.class  );
    public static final MapPropertyList<AtrPerson, Integer> NUMBERS = newPropertyList("Child", Integer.class);
     
}
