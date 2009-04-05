/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. èerven 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml.t003_list;

import org.ujoframework.implementation.map.MapPropertyList;
import org.ujoframework.implementation.map.MapUjo;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class UMasterBean extends MapUjo  {
    
    public static final MapPropertyList<UMasterBean, UItemBean> P0_L1ST  = newPropertyList("itemA", UItemBean.class );
    //public static final MapPropertyList<UMasterBean, UItemBean> P1_L1ST  = new MapPropertyList("itemB", UItemBean.class );
    
    
}
