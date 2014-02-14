/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.quick.domains;

import java.awt.Color;
import java.util.Date;
import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.quick.SmartUjo;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class SmartUjoImpl extends SmartUjo<SmartUjoImpl> {
    private static final KeyFactory<SmartUjoImpl> f = newFactory(SmartUjoImpl.class);
    
    public static final Key<SmartUjoImpl,Long>    PRO_P0 = f.newKey();
    public static final Key<SmartUjoImpl,Integer> PRO_P1 = f.newKey();
    public static final Key<SmartUjoImpl,String>  PRO_P2 = f.newKey();
    public static final Key<SmartUjoImpl,Date>    PRO_P3 = f.newKey();
    public static final Key<SmartUjoImpl,Class>   PRO_P4 = f.newKey();
    public static final ListKey<SmartUjoImpl,Color> PRO_LST1 = f.newListKey();

    // --- Mandatory initializaton ---
    static {
        f.lock();
    }

}
