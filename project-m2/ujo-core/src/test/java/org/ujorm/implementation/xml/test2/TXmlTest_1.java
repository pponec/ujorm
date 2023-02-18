/*
 * TXmlTest_1.java
 * JUnit based test
 *
 * Created on 8. June 2007, 23:42
 */
package org.ujorm.implementation.xml.test2;

import java.io.CharArrayWriter;
import java.util.ArrayList;
import junit.framework.*;
import org.ujorm.core.UjoManagerXML;
import org.ujorm.extensions.AbstractUjo;

/**
 *
 * @author Pavel Ponec
 */
public class TXmlTest_1 extends TestCase {


    /**
     * Test of printProperties method, of class org.ujorm.implementation.imlXML.MapUjo.
     */
    public void testPrintXMLRoot() throws Exception {
        System.out.println("testPrintXMLRoot: " + suite());

        CharArrayWriter writer = null;
        try {
            writer = new CharArrayWriter(256);

            AbstractUjo ujo = createUjoRoot();
            UjoManagerXML.getInstance().saveXML(writer, ujo, null, "TEST");

            System.err.println("XML Root:" + writer);
        } catch (RuntimeException ex) {
            ex.printStackTrace();

            if (writer != null) {
                System.err.println("XML ERROR:" + writer);
            }
        }
    }

    protected AbstractUjo createUjoRoot() {
        TXmlUjoRoot ujb = new TXmlUjoRoot();

        ArrayList<TXmlUjoItem> list = new ArrayList<TXmlUjoItem>();
        list.add(createUjoItem());
        list.add(createUjoItem());

        TXmlUjoRoot.PRO_P5.setValue(ujb, list);

        return ujb;
    }

    protected TXmlUjoItem createUjoItem() {
        TXmlUjoItem ujb = new TXmlUjoItem();
        return ujb;
    }
}
