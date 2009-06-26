/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. June 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.map;

import java.util.Date;
import org.ujoframework.UjoProperty;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class MapUjoImplChild extends MapUjoImpl {
    
    /** (Long) */
    public static final UjoProperty<MapUjoImplChild,Long>    PRO_P5 = newProperty("P5", Long.class);
    /** (Integer) */
    public static final UjoProperty<MapUjoImplChild,Integer> PRO_P6 = newProperty("P6", Integer.class);
    /** (String) */
    public static final UjoProperty<MapUjoImplChild,String>  PRO_P7 = newProperty("P7", String.class);
    /** (Date) */
    public static final UjoProperty<MapUjoImplChild,Date>    PRO_P8 = newProperty("P8", Date.class);
    /** (Float) */
    public static final UjoProperty<MapUjoImplChild,Float>   PRO_P9 = newProperty("P9", Float.class);
    
    
    
    /** Creates a new instance of UnifiedDataObjectImlp */
    public MapUjoImplChild() {
    }
    

    
}
