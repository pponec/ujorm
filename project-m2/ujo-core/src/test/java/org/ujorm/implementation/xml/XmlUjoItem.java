/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.xml;

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.implementation.map.MapUjo;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class XmlUjoItem extends MapUjo  {
    
    public static final Key<XmlUjoItem,Long>    PRO_P0 = newKey("P0");
    public static final Key<XmlUjoItem,Integer> PRO_P1 = newKey("P1");
    public static final Key<XmlUjoItem,String>  PRO_P2 = newKey("P2");
    public static final Key<XmlUjoItem,Date>    PRO_P3 = newKey("P3");
    public static final Key<XmlUjoItem,Float>   PRO_P4 = newKey("P4");

    static {
        init(XmlUjoItem.class);
    }
    
}
