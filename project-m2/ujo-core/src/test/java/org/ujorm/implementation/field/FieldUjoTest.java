/*
 * HUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:00
 */

package org.ujorm.implementation.field;

import java.util.Date;
import junit.framework.*;
import org.ujorm.MyTestCase;

/**
 * HashMap Unified Data Object Test
 * @author Pavel Ponec
 */
public class FieldUjoTest extends MyTestCase {

    public void testSpeedTime() throws Throwable {
        System.out.println("M1:testTime: " + suite());

        Long    o0 = Long.valueOf(Long.MAX_VALUE);
        Integer o1 = Integer.valueOf(1);
        String  o2 = "TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);
        Object result;

        callGC();
        long time1 = System.currentTimeMillis();

        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            FieldUjoImpl ujb = new FieldUjoImpl();

            FieldUjoImpl.PRO_P0.setValue(ujb, o0);
            FieldUjoImpl.PRO_P1.setValue(ujb, o1);
            FieldUjoImpl.PRO_P2.setValue(ujb, o2);
            FieldUjoImpl.PRO_P3.setValue(ujb, o3);
            FieldUjoImpl.PRO_P4.setValue(ujb, o4);
            FieldUjoImpl.PRO_P0.setValue(ujb, o0);
            FieldUjoImpl.PRO_P1.setValue(ujb, o1);
            FieldUjoImpl.PRO_P2.setValue(ujb, o2);
            FieldUjoImpl.PRO_P3.setValue(ujb, o3);
            FieldUjoImpl.PRO_P4.setValue(ujb, o4);

            assertEquals(o0, FieldUjoImpl.PRO_P0.of(ujb));
            assertEquals(o1, FieldUjoImpl.PRO_P1.of(ujb));
            assertEquals(o2, FieldUjoImpl.PRO_P2.of(ujb));
            assertEquals(o3, FieldUjoImpl.PRO_P3.of(ujb));
            assertEquals(o4, FieldUjoImpl.PRO_P4.of(ujb));
            assertEquals(o0, FieldUjoImpl.PRO_P0.of(ujb));
            assertEquals(o1, FieldUjoImpl.PRO_P1.of(ujb));
            assertEquals(o2, FieldUjoImpl.PRO_P2.of(ujb));
            assertEquals(o3, FieldUjoImpl.PRO_P3.of(ujb));
            assertEquals(o4, FieldUjoImpl.PRO_P4.of(ujb));
        }
        long time2 = System.currentTimeMillis();

        System.out.println("M1:TIME: " + (time2-time1)/1000f + " [sec]");
    }


    public void testSpeedTimeChild() throws Throwable {
        System.out.println("M2:testTime (child): " + suite());

        Long    o0 = Long.valueOf(Long.MAX_VALUE);
        Integer o1 = Integer.valueOf(1);
        String  o2 = "TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);
        Object result;

        callGC();
        long time1 = System.currentTimeMillis();

        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            FieldUjoImplChild ujb = new FieldUjoImplChild();

            FieldUjoImplChild.PRO_P0.setValue(ujb, o0);
            FieldUjoImplChild.PRO_P1.setValue(ujb, o1);
            FieldUjoImplChild.PRO_P2.setValue(ujb, o2);
            FieldUjoImplChild.PRO_P3.setValue(ujb, o3);
            FieldUjoImplChild.PRO_P4.setValue(ujb, o4);
            FieldUjoImplChild.PRO_P5.setValue(ujb, o0);
            FieldUjoImplChild.PRO_P6.setValue(ujb, o1);
            FieldUjoImplChild.PRO_P7.setValue(ujb, o2);
            FieldUjoImplChild.PRO_P8.setValue(ujb, o3);
            FieldUjoImplChild.PRO_P9.setValue(ujb, o4);

            assertEquals(o0, FieldUjoImplChild.PRO_P0.of(ujb));
            assertEquals(o1, FieldUjoImplChild.PRO_P1.of(ujb));
            assertEquals(o2, FieldUjoImplChild.PRO_P2.of(ujb));
            assertEquals(o3, FieldUjoImplChild.PRO_P3.of(ujb));
            assertEquals(o4, FieldUjoImplChild.PRO_P4.of(ujb));
            assertEquals(o0, FieldUjoImplChild.PRO_P5.of(ujb));
            assertEquals(o1, FieldUjoImplChild.PRO_P6.of(ujb));
            assertEquals(o2, FieldUjoImplChild.PRO_P7.of(ujb));
            assertEquals(o3, FieldUjoImplChild.PRO_P8.of(ujb));
            assertEquals(o4, FieldUjoImplChild.PRO_P9.of(ujb));
        }
        long time2 = System.currentTimeMillis();

        System.out.println("M2:TIME: " + (time2-time1)/1000f + " [sec]");
    }


    public void testPropertyCount() throws Throwable {
        FieldUjoImpl ujb1 = new FieldUjoImpl();
        FieldUjoImplChild  ujb2 = new FieldUjoImplChild();

        assertEquals( 5, ujb1.readKeys().size());
        assertEquals(10, ujb2.readKeys().size());
    }
}
