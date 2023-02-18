/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:31
 */

package org.ujorm.implementation.quick;

import java.awt.Color;
import java.util.Date;
import junit.framework.*;
import org.ujorm.KeyList;
import org.ujorm.MyTestCase;
import org.ujorm.implementation.quick.domains.SmartUjoChild;
import org.ujorm.implementation.quick.domains.SmartUjoCompany;
import org.ujorm.implementation.quick.domains.SmartUjoImpl;

/**
 * TextCase
 * @author Pavel Ponec
 */
public class SmartUjoBaseTest extends MyTestCase {

    /**
     * Test of readValue method,
     */
    public void testPropertyType() throws Throwable {
        System.out.println("testPropertyType");

        assertEquals(Long   .class, SmartUjoImpl.PRO_P0.getType());
        assertEquals(Integer.class, SmartUjoImpl.PRO_P1.getType());
        assertEquals(String .class, SmartUjoImpl.PRO_P2.getType());
        assertEquals(Date   .class, SmartUjoImpl.PRO_P3.getType());
        assertEquals(Class  .class, SmartUjoImpl.PRO_P4.getType());
        assertEquals(Color  .class, SmartUjoImpl.PRO_LST1.getItemType());
    }

    /**
     * Test of readValue method,
     */
    public void testPropertyName() throws Throwable {
        System.out.println("testPropertyName");

        assertEquals(SmartUjoImpl.class.getSimpleName() +'.' + SmartUjoImpl.PRO_P0.getName(), SmartUjoImpl.PRO_P0.getFullName());
        assertEquals(SmartUjoImpl.class.getSimpleName() +'.' + SmartUjoImpl.PRO_P1.getName(), SmartUjoImpl.PRO_P1.getFullName());
    }

    /**
     * Test of readValue method,
     */
    public void testPropertyChildype() throws Throwable {
        System.out.println("testPropertyType");

        assertEquals(Long   .class, SmartUjoChild.PRO_P0.getType());
        assertEquals(Integer.class, SmartUjoChild.PRO_P1.getType());
        assertEquals(String .class, SmartUjoChild.PRO_P2.getType());
        assertEquals(Date   .class, SmartUjoChild.PRO_P3.getType());
        assertEquals(Class  .class, SmartUjoChild.PRO_P4.getType());
        assertEquals(Long   .class, SmartUjoChild.PRO_P5.getType());
        assertEquals(Integer.class, SmartUjoChild.PRO_P6.getType());
        assertEquals(String .class, SmartUjoChild.PRO_P7.getType());
        assertEquals(Date   .class, SmartUjoChild.PRO_P8.getType());
        assertEquals(Class  .class, SmartUjoChild.PRO_P9.getType());
        assertEquals(Color  .class, SmartUjoChild.PRO_LST1.getItemType());
        assertEquals(Color  .class, SmartUjoChild.PRO_LST2.getItemType());
    }

    /**
     * Test of readValue method,
     */
    public void testPropertyDomainType() throws Throwable {
        System.out.println("testPropertyDomainType");

        assertEquals(SmartUjoImpl.class, SmartUjoImpl.PRO_P0.getDomainType());
        assertEquals(SmartUjoImpl.class, SmartUjoImpl.PRO_P1.getDomainType());
        assertEquals(SmartUjoImpl.class, SmartUjoImpl.PRO_P2.getDomainType());
        assertEquals(SmartUjoImpl.class, SmartUjoImpl.PRO_P3.getDomainType());
        assertEquals(SmartUjoImpl.class, SmartUjoImpl.PRO_P4.getDomainType());
        assertEquals(SmartUjoImpl.class, SmartUjoImpl.PRO_LST1.getDomainType());

        assertEquals(SmartUjoChild.class, SmartUjoChild.PRO_P5.getDomainType());
        assertEquals(SmartUjoChild.class, SmartUjoChild.PRO_P6.getDomainType());
        assertEquals(SmartUjoChild.class, SmartUjoChild.PRO_P7.getDomainType());
        assertEquals(SmartUjoChild.class, SmartUjoChild.PRO_P8.getDomainType());
        assertEquals(SmartUjoChild.class, SmartUjoChild.PRO_P9.getDomainType());
        assertEquals(SmartUjoChild.class, SmartUjoChild.PRO_LST2.getDomainType());
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
        Class<?> o4 = Color.class;

        SmartUjoImpl ujb = new SmartUjoImpl();

        SmartUjoImpl.PRO_P0.setValue(ujb, o0);
        SmartUjoImpl.PRO_P1.setValue(ujb, o1);
        SmartUjoImpl.PRO_P2.setValue(ujb, o2);
        SmartUjoImpl.PRO_P3.setValue(ujb, o3);
        SmartUjoImpl.PRO_P4.setValue(ujb, o4);

        assertEquals(o0, SmartUjoImpl.PRO_P0.of(ujb));
        assertEquals(o1, SmartUjoImpl.PRO_P1.of(ujb));
        assertEquals(o2, SmartUjoImpl.PRO_P2.of(ujb));
        assertEquals(o3, SmartUjoImpl.PRO_P3.of(ujb));
        assertEquals(o4, SmartUjoImpl.PRO_P4.of(ujb));
    }

    public void testSpeedTime() throws Throwable {
        System.out.println("A1:testSpeedTime: " + suite());

        Long    o0 = Long.valueOf(Long.MAX_VALUE);
        Integer o1 = Integer.valueOf(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Class<?> o4 = Color.class;
        Object result;

        callGC();
        long time1 = System.currentTimeMillis();

        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            SmartUjoImpl ujb = new SmartUjoImpl();
            SmartUjoImpl.PRO_P0.setValue(ujb, o0);
            SmartUjoImpl.PRO_P1.setValue(ujb, o1);
            SmartUjoImpl.PRO_P2.setValue(ujb, o2);
            SmartUjoImpl.PRO_P3.setValue(ujb, o3);
            SmartUjoImpl.PRO_P4.setValue(ujb, o4);
            SmartUjoImpl.PRO_P0.setValue(ujb, o0);
            SmartUjoImpl.PRO_P1.setValue(ujb, o1);
            SmartUjoImpl.PRO_P2.setValue(ujb, o2);
            SmartUjoImpl.PRO_P3.setValue(ujb, o3);
            SmartUjoImpl.PRO_P4.setValue(ujb, o4);

            assertEquals(o0, SmartUjoImpl.PRO_P0.of(ujb));
            assertEquals(o1, SmartUjoImpl.PRO_P1.of(ujb));
            assertEquals(o2, SmartUjoImpl.PRO_P2.of(ujb));
            assertEquals(o3, SmartUjoImpl.PRO_P3.of(ujb));
            assertEquals(o4, SmartUjoImpl.PRO_P4.of(ujb));
            assertEquals(o0, SmartUjoImpl.PRO_P0.of(ujb));
            assertEquals(o1, SmartUjoImpl.PRO_P1.of(ujb));
            assertEquals(o2, SmartUjoImpl.PRO_P2.of(ujb));
            assertEquals(o3, SmartUjoImpl.PRO_P3.of(ujb));
            assertEquals(o4, SmartUjoImpl.PRO_P4.of(ujb));
        }
        long time2 = System.currentTimeMillis();

        System.out.println("A1:TIME: " + (time2-time1)/1000f + " [sec]");
    }

    public void testSpeedTimeRecur() throws Throwable {
        System.out.println("A2:testSpeedTime (recur): " + super.suite());

        Long    o0 = Long.valueOf(Long.MAX_VALUE);
        Integer o1 = Integer.valueOf(1);
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

        System.out.println("A2:TIME: " + (time2-time1)/1000f + " [sec]");
    }


    /** Test of keys */
    public void testGetProperties1() throws Throwable {
        SmartUjoImpl ujb1 = new SmartUjoImpl();
        KeyList props = ujb1.readKeys();

        assertEquals(SmartUjoImpl.PRO_P0, props.get(0));
        assertEquals(SmartUjoImpl.PRO_P1, props.get(1));
        assertEquals(SmartUjoImpl.PRO_P2, props.get(2));
        assertEquals(SmartUjoImpl.PRO_P3, props.get(3));
        assertEquals(SmartUjoImpl.PRO_P4, props.get(4));
    }

    /** A compilation test of API Key class */
    public void testPathPropertyCompilation() {
        SmartUjoCompany company = new SmartUjoCompany();
        SmartUjoCompany.DIRECTOR.setValue(company, new SmartUjoChild());

        Integer compDir1 = SmartUjoCompany.DIRECTOR.add(SmartUjoChild.PRO_P6).of(company);
        Integer compDir2 = SmartUjoCompany.DIRECTOR.add(SmartUjoChild.PRO_P1).of(company);

        assertEquals(compDir1, compDir2);
    }

    /** A compilation test of API Key class */
    public void testList() {
        SmartUjoImpl<SmartUjoImpl> ujo = new SmartUjoImpl();

        ujo.getList(SmartUjoImpl.PRO_LST1).add(Color.RED);
        ujo.getList(SmartUjoImpl.PRO_LST1).add(Color.GREEN);
        ujo.getList(SmartUjoImpl.PRO_LST1).add(Color.BLUE);

        int i = 0;
        for (Color color : ujo.getList(SmartUjoImpl.PRO_LST1)) {
            ++i;
        }

        assertEquals(3, i);
        assertEquals(3, ujo.getList(SmartUjoImpl.PRO_LST1).size());
    }

}
