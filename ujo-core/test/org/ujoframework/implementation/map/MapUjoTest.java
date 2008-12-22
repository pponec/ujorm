/*
 * HUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. �erven 2007, 23:00
 */

package org.ujoframework.implementation.map;

import java.util.Date;
import junit.framework.*;
import org.ujoframework.MyTestCase;

/**
 * HashMap Unified Data Object Test
 * @author pavel
 */
public class MapUjoTest extends MyTestCase {
    
    public MapUjoTest(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(MapUjoTest.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testSpeedTime() throws Throwable {
        System.out.println("M1:testTime: " + suite().toString());
        
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);
        Object result;
        
        callGC();
        long time1 = System.currentTimeMillis();
        
        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            MapUjoImpl ujb = new MapUjoImpl();
            
            MapUjoImpl.PRO_P0.setValue(ujb, o0);
            MapUjoImpl.PRO_P1.setValue(ujb, o1);
            MapUjoImpl.PRO_P2.setValue(ujb, o2);
            MapUjoImpl.PRO_P3.setValue(ujb, o3);
            MapUjoImpl.PRO_P4.setValue(ujb, o4);
            MapUjoImpl.PRO_P0.setValue(ujb, o0);
            MapUjoImpl.PRO_P1.setValue(ujb, o1);
            MapUjoImpl.PRO_P2.setValue(ujb, o2);
            MapUjoImpl.PRO_P3.setValue(ujb, o3);
            MapUjoImpl.PRO_P4.setValue(ujb, o4);
            
            assertEquals(o0, MapUjoImpl.PRO_P0.getValue(ujb));
            assertEquals(o1, MapUjoImpl.PRO_P1.getValue(ujb));
            assertEquals(o2, MapUjoImpl.PRO_P2.getValue(ujb));
            assertEquals(o3, MapUjoImpl.PRO_P3.getValue(ujb));
            assertEquals(o4, MapUjoImpl.PRO_P4.getValue(ujb));
            assertEquals(o0, MapUjoImpl.PRO_P0.getValue(ujb));
            assertEquals(o1, MapUjoImpl.PRO_P1.getValue(ujb));
            assertEquals(o2, MapUjoImpl.PRO_P2.getValue(ujb));
            assertEquals(o3, MapUjoImpl.PRO_P3.getValue(ujb));
            assertEquals(o4, MapUjoImpl.PRO_P4.getValue(ujb));
        }
        long time2 = System.currentTimeMillis();
        
        System.out.println("M1:TIME: " + (time2-time1)/1000f + " [sec]");
    }
        
    
    public void testSpeedTimeChild() throws Throwable {
        System.out.println("M2:testTime (child): " + suite().toString());
        
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);
        Object result;
        
        callGC();
        long time1 = System.currentTimeMillis();
        
        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            MapUjoImplChild ujb = new MapUjoImplChild();
            
            MapUjoImplChild.PRO_P0.setValue(ujb, o0);
            MapUjoImplChild.PRO_P1.setValue(ujb, o1);
            MapUjoImplChild.PRO_P2.setValue(ujb, o2);
            MapUjoImplChild.PRO_P3.setValue(ujb, o3);
            MapUjoImplChild.PRO_P4.setValue(ujb, o4);
            MapUjoImplChild.PRO_P5.setValue(ujb, o0);
            MapUjoImplChild.PRO_P6.setValue(ujb, o1);
            MapUjoImplChild.PRO_P7.setValue(ujb, o2);
            MapUjoImplChild.PRO_P8.setValue(ujb, o3);
            MapUjoImplChild.PRO_P9.setValue(ujb, o4);
            
            assertEquals(o0, MapUjoImplChild.PRO_P0.getValue(ujb));
            assertEquals(o1, MapUjoImplChild.PRO_P1.getValue(ujb));
            assertEquals(o2, MapUjoImplChild.PRO_P2.getValue(ujb));
            assertEquals(o3, MapUjoImplChild.PRO_P3.getValue(ujb));
            assertEquals(o4, MapUjoImplChild.PRO_P4.getValue(ujb));
            assertEquals(o0, MapUjoImplChild.PRO_P5.getValue(ujb));
            assertEquals(o1, MapUjoImplChild.PRO_P6.getValue(ujb));
            assertEquals(o2, MapUjoImplChild.PRO_P7.getValue(ujb));
            assertEquals(o3, MapUjoImplChild.PRO_P8.getValue(ujb));
            assertEquals(o4, MapUjoImplChild.PRO_P9.getValue(ujb));
        }
        long time2 = System.currentTimeMillis();
        
        System.out.println("M2:TIME: " + (time2-time1)/1000f + " [sec]");
    }
    
    
    public void testPropertyCount() throws Throwable {
        MapUjoImpl ujb1 = new MapUjoImpl();
        MapUjoImplChild  ujb2 = new MapUjoImplChild();
        
        assertEquals( 5, ujb1.readProperties().length);
        assertEquals(10, ujb2.readProperties().length);
    }
    
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
