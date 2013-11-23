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
import org.ujorm.implementation.quick.QuickUjo;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class ArrayUjoImpl extends QuickUjo {

    /** Factory */
    private static final KeyFactory<ArrayUjoImpl> f = newFactory(ArrayUjoImpl.class);
    
    public static final Key<ArrayUjoImpl,Long>    PRO_P0 = f.newKey("P0");
    public static final Key<ArrayUjoImpl,Integer> PRO_P1 = f.newKey("P1");
    public static final Key<ArrayUjoImpl,String>  PRO_P2 = f.newKey("P2");
    public static final Key<ArrayUjoImpl,Date>    PRO_P3 = f.newKey("P3");
    public static final Key<ArrayUjoImpl,Float>   PRO_P4 = f.newKey("P4");

    /** Verify unique constants */
    static{
        f.lock();
    }

    /** Creates a new instance of UnifiedDataObjectImlp */
    public ArrayUjoImpl() {
    }

}
