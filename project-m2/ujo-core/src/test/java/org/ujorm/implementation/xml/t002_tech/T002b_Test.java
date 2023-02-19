/*
 * T002a_Test.java
 * JUnit based test
 *
 * Created on 8. June 2007, 23:42
 */

package org.ujorm.implementation.xml.t002_tech;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.ujorm.AbstractTest;
import org.ujorm.Key;
import org.ujorm.core.UjoManagerXML;

/**
 *
 * @author Pavel Ponec
 */
public class T002b_Test extends AbstractTest {

    /**
     * Test of printProperties method, of class org.ujorm.person.implementation.imlXML.XmlUjo.
     */
    @Test
    public void testRestoreXMLb() throws Exception {
        System.out.println("testPrintXMLb: " + testName());
        StringBuilder writer = new StringBuilder(256);
        //
        UTechnicalBean person = createPerson();
        UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
        if (true) {
            System.err.println("XML:\n" + writer);
        }

        ByteArrayInputStream is = new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));
        UTechnicalBean person2 = UjoManagerXML.getInstance().parseXML(is, UTechnicalBean.class, false);
        assertEquals(person, person2);
    }

    protected UTechnicalBean createPerson() {
        UTechnicalBean result = new UTechnicalBean();
        for (Key prop : result.readKeys()) {
            result.writeValue(prop, null);
        }
        return result;
    }
}
