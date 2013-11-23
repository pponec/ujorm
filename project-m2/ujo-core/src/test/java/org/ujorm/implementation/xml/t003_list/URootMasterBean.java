/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.xml.t003_list;

import org.ujorm.Key;
import org.ujorm.implementation.quick.QuickUjo;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class URootMasterBean extends QuickUjo  {
    
    public static final Key<URootMasterBean, UMasterBean> MASTER = newKey();
    
}
