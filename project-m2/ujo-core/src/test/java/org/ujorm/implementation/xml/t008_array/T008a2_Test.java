/*
 * T008a1_Test.java
 * JUnit based test
 *
 * Created on 8. June 2007, 23:42
 */

package org.ujorm.implementation.xml.t008_array;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.util.Calendar;
import junit.framework.*;
import org.ujorm.MyTestCase;
import org.ujorm.core.UjoManagerXML;

/**
 *
 * @author Pavel Ponec
 */
public class T008a2_Test extends MyTestCase {
    
    public T008a2_Test(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(T008a2_Test.class);
        return suite;
    }
    
    /**
     * Test of printProperties method, of class org.apache.person.implementation.imlXML.XmlUjo.
     */
    public void x_testPrintXML() throws Exception {
        System.out.println("testPrintXML: " + suite().toString());
        StringBuilder writer = new StringBuilder(256);
        try {
            AtrPerson person = createPerson();
            // Serialization:
            UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
            
            System.out.println("XML==PERSON:\n" + writer.toString());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Test of printProperties method, of class org.ujorm.person.implementation.imlXML.XmlUjo.
     * TODO.pop: to implement the nextfeature:
     */
    public void testRestoreXML() throws Exception {
        System.out.println("testPrintXML: " + suite().toString());
        StringBuilder writer = new StringBuilder(256);
        //
        AtrPerson person = createPerson();
        UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
        
        /** Replace numbers to elements */
        String body = writer.toString()
                .replace("<Child>10,20,30,40</Child>"
                , "<Child>10</Child>"
                + "<Child>20</Child>"
                + "<Child>30</Child>"
                + "<Child>40</Child>");
        ByteArrayInputStream is = new ByteArrayInputStream(body.toString().getBytes("UTF-8"));        
        AtrPerson person2 = UjoManagerXML.getInstance().parseXML(is, AtrPerson.class, false);
        
        int size1 = AtrPerson.NUMBERS.of(person).size();
        int size2 = AtrPerson.NUMBERS.of(person2).size(); 
        assertEquals(size1, size2);
        assertEquals(10, AtrPerson.NUMBERS.getItem(person , 0).intValue());
        assertEquals(10, AtrPerson.NUMBERS.getItem(person2, 0).intValue());
        assertEquals(20, AtrPerson.NUMBERS.getItem(person , 1).intValue());
        assertEquals(20, AtrPerson.NUMBERS.getItem(person2, 1).intValue());
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
