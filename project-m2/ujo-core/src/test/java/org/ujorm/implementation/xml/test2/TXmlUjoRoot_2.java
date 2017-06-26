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
import org.ujorm.implementation.quick.QuickUjo;


/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class TXmlUjoRoot_2 extends QuickUjo  {

    //public static final UjoPropertyImpl<TXmlUjoRoot_2,Object[]>   PRO_P4 = newKey("ObjArray");
    
    //public static final UjoPropertyList<TXmlUjoRoot_2, Integer> PRO_P5 = newListKey("List");
    public static final ListKey<TXmlUjoRoot_2, Integer> PRO_P5 = newListKey("List");

}
