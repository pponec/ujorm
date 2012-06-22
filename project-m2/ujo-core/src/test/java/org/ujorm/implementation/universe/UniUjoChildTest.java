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
 *
 * @author Pavel Ponec
 */
public class UniUjoChildTest extends MyTestCase {
    private static final Class CLASS = UniUjoChildTest.class;
    
    public UniUjoChildTest(String testName) {
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
        Float   o4 = new Float(123456.456);

        UniUjoChild ujb = new UniUjoChild();

        UniUjoChild.PRO_P0.setValue(ujb, o0);
        UniUjoChild.PRO_P1.setValue(ujb, o1);
        UniUjoChild.PRO_P2.setValue(ujb, o2);
        UniUjoChild.PRO_P3.setValue(ujb, o3);
        UniUjoChild.PRO_P4.addItem (ujb, o4);
        UniUjoChild.PRO_P5.setValue(ujb, o0);
        UniUjoChild.PRO_P6.setValue(ujb, o1);
        UniUjoChild.PRO_P7.setValue(ujb, o2);
        UniUjoChild.PRO_P8.setValue(ujb, o3);
        UniUjoChild.PRO_P9.addItem (ujb, o4);

        assertEquals(o0, UniUjoChild.PRO_P0.of(ujb));
        assertEquals(o1, UniUjoChild.PRO_P1.of(ujb));
        assertEquals(o2, UniUjoChild.PRO_P2.of(ujb));
        assertEquals(o3, UniUjoChild.PRO_P3.of(ujb));
        assertEquals(o4, UniUjoChild.PRO_P4.of(ujb,0));
        assertEquals(o0, UniUjoChild.PRO_P5.of(ujb));
        assertEquals(o1, UniUjoChild.PRO_P6.of(ujb));
        assertEquals(o2, UniUjoChild.PRO_P7.of(ujb));
        assertEquals(o3, UniUjoChild.PRO_P8.of(ujb));
        assertEquals(o4, UniUjoChild.PRO_P9.of(ujb,0));
    }

    public void testSpeedTime() throws Throwable {
        System.out.println("testTime: " + suite().toString());

        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);

        callGC();
        long time1 = System.currentTimeMillis();

        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            UniUjoChild ujb = new UniUjoChild();

            UniUjoChild.PRO_P0.setValue(ujb, o0);
            UniUjoChild.PRO_P1.setValue(ujb, o1);
            UniUjoChild.PRO_P2.setValue(ujb, o2);
            UniUjoChild.PRO_P3.setValue(ujb, o3);
            UniUjoChild.PRO_P4.addItem (ujb, o4);
            UniUjoChild.PRO_P5.setValue(ujb, o0);
            UniUjoChild.PRO_P6.setValue(ujb, o1);
            UniUjoChild.PRO_P7.setValue(ujb, o2);
            UniUjoChild.PRO_P8.setValue(ujb, o3);
            UniUjoChild.PRO_P9.addItem (ujb, o4);

            assertEquals(o0, UniUjoChild.PRO_P0.of(ujb));
            assertEquals(o1, UniUjoChild.PRO_P1.of(ujb));
            assertEquals(o2, UniUjoChild.PRO_P2.of(ujb));
            assertEquals(o3, UniUjoChild.PRO_P3.of(ujb));
            assertEquals(o4, UniUjoChild.PRO_P4.of(ujb,0));
            assertEquals(o0, UniUjoChild.PRO_P5.of(ujb));
            assertEquals(o1, UniUjoChild.PRO_P6.of(ujb));
            assertEquals(o2, UniUjoChild.PRO_P7.of(ujb));
            assertEquals(o3, UniUjoChild.PRO_P8.of(ujb));
            assertEquals(o4, UniUjoChild.PRO_P9.of(ujb,0));
        }

        printTime("TIME: ", time1);
    }

    public void testPropertyCount() throws Throwable {
        UniUjoBase  ujb1 = new UniUjoBase();
        UniUjoChild ujb2 = new UniUjoChild();

        assertEquals( 5, ujb1.readProperties().size());
        assertEquals(10, ujb2.readProperties().size());
    }

    /** */
    public void testGetProperties1() throws Throwable {
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
