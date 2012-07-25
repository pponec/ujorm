/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.xml.test2;

import org.ujorm.ListKey;
import org.ujorm.implementation.map.MapUjo;


/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class TXmlUjoRoot extends MapUjo  {

    //public static final UjoPropertyImpl<TXmlUjoRoot,Object[]>   PRO_P4 = newProperty("ObjArray", Object[].class);
    public static final ListKey<TXmlUjoRoot, TXmlUjoItem> PRO_P5 = newListProperty("List", TXmlUjoItem.class);
    
}
