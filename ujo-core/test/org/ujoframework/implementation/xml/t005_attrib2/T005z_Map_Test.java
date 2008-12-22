/*
 * T004a_Test.java
 * JUnit based test
 *
 * Created on 8. èerven 2007, 23:42
 */

package org.ujoframework.implementation.xml.t005_attrib2;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import junit.framework.*;
import org.ujoframework.MyTestCase;
import org.ujoframework.core.UjoManagerXML;

/**
 * @author Pavel Ponec
 */
public class T005z_Map_Test extends MyTestCase {
    
    public T005z_Map_Test(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(T005z_Map_Test.class);
        return suite;
    }
    
    /**
     * Test of printProperties method, of class org.apache.person.implementation.imlXML.XmlUjo.
     */
    public void testPrintXML() throws Exception {
        System.out.println("testPrintXML: " + suite().toString());
        CharArrayWriter writer = new CharArrayWriter(256);
        try {
            AtrPersonMap person = createPerson();
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
        AtrPersonMap person = createPerson();
        UjoManagerXML.getInstance().saveXML(writer, person, null, "TEST");
        ByteArrayInputStream is = new ByteArrayInputStream(writer.toString().getBytes("UTF-8"));
        AtrPersonMap person2 = UjoManagerXML.getInstance().parseXML(is, AtrPersonMap.class, false);
        
        assertEquals(person, person2);
    }
    
    
    /** Create persons with different times */
    protected AtrPersonMap createPerson() {
        AtrPersonMap result = createPersonOne();
        AtrPersonMap child  = null;
        AtrPersonMap.CHILDS.addItem(result, child=createPersonOne());
        AtrPersonMap.CHILDS.addItem(result, child=createPersonOne());
        AtrPersonMap.CHILDS.addItem(child , child=createPersonOne());
        AtrPersonMap.CHILDS.addItem(result, child=createPersonOne());
        AtrPersonMap.CHILDS.addItem(child , child=createPersonOne());
        AtrPersonMap.CHILDS.addItem(child , child=createPersonOne());
        
        return result;
    }
    
    protected AtrPersonMap createPersonOne() {
        AtrPersonMap result = new AtrPersonMap();
        AtrPersonMap.NAME_ATTR.setValue(result, "ATTRIB");
        AtrPersonMap.NAME_ELEM.setValue(result, "ELEMENT");
        return result;
    }
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
