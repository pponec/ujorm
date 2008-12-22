/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. èerven 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml.test2;

import java.util.Date;
import org.ujoframework.implementation.map.MapUjo;
import org.ujoframework.implementation.map.MapProperty;

/**
 * An UnifiedDataObject Imlpementation
 * @author pavel
 */
public class TXmlUjoItem extends MapUjo  {
    
    public static final MapProperty<TXmlUjoItem,Integer> PRO_P1 = newProperty("P1", Integer.class);
    public static final MapProperty<TXmlUjoItem,Integer> PRO_P2 = newProperty("P2", Integer.class);
    
}
