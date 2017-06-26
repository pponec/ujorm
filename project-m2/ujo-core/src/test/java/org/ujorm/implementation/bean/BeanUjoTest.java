/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:31
 */

package org.ujorm.implementation.bean;


import java.util.Date;
import junit.framework.*;
import org.ujorm.MyTestCase;

/**
 *
 * @author Pavel Ponec
 */
public class BeanUjoTest extends MyTestCase {
    
    public BeanUjoTest(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(BeanUjoTest.class);
        return suite;
    }
    
    /**
     * Test of readValue method, of class org.ujorm.hmapImlp.AUnifiedDataObject.
     */
    public void testReadWrite() throws Throwable {
        System.out.println("testReadWrite");
        
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);
        
        BeanUjoImpl ujb = new BeanUjoImpl();
        
        BeanUjoImpl.PRO_P0.setValue(ujb, o0);
        BeanUjoImpl.PRO_P1.setValue(ujb, o1);
        BeanUjoImpl.PRO_P2.setValue(ujb, o2);
        BeanUjoImpl.PRO_P3.setValue(ujb, o3);
        BeanUjoImpl.PRO_P4.setValue(ujb, o4);
        BeanUjoImpl.PRO_P0.setValue(ujb, o0);
        BeanUjoImpl.PRO_P1.setValue(ujb, o1);
        BeanUjoImpl.PRO_P2.setValue(ujb, o2);
        BeanUjoImpl.PRO_P3.setValue(ujb, o3);
        BeanUjoImpl.PRO_P4.setValue(ujb, o4);
        
        assertEquals(o0, BeanUjoImpl.PRO_P0.of(ujb));
        assertEquals(o1, BeanUjoImpl.PRO_P1.of(ujb));
        assertEquals(o2, BeanUjoImpl.PRO_P2.of(ujb));
        assertEquals(o3, BeanUjoImpl.PRO_P3.of(ujb));
        assertEquals(o4, BeanUjoImpl.PRO_P4.of(ujb));
        assertEquals(o0, BeanUjoImpl.PRO_P0.of(ujb));
        assertEquals(o1, BeanUjoImpl.PRO_P1.of(ujb));
        assertEquals(o2, BeanUjoImpl.PRO_P2.of(ujb));
        assertEquals(o3, BeanUjoImpl.PRO_P3.of(ujb));
        assertEquals(o4, BeanUjoImpl.PRO_P4.of(ujb));
        
    }

    public void XXX_testSpeedTime_warm() throws Throwable {
        testSpeedTime();
    }
    
    public void testSpeedTime() throws Throwable {
        System.out.println("U1:testSpeedTime: " + suite().toString());
        
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);
        Object result;
        
        callGC();
        long time1 = System.currentTimeMillis();
        
        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            BeanUjoImpl ujb = new BeanUjoImpl();
            
            BeanUjoImpl.PRO_P0.setValue(ujb, o0);
            BeanUjoImpl.PRO_P1.setValue(ujb, o1);
            BeanUjoImpl.PRO_P2.setValue(ujb, o2);
            BeanUjoImpl.PRO_P3.setValue(ujb, o3);
            BeanUjoImpl.PRO_P4.setValue(ujb, o4);
            BeanUjoImpl.PRO_P0.setValue(ujb, o0);
            BeanUjoImpl.PRO_P1.setValue(ujb, o1);
            BeanUjoImpl.PRO_P2.setValue(ujb, o2);
            BeanUjoImpl.PRO_P3.setValue(ujb, o3);
            BeanUjoImpl.PRO_P4.setValue(ujb, o4);
            
            assertEquals(o0, BeanUjoImpl.PRO_P0.of(ujb));
            assertEquals(o1, BeanUjoImpl.PRO_P1.of(ujb));
            assertEquals(o2, BeanUjoImpl.PRO_P2.of(ujb));
            assertEquals(o3, BeanUjoImpl.PRO_P3.of(ujb));
            assertEquals(o4, BeanUjoImpl.PRO_P4.of(ujb));
            assertEquals(o0, BeanUjoImpl.PRO_P0.of(ujb));
            assertEquals(o1, BeanUjoImpl.PRO_P1.of(ujb));
            assertEquals(o2, BeanUjoImpl.PRO_P2.of(ujb));
            assertEquals(o3, BeanUjoImpl.PRO_P3.of(ujb));
            assertEquals(o4, BeanUjoImpl.PRO_P4.of(ujb));
        }
        long time2 = System.currentTimeMillis();
        
        System.out.println("U1:TIME: " + (time2-time1)/1000f + " [sec]");
    }
    
    public void testSpeedTime2() throws Throwable {
        System.out.println("U2:testSpeedTime (child): " + suite().toString());
        
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);
        Object result;
        
        callGC();
        long time1 = System.currentTimeMillis();
        
        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            BeanUjoImplChild ujb = new BeanUjoImplChild();
            
            BeanUjoImplChild.PRO_P0.setValue(ujb, o0);
            BeanUjoImplChild.PRO_P1.setValue(ujb, o1);
            BeanUjoImplChild.PRO_P2.setValue(ujb, o2);
            BeanUjoImplChild.PRO_P3.setValue(ujb, o3);
            BeanUjoImplChild.PRO_P4.setValue(ujb, o4);
            BeanUjoImplChild.PRO_P5.setValue(ujb, o0);
            BeanUjoImplChild.PRO_P6.setValue(ujb, o1);
            BeanUjoImplChild.PRO_P7.setValue(ujb, o2);
            BeanUjoImplChild.PRO_P8.setValue(ujb, o3);
            BeanUjoImplChild.PRO_P9.setValue(ujb, o4);
            
            assertEquals(o0, BeanUjoImplChild.PRO_P0.of(ujb));
            assertEquals(o1, BeanUjoImplChild.PRO_P1.of(ujb));
            assertEquals(o2, BeanUjoImplChild.PRO_P2.of(ujb));
            assertEquals(o3, BeanUjoImplChild.PRO_P3.of(ujb));
            assertEquals(o4, BeanUjoImplChild.PRO_P4.of(ujb));
            assertEquals(o0, BeanUjoImplChild.PRO_P5.of(ujb));
            assertEquals(o1, BeanUjoImplChild.PRO_P6.of(ujb));
            assertEquals(o2, BeanUjoImplChild.PRO_P7.of(ujb));
            assertEquals(o3, BeanUjoImplChild.PRO_P8.of(ujb));
            assertEquals(o4, BeanUjoImplChild.PRO_P9.of(ujb));
        }
        long time2 = System.currentTimeMillis();
        
        System.out.println("U2:TIME: " + (time2-time1)/1000f + " [sec]");
    }
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
