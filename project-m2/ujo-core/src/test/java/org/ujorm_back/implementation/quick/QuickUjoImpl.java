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
import org.ujorm.extensions.Property;
import org.ujorm.implementation.quick.QuickUjoMid;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class QuickUjoImpl extends QuickUjoMid<QuickUjoImpl> {
    
    public static final Property<QuickUjoImpl,Long>    PRO_P0 = newProperty(Long.class);
    public static final Property<QuickUjoImpl,Integer> PRO_P1 = newProperty(Integer.class);
    public static final Property<QuickUjoImpl,String>  PRO_P2 = newProperty(String.class);
    public static final Property<QuickUjoImpl,Date>    PRO_P3 = newProperty(Date.class);
    public static final Property<QuickUjoImpl,Float>   PRO_P4 = newProperty(Float.class);


    // --- Mandatory initializaton ---
    static { 
        init(QuickUjoImpl.class);
    }

}
