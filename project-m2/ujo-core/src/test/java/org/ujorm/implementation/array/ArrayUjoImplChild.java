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

/**
 * An UnifiedDataObject Imlpementation
 * @author Pavel Ponec
 */
public class ArrayUjoImplChild extends ArrayUjoImpl {
    
    /** Incrementator */
    protected static int propertyCount = ArrayUjoImpl.propertyCount;

    public static final Key <ArrayUjoImplChild, Long>    PRO_P5 = newProperty("P5", Long.class, propertyCount++);
    public static final Key <ArrayUjoImplChild, Integer> PRO_P6 = newProperty("P6", Integer.class, propertyCount++);
    public static final Key <ArrayUjoImplChild, String>  PRO_P7 = newProperty("P7", String.class, propertyCount++);
    public static final Key <ArrayUjoImplChild, Date>    PRO_P8 = newProperty("P8", Date.class, propertyCount++);
    public static final Key <ArrayUjoImplChild, Float>   PRO_P9 = newProperty("P9", Float.class, propertyCount++);
    /** Verify unique constants */
    static{init(ArrayUjoImplChild.class,true);}
    
    /** Creates a new instance of UnifiedDataObjectImlp */
    public ArrayUjoImplChild() {
    }
    
     /** Returns a count of keys. */
    @Override
     public int readPropertyCount() {
         return propertyCount;
     }
    
    
    
}
