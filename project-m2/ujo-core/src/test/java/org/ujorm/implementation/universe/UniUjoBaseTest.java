/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:31
 */

package org.ujorm.implementation.universe;


import java.util.Date;
import junit.framework.*;
import org.ujorm.MyTestCase;
import org.ujorm.UjoPropertyList;

/**
 * TextCase
 * @author Pavel Ponec
 */
public class UniUjoBaseTest extends MyTestCase {
    private static final Class CLASS = UniUjoBaseTest.class;

    
    public UniUjoBaseTest(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(CLASS);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of readValue method,
     */
    public void XtestReadWrite() throws Throwable {
        System.out.println("testReadWrite");
        
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);
        
        UniUjoBase ujb = new UniUjoBase();
        
        UniUjoBase.PRO_P0.setValue(ujb, o0);
        UniUjoBase.PRO_P1.setValue(ujb, o1);
        UniUjoBase.PRO_P2.setValue(ujb, o2);
        UniUjoBase.PRO_P3.setValue(ujb, o3);
        UniUjoBase.PRO_P4.setValue(ujb, o4);
        
        assertEquals(o0, UniUjoBase.PRO_P0.of(ujb));
        assertEquals(o1, UniUjoBase.PRO_P1.of(ujb));
        assertEquals(o2, UniUjoBase.PRO_P2.of(ujb));
        assertEquals(o3, UniUjoBase.PRO_P3.of(ujb));
        assertEquals(o4, UniUjoBase.PRO_P4.of(ujb));
    }
    
    public void XtestSpeedTime() throws Throwable {
        System.out.println("A1:testSpeedTime: " + suite().toString());
        
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);
        Object result;
        
        callGC();
        long time1 = System.currentTimeMillis();
        
        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            UniUjoBase ujb = new UniUjoBase();
            UniUjoBase.PRO_P0.setValue(ujb, o0);
            UniUjoBase.PRO_P1.setValue(ujb, o1);
            UniUjoBase.PRO_P2.setValue(ujb, o2);
            UniUjoBase.PRO_P3.setValue(ujb, o3);
            UniUjoBase.PRO_P4.setValue(ujb, o4);
            UniUjoBase.PRO_P0.setValue(ujb, o0);
            UniUjoBase.PRO_P1.setValue(ujb, o1);
            UniUjoBase.PRO_P2.setValue(ujb, o2);
            UniUjoBase.PRO_P3.setValue(ujb, o3);
            UniUjoBase.PRO_P4.setValue(ujb, o4);
            
            assertEquals(o0, UniUjoBase.PRO_P0.getValue(ujb));
            assertEquals(o1, UniUjoBase.PRO_P1.getValue(ujb));
            assertEquals(o2, UniUjoBase.PRO_P2.getValue(ujb));
            assertEquals(o3, UniUjoBase.PRO_P3.getValue(ujb));
            assertEquals(o4, UniUjoBase.PRO_P4.getValue(ujb));
            assertEquals(o0, UniUjoBase.PRO_P0.getValue(ujb));
            assertEquals(o1, UniUjoBase.PRO_P1.getValue(ujb));
            assertEquals(o2, UniUjoBase.PRO_P2.getValue(ujb));
            assertEquals(o3, UniUjoBase.PRO_P3.getValue(ujb));
            assertEquals(o4, UniUjoBase.PRO_P4.getValue(ujb));
        }
        printTime("A1:TIME: ", time1);
    }
    
        
    
    /** Test of properties */
    public void XtestGetProperties1() throws Throwable {
        UniUjoBase ujb1 = new UniUjoBase();
        UjoPropertyList props = ujb1.readProperties();
        
        assertEquals(UniUjoBase.PRO_P0, props.get(0));
        assertEquals(UniUjoBase.PRO_P1, props.get(1));
        assertEquals(UniUjoBase.PRO_P2, props.get(2));
        assertEquals(UniUjoBase.PRO_P3, props.get(3));
        assertEquals(UniUjoBase.PRO_P4, props.get(4));
    }
    
    /** Test of properties */
    public void testDummy() throws Throwable {
    }

    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
