/*
 * T004a_Test.java
 * JUnit based test
 *
 * Created on 8. June 2007, 23:42
 */

package org.ujorm.implementation.xml.t005_attrib2;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.ujorm.MyTestCase;
import org.ujorm.core.UjoManagerXML;

/**
 * @author Pavel Ponec
 */
public class T005d_Array_Test extends MyTestCase {


    /**
     * Test of printProperties method, of class org.apache.person.implementation.imlXML.XmlUjo.
     */
    @Test
    public void testPrintXML() throws Exception {
        System.out.println("testPrintXML: " + suite());
        StringBuilder writer = new StringBuilder(256);
        try {
            AtrPersonArray person = createPerson();
            // Serialization:
            UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");

            System.out.println("XML==PERSON:\n" + writer);
        } catch (RuntimeException | OutOfMemoryError ex) {
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
        AtrPersonArray person = createPerson();
        UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
        ByteArrayInputStream is = new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));
        AtrPersonArray person2 = UjoManagerXML.getInstance().parseXML(is, AtrPersonArray.class, false);

        assertEquals(person, person2);
    }


    /** Create persons with different times */
    protected AtrPersonArray createPerson() {
        AtrPersonArray result = createPersonOne();
        AtrPersonArray child  = null;
        AtrPersonArray.CHILDREN.addItem(result, child=createPersonOne());
        AtrPersonArray.CHILDREN.addItem(result, child=createPersonOne());
        AtrPersonArray.CHILDREN.addItem(child , child=createPersonOne());
        AtrPersonArray.CHILDREN.addItem(result, child=createPersonOne());
        AtrPersonArray.CHILDREN.addItem(child , child=createPersonOne());
        AtrPersonArray.CHILDREN.addItem(child , child=createPersonOne());

        return result;
    }

    protected AtrPersonArray createPersonOne() {
        AtrPersonArray result = new AtrPersonArray();
        AtrPersonArray.NAME_ATTR.setValue(result, "ATTRIB");
        AtrPersonArray.NAME_ELEM.setValue(result, "ELEMENT");
        return result;
    }
}
