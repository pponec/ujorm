/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:31
 */

package org.ujorm.implementation.quick;


import java.awt.Color;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.ujorm.KeyList;
import org.ujorm.AbstractTest;
import org.ujorm.implementation.quick.domains.SmartUjoChild;
import org.ujorm.implementation.quick.domains.SmartUjoImpl;

/**
 *
 * @author Pavel Ponec
 */
public class SmartUjoChildTest extends AbstractTest {
    private final static Class CLASS = SmartUjoChildTest.class;

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
        Class<?> o4 = Color.class;

        SmartUjoChild ujb = new SmartUjoChild();

        SmartUjoChild.PRO_P0.setValue(ujb, o0);
        SmartUjoChild.PRO_P1.setValue(ujb, o1);
        SmartUjoChild.PRO_P2.setValue(ujb, o2);
        SmartUjoChild.PRO_P3.setValue(ujb, o3);
        SmartUjoChild.PRO_P4.setValue(ujb, o4);
        SmartUjoChild.PRO_P5.setValue(ujb, o0);
        SmartUjoChild.PRO_P6.setValue(ujb, o1);
        SmartUjoChild.PRO_P7.setValue(ujb, o2);
        SmartUjoChild.PRO_P8.setValue(ujb, o3);
        SmartUjoChild.PRO_P9.setValue(ujb, o4);

        assertEquals(o0, SmartUjoChild.PRO_P0.of(ujb));
        assertEquals(o1, SmartUjoChild.PRO_P1.of(ujb));
        assertEquals(o2, SmartUjoChild.PRO_P2.of(ujb));
        assertEquals(o3, SmartUjoChild.PRO_P3.of(ujb));
        assertEquals(o4, SmartUjoChild.PRO_P4.of(ujb));
        assertEquals(o0, SmartUjoChild.PRO_P5.of(ujb));
        assertEquals(o1, SmartUjoChild.PRO_P6.of(ujb));
        assertEquals(o2, SmartUjoChild.PRO_P7.of(ujb));
        assertEquals(o3, SmartUjoChild.PRO_P8.of(ujb));
        assertEquals(o4, SmartUjoChild.PRO_P9.of(ujb));
    }

    @Test
    public void testSpeedTime() throws Throwable {
        System.out.println("testTime: " + testName());

        Long    o0 = Long.MAX_VALUE;
        Integer o1 = 1;
        String  o2 ="TEST";
        Date    o3 = new Date();
        Class<?> o4 = Color.class;
        Object result;

        callGC();
        long time1 = System.currentTimeMillis();

        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            SmartUjoChild ujb = new SmartUjoChild();

            SmartUjoChild.PRO_P0.setValue(ujb, o0);
            SmartUjoChild.PRO_P1.setValue(ujb, o1);
            SmartUjoChild.PRO_P2.setValue(ujb, o2);
            SmartUjoChild.PRO_P3.setValue(ujb, o3);
            SmartUjoChild.PRO_P4.setValue(ujb, o4);
            SmartUjoChild.PRO_P5.setValue(ujb, o0);
            SmartUjoChild.PRO_P6.setValue(ujb, o1);
            SmartUjoChild.PRO_P7.setValue(ujb, o2);
            SmartUjoChild.PRO_P8.setValue(ujb, o3);
            SmartUjoChild.PRO_P9.setValue(ujb, o4);

            assertEquals(o0, SmartUjoChild.PRO_P0.of(ujb));
            assertEquals(o1, SmartUjoChild.PRO_P1.of(ujb));
            assertEquals(o2, SmartUjoChild.PRO_P2.of(ujb));
            assertEquals(o3, SmartUjoChild.PRO_P3.of(ujb));
            assertEquals(o4, SmartUjoChild.PRO_P4.of(ujb));
            assertEquals(o0, SmartUjoChild.PRO_P5.of(ujb));
            assertEquals(o1, SmartUjoChild.PRO_P6.of(ujb));
            assertEquals(o2, SmartUjoChild.PRO_P7.of(ujb));
            assertEquals(o3, SmartUjoChild.PRO_P8.of(ujb));
            assertEquals(o4, SmartUjoChild.PRO_P9.of(ujb));

        }

        long time2 = System.currentTimeMillis();
        System.out.println("TIME: " + (time2-time1)/1000f + " [sec]");
    }

    @Test
    public void testPropertyCount() throws Throwable {
        SmartUjoImpl ujb1 = new SmartUjoImpl();
        SmartUjoChild  ujb2 = new SmartUjoChild();

        assertEquals( 6, ujb1.readKeys().size());
        assertEquals(12, ujb2.readKeys().size());
    }

    @Test
    public void testPropertyName() throws Throwable {
        SmartUjoImpl ujb1 = new SmartUjoImpl();
        SmartUjoChild  ujb2 = new SmartUjoChild();

        assertEquals( "PRO_P0", SmartUjoChild.PRO_P0.getName());
        assertEquals( "PRO_P4", SmartUjoChild.PRO_P4.getName());
        assertEquals( "PRO_P5", SmartUjoChild.PRO_P5.getName());
        assertEquals( "PRO_P6", SmartUjoChild.PRO_P6.getName());
    }

    /** */
    @Test
    public void testGetProperties1() throws Throwable {
        SmartUjoImpl ujb1 = new SmartUjoImpl();
        KeyList props = ujb1.readKeys();

        assertEquals(SmartUjoChild.PRO_P0, props.get(0));
        assertEquals(SmartUjoChild.PRO_P1, props.get(1));
        assertEquals(SmartUjoChild.PRO_P2, props.get(2));
        assertEquals(SmartUjoChild.PRO_P3, props.get(3));
        assertEquals(SmartUjoChild.PRO_P4, props.get(4));
    }

    /** A compilation test of API Key class */
    @Test
    public void testList() {
        SmartUjoChild<SmartUjoChild> ujo = new SmartUjoChild();

        ujo.getList(SmartUjoChild.PRO_LST1).add(Color.RED);
        ujo.getList(SmartUjoChild.PRO_LST1).add(Color.GREEN);
        ujo.getList(SmartUjoChild.PRO_LST1).add(Color.BLUE);

        ujo.getList(SmartUjoChild.PRO_LST2).add(Color.RED);
        ujo.getList(SmartUjoChild.PRO_LST2).add(Color.GREEN);
        ujo.getList(SmartUjoChild.PRO_LST2).add(Color.BLUE);


        int i = 0;
        for (Color color : ujo.getList(SmartUjoChild.PRO_LST2)) {
            ++i;
        }

        assertEquals(3, i);
        assertEquals(3, ujo.getList(SmartUjoChild.PRO_LST1).size());
        assertEquals(3, ujo.getList(SmartUjoChild.PRO_LST2).size());
    }
}
