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
import org.ujorm.ListKey;
import org.ujorm.core.KeyFactory;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class UniUjoChild extends UniUjoBase {
    
    /** Factory */
    private static final KeyFactory<UniUjoChild> f = KeyFactory.Builder.get(UniUjoChild.class);

    public static final Key <UniUjoChild, Long>    PRO_P5 = f.newKey("P5");
    public static final Key <UniUjoChild, Integer> PRO_P6 = f.newKey("P6");
    public static final Key <UniUjoChild, String>  PRO_P7 = f.newKey("P7");
    public static final Key <UniUjoChild, Date>    PRO_P8 = f.newKey("P8");
    public static final ListKey<UniUjoChild,Float> PRO_P9 = f.newListKey("P9");

    // Lock the Key factory
    static { f.lock(); }

    /** An optional method for a better performance.
     * @return Return all direct Keys (An implementation from hhe Ujo API)
     */
    @Override
    public KeyList readKeys() {
        return f.getKeys();
    }


    
}
