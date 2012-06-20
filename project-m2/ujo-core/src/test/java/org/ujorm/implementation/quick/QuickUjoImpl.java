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
import org.ujorm.extensions.ListProperty;
import org.ujorm.extensions.Property;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class QuickUjoImpl extends QuickUjoMid<QuickUjoImpl> {
    
    public static final Property<QuickUjoImpl,Long>    PRO_P0 = newProperty();
    public static final Property<QuickUjoImpl,Integer> PRO_P1 = newProperty();
    public static final Property<QuickUjoImpl,String>  PRO_P2 = newProperty();
    public static final Property<QuickUjoImpl,Date>    PRO_P3 = newProperty();
    public static final Property<QuickUjoImpl,Class>   PRO_P4 = newProperty();
    public static final ListProperty<QuickUjoImpl,Color> PRO_LST1 = newListProperty();

    // --- Mandatory initializaton ---
    static {
        init(QuickUjoImpl.class);
    }

}
