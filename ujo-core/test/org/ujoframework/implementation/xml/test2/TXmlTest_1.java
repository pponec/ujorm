/*
 * TXmlTest_1.java
 * JUnit based test
 *
 * Created on 8. �erven 2007, 23:42
 */

package org.ujoframework.implementation.xml.test2;

import java.io.CharArrayWriter;
import java.util.ArrayList;
import junit.framework.*;
import org.ujoframework.core.UjoManagerXML;
import org.ujoframework.implementation.map.MapUjo;

/**
 *
 * @author Pavel Ponec
 */
public class TXmlTest_1 extends TestCase {
    
    public TXmlTest_1(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(TXmlTest_1.class);
        return suite;
    }
    
    /**
     * Test of printProperties method, of class org.ujoframework.implementation.imlXML.MapUjo.
     */
    public void testPrintXMLRoot() throws Exception {
        System.out.println("testPrintXMLRoot: " + suite().toString());
        
        CharArrayWriter writer = null;
        try {
            writer = new CharArrayWriter(256);
            
            MapUjo ujo = createUjoRoot();
            UjoManagerXML.getInstance().saveXML(writer, ujo, null, "TEST");
            
            System.err.println("XML Root:" + writer.toString());
        } catch (Throwable ex) {
            ex.printStackTrace();
            
            if (writer!=null) {
                System.err.println("XML ERROR:" + writer.toString());
            }
        }
    }
    
    
    
    protected MapUjo createUjoRoot() {
        TXmlUjoRoot ujb = new TXmlUjoRoot();
        
        
        ArrayList<TXmlUjoItem> list =  new ArrayList<TXmlUjoItem>();
        list.add(createUjoItem());
        list.add(createUjoItem());
        
        ujb.PRO_P5.setValue(ujb, list);
        
        return ujb;
    }
    
    
    
    
    protected TXmlUjoItem createUjoItem() {
        TXmlUjoItem ujb = new TXmlUjoItem();
        return ujb;
    }
    
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
