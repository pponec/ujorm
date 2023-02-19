/*
 * T001a_Test.java
 * JUnit based test
 *
 * Created on 8. June 2007, 23:42
 */

package org.ujorm.implementation.xml.t001_simple;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.ujorm.AbstractTest;
import org.ujorm.core.UjoManagerXML;

/**
 *
 * @author Pavel Ponec
 */
public class T001b_Test extends AbstractTest {


    @Test
    public void test_01 () throws Exception {
        restoreXML(" Naz dar \nxxx ", true);
    }

    /**
     * Test of printProperties method, of class org.ujorm.person.implementation.imlXML.XmlUjo.
     */
    public void restoreXML(String name, boolean printText) throws Exception {
        System.out.println( "restoreXML \"" + name + "\": " + testName());
        StringBuilder writer = new StringBuilder(256);
        //
        UPerson person = createPerson();
        UPerson.NAME.setValue(person, name);
        UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");

        if (printText) {
           System.out.println("XML:\n" + writer);
        }

        ByteArrayInputStream is = new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));
        UPerson person2 = UjoManagerXML.getInstance().parseXML(is, UPerson.class, false);

        assertEquals(person, person2);
    }



    protected UPerson createPerson() {
        UPerson result = new UPerson();
        UPerson.NAME.setValue(result, "Pavel");
        UPerson.MALE.setValue(result,  true);
        UPerson.BIRTH.setValue(result, new Date());

        return result;
    }
}
