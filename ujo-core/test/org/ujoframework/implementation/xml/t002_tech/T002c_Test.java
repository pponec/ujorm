/*
 * T002a_Test.java
 * JUnit based test
 *
 * Created on 8. June 2007, 23:42
 */

package org.ujoframework.implementation.xml.t002_tech;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import junit.framework.*;
import org.ujoframework.MyTestCase;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoManagerXML;
import org.ujoframework.core.UjoPropertyList;

/**
 *
 * @author Pavel Ponec
 */
public class T002c_Test extends MyTestCase {
    
    public T002c_Test(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(T002c_Test.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        //UjoManager.getInstance().setZeroProviderEnabled(true);
    }
    
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of printProperties method, of class org.apache.person.implementation.imlXML.XmlUjo.
     */
    public void testRestoreXMLc() throws Exception {
        System.out.println("testRestoreXMLc: " + suite().toString());
        //
        CharArrayWriter writer = new CharArrayWriter(256);
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
        UjoPropertyList props = result.readProperties();
        for (UjoProperty prop : props) {
            result.writeValue(prop, null);
        }
        return result;
    }
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
