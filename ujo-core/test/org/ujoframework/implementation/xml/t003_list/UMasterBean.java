/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.xml.t003_list;

import org.ujoframework.extensions.ListProperty;
import org.ujoframework.implementation.map.MapUjo;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class UMasterBean extends MapUjo  {
    
    public static final ListProperty<UMasterBean, UItemBean> P0_L1ST  = newListProperty("itemA", UItemBean.class );
    //public static final ListProperty<UMasterBean, UItemBean> P1_L1ST  = new MapPropertyList("itemB", UItemBean.class );
    
    
}
