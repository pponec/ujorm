/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.array;

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class ArrayUjoImplChild extends ArrayUjoImpl {
    
    /** Factory */
    private static final KeyFactory<ArrayUjoImplChild> f = newFactory(ArrayUjoImplChild.class);

    public static final Key <ArrayUjoImplChild, Long>    PRO_P5 = f.newKey("P5");
    public static final Key <ArrayUjoImplChild, Integer> PRO_P6 = f.newKey("P6");
    public static final Key <ArrayUjoImplChild, String>  PRO_P7 = f.newKey("P7");
    public static final Key <ArrayUjoImplChild, Date>    PRO_P8 = f.newKey("P8");
    public static final Key <ArrayUjoImplChild, Float>   PRO_P9 = f.newKey("P9");
    
    static {
        f.lock();
    }
    
    /** Creates a new instance of UnifiedDataObjectImlp */
    public ArrayUjoImplChild() {
    }    
    
}
