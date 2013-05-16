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
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class MapUjoImpl extends MapUjo {

    private static final KeyFactory<MapUjoImpl> f = KeyFactory.Builder.get(MapUjoImpl.class);
    
    /** (Long) */
    public static final Key<MapUjoImpl,Long>    PRO_P0 = f.newKey("P0");
    /** (Integer) */
    public static final Key<MapUjoImpl,Integer> PRO_P1 = f.newKey("P1");
    /** (String) */
    public static final Key<MapUjoImpl,String>  PRO_P2 = f.newKey("P2");
    /** (Date) */
    public static final Key<MapUjoImpl,Date>    PRO_P3 = f.newKey("P3");
    /** (Float) */
    public static final Key<MapUjoImpl,Float>   PRO_P4 = f.newKey("P4");

    static {
        f.lock();
    }
    
    /** Creates a new instance of UnifiedDataObjectImlp */
    public MapUjoImpl() {
    }
    
}
