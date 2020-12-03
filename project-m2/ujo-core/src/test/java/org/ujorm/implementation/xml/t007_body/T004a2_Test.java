/*
 * T004a2_Test.java
 * JUnit based test
 *
 * Created on 8. June 2007, 23:42
 */

package org.ujorm.implementation.xml.t007_body;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.util.Date;
import junit.framework.*;
import org.ujorm.MyTestCase;
import org.ujorm.core.UjoManagerXML;

/**
 *
 * @author Pavel Ponec
 */
public class T004a2_Test extends MyTestCase {
    
    public T004a2_Test(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(T004a2_Test.class);
        return suite;
    }
    
    /**
     * Test of printProperties method, of class org.apache.person.implementation.imlXML.XmlUjo.
     */
    public void testPrintXML() throws Exception {
        System.out.println("testPrintXML: " + suite().toString());
        StringBuilder writer = new StringBuilder(256);
        try {
            AtrPerson2 person = createPerson();
            // Serialization:
            UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
            
            System.out.println("XML==PERSON:\n" + writer.toString());
        } catch (RuntimeException | OutOfMemoryError ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Test of printProperties method, of class org.ujorm.person.implementation.imlXML.XmlUjo.
     */
    public void testRestoreXML() throws Exception {
        System.out.println("testPrintXML: " + suite().toString());
        StringBuilder writer = new StringBuilder(256);
        //
        AtrPerson2 person = createPerson();
        UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
        ByteArrayInputStream is = new ByteArrayInputStream(writer.toString().getBytes("UTF-8"));
        AtrPerson2 person2 = UjoManagerXML.getInstance().parseXML(is, AtrPerson2.class, false);
        
        assertEquals(person, person2);
    }
    
    
    
    protected AtrPerson2 createPerson() {
        AtrPerson2 result = new AtrPerson2();
        AtrPerson2.NAME.setValue(result, "Pavel");
        AtrPerson2.MALE.setValue(result,  true);
        AtrPerson2.BIRTH.setValue(result, new Date());
        
        return result;
    }
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
