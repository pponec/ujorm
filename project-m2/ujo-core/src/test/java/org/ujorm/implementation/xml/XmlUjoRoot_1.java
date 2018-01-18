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
import org.ujorm.Ujo;
import org.ujorm.implementation.map.MapUjo;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class XmlUjoRoot_1 extends MapUjo  {

    public static final Key<XmlUjoRoot_1,Long>       PRO_P0 = newKey("P0");
    public static final Key<XmlUjoRoot_1,XmlUjoItem> PRO_P1 = newKey("P1");
    public static final Key<XmlUjoRoot_1,String>     PRO_P2 = newKey("P2");
    public static final Key<XmlUjoRoot_1,Ujo>        PRO_P3 = newKey("P3");
    public static final Key<XmlUjoRoot_1,Object[]>   PRO_P4 = newKey("ARRAY_OBJ");
    public static final Key<XmlUjoRoot_1,ArrayList>  PRO_P5 = newKey("LIST");

    static {
        init(XmlUjoRoot_1.class);
    }

}
