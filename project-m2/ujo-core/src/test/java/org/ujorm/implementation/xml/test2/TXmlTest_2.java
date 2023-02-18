/*
 * TXmlTest.java
 * JUnit based test
 *
 * Created on 8. June 2007, 23:42
 */
package org.ujorm.implementation.xml.test2;

import java.util.ArrayList;
import junit.framework.*;
import org.ujorm.core.UjoManagerXML;
import org.ujorm.extensions.AbstractUjo;

/**
 *
 * @author Pavel Ponec
 */
public class TXmlTest_2 extends org.junit.jupiter.api.Assertions {

    /**
     * Test of printProperties method, of class org.ujorm.implementation.imlXML.MapUjo.
     */
    public void testPrintXMLRoot() throws Exception {

        final StringBuilder writer = new StringBuilder(256);
        try {
            AbstractUjo ujo = createUjoRoot();
            UjoManagerXML.getInstance().saveXML(writer, ujo, null, "TEST");

            System.err.println("XML Root:" + writer);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }

    protected AbstractUjo createUjoRoot() {
        TXmlUjoRoot_2 ujb = new TXmlUjoRoot_2();

        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);

        TXmlUjoRoot_2.PRO_P5.setValue(ujb, list);

        return ujb;
    }
}
