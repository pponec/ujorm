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
import org.ujorm.Key;
import org.ujorm.implementation.map.MapUjo;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class XmlUjoRoot_2 extends MapUjo  {
    
    public static final Key<XmlUjoRoot_2,Object[]>  PRO_P4 = newKey("ARRAY_OBJ");
    public static final Key<XmlUjoRoot_2,ArrayList> PRO_P5 = newKey("LIST");
    
    static {
        init(XmlUjoRoot_2.class);
    }
    
}
