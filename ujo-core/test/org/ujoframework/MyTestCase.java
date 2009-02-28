/*
 * MyTestCase.java
 * JUnit based test
 *
 * Created on 8. èerven 2007, 23:42
 */

package org.ujoframework;

import java.util.Arrays;
import java.util.List;
import junit.framework.*;
import org.ujoframework.extensions.UjoTextable;

/**
 *
 * @author Pavel Ponec
 */
abstract public class MyTestCase extends TestCase {
    
    public MyTestCase(String testName) {
        super(testName);
    }
    
    /** A TimeTest loop count. */
    public int getTimeLoopCount() {
        //return 5*1000*1000;
        return   5*1000;
    }
    
    /** Modify a Test directory */
    public String getTestDir() {
        return "c:/temp/";
    }
    
    /**
     * Compare two Ujo objects.
     */
    public static void assertEquals(Ujo expected, Ujo actual) {
        
        if (expected==actual) { return; }
        assertEquals(expected.getClass(), expected.getClass());
        
        UjoProperty[] properties = expected.readProperties();
        
        if (expected instanceof UjoTextable) {
            for (int i=properties.length-1; i>=0; i--) {
                UjoProperty property = properties[i];
                String o1 = String.valueOf(((UjoTextable)expected).readValueString(property, null));
                String o2 = String.valueOf(((UjoTextable)actual  ).readValueString(property, null));
                assertEquals("Property \"" + property.getName() + "\"", o1, o2);
            }
        }
       
        
        for (int i=properties.length-1; i>=0; i--) {
            UjoProperty property = properties[i];
            Object o1 = expected.readValue(property);
            Object o2 = actual  .readValue(property);
            
            String item = "Property \"" + property.getName() + "\"";
            if (byte[].class.equals(property.getType())) {
                assertEquals(item, (byte[]) o1, (byte[]) o2);
            } else if (char[].class.equals(property.getType())) {
                assertEquals(item, (char[]) o1, (char[]) o2);
            } else if (property.isTypeOf(List.class)) {
                assertEquals(item, (List) o1, (List) o2);
            } else {
                assertEquals(item, o1, o2);
            }
        }
    }
    
    /**
     * Compare two Ujo objects.
     */
    public static void assertEquals(String item, byte[] expected, byte[] actual) {
        assertTrue(item, Arrays.equals(expected, actual));
    }
    
    /**
     * Compare two Ujo objects.
     */
    public static void assertEquals(String item, char[] expected, char[] actual) {
        assertTrue(item, Arrays.equals(expected, actual));
    }
    
    /**
     * Compare two Ujo objects.
     */
    public static void assertEquals(String item, List expected, List actual) {
        if (expected==actual) { return; }
        assertEquals(item, expected.size(), actual.size());
        
        if (item.endsWith("\"")) {
            item = item.substring(0, item.length()-1);
        }
        
        for (int i=0; i<expected.size(); i++) {
            Object oe = expected.get(i);
            Object oa = actual.get(i);
            if (oe==oa) { continue; }
            assertEquals(item+"["+i+"\"]", oe, oa);
        }
    }
    
    /** Call GC and sleep .025 ms */
    protected void callGC() {
        System.gc();
        try {
            Thread.sleep(250);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    protected void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Throwable e) {
            ;
        }
    }
}
