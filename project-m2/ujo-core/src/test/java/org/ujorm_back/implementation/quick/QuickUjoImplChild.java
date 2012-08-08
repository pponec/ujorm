/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm_back.implementation.quick;

import java.util.Date;
import org.ujorm.UjoProperty;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class QuickUjoImplChild extends QuickUjoImpl {
    
    public static final UjoProperty <QuickUjoImplChild, Long>    PRO_P5 = newProperty(Long.class);
    public static final UjoProperty <QuickUjoImplChild, Integer> PRO_P6 = newProperty(Integer.class);
    public static final UjoProperty <QuickUjoImplChild, String>  PRO_P7 = newProperty(String.class);
    public static final UjoProperty <QuickUjoImplChild, Date>    PRO_P8 = newProperty(Date.class);
    public static final UjoProperty <QuickUjoImplChild, Float>   PRO_P9 = newProperty(Float.class);

    // --- Mandatory initializaton ---
    static {
        init(QuickUjoImpl.class);
    }
    
}
