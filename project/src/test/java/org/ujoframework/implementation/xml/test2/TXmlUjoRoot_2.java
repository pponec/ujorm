/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml.test2;

import org.ujoframework.extensions.ListProperty;
import org.ujoframework.implementation.map.MapUjo;


/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class TXmlUjoRoot_2 extends MapUjo  {

    //public static final UjoPropertyImpl<TXmlUjoRoot_2,Object[]>   PRO_P4 = newProperty("ObjArray", Object[].class);
    
    //public static final UjoPropertyList<TXmlUjoRoot_2, Integer> PRO_P5 = newListProperty("List", Integer.class);
    public static final ListProperty<TXmlUjoRoot_2, Integer> PRO_P5 = newListProperty("List", Integer.class);

    
}
