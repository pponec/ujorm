/*
 * T004a_Test.java
 * JUnit based test
 *
 * Created on 8. June 2007, 23:42
 */

package org.ujorm.implementation.xml.t007_body;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.ujorm.MyTestCase;
import org.ujorm.core.UjoManagerXML;

/**
 *
 * @author Pavel Ponec
 */
public class T004c_Test extends MyTestCase {


    /**
     * Test of printProperties method, of class org.apache.person.implementation.imlXML.XmlUjo.
     */
    @Test
    public void testPrintXML() throws Exception {
        System.out.println("testPrintXML: " + suite());
        StringBuilder writer = new StringBuilder(256);
        try {
            AtrPerson person = createPerson();
            // Serialization:
            UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");

            System.out.println("XML==PERSON:\n" + writer);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Test of printProperties method, of class org.ujorm.person.implementation.imlXML.XmlUjo.
     */
    @Test
    public void testRestoreXML() throws Exception {
        System.out.println("testPrintXML: " + suite());
        StringBuilder writer = new StringBuilder(256);
        //
        AtrPerson person = createPerson();
        UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
        ByteArrayInputStream is = new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));
        AtrPerson person2 = UjoManagerXML.getInstance().parseXML(is, AtrPerson.class, false);

        assertEquals(person, person2);
    }


    /** Create persons with different times */
    protected AtrPerson createPerson() {
        AtrPerson result = createPersonOne();
        AtrPerson.MALE.setValue(result, null);
        AtrPerson.NAME.setValue(result, null);

        if (true) {
            AtrPerson.CHILDREN.addItem(result, createPersonOne()); sleep(100);
            AtrPerson.CHILDREN.addItem(result, createPersonOne()); sleep(100);
            AtrPerson.CHILDREN.addItem(result, createPersonOne()); sleep(10);
        } else {
            AtrPerson.CHILDREN.addItem(result, createPersonOne()); sleep(100);
            //AtrPerson.CHILDREN.addItem(result, createPersonOne()); sleep(100);
            //AtrPerson.CHILDREN.addItem(result, createPersonOne()); sleep(10);

        }
        return result;
    }

    protected AtrPerson createPersonOne() {
        AtrPerson result = new AtrPerson();
        AtrPerson.NAME.setValue(result, "Pavel");
        AtrPerson.MALE.setValue(result,  true);
        AtrPerson.BIRTH.setValue(result, new Date());

        return result;
    }
}
