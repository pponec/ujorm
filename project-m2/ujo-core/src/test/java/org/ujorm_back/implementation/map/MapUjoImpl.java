/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujorm_back.implementation.map;

import java.util.Date;
import org.ujorm.extensions.Property;
import org.ujorm.implementation.map.MapUjo;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class MapUjoImpl extends MapUjo {
    
    /** (Long) */
    public static final Property<MapUjoImpl,Long>    PRO_P0 = newProperty("P0", Long.class);
    /** (Integer) */
    public static final Property<MapUjoImpl,Integer> PRO_P1 = newProperty("P1", Integer.class);
    /** (String) */
    public static final Property<MapUjoImpl,String>  PRO_P2 = newProperty("P2", String.class);
    /** (Date) */
    public static final Property<MapUjoImpl,Date>    PRO_P3 = newProperty("P3", Date.class);
    /** (Float) */
    public static final Property<MapUjoImpl,Float>   PRO_P4 = newProperty("P4", Float.class);
    
    
    /** Creates a new instance of UnifiedDataObjectImlp */
    public MapUjoImpl() {
    }
    
}
