/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:31
 */

package org.ujorm.implementation.universe;


import java.util.Date;
import org.junit.jupiter.api.Test;
import org.ujorm.KeyList;
import org.ujorm.MyTestCase;

/**
 *
 * @author Pavel Ponec
 */
public class ImplUniUjoTest extends MyTestCase {
    private static final Class CLASS = ImplUniUjoTest.class;

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

        ImplUjoBase ujb = new ImplUjoBase();

        ImplUjoBase.PRO_P0.setValue(ujb, o0);
        ImplUjoBase.PRO_P1.setValue(ujb, o1);
        ImplUjoBase.PRO_P2.setValue(ujb, o2);
        ImplUjoBase.PRO_P3.setValue(ujb, o3);
        ImplUjoBase.PRO_P4.addItem (ujb, o4);
        ImplUjoBase.PRO_P5.setValue(ujb, o0);
        ImplUjoBase.PRO_P6.setValue(ujb, o1);
        ImplUjoBase.PRO_P7.setValue(ujb, o2);
        ImplUjoBase.PRO_P8.setValue(ujb, o3);
        ImplUjoBase.PRO_P9.addItem (ujb, o4);

        assertEquals(o0, ImplUjoBase.PRO_P0.of(ujb));
        assertEquals(o1, ImplUjoBase.PRO_P1.of(ujb));
        assertEquals(o2, ImplUjoBase.PRO_P2.of(ujb));
        assertEquals(o3, ImplUjoBase.PRO_P3.of(ujb));
        assertEquals(o4, ImplUjoBase.PRO_P4.of(ujb,0));
        assertEquals(o0, ImplUjoBase.PRO_P5.of(ujb));
        assertEquals(o1, ImplUjoBase.PRO_P6.of(ujb));
        assertEquals(o2, ImplUjoBase.PRO_P7.of(ujb));
        assertEquals(o3, ImplUjoBase.PRO_P8.of(ujb));
        assertEquals(o4, ImplUjoBase.PRO_P9.of(ujb,0));
    }

    @Test
    public void testSpeedTime() throws Throwable {
        System.out.println("testTime: " + suite());

        Long    o0 = Long.MAX_VALUE;
        Integer o1 = 1;
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = 123456.456F;

        callGC();
        long time1 = System.currentTimeMillis();

        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            ImplUjoBase ujb = new ImplUjoBase();

            ImplUjoBase.PRO_P0.setValue(ujb, o0);
            ImplUjoBase.PRO_P1.setValue(ujb, o1);
            ImplUjoBase.PRO_P2.setValue(ujb, o2);
            ImplUjoBase.PRO_P3.setValue(ujb, o3);
            ImplUjoBase.PRO_P4.addItem (ujb, o4);
            ImplUjoBase.PRO_P5.setValue(ujb, o0);
            ImplUjoBase.PRO_P6.setValue(ujb, o1);
            ImplUjoBase.PRO_P7.setValue(ujb, o2);
            ImplUjoBase.PRO_P8.setValue(ujb, o3);
            ImplUjoBase.PRO_P9.addItem (ujb, o4);

            assertEquals(o0, ImplUjoBase.PRO_P0.of(ujb));
            assertEquals(o1, ImplUjoBase.PRO_P1.of(ujb));
            assertEquals(o2, ImplUjoBase.PRO_P2.of(ujb));
            assertEquals(o3, ImplUjoBase.PRO_P3.of(ujb));
            assertEquals(o4, ImplUjoBase.PRO_P4.of(ujb,0));
            assertEquals(o0, ImplUjoBase.PRO_P5.of(ujb));
            assertEquals(o1, ImplUjoBase.PRO_P6.of(ujb));
            assertEquals(o2, ImplUjoBase.PRO_P7.of(ujb));
            assertEquals(o3, ImplUjoBase.PRO_P8.of(ujb));
            assertEquals(o4, ImplUjoBase.PRO_P9.of(ujb,0));
        }

        printTime("TIME: ", time1);
    }

    @Test
    public void testPropertyCount() throws Throwable {
        UniUjoBase  ujb1 = new UniUjoBase();
        ImplUjoBase ujb2 = new ImplUjoBase();

        assertEquals( 5, ujb1.readKeys().size());
        assertEquals(10, ujb2.readKeys().size());
    }

    /** */
    @Test
    public void testGetProperties1() throws Throwable {
        UniUjoBase ujb1 = new UniUjoBase();
        KeyList props = ujb1.readKeys();

        assertEquals(UniUjoBase.PRO_P0, props.get(0));
        assertEquals(UniUjoBase.PRO_P1, props.get(1));
        assertEquals(UniUjoBase.PRO_P2, props.get(2));
        assertEquals(UniUjoBase.PRO_P3, props.get(3));
        assertEquals(UniUjoBase.PRO_P4, props.get(4));
    }
}
