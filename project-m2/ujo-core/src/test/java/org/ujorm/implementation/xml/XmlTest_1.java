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
import java.util.List;
import junit.framework.*;
import org.ujorm.core.UjoManagerXML;
import org.ujorm.implementation.map.MapUjo;

/**
 * @author Pavel Ponec
 */
public class XmlTest_1 extends TestCase {
    
    public XmlTest_1(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(XmlTest_1.class);
        return suite;
    }
    
    
    /**
     * Test of printProperties method, of class org.ujorm.implementation.imlXML.MapUjo.
     */
    public void testPrintXML() throws Exception {
        StringBuilder writer = new StringBuilder(256);
        MapUjo ujo = createUjo();
        UjoManagerXML.getInstance().saveXML(writer, ujo, null, "TEST");
        
        System.err.println("XML:" + writer.toString());
    }
    
    /**
     * Test of printProperties method, of class org.ujorm.implementation.imlXML.MapUjo.
     */
    public void testPrintXMLRoot() throws Exception {
                System.out.println("testTime: " + suite().toString());
                
        CharArrayWriter writer = null;
        try {
            writer = new CharArrayWriter(256);
            
            MapUjo ujo = createUjoRoot();
            UjoManagerXML.getInstance().saveXML(writer, ujo, null, "TEST");
            
            System.out.println("XML Root:" + writer.toString());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            
            if (writer!=null) {
               System.out.println("XML ERROR:" + writer.toString());
            }
        }
    }
    
    /**
     * Test of printProperties method, of class org.ujorm.implementation.imlXML.MapUjo.
     */
    public void testPrintXMLRoot2() throws Exception {
        System.out.println("testPrintXMLRoot2 ---");
        
        CharArrayWriter writer = null;
        try {
            writer = new CharArrayWriter(256);
           
            MapUjo ujo = createUjoRoot2();
            UjoManagerXML.getInstance().saveXML(writer, ujo, null, "TEST");
            
            System.err.println("XML Root2: \n" + writer.toString());
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            
            if (writer!=null) {
               System.err.println("XML ERROR2: \n" + writer.toString());
            }
        }
    }
    
    
    protected MapUjo createUjoRoot() {
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Float   o4 = new Float(123456.456);
        
        XmlUjoRoot_1 ujb = new XmlUjoRoot_1();
        
        ujb.writeValue(ujb.PRO_P0, o0);
        ujb.writeValue(ujb.PRO_P1, createUjo());
        ujb.writeValue(ujb.PRO_P2, o2);
        ujb.writeValue(ujb.PRO_P3, createUjo());
        
        return ujb;
    }
    
    @SuppressWarnings("unchecked")
    protected MapUjo createUjoRoot2() {
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        List    o4 = new ArrayList();
        Object[]o5 = new Object[2];
        
        // Array & List:
        o4.add(createUjo());
        o4.add(createUjo());
        o4.add(createUjo());
        o5[0] = createUjo();
        o5[1] = new Object();
        
        XmlUjoRoot_1 ujb = new XmlUjoRoot_1();
        
        ujb.writeValue(ujb.PRO_P0, o0);
        ujb.writeValue(ujb.PRO_P1, createUjo());
        ujb.writeValue(ujb.PRO_P2, o2);
        ujb.writeValue(ujb.PRO_P3, createUjo());
        ujb.writeValue(ujb.PRO_P5, o4);
        ujb.writeValue(ujb.PRO_P4, o5);
        
        return ujb;
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
