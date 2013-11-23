/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm.implementation.map;

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;

/**
 * An UnifiedDataObject Implementation
 * @author Pavel Ponec
 */
public class MapUjoImplChild extends MapUjoImpl {

    private static final KeyFactory<MapUjoImplChild> f = KeyFactory.Builder.get(MapUjoImplChild.class);
    
    /** (Long) */
    public static final Key<MapUjoImplChild,Long>    PRO_P5 = f.newKey("P5");
    /** (Integer) */
    public static final Key<MapUjoImplChild,Integer> PRO_P6 = f.newKey("P6");
    /** (String) */
    public static final Key<MapUjoImplChild,String>  PRO_P7 = f.newKey("P7");
    /** (Date) */
    public static final Key<MapUjoImplChild,Date>    PRO_P8 = f.newKey("P8");
    /** (Float) */
    public static final Key<MapUjoImplChild,Float>   PRO_P9 = f.newKey("P9");

    static {
        f.lock();
    }
    
    /** Creates a new instance of UnifiedDataObjectImlp */
    public MapUjoImplChild() {
    }
    

    
}
