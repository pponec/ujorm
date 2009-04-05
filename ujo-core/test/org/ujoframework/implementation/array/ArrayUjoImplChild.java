/*
 * UnifiedDataObjectImlp.java
 *
 * Created on 3. èerven 2007, 23:00
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.ujoframework.implementation.array;

import java.util.Date;

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class ArrayUjoImplChild extends ArrayUjoImpl {
    
    /** Incrementator */
    protected static int propertyCount = ArrayUjoImpl.propertyCount;

    public static final ArrayProperty <ArrayUjoImplChild, Long>    PRO_P5 = newProperty("P5", Long.class, propertyCount++);
    public static final ArrayProperty <ArrayUjoImplChild, Integer> PRO_P6 = newProperty("P6", Integer.class, propertyCount++);
    public static final ArrayProperty <ArrayUjoImplChild, String>  PRO_P7 = newProperty("P7", String.class, propertyCount++);
    public static final ArrayProperty <ArrayUjoImplChild, Date>    PRO_P8 = newProperty("P8", Date.class, propertyCount++);
    public static final ArrayProperty <ArrayUjoImplChild, Float>   PRO_P9 = newProperty("P9", Float.class, propertyCount++);
    
    /** Creates a new instance of UnifiedDataObjectImlp */
    public ArrayUjoImplChild() {
    }
    
     /** Returns a count of properties. */
    @Override
     public int readPropertyCount() {
         return propertyCount;
     }
    
    
    
}
