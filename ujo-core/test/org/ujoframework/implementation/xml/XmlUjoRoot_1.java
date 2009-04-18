/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml;

import java.util.ArrayList;
import java.util.List;
import org.ujoframework.Ujo;
import org.ujoframework.implementation.map.MapUjo;
import org.ujoframework.implementation.map.MapProperty;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class XmlUjoRoot_1 extends MapUjo  {
    
    public static final MapProperty<XmlUjoRoot_1,Long>       PRO_P0 = newProperty("P0", Long.class);
    public static final MapProperty<XmlUjoRoot_1,XmlUjoItem> PRO_P1 = newProperty("P1", XmlUjoItem.class);
    public static final MapProperty<XmlUjoRoot_1,String>     PRO_P2 = newProperty("P2", String.class);
    public static final MapProperty<XmlUjoRoot_1, Ujo>       PRO_P3 = newProperty("P3", Ujo.class);
    public static final MapProperty<XmlUjoRoot_1,Object[]>   PRO_P4 = newProperty("ARRAY_OBJ", Object[].class);
    public static final MapProperty<XmlUjoRoot_1,ArrayList>  PRO_P5 = newProperty("LIST", ArrayList.class);
    
}
