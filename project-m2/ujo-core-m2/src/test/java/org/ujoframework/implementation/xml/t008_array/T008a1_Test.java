/*
 * T008a1_Test.java
 * JUnit based test
 *
 * Created on 8. June 2007, 23:42
 */

package org.ujoframework.implementation.xml.t008_array;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.util.Calendar;
import java.util.Date;
import junit.framework.*;
import org.ujoframework.MyTestCase;
import org.ujoframework.core.UjoManagerXML;

/**
 *
 * @author Pavel Ponec
 */
public class T008a1_Test extends MyTestCase {
    
    public T008a1_Test(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(T008a1_Test.class);
        return suite;
    }
    
    /**
     * Test of printProperties method, of class org.apache.person.implementation.imlXML.XmlUjo.
     */
    public void testPrintXML() throws Exception {
        System.out.println("testPrintXML: " + suite().toString());
        CharArrayWriter writer = new CharArrayWriter(256);
        try {
            AtrPerson person = createPerson();
            // Serialization:
            UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
            
            System.out.println("XML==PERSON:\n" + writer.toString());
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Test of printProperties method, of class org.ujoframework.person.implementation.imlXML.XmlUjo.
     */
    public void testRestoreXML() throws Exception {
        System.out.println("testPrintXML: " + suite().toString());
        CharArrayWriter writer = new CharArrayWriter(256);
        //
        AtrPerson person = createPerson();
        UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
        ByteArrayInputStream is = new ByteArrayInputStream(writer.toString().getBytes("UTF-8"));
        AtrPerson person2 = UjoManagerXML.getInstance().parseXML(is, AtrPerson.class, false);
        AtrPerson.NUMBERS.of(person2); AtrPerson.NUMBERS.of(person);
        assertEquals(person, person2);
    }
    
    
    @SuppressWarnings("deprecation")
    protected AtrPerson createPerson() {
        AtrPerson result = new AtrPerson();
        AtrPerson.NAME.setValue(result, "Pavel");
        AtrPerson.MALE.setValue(result,  true);
        AtrPerson.BIRTH.setValue(result, new java.sql.Date(2009, Calendar.APRIL, 12));
        AtrPerson.NUMBERS.addItem(result, 10);
        AtrPerson.NUMBERS.addItem(result, 20);
        AtrPerson.NUMBERS.addItem(result, 30);
        AtrPerson.NUMBERS.addItem(result, 40);

        
        return result;
    }
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
