/*
 * T002a_Test.java
 * JUnit based test
 *
 * Created on 8. June 2007, 23:42
 */

package org.ujorm.implementation.xml.t002_tech;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import junit.framework.*;
import org.ujorm.MyTestCase;
import org.ujorm.core.UjoManagerXML;

/**
 *
 * @author Pavel Ponec
 */
public class T002a_Test extends MyTestCase {

    /**
     * Test of printProperties method, of class org.apache.person.implementation.imlXML.XmlUjo.
     */
    public void testPrintXML() throws Exception {
        System.out.println("testPrintXML: " + suite());
        StringBuilder writer = new StringBuilder(256);
        try {
            UTechnicalBean person = createPerson();
            UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");

            System.out.println("XML:\n" + writer);
        } catch (RuntimeException | OutOfMemoryError ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Test of printProperties method, of class org.ujorm.person.implementation.imlXML.XmlUjo.
     */
    public void testRestoreXML() throws Exception {
        System.out.println("testPrintXML: " + suite());
        StringBuilder writer = new StringBuilder(256);
        //
        UTechnicalBean person = createPerson();
        UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
        ByteArrayInputStream is = new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));
        UTechnicalBean person2 = UjoManagerXML.getInstance().parseXML(is, UTechnicalBean.class, false);

        assertEquals(person, person2);
    }


    protected UTechnicalBean createPerson() {
        UTechnicalBean result = new UTechnicalBean();
        UTechnicalBean.P0_BOOL.setValue(result, true);
        UTechnicalBean.P1_BYTE.setValue(result, Byte.valueOf((byte) 60));
        UTechnicalBean.P2_CHAR.setValue(result, 'A');
        UTechnicalBean.P3_SHORT.setValue(result, Short.valueOf((short) 314));
        UTechnicalBean.P4_INTE.setValue(result, 314000);
        UTechnicalBean.P5_LONG.setValue(result, 123456789L);
        UTechnicalBean.P6_FLOAT.setValue(result, 5.5f);
        UTechnicalBean.P7_DOUBLE.setValue(result, 5.5d);
        UTechnicalBean.P8_BIG_INT.setValue(result, BigInteger.valueOf(300));
        UTechnicalBean.P9_BIG_DECI.setValue(result, BigDecimal.valueOf(300.003));
        UTechnicalBean.PD_DATE.setValue(result, new Date());
        UTechnicalBean.PA_BYTES.setValue(result, new byte[]{ 63,64,65 });
        UTechnicalBean.PB_CHARS.setValue(result, new char[]{ 'X', 'Y', 'X' });

        return result;
    }
}
