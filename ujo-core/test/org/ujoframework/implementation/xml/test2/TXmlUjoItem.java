/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml.test2;

import org.ujoframework.UjoProperty;
import org.ujoframework.implementation.map.MapUjo;


/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class TXmlUjoItem extends MapUjo  {
    
    public static final UjoProperty<TXmlUjoItem,Integer> PRO_P1 = newProperty("P1", Integer.class);
    public static final UjoProperty<TXmlUjoItem,Integer> PRO_P2 = newProperty("P2", Integer.class);
    
}
