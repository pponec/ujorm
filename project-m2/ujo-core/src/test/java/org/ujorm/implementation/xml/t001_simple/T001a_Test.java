/*
 * T001a_Test.java
 * JUnit based test
 *
 * Created on 8. June 2007, 23:42
 */

package org.ujorm.implementation.xml.t001_simple;

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
public class T001a_Test extends MyTestCase {
    
    public T001a_Test(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(T001a_Test.class);
        return suite;
    }
    
    /**
     * Test of printProperties method, of class org.apache.person.implementation.imlXML.XmlUjo.
     */
    public void testPrintXML() throws Exception {
        System.out.println("testPrintXML: " + suite().toString());
        CharArrayWriter writer = new CharArrayWriter(256);
        try {
            UPerson person = createPerson();
            // Serialization:
            UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
            
            System.out.println("XML==PERSON:\n" + writer.toString());
        } catch (RuntimeException  | OutOfMemoryError ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Test of printProperties method, of class org.ujorm.person.implementation.imlXML.XmlUjo.
     */
    public void testRestoreXML() throws Exception {
        System.out.println("testPrintXML: " + suite().toString());
        CharArrayWriter writer = new CharArrayWriter(256);
        //
        UPerson person = createPerson();
        UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
        ByteArrayInputStream is = new ByteArrayInputStream(writer.toString().getBytes("UTF-8"));
        UPerson person2 = UjoManagerXML.getInstance().parseXML(is, UPerson.class, false);
        
        assertEquals(person, person2);
    }
    
    /**
     * Test of printProperties method, of class org.ujorm.person.implementation.imlXML.XmlUjo.
     */
    public void testEncodedXML() throws Exception {
        System.out.println("testPrintXML: " + suite().toString());
        CharArrayWriter writer = new CharArrayWriter(256);
        String ENCODE = "windows-1250";
        String personName = "ÁĚŠČŘŽÝÁÍÉ-áěščřžýáíé";
        //
        UPerson person = createPerson();
        UPerson.NAME.setValue(person, personName);
        UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
        String xmlContent = writer.toString().replace("UTF-8", ENCODE);
        ByteArrayInputStream is = new ByteArrayInputStream(xmlContent.getBytes(ENCODE));
        UPerson person2 = UjoManagerXML.getInstance().parseXML(is, UPerson.class, false);

        assertEquals(personName, UPerson.NAME.of(person2));
        assertEquals(person, person2);
    }
        
    protected UPerson createPerson() {
        UPerson result = new UPerson();
        UPerson.NAME.setValue(result, "Pavel");
        UPerson.MALE.setValue(result,  true);
        UPerson.BIRTH.setValue(result, new Date());
        
        return result;
    }
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
