/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.quick;

import java.util.Date;
import org.ujorm.UjoProperty;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class QuickUjoImplChild extends QuickUjoImpl {
    
    public static final UjoProperty <QuickUjoImplChild, Long>    PRO_P5 = newProperty();
    public static final UjoProperty <QuickUjoImplChild, Integer> PRO_P6 = newProperty();
    public static final UjoProperty <QuickUjoImplChild, String>  PRO_P7 = newProperty();
    public static final UjoProperty <QuickUjoImplChild, Date>    PRO_P8 = newProperty();
    public static final UjoProperty <QuickUjoImplChild, Class>   PRO_P9 = newProperty();

    // --- Mandatory initializaton ---
    static {
        init(QuickUjoImpl.class);
    }
    
}
