/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:31
 */

package org.ujorm.implementation.array;


import java.util.Date;
import org.junit.jupiter.api.Test;
import org.ujorm.AbstractTest;
import org.ujorm.KeyList;

/**
 *
 * @author Pavel Ponec
 */
public class ArrayUjoChildTest extends AbstractTest {
    private static final Class CLASS = ArrayUjoChildTest.class;

    /**
     * Test of readValue method, of class org.ujorm.mapImlp.AUnifiedDataObject.
     */
    @Test
    public void testReadWrite() throws Throwable {
        System.out.println("testReadWrite");

        Long    o0 = Long.MAX_VALUE;
        Integer o1 = 1;
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = 123456.456F;

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

    @Test
    public void testSpeedTime() throws Throwable {
        System.out.println("testTime: " + testName());

        Long    o0 = Long.MAX_VALUE;
        Integer o1 = 1;
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = 123456.456F;

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

    @Test
    public void testPropertyCount() throws Throwable {
        ArrayUjoImpl ujb1 = new ArrayUjoImpl();
        ArrayUjoImplChild  ujb2 = new ArrayUjoImplChild();

        assertEquals( 5, ujb1.readKeys().size());
        assertEquals(10, ujb2.readKeys().size());
    }

    /** */
    @Test
    public void testGetProperties1() throws Throwable {
        ArrayUjoImpl ujb1 = new ArrayUjoImpl();
        KeyList props = ujb1.readKeys();

        assertEquals(ArrayUjoImpl.PRO_P0, props.get(0));
        assertEquals(ArrayUjoImpl.PRO_P1, props.get(1));
        assertEquals(ArrayUjoImpl.PRO_P2, props.get(2));
        assertEquals(ArrayUjoImpl.PRO_P3, props.get(3));
        assertEquals(ArrayUjoImpl.PRO_P4, props.get(4));
    }
}
