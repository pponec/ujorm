/*
 * XmlTest_1.java
 * JUnit based test
 *
 * Created on 8. June 2007, 23:42
 */

package org.ujorm.implementation.xml;

import java.io.CharArrayWriter;
import java.util.ArrayList;
import java.util.Date;
import junit.framework.*;
import org.ujorm.core.UjoManagerXML;
import org.ujorm.implementation.map.MapUjo;

/**
 * Short XmlTest_2
 * @author Pavel Ponec
 */
public class XmlTest_2 extends TestCase {
    
    public XmlTest_2(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(XmlTest_2.class);
        return suite;
    }
    
    
    
    /**
     * Test of printProperties method, of class org.ujorm.implementation.imlXML.MapUjo.
     */
    public void testPrintXMLRoot() throws Exception {
        System.out.println("testTime: " + suite().toString());
        
        CharArrayWriter writer = null;
        try {
            writer = new CharArrayWriter(256);
            MapUjo ujo = createUjoRoot2();
            
            UjoManagerXML.getInstance().saveXML(writer, ujo, null, "TEST");
            
            System.err.println("XML Root:\n" + writer.toString());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            
            if (writer!=null) {
                System.err.println("XML ERROR:" + writer.toString());
            }
        }
    }
    
    protected MapUjo createUjoRoot2() {
//        Long    o0 = new Long(Long.MAX_VALUE);
//        Integer o1 = new Integer(1);
//        String  o2 ="TEST";
//        Date    o3 = new Date();
        Object[]  o4 = new Object[2];
        ArrayList<Object> o5 = new ArrayList<Object>();
        
        // Array & List:
        o4[0] = createUjo();
        o4[1] = createUjo();
        o5.add(createUjo());
        o5.add(null);
        o5.add(createUjo());
        
        XmlUjoRoot_2 ujo = new XmlUjoRoot_2();
        
//        ujo.writeValue(ujo.PRO_P0, o0);
//        ujo.writeValue(ujo.PRO_P1, createUjo());
//        ujo.writeValue(ujo.PRO_P2, o2);
//        ujo.writeValue(ujo.PRO_P3, createUjo());
        XmlUjoRoot_2.PRO_P4.setValue(ujo, o4);
        XmlUjoRoot_2.PRO_P5.setValue(ujo, o5);
        
        return ujo;
    }
    
    
    
    protected MapUjo createUjo() {
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);
        
        XmlUjoItem ujb = new XmlUjoItem();
        
        ujb.writeValue(ujb.PRO_P0, o0);
        ujb.writeValue(ujb.PRO_P1, o1);
        ujb.writeValue(ujb.PRO_P2, o2);
        ujb.writeValue(ujb.PRO_P3, o3);
        ujb.writeValue(ujb.PRO_P4, o4);
        
        return ujb;
    }
    
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
