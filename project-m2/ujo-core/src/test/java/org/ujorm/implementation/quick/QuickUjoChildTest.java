/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:31
 */

package org.ujorm.implementation.quick;


import java.awt.Color;
import java.util.Date;
import junit.framework.*;
import org.ujorm.MyTestCase;
import org.ujorm.KeyList;

/**
 *
 * @author Pavel Ponec
 */
public class QuickUjoChildTest extends MyTestCase {
    private final static Class CLASS = QuickUjoChildTest.class;
    
    public QuickUjoChildTest(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        return new TestSuite(CLASS);
    }
    
    @Override
    protected void setUp() throws Exception {
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of readValue method, of class org.ujorm.mapImlp.AUnifiedDataObject.
     */
    public void testReadWrite() throws Throwable {
        System.out.println("testReadWrite");
        
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Class<?> o4 = Color.class;
        
        QuickUjoImplChild ujb = new QuickUjoImplChild();
        
        QuickUjoImplChild.PRO_P0.setValue(ujb, o0);
        QuickUjoImplChild.PRO_P1.setValue(ujb, o1);
        QuickUjoImplChild.PRO_P2.setValue(ujb, o2);
        QuickUjoImplChild.PRO_P3.setValue(ujb, o3);
        QuickUjoImplChild.PRO_P4.setValue(ujb, o4);
        QuickUjoImplChild.PRO_P5.setValue(ujb, o0);
        QuickUjoImplChild.PRO_P6.setValue(ujb, o1);
        QuickUjoImplChild.PRO_P7.setValue(ujb, o2);
        QuickUjoImplChild.PRO_P8.setValue(ujb, o3);
        QuickUjoImplChild.PRO_P9.setValue(ujb, o4);
        
        assertEquals(o0, QuickUjoImplChild.PRO_P0.of(ujb));
        assertEquals(o1, QuickUjoImplChild.PRO_P1.of(ujb));
        assertEquals(o2, QuickUjoImplChild.PRO_P2.of(ujb));
        assertEquals(o3, QuickUjoImplChild.PRO_P3.of(ujb));
        assertEquals(o4, QuickUjoImplChild.PRO_P4.of(ujb));
        assertEquals(o0, QuickUjoImplChild.PRO_P5.of(ujb));
        assertEquals(o1, QuickUjoImplChild.PRO_P6.of(ujb));
        assertEquals(o2, QuickUjoImplChild.PRO_P7.of(ujb));
        assertEquals(o3, QuickUjoImplChild.PRO_P8.of(ujb));
        assertEquals(o4, QuickUjoImplChild.PRO_P9.of(ujb));
    }
    
    public void testSpeedTime() throws Throwable {
        System.out.println("testTime: " + suite().toString());
        
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Class<?> o4 = Color.class;
        Object result;
        
        callGC();
        long time1 = System.currentTimeMillis();
        
        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            QuickUjoImplChild ujb = new QuickUjoImplChild();
            
            QuickUjoImplChild.PRO_P0.setValue(ujb, o0);
            QuickUjoImplChild.PRO_P1.setValue(ujb, o1);
            QuickUjoImplChild.PRO_P2.setValue(ujb, o2);
            QuickUjoImplChild.PRO_P3.setValue(ujb, o3);
            QuickUjoImplChild.PRO_P4.setValue(ujb, o4);
            QuickUjoImplChild.PRO_P5.setValue(ujb, o0);
            QuickUjoImplChild.PRO_P6.setValue(ujb, o1);
            QuickUjoImplChild.PRO_P7.setValue(ujb, o2);
            QuickUjoImplChild.PRO_P8.setValue(ujb, o3);
            QuickUjoImplChild.PRO_P9.setValue(ujb, o4);
            
            assertEquals(o0, QuickUjoImplChild.PRO_P0.of(ujb));
            assertEquals(o1, QuickUjoImplChild.PRO_P1.of(ujb));
            assertEquals(o2, QuickUjoImplChild.PRO_P2.of(ujb));
            assertEquals(o3, QuickUjoImplChild.PRO_P3.of(ujb));
            assertEquals(o4, QuickUjoImplChild.PRO_P4.of(ujb));
            assertEquals(o0, QuickUjoImplChild.PRO_P5.of(ujb));
            assertEquals(o1, QuickUjoImplChild.PRO_P6.of(ujb));
            assertEquals(o2, QuickUjoImplChild.PRO_P7.of(ujb));
            assertEquals(o3, QuickUjoImplChild.PRO_P8.of(ujb));
            assertEquals(o4, QuickUjoImplChild.PRO_P9.of(ujb));
            
        }
        
        long time2 = System.currentTimeMillis();
        System.out.println("TIME: " + (time2-time1)/1000f + " [sec]");
    }
    
    public void testPropertyCount() throws Throwable {
        QuickUjoImpl ujb1 = new QuickUjoImpl();
        QuickUjoImplChild  ujb2 = new QuickUjoImplChild();
        
        assertEquals( 6, ujb1.readKeys().size());
        assertEquals(12, ujb2.readKeys().size());
    }

    public void testPropertyName() throws Throwable {
        QuickUjoImpl ujb1 = new QuickUjoImpl();
        QuickUjoImplChild  ujb2 = new QuickUjoImplChild();

        assertEquals( "PRO_P0", QuickUjoImplChild.PRO_P0.getName());
        assertEquals( "PRO_P4", QuickUjoImplChild.PRO_P4.getName());
        assertEquals( "PRO_P5", QuickUjoImplChild.PRO_P5.getName());
        assertEquals( "PRO_P6", QuickUjoImplChild.PRO_P6.getName());
    }


    
    /** */
    public void testGetProperties1() throws Throwable {
        QuickUjoImpl ujb1 = new QuickUjoImpl();
        KeyList props = ujb1.readKeys();
        
        assertEquals(QuickUjoImplChild.PRO_P0, props.get(0));
        assertEquals(QuickUjoImplChild.PRO_P1, props.get(1));
        assertEquals(QuickUjoImplChild.PRO_P2, props.get(2));
        assertEquals(QuickUjoImplChild.PRO_P3, props.get(3));
        assertEquals(QuickUjoImplChild.PRO_P4, props.get(4));
    }
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
}
