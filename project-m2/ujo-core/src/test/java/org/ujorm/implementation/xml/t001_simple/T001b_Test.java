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
public class T001b_Test extends MyTestCase {
    
    public T001b_Test(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(T001b_Test.class);
        return suite;
    }
    
    public void test_01 () throws Exception {
        restoreXML(" Naz dar \nxxx ", true);
    }
    
    /**
     * Test of printProperties method, of class org.ujorm.person.implementation.imlXML.XmlUjo.
     */
    public void restoreXML(String name, boolean printText) throws Exception {
        System.out.println( "restoreXML \"" + name + "\": " + suite().toString() );
        StringBuilder writer = new StringBuilder(256);
        //
        UPerson person = createPerson();
        UPerson.NAME.setValue(person, name);
        UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
        
        if (printText) {
           System.out.println("XML:\n" + writer.toString());
        }
        
        ByteArrayInputStream is = new ByteArrayInputStream(writer.toString().getBytes("UTF-8"));
        UPerson person2 = UjoManagerXML.getInstance().parseXML(is, UPerson.class, false);
        
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
