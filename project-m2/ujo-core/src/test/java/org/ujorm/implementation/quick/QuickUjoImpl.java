/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.quick;

import java.awt.Color;
import java.util.Date;
import org.ujorm.Key;
import org.ujorm.ListKey;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class QuickUjoImpl extends QuickUjoMid<QuickUjoImpl> {
    
    public static final Key<QuickUjoImpl,Long>    PRO_P0 = newKey();
    public static final Key<QuickUjoImpl,Integer> PRO_P1 = newKey();
    public static final Key<QuickUjoImpl,String>  PRO_P2 = newKey();
    public static final Key<QuickUjoImpl,Date>    PRO_P3 = newKey();
    public static final Key<QuickUjoImpl,Class>   PRO_P4 = newKey();
    public static final ListKey<QuickUjoImpl,Color> PRO_LST1 = newListKey();

    // --- Mandatory initializaton ---
    static {
        init(QuickUjoImpl.class);
    }

}
