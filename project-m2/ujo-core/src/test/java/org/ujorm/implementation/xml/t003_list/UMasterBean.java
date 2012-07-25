/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.xml.t003_list;

import org.ujorm.ListKey;
import org.ujorm.implementation.map.MapUjo;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class UMasterBean extends MapUjo  {
    
    public static final ListKey<UMasterBean, UItemBean> P0_L1ST  = newListProperty("itemA", UItemBean.class );
    //public static final ListKey<UMasterBean, UItemBean> P1_L1ST  = newPropertyList("itemB", UItemBean.class );
    
    
}
