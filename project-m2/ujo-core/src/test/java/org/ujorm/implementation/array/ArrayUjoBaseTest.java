/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:31
 */

package org.ujorm.implementation.array;


import java.util.Date;
import junit.framework.*;
import org.ujorm.KeyList;
import org.ujorm.MyTestCase;

/**
 * TextCase
 * @author Pavel Ponec
 */
public class ArrayUjoBaseTest extends MyTestCase {

    /**
     * Test of readValue method,
     */
    public void testReadWrite() throws Throwable {
        System.out.println("testReadWrite");

        Long    o0 = Long.MAX_VALUE;
        Integer o1 = 1;
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = 123456.456F;

        ArrayUjoImpl ujb = new ArrayUjoImpl();

        ArrayUjoImpl.PRO_P0.setValue(ujb, o0);
        ArrayUjoImpl.PRO_P1.setValue(ujb, o1);
        ArrayUjoImpl.PRO_P2.setValue(ujb, o2);
        ArrayUjoImpl.PRO_P3.setValue(ujb, o3);
        ArrayUjoImpl.PRO_P4.setValue(ujb, o4);

        assertEquals(o0, ArrayUjoImpl.PRO_P0.of(ujb));
        assertEquals(o1, ArrayUjoImpl.PRO_P1.of(ujb));
        assertEquals(o2, ArrayUjoImpl.PRO_P2.of(ujb));
        assertEquals(o3, ArrayUjoImpl.PRO_P3.of(ujb));
        assertEquals(o4, ArrayUjoImpl.PRO_P4.of(ujb));
    }

    public void testSpeedTime() throws Throwable {
        System.out.println("A1:testSpeedTime: " + suite());

        Long    o0 = Long.MAX_VALUE;
        Integer o1 = 1;
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = 123456.456F;
        Object result;

        callGC();
        long time1 = System.currentTimeMillis();

        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            ArrayUjoImpl ujb = new ArrayUjoImpl();
            ArrayUjoImpl.PRO_P0.setValue(ujb, o0);
            ArrayUjoImpl.PRO_P1.setValue(ujb, o1);
            ArrayUjoImpl.PRO_P2.setValue(ujb, o2);
            ArrayUjoImpl.PRO_P3.setValue(ujb, o3);
            ArrayUjoImpl.PRO_P4.setValue(ujb, o4);
            ArrayUjoImpl.PRO_P0.setValue(ujb, o0);
            ArrayUjoImpl.PRO_P1.setValue(ujb, o1);
            ArrayUjoImpl.PRO_P2.setValue(ujb, o2);
            ArrayUjoImpl.PRO_P3.setValue(ujb, o3);
            ArrayUjoImpl.PRO_P4.setValue(ujb, o4);

            assertEquals(o0, ArrayUjoImpl.PRO_P0.of(ujb));
            assertEquals(o1, ArrayUjoImpl.PRO_P1.of(ujb));
            assertEquals(o2, ArrayUjoImpl.PRO_P2.of(ujb));
            assertEquals(o3, ArrayUjoImpl.PRO_P3.of(ujb));
            assertEquals(o4, ArrayUjoImpl.PRO_P4.of(ujb));
            assertEquals(o0, ArrayUjoImpl.PRO_P0.of(ujb));
            assertEquals(o1, ArrayUjoImpl.PRO_P1.of(ujb));
            assertEquals(o2, ArrayUjoImpl.PRO_P2.of(ujb));
            assertEquals(o3, ArrayUjoImpl.PRO_P3.of(ujb));
            assertEquals(o4, ArrayUjoImpl.PRO_P4.of(ujb));
        }
        printTime("A1:TIME: ", time1);
    }

    public void testSpeedTimeRecur() throws Throwable {
        System.out.println("A2:testSpeedTime (recur): " + suite());

        Long    o0 = Long.MAX_VALUE;
        Integer o1 = 1;
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = 123456.456F;
        Object result;

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

        printTime("A2:TIME: ", time1);
    }


    /** Test of keys */
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
