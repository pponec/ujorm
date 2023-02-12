/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:31
 */

package org.ujorm.implementation.universe;


import java.util.Date;
import junit.framework.*;
import org.ujorm.KeyList;
import org.ujorm.MyTestCase;

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

        assertEquals("proP0", UniUjoChild.PRO_P0.getName());
        assertEquals("proP1", UniUjoChild.PRO_P1.getName());
        assertEquals("proP2", UniUjoChild.PRO_P2.getName());
        assertEquals("proP3", UniUjoChild.PRO_P3.getName());
        assertEquals("proP4", UniUjoChild.PRO_P4.getName());
        assertEquals("P5"   , UniUjoChild.PRO_P5.getName());
        assertEquals("P6"   , UniUjoChild.PRO_P6.getName());
        assertEquals("P7"   , UniUjoChild.PRO_P7.getName());
        assertEquals("P8"   , UniUjoChild.PRO_P8.getName());
        assertEquals("P9"   , UniUjoChild.PRO_P9.getName());
        //
        assertEquals(0, UniUjoChild.PRO_P0.getIndex());
        assertEquals(1, UniUjoChild.PRO_P1.getIndex());
        assertEquals(2, UniUjoChild.PRO_P2.getIndex());
        assertEquals(3, UniUjoChild.PRO_P3.getIndex());
        assertEquals(4, UniUjoChild.PRO_P4.getIndex());
        assertEquals(5, UniUjoChild.PRO_P5.getIndex());
        assertEquals(6, UniUjoChild.PRO_P6.getIndex());
        assertEquals(7, UniUjoChild.PRO_P7.getIndex());
        assertEquals(8, UniUjoChild.PRO_P8.getIndex());
        assertEquals(9, UniUjoChild.PRO_P9.getIndex());


        Long    o0 = Long.valueOf(Long.MAX_VALUE);
        Integer o1 = Integer.valueOf(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);

        UniUjoChild ujb = new UniUjoChild();
        assertEquals(10, ujb.readKeys().size());

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
        System.out.println("testTime: " + suite());

        Long    o0 = Long.valueOf(Long.MAX_VALUE);
        Integer o1 = Integer.valueOf(1);
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

        assertEquals( 5, ujb1.readKeys().size());
        assertEquals(10, ujb2.readKeys().size());
    }

    /** */
    public void testGetProperties1() throws Throwable {
        UniUjoBase ujb1 = new UniUjoBase();
        KeyList props = ujb1.readKeys();

        assertEquals(UniUjoBase.PRO_P0, props.get(0));
        assertEquals(UniUjoBase.PRO_P1, props.get(1));
        assertEquals(UniUjoBase.PRO_P2, props.get(2));
        assertEquals(UniUjoBase.PRO_P3, props.get(3));
        assertEquals(UniUjoBase.PRO_P4, props.get(4));
    }


    /**
     * Test of readValue method, of class org.ujorm.mapImlp.AUnifiedDataObject.
     */
    public void testReadWriteChildImpl() throws Throwable {
        System.out.println("testReadWriteChildImpl");

        assertEquals("proP0", UniUjoChildImpl.PRO_P0.getName());
        assertEquals("proP1", UniUjoChildImpl.PRO_P1.getName());
        assertEquals("proP2", UniUjoChildImpl.PRO_P2.getName());
        assertEquals("proP3", UniUjoChildImpl.PRO_P3.getName());
        assertEquals("proP4", UniUjoChildImpl.PRO_P4.getName());
        assertEquals("proP5", UniUjoChildImpl.PRO_P5.getName());
        assertEquals("proP6", UniUjoChildImpl.PRO_P6.getName());
        assertEquals("proP7", UniUjoChildImpl.PRO_P7.getName());
        assertEquals("proP8", UniUjoChildImpl.PRO_P8.getName());
        assertEquals("proP9", UniUjoChildImpl.PRO_P9.getName());
        //
        assertEquals(0, UniUjoChildImpl.PRO_P0.getIndex());
        assertEquals(1, UniUjoChildImpl.PRO_P1.getIndex());
        assertEquals(2, UniUjoChildImpl.PRO_P2.getIndex());
        assertEquals(3, UniUjoChildImpl.PRO_P3.getIndex());
        assertEquals(4, UniUjoChildImpl.PRO_P4.getIndex());
        assertEquals(5, UniUjoChildImpl.PRO_P5.getIndex());
        assertEquals(6, UniUjoChildImpl.PRO_P6.getIndex());
        assertEquals(7, UniUjoChildImpl.PRO_P7.getIndex());
        assertEquals(8, UniUjoChildImpl.PRO_P8.getIndex());
        assertEquals(9, UniUjoChildImpl.PRO_P9.getIndex());

        Long    o0 = Long.valueOf(Long.MAX_VALUE);
        Integer o1 = Integer.valueOf(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);

        UniUjoChildImpl ujb = new UniUjoChildImpl();
        assertEquals(10, ujb.readKeys().size());

        UniUjoChildImpl.PRO_P0.setValue(ujb, o0);
        UniUjoChildImpl.PRO_P1.setValue(ujb, o1);
        UniUjoChildImpl.PRO_P2.setValue(ujb, o2);
        UniUjoChildImpl.PRO_P3.setValue(ujb, o3);
        UniUjoChildImpl.PRO_P4.addItem (ujb, o4);
        UniUjoChildImpl.PRO_P5.setValue(ujb, o0);
        UniUjoChildImpl.PRO_P6.setValue(ujb, o1);
        UniUjoChildImpl.PRO_P7.setValue(ujb, o2);
        UniUjoChildImpl.PRO_P8.setValue(ujb, o3);
        UniUjoChildImpl.PRO_P9.addItem (ujb, o4);

        assertEquals(o0, UniUjoChildImpl.PRO_P0.of(ujb));
        assertEquals(o1, UniUjoChildImpl.PRO_P1.of(ujb));
        assertEquals(o2, UniUjoChildImpl.PRO_P2.of(ujb));
        assertEquals(o3, UniUjoChildImpl.PRO_P3.of(ujb));
        assertEquals(o4, UniUjoChildImpl.PRO_P4.of(ujb,0));
        assertEquals(o0, UniUjoChildImpl.PRO_P5.of(ujb));
        assertEquals(o1, UniUjoChildImpl.PRO_P6.of(ujb));
        assertEquals(o2, UniUjoChildImpl.PRO_P7.of(ujb));
        assertEquals(o3, UniUjoChildImpl.PRO_P8.of(ujb));
        assertEquals(o4, UniUjoChildImpl.PRO_P9.of(ujb,0));
    }

    /** Test of keys */
    public void testDummy() throws Throwable {
    }

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
}
