/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.universe;

import java.util.Date;
import org.ujorm.UjoProperty;
import org.ujorm.UjoPropertyList;
import org.ujorm.core.PropertyFactory;
import org.ujorm.ListUjoProperty;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class UniUjoChild extends UniUjoBase {
    
    /** Factory */
    private static final PropertyFactory<UniUjoChild> pf = PropertyFactory.Builder.get(UniUjoChild.class);

    public static final UjoProperty <UniUjoChild, Long>    PRO_P5 = pf.newProperty("P5");
    public static final UjoProperty <UniUjoChild, Integer> PRO_P6 = pf.newProperty("P6");
    public static final UjoProperty <UniUjoChild, String>  PRO_P7 = pf.newProperty("P7");
    public static final UjoProperty <UniUjoChild, Date>    PRO_P8 = pf.newProperty("P8");
    public static final ListUjoProperty<UniUjoChild,Float> PRO_P9 = pf.newListProperty("P9");

    static {
        pf.lock();
    }

    @Override
    public UjoPropertyList<?> readProperties() {
        return pf.getPropertyList();
    }


    
}
