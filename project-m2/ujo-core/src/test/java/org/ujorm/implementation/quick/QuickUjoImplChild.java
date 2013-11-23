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
import org.ujorm.extensions.ListProperty;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class QuickUjoImplChild extends QuickUjoImpl {
    
    public static final Key <QuickUjoImplChild, Long>    PRO_P5 = newKey();
    public static final Key <QuickUjoImplChild, Integer> PRO_P6 = newKey();
    public static final Key <QuickUjoImplChild, String>  PRO_P7 = newKey();
    public static final Key <QuickUjoImplChild, Date>    PRO_P8 = newKey();
    public static final Key <QuickUjoImplChild, Class>   PRO_P9 = newKey();
    public static final ListProperty <QuickUjoImplChild,Color> PRO_LST2 = newListKey();
    
    // --- Mandatory initializaton ---
    static {
        init(QuickUjoImplChild.class);
    }
    
}
