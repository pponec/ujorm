/*
 * Person.java
 *
 * Created on 9. June 2007, 22:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package samples.array;

import java.util.Date;
import org.ujoframework.Ujo;
import org.ujoframework.core.UjoManager;
import org.ujoframework.implementation.array.ArrayProperty;
import org.ujoframework.implementation.array.ArrayUjo;

public class Person extends ArrayUjo {
    
    /** An Incrementator. Use a new counter for each subclass by sample. */
    protected static int propertyCount = ArrayUjo.propertyCount;
    
    public static final ArrayProperty NAME  = newProperty("name", String.class , propertyCount++);
    public static final ArrayProperty MALE  = newProperty("male", Boolean.class, propertyCount++);
    public static final ArrayProperty BIRTH = newProperty("birth", Date.class  , propertyCount++);
    
    

    /** Equals */
    public boolean equals(Object obj) {
        return UjoManager.getInstance().equalsUjo(this, (Ujo) obj );
    }    

    /** Equals */    
    @Override
    public Object clone(int depth, Object context) {
        return UjoManager.getInstance().clone(this, depth, context);
    }    

    public int readPropertyCount() {
        return propertyCount;
    }
    
    
}
