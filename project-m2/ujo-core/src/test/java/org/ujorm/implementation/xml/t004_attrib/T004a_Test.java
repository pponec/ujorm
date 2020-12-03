/*
 * T004a_Test.java
 * JUnit based test
 *
 * Created on 8. June 2007, 23:42
 */

package org.ujorm.implementation.xml.t004_attrib;

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
public class T004a_Test extends MyTestCase {
    
    public T004a_Test(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(T004a_Test.class);
        return suite;
    }
    
    /**
     * Test of printProperties method, of class org.apache.person.implementation.imlXML.XmlUjo.
     */
    public void testPrintXML() throws Exception {
        System.out.println("testPrintXML: " + suite().toString());
        StringBuilder writer = new StringBuilder(256);
        try {
            AtrPerson person = createPerson();
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
        AtrPerson person = createPerson();
        UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
        ByteArrayInputStream is = new ByteArrayInputStream(writer.toString().getBytes("UTF-8"));
        AtrPerson person2 = UjoManagerXML.getInstance().parseXML(is, AtrPerson.class, false);
        
        assertEquals(person, person2);
    }
    
    
    
    protected AtrPerson createPerson() {
        AtrPerson result = new AtrPerson();
        AtrPerson.NAME.setValue(result, "Pavel");
        AtrPerson.MALE.setValue(result,  true);
        AtrPerson.BIRTH.setValue(result, new Date());
        
        return result;
    }
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
