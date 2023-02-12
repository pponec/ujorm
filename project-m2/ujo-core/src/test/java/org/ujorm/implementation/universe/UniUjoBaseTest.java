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
import org.ujorm.KeyList;

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
    public void testPropertyName() throws Throwable {
        System.out.println("testPropertyName");

        assertEquals(UniUjoBase.class.getSimpleName() +'.' + UniUjoBase.PRO_P0.getName(), UniUjoBase.PRO_P0.getFullName());
        assertEquals(UniUjoBase.class.getSimpleName() +'.' + UniUjoBase.PRO_P1.getName(), UniUjoBase.PRO_P1.getFullName());
    }

    /**
     * Test of readValue method,
     */
    public void testReadWrite() throws Throwable {
        System.out.println("testReadWrite");

        Long    o0 = Long.valueOf(Long.MAX_VALUE);
        Integer o1 = Integer.valueOf(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);

        UniUjoBase ujb = new UniUjoBase();

        UniUjoBase.PRO_P0.setValue(ujb, o0);
        UniUjoBase.PRO_P1.setValue(ujb, o1);
        UniUjoBase.PRO_P2.setValue(ujb, o2);
        UniUjoBase.PRO_P3.setValue(ujb, o3);
        UniUjoBase.PRO_P4.addItem (ujb, o4);

        assertEquals(o0, UniUjoBase.PRO_P0.of(ujb));
        assertEquals(o1, UniUjoBase.PRO_P1.of(ujb));
        assertEquals(o2, UniUjoBase.PRO_P2.of(ujb));
        assertEquals(o3, UniUjoBase.PRO_P3.of(ujb));
        assertEquals(o4, UniUjoBase.PRO_P4.of(ujb,0));
    }

    public void testSpeedTime() throws Throwable {
        System.out.println("A1:testSpeedTime: " + suite());

        Long    o0 = Long.valueOf(Long.MAX_VALUE);
        Integer o1 = Integer.valueOf(1);
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
            UniUjoBase.PRO_P4.addItem (ujb, o4);
            UniUjoBase.PRO_P0.setValue(ujb, o0);
            UniUjoBase.PRO_P1.setValue(ujb, o1);
            UniUjoBase.PRO_P2.setValue(ujb, o2);
            UniUjoBase.PRO_P3.setValue(ujb, o3);
            UniUjoBase.PRO_P4.addItem (ujb, o4);

            assertEquals(o0, UniUjoBase.PRO_P0.of(ujb));
            assertEquals(o1, UniUjoBase.PRO_P1.of(ujb));
            assertEquals(o2, UniUjoBase.PRO_P2.of(ujb));
            assertEquals(o3, UniUjoBase.PRO_P3.of(ujb));
            assertEquals(o4, UniUjoBase.PRO_P4.getItem(ujb,0));
            assertEquals(o0, UniUjoBase.PRO_P0.of(ujb));
            assertEquals(o1, UniUjoBase.PRO_P1.of(ujb));
            assertEquals(o2, UniUjoBase.PRO_P2.of(ujb));
            assertEquals(o3, UniUjoBase.PRO_P3.of(ujb));
            assertEquals(o4, UniUjoBase.PRO_P4.getItem(ujb,0));
        }
        printTime("A1:TIME: ", time1);
    }



    /** Test of keys */
    public void testGetProperties1() throws Throwable {
        UniUjoBase ujb1 = new UniUjoBase();
        KeyList props = ujb1.readKeys();

        assertEquals(UniUjoBase.PRO_P0, props.get(0));
        assertEquals(UniUjoBase.PRO_P1, props.get(1));
        assertEquals(UniUjoBase.PRO_P2, props.get(2));
        assertEquals(UniUjoBase.PRO_P3, props.get(3));
        assertEquals(UniUjoBase.PRO_P4, props.get(4));
    }

    /** Test of keys (temporary removed). The test logs an error to the standard stream. */
    // @ExpectedException(class=IllegalStateException.class, message="Exception Message", causeException)
    public void temporarryRemoved_testMismuch() throws Throwable {
        try {
            new MismuchUjoBase().readKeys();
        } catch (ExceptionInInitializerError e) {
            return;
        } catch (RuntimeException | OutOfMemoryError e) {
            return;
        }
        assertNull("The exception " + IllegalArgumentException.class.getSimpleName() + " is expected.");
    }

    /** Tesn an Interface */
    public void testUniUjoInterface() throws Throwable {

        assertEquals("proP0", UniUjoInterface.PRO_P0.getName());
        assertEquals("proP1", UniUjoInterface.PRO_P1.getName());
        assertEquals("proP2", UniUjoInterface.PRO_P2.getName());
        assertEquals("proP3", UniUjoInterface.PRO_P3.getName());
        assertEquals("proP4", UniUjoInterface.PRO_P4.getName());

        Long    o0 = Long.valueOf(Long.MAX_VALUE);
        Integer o1 = Integer.valueOf(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);

        UniUjoInterface ujo = new UniUjoImpl();

        UniUjoInterface.PRO_P0.setValue(ujo, o0);
        UniUjoInterface.PRO_P1.setValue(ujo, o1);
        UniUjoInterface.PRO_P2.setValue(ujo, o2);
        UniUjoInterface.PRO_P3.setValue(ujo, o3);
        UniUjoInterface.PRO_P4.addItem (ujo, o4);

        assertEquals(o0, UniUjoInterface.PRO_P0.of(ujo));
        assertEquals(o1, UniUjoInterface.PRO_P1.of(ujo));
        assertEquals(o2, UniUjoInterface.PRO_P2.of(ujo));
        assertEquals(o3, UniUjoInterface.PRO_P3.of(ujo));
        assertEquals(o4, UniUjoInterface.PRO_P4.of(ujo,0));
    }


    /** Test of keys */
    public void testDummy() throws Throwable {
    }


    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }

}
