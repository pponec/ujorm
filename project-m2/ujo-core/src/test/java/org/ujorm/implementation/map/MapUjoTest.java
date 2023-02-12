/*
 * HUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:00
 */

package org.ujorm.implementation.map;

import java.util.Date;
import junit.framework.*;
import org.ujorm.MyTestCase;

/**
 * HashMap Unified Data Object Test
 * @author Pavel Ponec
 */
public class MapUjoTest extends MyTestCase {

    public MapUjoTest(String testName) {
        super(testName);
    }

    public static TestSuite suite() {
        TestSuite suite = new TestSuite(MapUjoTest.class);
        return suite;
    }


    public void testSpeedTime() throws Throwable {
        System.out.println("M1:testTime: " + suite());

        Long    o0 = Long.valueOf(Long.MAX_VALUE);
        Integer o1 = Integer.valueOf(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);

        callGC();
        long time1 = System.currentTimeMillis();

        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            MapUjoImpl ujb = new MapUjoImpl();

            MapUjoImpl.PRO_P0.setValue(ujb, o0);
            MapUjoImpl.PRO_P1.setValue(ujb, o1);
            MapUjoImpl.PRO_P2.setValue(ujb, o2);
            MapUjoImpl.PRO_P3.setValue(ujb, o3);
            MapUjoImpl.PRO_P4.setValue(ujb, o4);
            MapUjoImpl.PRO_P0.setValue(ujb, o0);
            MapUjoImpl.PRO_P1.setValue(ujb, o1);
            MapUjoImpl.PRO_P2.setValue(ujb, o2);
            MapUjoImpl.PRO_P3.setValue(ujb, o3);
            MapUjoImpl.PRO_P4.setValue(ujb, o4);

            assertEquals(o0, MapUjoImpl.PRO_P0.of(ujb));
            assertEquals(o1, MapUjoImpl.PRO_P1.of(ujb));
            assertEquals(o2, MapUjoImpl.PRO_P2.of(ujb));
            assertEquals(o3, MapUjoImpl.PRO_P3.of(ujb));
            assertEquals(o4, MapUjoImpl.PRO_P4.of(ujb));
            assertEquals(o0, MapUjoImpl.PRO_P0.of(ujb));
            assertEquals(o1, MapUjoImpl.PRO_P1.of(ujb));
            assertEquals(o2, MapUjoImpl.PRO_P2.of(ujb));
            assertEquals(o3, MapUjoImpl.PRO_P3.of(ujb));
            assertEquals(o4, MapUjoImpl.PRO_P4.of(ujb));
        }
        long time2 = System.currentTimeMillis();

        System.out.println("M1:TIME: " + (time2-time1)/1000f + " [sec]");
    }


    public void testSpeedTimeChild() throws Throwable {
        System.out.println("M2:testTime (child): " + suite());

        Long    o0 = Long.valueOf(Long.MAX_VALUE);
        Integer o1 = Integer.valueOf(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);

        callGC();
        long time1 = System.currentTimeMillis();

        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            MapUjoImplChild ujb = new MapUjoImplChild();

            MapUjoImplChild.PRO_P0.setValue(ujb, o0);
            MapUjoImplChild.PRO_P1.setValue(ujb, o1);
            MapUjoImplChild.PRO_P2.setValue(ujb, o2);
            MapUjoImplChild.PRO_P3.setValue(ujb, o3);
            MapUjoImplChild.PRO_P4.setValue(ujb, o4);
            MapUjoImplChild.PRO_P5.setValue(ujb, o0);
            MapUjoImplChild.PRO_P6.setValue(ujb, o1);
            MapUjoImplChild.PRO_P7.setValue(ujb, o2);
            MapUjoImplChild.PRO_P8.setValue(ujb, o3);
            MapUjoImplChild.PRO_P9.setValue(ujb, o4);

            assertEquals(o0, MapUjoImplChild.PRO_P0.of(ujb));
            assertEquals(o1, MapUjoImplChild.PRO_P1.of(ujb));
            assertEquals(o2, MapUjoImplChild.PRO_P2.of(ujb));
            assertEquals(o3, MapUjoImplChild.PRO_P3.of(ujb));
            assertEquals(o4, MapUjoImplChild.PRO_P4.of(ujb));
            assertEquals(o0, MapUjoImplChild.PRO_P5.of(ujb));
            assertEquals(o1, MapUjoImplChild.PRO_P6.of(ujb));
            assertEquals(o2, MapUjoImplChild.PRO_P7.of(ujb));
            assertEquals(o3, MapUjoImplChild.PRO_P8.of(ujb));
            assertEquals(o4, MapUjoImplChild.PRO_P9.of(ujb));
        }
        long time2 = System.currentTimeMillis();

        System.out.println("M2:TIME: " + (time2-time1)/1000f + " [sec]");
    }


    public void testPropertyCount() throws Throwable {
        MapUjoImpl ujb1 = new MapUjoImpl();
        MapUjoImplChild  ujb2 = new MapUjoImplChild();

        assertEquals( 5, ujb1.readKeys().size());
        assertEquals(10, ujb2.readKeys().size());
    }


    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }

}
