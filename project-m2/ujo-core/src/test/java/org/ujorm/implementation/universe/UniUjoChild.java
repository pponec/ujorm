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
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.core.KeyFactory;
import org.ujorm.ListKey;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class UniUjoChild extends UniUjoBase {
    
    /** Factory */
    private static final KeyFactory<UniUjoChild> pf = KeyFactory.Builder.get(UniUjoChild.class);

    public static final Key <UniUjoChild, Long>    PRO_P5 = pf.newKey("P5");
    public static final Key <UniUjoChild, Integer> PRO_P6 = pf.newKey("P6");
    public static final Key <UniUjoChild, String>  PRO_P7 = pf.newKey("P7");
    public static final Key <UniUjoChild, Date>    PRO_P8 = pf.newKey("P8");
    public static final ListKey<UniUjoChild,Float> PRO_P9 = pf.newListProperty("P9");

    static {
        pf.lock();
    }

    @Override
    public KeyList<?> readKeys() {
        return pf.getPropertyList();
    }


    
}
