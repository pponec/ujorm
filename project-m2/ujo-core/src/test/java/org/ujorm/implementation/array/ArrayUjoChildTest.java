/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:31
 */

package org.ujorm.implementation.array;


import java.util.Date;
import junit.framework.*;
import org.ujorm.MyTestCase;
import org.ujorm.KeyList;

/**
 *
 * @author Pavel Ponec
 */
public class ArrayUjoChildTest extends MyTestCase {
    private static final Class CLASS = ArrayUjoChildTest.class;

    public ArrayUjoChildTest(String testName) {
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

        Long    o0 = Long.valueOf(Long.MAX_VALUE);
        Integer o1 = Integer.valueOf(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);

        ArrayUjoImplChild ujb = new ArrayUjoImplChild();

        ArrayUjoImplChild.PRO_P0.setValue(ujb, o0);
        ArrayUjoImplChild.PRO_P1.setValue(ujb, o1);
        ArrayUjoImplChild.PRO_P2.setValue(ujb, o2);
        ArrayUjoImplChild.PRO_P3.setValue(ujb, o3);
        ArrayUjoImplChild.PRO_P4.setValue(ujb, o4);
        ArrayUjoImplChild.PRO_P5.setValue(ujb, o0);
        ArrayUjoImplChild.PRO_P6.setValue(ujb, o1);
        ArrayUjoImplChild.PRO_P7.setValue(ujb, o2);
        ArrayUjoImplChild.PRO_P8.setValue(ujb, o3);
        ArrayUjoImplChild.PRO_P9.setValue(ujb, o4);

        assertEquals(o0, ArrayUjoImplChild.PRO_P0.of(ujb));
        assertEquals(o1, ArrayUjoImplChild.PRO_P1.of(ujb));
        assertEquals(o2, ArrayUjoImplChild.PRO_P2.of(ujb));
        assertEquals(o3, ArrayUjoImplChild.PRO_P3.of(ujb));
        assertEquals(o4, ArrayUjoImplChild.PRO_P4.of(ujb));
        assertEquals(o0, ArrayUjoImplChild.PRO_P5.of(ujb));
        assertEquals(o1, ArrayUjoImplChild.PRO_P6.of(ujb));
        assertEquals(o2, ArrayUjoImplChild.PRO_P7.of(ujb));
        assertEquals(o3, ArrayUjoImplChild.PRO_P8.of(ujb));
        assertEquals(o4, ArrayUjoImplChild.PRO_P9.of(ujb));
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
            ArrayUjoImplChild ujb = new ArrayUjoImplChild();

            ArrayUjoImplChild.PRO_P0.setValue(ujb, o0);
            ArrayUjoImplChild.PRO_P1.setValue(ujb, o1);
            ArrayUjoImplChild.PRO_P2.setValue(ujb, o2);
            ArrayUjoImplChild.PRO_P3.setValue(ujb, o3);
            ArrayUjoImplChild.PRO_P4.setValue(ujb, o4);
            ArrayUjoImplChild.PRO_P5.setValue(ujb, o0);
            ArrayUjoImplChild.PRO_P6.setValue(ujb, o1);
            ArrayUjoImplChild.PRO_P7.setValue(ujb, o2);
            ArrayUjoImplChild.PRO_P8.setValue(ujb, o3);
            ArrayUjoImplChild.PRO_P9.setValue(ujb, o4);

            assertEquals(o0, ArrayUjoImplChild.PRO_P0.of(ujb));
            assertEquals(o1, ArrayUjoImplChild.PRO_P1.of(ujb));
            assertEquals(o2, ArrayUjoImplChild.PRO_P2.of(ujb));
            assertEquals(o3, ArrayUjoImplChild.PRO_P3.of(ujb));
            assertEquals(o4, ArrayUjoImplChild.PRO_P4.of(ujb));
            assertEquals(o0, ArrayUjoImplChild.PRO_P5.of(ujb));
            assertEquals(o1, ArrayUjoImplChild.PRO_P6.of(ujb));
            assertEquals(o2, ArrayUjoImplChild.PRO_P7.of(ujb));
            assertEquals(o3, ArrayUjoImplChild.PRO_P8.of(ujb));
            assertEquals(o4, ArrayUjoImplChild.PRO_P9.of(ujb));

        }

        printTime("TIME: ", time1);
    }

    public void testPropertyCount() throws Throwable {
        ArrayUjoImpl ujb1 = new ArrayUjoImpl();
        ArrayUjoImplChild  ujb2 = new ArrayUjoImplChild();

        assertEquals( 5, ujb1.readKeys().size());
        assertEquals(10, ujb2.readKeys().size());
    }

    /** */
    public void testGetProperties1() throws Throwable {
        ArrayUjoImpl ujb1 = new ArrayUjoImpl();
        KeyList props = ujb1.readKeys();

        assertEquals(ArrayUjoImpl.PRO_P0, props.get(0));
        assertEquals(ArrayUjoImpl.PRO_P1, props.get(1));
        assertEquals(ArrayUjoImpl.PRO_P2, props.get(2));
        assertEquals(ArrayUjoImpl.PRO_P3, props.get(3));
        assertEquals(ArrayUjoImpl.PRO_P4, props.get(4));
    }

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
}
