/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. èerven 2007, 23:31
 */

package org.ujoframework.implementation.array;


import java.util.Date;
import junit.framework.*;
import org.ujoframework.MyTestCase;
import org.ujoframework.UjoProperty;

/**
 *
 * @author Pavel Ponec
 */
public class ArrayUjoChildTest extends MyTestCase {
    
    public ArrayUjoChildTest(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        return new TestSuite(ArrayUjoChildTest.class);
    }
    
    @Override
    protected void setUp() throws Exception {
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of readValue method, of class org.ujoframework.mapImlp.AUnifiedDataObject.
     */
    public void testReadWrite() throws Throwable {
        System.out.println("testReadWrite");
        
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);
        
        ArrayUjoImplChild ujb = new ArrayUjoImplChild();
        
        ujb.PRO_P0.setValue(ujb, o0);
        ujb.PRO_P1.setValue(ujb, o1);
        ujb.PRO_P2.setValue(ujb, o2);
        ujb.PRO_P3.setValue(ujb, o3);
        ujb.PRO_P4.setValue(ujb, o4);
        ujb.PRO_P5.setValue(ujb, o0);
        ujb.PRO_P6.setValue(ujb, o1);
        ujb.PRO_P7.setValue(ujb, o2);
        ujb.PRO_P8.setValue(ujb, o3);
        ujb.PRO_P9.setValue(ujb, o4);
        
        assertEquals(o0, ujb.PRO_P0.of(ujb));
        assertEquals(o1, ujb.PRO_P1.of(ujb));
        assertEquals(o2, ujb.PRO_P2.of(ujb));
        assertEquals(o3, ujb.PRO_P3.of(ujb));
        assertEquals(o4, ujb.PRO_P4.of(ujb));
        assertEquals(o0, ujb.PRO_P5.of(ujb));
        assertEquals(o1, ujb.PRO_P6.of(ujb));
        assertEquals(o2, ujb.PRO_P7.of(ujb));
        assertEquals(o3, ujb.PRO_P8.of(ujb));
        assertEquals(o4, ujb.PRO_P9.of(ujb));
    }
    
    public void testSpeedTime() throws Throwable {
        System.out.println("testTime: " + suite().toString());
        
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);
        Object result;
        
        callGC();
        long time1 = System.currentTimeMillis();
        
        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            ArrayUjoImplChild ujb = new ArrayUjoImplChild();
            
            ujb.PRO_P0.setValue(ujb, o0);
            ujb.PRO_P1.setValue(ujb, o1);
            ujb.PRO_P2.setValue(ujb, o2);
            ujb.PRO_P3.setValue(ujb, o3);
            ujb.PRO_P4.setValue(ujb, o4);
            ujb.PRO_P5.setValue(ujb, o0);
            ujb.PRO_P6.setValue(ujb, o1);
            ujb.PRO_P7.setValue(ujb, o2);
            ujb.PRO_P8.setValue(ujb, o3);
            ujb.PRO_P9.setValue(ujb, o4);
            
            assertEquals(o0, ujb.PRO_P0.of(ujb));
            assertEquals(o1, ujb.PRO_P1.of(ujb));
            assertEquals(o2, ujb.PRO_P2.of(ujb));
            assertEquals(o3, ujb.PRO_P3.of(ujb));
            assertEquals(o4, ujb.PRO_P4.of(ujb));
            assertEquals(o0, ujb.PRO_P5.of(ujb));
            assertEquals(o1, ujb.PRO_P6.of(ujb));
            assertEquals(o2, ujb.PRO_P7.of(ujb));
            assertEquals(o3, ujb.PRO_P8.of(ujb));
            assertEquals(o4, ujb.PRO_P9.of(ujb));
            
        }
        
        long time2 = System.currentTimeMillis();
        System.out.println("TIME: " + (time2-time1)/1000f + " [sec]");
    }
    
    public void testPropertyCount() throws Throwable {
        ArrayUjoImpl ujb1 = new ArrayUjoImpl();
        ArrayUjoImplChild  ujb2 = new ArrayUjoImplChild();
        
        assertEquals( 5, ujb1.readPropertyCount());
        assertEquals(10, ujb2.readPropertyCount());
    }
    
    /** */
    public void testGetProperties1() throws Throwable {
        ArrayUjoImpl ujb1 = new ArrayUjoImpl();
        UjoProperty[] props = ujb1.readProperties();
        
        assertEquals(ujb1.PRO_P0, props[0]);
        assertEquals(ujb1.PRO_P1, props[1]);
        assertEquals(ujb1.PRO_P2, props[2]);
        assertEquals(ujb1.PRO_P3, props[3]);
        assertEquals(ujb1.PRO_P4, props[4]);
    }
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
}
