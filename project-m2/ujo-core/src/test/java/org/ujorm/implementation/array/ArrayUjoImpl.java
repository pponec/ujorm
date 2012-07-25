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
public class ArrayUjoImpl extends ArrayUjo {
    
    /** An Incrementator. Use a new counter for each subclass by sample:
     *<pre class="pre">
     * <span class="java-block-comment">&#47&#42&#42 An Incrementator. Use a new counter for each subclass. &#42&#47</span>
     * <span class="java-keywords">protected</span> <span class="java-keywords">static</span> <span class="java-keywords">int</span> propertyCount = [SuperClass].propertyCount;
     *</pre>
     */
    protected static int propertyCount = ArrayUjo.propertyCount;
    
    public static final Key<ArrayUjoImpl,Long>    PRO_P0 = newProperty("P0", Long.class, propertyCount++);
    public static final Key<ArrayUjoImpl,Integer> PRO_P1 = newProperty("P1", Integer.class, propertyCount++);
    public static final Key<ArrayUjoImpl,String>  PRO_P2 = newProperty("P2", String.class, propertyCount++);
    public static final Key<ArrayUjoImpl,Date>    PRO_P3 = newProperty("P3", Date.class, propertyCount++);
    public static final Key<ArrayUjoImpl,Float>   PRO_P4 = newProperty("P4", Float.class, propertyCount++);

    /** Verify unique constants */
    static{
        init(ArrayUjoImpl.class,true);
    }

    /** Creates a new instance of UnifiedDataObjectImlp */
    public ArrayUjoImpl() {
    }
    
     /** Returns a count of properties. */
    @Override
     public int readPropertyCount() {
         return propertyCount;
     }
}
