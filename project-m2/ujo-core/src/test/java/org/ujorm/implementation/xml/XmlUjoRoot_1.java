/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.xml;

import java.util.ArrayList;
import org.ujorm.Ujo;
import org.ujorm.extensions.Property;
import org.ujorm.implementation.map.MapUjo;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class XmlUjoRoot_1 extends MapUjo  {
    
    public static final Property<XmlUjoRoot_1,Long>       PRO_P0 = newProperty("P0", Long.class);
    public static final Property<XmlUjoRoot_1,XmlUjoItem> PRO_P1 = newProperty("P1", XmlUjoItem.class);
    public static final Property<XmlUjoRoot_1,String>     PRO_P2 = newProperty("P2", String.class);
    public static final Property<XmlUjoRoot_1, Ujo>       PRO_P3 = newProperty("P3", Ujo.class);
    public static final Property<XmlUjoRoot_1,Object[]>   PRO_P4 = newProperty("ARRAY_OBJ", Object[].class);
    public static final Property<XmlUjoRoot_1,ArrayList>  PRO_P5 = newProperty("LIST", ArrayList.class);
    
}
