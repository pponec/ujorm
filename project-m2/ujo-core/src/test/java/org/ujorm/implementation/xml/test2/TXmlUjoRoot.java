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
public class TXmlUjoRoot extends QuickUjo  {

    //public static final UjoPropertyImpl<TXmlUjoRoot,Object[]>   PRO_P4 = newKey("ObjArray", Object[].class);
    public static final ListKey<TXmlUjoRoot, TXmlUjoItem> PRO_P5 = newListKey("List");

}
