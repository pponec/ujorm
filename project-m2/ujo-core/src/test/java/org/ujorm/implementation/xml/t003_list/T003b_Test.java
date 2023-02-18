/*
 * T003a_Test.java
 * JUnit based test
 *
 * Created on 8. June 2007, 23:42
 */

package org.ujorm.implementation.xml.t003_list;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.ujorm.MyTestCase;
import org.ujorm.core.UjoManagerXML;

/**
 *
 * @author Pavel Ponec
 */
public class T003b_Test extends MyTestCase {

    /**
     * Test of printProperties method, of class org.ujorm.person.implementation.imlXML.XmlUjo.
     */
    @Test
    public void testRestoreXML() throws Exception {
        System.out.println("testPrintXML: " + suite());
        StringBuilder writer = new StringBuilder(256);
        //
        UMasterBean person = createMaster();
        UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");

        if (true) {
            System.out.println("XML:\n" + writer);
        }

        ByteArrayInputStream is = new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));
        UMasterBean person2 = UjoManagerXML.getInstance().parseXML(is, UMasterBean.class, false);

        assertEquals(person, person2);

    }


    protected UMasterBean createMaster() {
        UMasterBean masterBean = new UMasterBean();
        //
        UMasterBean.P0_L1ST.addItem(masterBean, createItem());

//        UMasterBean.P1_L1ST.addItem(masterBean, createItem());
//        UMasterBean.P1_L1ST.addItem(masterBean, createItem());

        return masterBean;

    }

    protected UItemBean createItem() {
        UItemBean result = new UItemBean();
        UItemBean.P0_BOOL.setValue(result, true);
        UItemBean.P1_BYTE.setValue(result, Byte.valueOf((byte) 60));
        UItemBean.P2_CHAR.setValue(result, 'A');
        UItemBean.P3_SHORT.setValue(result, Short.valueOf((short) 314));
        UItemBean.P4_INTE.setValue(result, 314000);
        UItemBean.P5_LONG.setValue(result, 123456789L);
        UItemBean.P6_FLOAT.setValue(result, 5.5f);
        UItemBean.P7_DOUBLE.setValue(result, 5.5d);
        UItemBean.P8_BIG_INT.setValue(result, BigInteger.valueOf(300));
        UItemBean.P9_BIG_DECI.setValue(result, BigDecimal.valueOf(300.003));
        UItemBean.PD_DATE.setValue(result, new Date());
        UItemBean.PA_BYTES.setValue(result, new byte[]{ 63,64,65 });
        UItemBean.PB_CHARS.setValue(result, new char[]{ 'X', 'Y', 'X' });
        //
        //-UMasterBean.P0_L1ST.setValue(result, new ArrayList());
        //-UMasterBean.P1_L1ST.setValue(result, new ArrayList());
        //
        return result;
    }
}
