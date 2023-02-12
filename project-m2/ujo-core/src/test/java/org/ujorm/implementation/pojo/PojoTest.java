/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:31
 */

package org.ujorm.implementation.pojo;


import java.util.Date;
import junit.framework.*;
import org.ujorm.MyTestCase;

/**
 *
 * @author Pavel Ponec
 */
public class PojoTest extends MyTestCase {

    public PojoTest(String testName) {
        super(testName);
    }

    public static TestSuite suite() {
        TestSuite suite = new TestSuite(PojoTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
    }

    @Override
    protected void tearDown() throws Exception {
    }

    /**
     * Test of readValue method, of class org.ujorm.hmapImlp.AUnifiedDataObject.
     */
    public void testReadWrite() throws Throwable {
        System.out.println("testReadWrite");

        Long    o0 = Long.valueOf(Long.MAX_VALUE);
        Integer o1 = Integer.valueOf(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);

        PojoImpl ujb = new PojoImpl();

        ujb.setP0(o0);
        ujb.setP1(o1);
        ujb.setP2(o2);
        ujb.setP3(o3);
        ujb.setP4(o4);
        ujb.setP0(o0);
        ujb.setP1(o1);
        ujb.setP2(o2);
        ujb.setP3(o3);
        ujb.setP4(o4);

        assertEquals(o0, ujb.getP0());
        assertEquals(o1, ujb.getP1());
        assertEquals(o2, ujb.getP2());
        assertEquals(o3, ujb.getP3());
        assertEquals(o4, ujb.getP4());
        assertEquals(o0, ujb.getP0());
        assertEquals(o1, ujb.getP1());
        assertEquals(o2, ujb.getP2());
        assertEquals(o3, ujb.getP3());
        assertEquals(o4, ujb.getP4());
    }

    public void testSpeedTime() throws Throwable {
        System.out.println("P1:testSpeedTime: " + suite());

        Long    o0 = Long.valueOf(Long.MAX_VALUE);
        Integer o1 = Integer.valueOf(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);
        Object result;

        callGC();
        long time1 = System.currentTimeMillis();

        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            PojoImpl ujb = new PojoImpl();

            ujb.setP0(o0);
            ujb.setP1(o1);
            ujb.setP2(o2);
            ujb.setP3(o3);
            ujb.setP4(o4);
            ujb.setP0(o0);
            ujb.setP1(o1);
            ujb.setP2(o2);
            ujb.setP3(o3);
            ujb.setP4(o4);

            assertEquals(o0, ujb.getP0());
            assertEquals(o1, ujb.getP1());
            assertEquals(o2, ujb.getP2());
            assertEquals(o3, ujb.getP3());
            assertEquals(o4, ujb.getP4());
            assertEquals(o0, ujb.getP0());
            assertEquals(o1, ujb.getP1());
            assertEquals(o2, ujb.getP2());
            assertEquals(o3, ujb.getP3());
            assertEquals(o4, ujb.getP4());
        }
        long time2 = System.currentTimeMillis();

        System.out.println("P1:TIME: " + (time2-time1)/1000f + " [sec]");
    }

    public void testSpeedTime2() throws Throwable {
        System.out.println("P2:testSpeedTime (child): " + suite());

        Long    o0 = Long.valueOf(Long.MAX_VALUE);
        Integer o1 = Integer.valueOf(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);
        Object result;

        callGC();
        long time1 = System.currentTimeMillis();

        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            PojoImplChild ujb = new PojoImplChild();

            ujb.setP0(o0);
            ujb.setP1(o1);
            ujb.setP2(o2);
            ujb.setP3(o3);
            ujb.setP4(o4);
            ujb.setP5(o0);
            ujb.setP6(o1);
            ujb.setP7(o2);
            ujb.setP8(o3);
            ujb.setP9(o4);

            assertEquals(o0, ujb.getP0());
            assertEquals(o1, ujb.getP1());
            assertEquals(o2, ujb.getP2());
            assertEquals(o3, ujb.getP3());
            assertEquals(o4, ujb.getP4());
            assertEquals(o0, ujb.getP5());
            assertEquals(o1, ujb.getP6());
            assertEquals(o2, ujb.getP7());
            assertEquals(o3, ujb.getP8());
            assertEquals(o4, ujb.getP9());
        }
        long time2 = System.currentTimeMillis();

        System.out.println("P2:TIME: " + (time2-time1)/1000f + " [sec]");
    }

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }

}
