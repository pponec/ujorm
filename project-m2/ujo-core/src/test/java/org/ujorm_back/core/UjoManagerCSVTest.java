/*
 * UjoManagerCSVTest.java
 * JUnit based test
 *
 * Created on 3. May 2008, 20:26
 */

package org.ujorm_back.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.ujorm_back.MyTestCase;
import org.ujorm.core.UjoManagerCSV;
import org.ujorm_back.core.ujos.UjoCSV;

/**
 *
 * @author Pavel Ponec
 */
public class UjoManagerCSVTest extends MyTestCase {
    
    protected UjoManagerCSV<UjoCSV> manager;
    protected Object context = "CSV-Context";
    protected UjoCSV ujo;
    protected List<UjoCSV> ujoList;
    
    
    public UjoManagerCSVTest(String testName) {
        super(testName);
    }
    
    private static Class suite() {
        return UjoManagerCSVTest.class;
    }
    
    protected void setUp() throws Exception {
        manager = UjoManagerCSV.getInstance(UjoCSV.class);
        context = "CSV-Context";
        ujo = new UjoCSV();
        ujoList = new ArrayList<UjoCSV>();
        ujoList.add(ujo);
    }
    
    /**
     * Test of saveCSV method, of class org.ujorm.core.UjoManagerCSV.
     */
    public void testSaveCSV() throws Exception {
        System.out.println("saveCSV");
        //
        UjoCSV.P1.setValue(ujo, "A");
        UjoCSV.P2.setValue(ujo, "B");
        UjoCSV.P3.setValue(ujo, "C");
        //
        ByteArrayOutputStream out = createOS("F1");
        manager.saveCSV(out, null, ujoList, context);
        out.close();
        
        ByteArrayInputStream is = createIS(out);
        List<UjoCSV> list2 = manager.loadCSV(new Scanner(is), context);
        
        assertEquals(ujoList.size(), list2.size());
        
        UjoCSV u1 = ujoList.get(0);
        UjoCSV u2 = list2.get(0);
        assertEquals(u1, u2);
        
        // PrintIt
        // System.out.print("-----\n" + out.toString("windows-1250") + "\n-----\n");
    }
    
    public void testSaveCSV2() throws Exception {
        System.out.println("saveCSV-2");
        //
        UjoCSV.P1.setValue(ujo, "");
        UjoCSV.P2.setValue(ujo, "");
        UjoCSV.P3.setValue(ujo, "");
        //
        ByteArrayOutputStream out = createOS("F1");
        manager.saveCSV(out, null, ujoList, context);
        out.close();
        
        ByteArrayInputStream is = createIS(out);
        List<UjoCSV> list2 = manager.loadCSV(new Scanner(is), context);
        
        assertEquals(ujoList.size(), list2.size());
        
        UjoCSV u1 = ujoList.get(0);
        UjoCSV u2 = list2.get(0);
        assertEquals(u1, u2);
        
        // PrintIt
        //System.out.print("-----\n" + out.toString("windows-1250") + "\n-----\n");
    }

    
    public void testSaveCSV3() throws Exception {
        System.out.println("saveCSV-3");
        //
        UjoCSV.P1.setValue(ujo, ";");
        UjoCSV.P2.setValue(ujo, "\"");
        UjoCSV.P3.setValue(ujo, "\"-;-\"\"");
        //
        ByteArrayOutputStream out = createOS("F1");
        manager.saveCSV(out, null, ujoList, context);
        out.close();
        
        ByteArrayInputStream is = createIS(out);
        List<UjoCSV> list2 = manager.loadCSV(new Scanner(is), context);
        
        assertEquals(ujoList.size(), list2.size());
        
        UjoCSV u1 = ujoList.get(0);
        UjoCSV u2 = list2.get(0);
        assertEquals(u1, u2);
        
        // PrintIt
        System.out.print("-----\n" + out.toString("windows-1250") + "\n-----\n");
    }
    
    public void testSaveCSV4() throws Exception {
        System.out.println("saveCSV-3");
        //
        ujoList.clear();
        //
        ByteArrayOutputStream out = createOS("F1");
        manager.saveCSV(out, null, ujoList, context);
        out.close();
        
        ByteArrayInputStream is = createIS(out);
        List<UjoCSV> list2 = manager.loadCSV(new Scanner(is), context);
        
        assertEquals(ujoList.size(), list2.size());
        
        // PrintIt
        System.out.print("-----\n" + out.toString("windows-1250") + "\n-----\n");
    }
        
    
    
    public ByteArrayOutputStream createOS(String file) {
        ByteArrayOutputStream result = new ByteArrayOutputStream(8000*1000);
        return result;
    }
    
    public ByteArrayInputStream createIS(ByteArrayOutputStream data) {
        ByteArrayInputStream result = new ByteArrayInputStream(data.toByteArray());
        return result;
    }
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
