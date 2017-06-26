/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.xml.test2;

import org.ujorm.Key;
import org.ujorm.implementation.quick.QuickUjo;


/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class TXmlUjoItem extends QuickUjo  {
    
    public static final Key<TXmlUjoItem,Integer> PRO_P1 = newKey("P1");
    public static final Key<TXmlUjoItem,Integer> PRO_P2 = newKey("P2");

}
