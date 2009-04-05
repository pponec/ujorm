/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. èerven 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml.test2;

import java.util.ArrayList;
import java.util.List;
import org.ujoframework.implementation.map.MapUjo;
import org.ujoframework.implementation.map.MapPropertyList;
import org.ujoframework.implementation.map.MapProperty;


/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class TXmlUjoRoot extends MapUjo  {

    //public static final MapProperty<TXmlUjoRoot,Object[]>   PRO_P4 = newProperty("ObjArray", Object[].class);
    public static final MapPropertyList<TXmlUjoRoot, TXmlUjoItem> PRO_P5 = newPropertyList("List", TXmlUjoItem.class);
    
}
