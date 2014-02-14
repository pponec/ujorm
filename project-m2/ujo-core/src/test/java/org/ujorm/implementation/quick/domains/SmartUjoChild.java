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
import org.ujorm.core.KeyFactory;
import org.ujorm.extensions.ListProperty;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class SmartUjoChild extends SmartUjoImpl {
    private static final KeyFactory<SmartUjoChild> f = newFactory(SmartUjoChild.class);
    
    public static final Key <SmartUjoChild, Long>    PRO_P5 = f.newKey();
    public static final Key <SmartUjoChild, Integer> PRO_P6 = f.newKey();
    public static final Key <SmartUjoChild, String>  PRO_P7 = f.newKey();
    public static final Key <SmartUjoChild, Date>    PRO_P8 = f.newKey();
    public static final Key <SmartUjoChild, Class>   PRO_P9 = f.newKey();
    public static final ListProperty <SmartUjoChild,Color> PRO_LST2 = f.newListKey();
    
    // --- Mandatory initializaton ---
    static {
        f.lock();
    }
    
}
