/*
 * T002a_Test.java
 * JUnit based test
 *
 * Created on 8. June 2007, 23:42
 */

package org.ujorm.implementation.xml.t002_tech;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import junit.framework.*;
import org.ujorm.MyTestCase;
import org.ujorm.Key;
import org.ujorm.core.UjoManagerXML;
import org.ujorm.KeyList;

/**
 *
 * @author Pavel Ponec
 */
public class T002b_Test extends MyTestCase {
    
    public T002b_Test(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(T002b_Test.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        //UjoManager.getInstance().setZeroProviderEnabled(false);
    }
    
    protected void tearDown() throws Exception {
        //UjoManager.getInstance().setZeroProviderEnabled(true);
    }
    
    /**
     * Test of printProperties method, of class org.ujorm.person.implementation.imlXML.XmlUjo.
     */
    public void testRestoreXMLb() throws Exception {
        System.out.println("testPrintXMLb: " + suite().toString());
        StringBuilder writer = new StringBuilder(256);
        //
        UTechnicalBean person = createPerson();
        UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
        if (true) {
            System.err.println("XML:\n" + writer.toString() );
        }
        
        ByteArrayInputStream is = new ByteArrayInputStream(writer.toString().getBytes("UTF-8"));
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
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
