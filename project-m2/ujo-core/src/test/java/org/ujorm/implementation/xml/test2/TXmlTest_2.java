/*
 * TXmlTest.java
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
public class TXmlTest_2 extends TestCase {

    public TXmlTest_2(String testName) {
        super(testName);
    }

    public static TestSuite suite() {
        TestSuite suite = new TestSuite(TXmlTest_2.class);
        return suite;
    }

    /**
     * Test of printProperties method, of class org.ujorm.implementation.imlXML.MapUjo.
     */
    public void testPrintXMLRoot() throws Exception {
        System.out.println("testPrintXMLRoot: " + suite().toString());

        CharArrayWriter writer = null;
        try {
            writer = new CharArrayWriter(256);

            AbstractUjo ujo = createUjoRoot();
            UjoManagerXML.getInstance().saveXML(writer, ujo, null, "TEST");

            System.err.println("XML Root:" + writer.toString());
        } catch (RuntimeException ex) {
            ex.printStackTrace();

            if (writer != null) {
                System.err.println("XML ERROR:" + writer.toString());
            }
        }
    }

    protected AbstractUjo createUjoRoot() {
        TXmlUjoRoot_2 ujb = new TXmlUjoRoot_2();

        ArrayList<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);

        ujb.PRO_P5.setValue(ujb, list);

        return ujb;
    }

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
}
