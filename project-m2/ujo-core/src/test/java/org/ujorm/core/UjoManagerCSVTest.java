/*
 * UjoManagerCSVTest.java
 * JUnit based test
 *
 * Created on 3. May 2008, 20:26
 */

package org.ujorm.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.ujorm.MyTestCase;
import org.ujorm.core.ujos.UjoCSV;
import static org.ujorm.core.ujos.UjoCSV.*;

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

        ujo.set(P1, "A");
        ujo.set(P2, "B");
        ujo.set(P3, "C");

        UjoManagerCSV manager = UjoManagerCSV.getInstance(UjoCSV.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // Save the ujoList to an outputStream:
        manager.saveCSV(out, UjoManagerCSV.UTF_8, ujoList, "CSV-Context");

        String outputExpected = MessageFormat.format("P1;P2;P3{0}A;B;C", System.getProperty("line.separator"));
        String output = new String(out.toByteArray(), "utf-8").trim();
        assertEquals(outputExpected, output);

        // Restore original objects from the byte array:
        InputStream is = new ByteArrayInputStream(out.toByteArray());
        List<UjoCSV> result = manager.loadCSV(new Scanner(is), "CSV-Context");

        // Check the first objects
        assertEquals(ujoList.get(0), result.get(0));
        assertEquals(ujoList.size(), result.size());

        // PrintIt
        // System.out.print("-----\n" + out.toString("utf-8") + "\n-----\n");
    }

    public void testSaveCSV2() throws Exception {
        System.out.println("saveCSV-2");
        //
        ujo.set(P1, "");
        ujo.set(P2, "");
        ujo.set(P3, "");
        //
        ByteArrayOutputStream out = createOutputStream();
        manager.saveCSV(out, null, ujoList, context);
        out.close();

        ByteArrayInputStream is = createInputStream(out);
        List<UjoCSV> list2 = manager.loadCSV(new Scanner(is), context);

        assertEquals(ujoList.size(), list2.size());

        UjoCSV u1 = ujoList.get(0);
        UjoCSV u2 = list2.get(0);
        assertEquals(u1, u2);

        // PrintIt
        //System.out.print("-----\n" + out.toString("utf-8") + "\n-----\n");
    }


    public void testSaveCSV3() throws Exception {
        System.out.println("saveCSV-3");
        //
        UjoCSV.P1.setValue(ujo, ";");
        UjoCSV.P2.setValue(ujo, "\"");
        UjoCSV.P3.setValue(ujo, "\"-;-\"\"");
        //
        ByteArrayOutputStream out = createOutputStream();
        manager.saveCSV(out, null, ujoList, context);
        out.close();

        ByteArrayInputStream is = createInputStream(out);
        List<UjoCSV> list2 = manager.loadCSV(new Scanner(is), context);

        assertEquals(ujoList.size(), list2.size());

        UjoCSV u1 = ujoList.get(0);
        UjoCSV u2 = list2.get(0);
        assertEquals(u1, u2);

        // PrintIt
        // System.out.print("-----\n" + out.toString("utf-8") + "\n-----\n");
    }

    public void testSaveCSV4() throws Exception {
        System.out.println("saveCSV-4");
        //
        ujoList.clear();
        //
        ByteArrayOutputStream out = createOutputStream();
        manager.saveCSV(out, null, ujoList, context);
        out.close();

        ByteArrayInputStream is = createInputStream(out);
        List<UjoCSV> list2 = manager.loadCSV(new Scanner(is), context);

        assertEquals(ujoList.size(), list2.size());

        // PrintIt
        // System.out.print("-----\n" + out.toString("utf-8") + "\n-----\n");
    }

    /** Modify or validate the CSV header */
    public void testSaveCSV5() throws Exception {
        System.out.println("saveCSV");
        //
        UjoCSV.P1.setValue(ujo, "A");
        UjoCSV.P2.setValue(ujo, "B");
        UjoCSV.P3.setValue(ujo, "C");
        //
        ByteArrayOutputStream out = createOutputStream();
        UjoManagerCSV manager5 = UjoManagerCSV.getInstance(UjoCSV.class);
        manager5.setHeaderContent("Ah","Bh","Ch");

        manager5.saveCSV(out, null, ujoList, context);
        out.close();

        ByteArrayInputStream is = createInputStream(out);
        List<UjoCSV> list2 = manager5.loadCSV(new Scanner(is), context);

        assertEquals(ujoList.size(), list2.size());

        UjoCSV u1 = ujoList.get(0);
        UjoCSV u2 = list2.get(0);
        assertEquals(u1, u2);

        // PrintIt
        // System.out.print("-----\n" + out.toString("utf-8") + "\n-----\n");

        try {
            manager5.setHeaderContent("wrong header");
            ByteArrayInputStream is2 = createInputStream(out);
            List<UjoCSV> list2b = manager5.loadCSV(new Scanner(is2), context);
            assertTrue("Wrong header", false);
        } catch (IllegalStateException e) {
            assertTrue("Wrong header", true);
        }
    }

    // ------------------------------------------------

    public ByteArrayOutputStream createOutputStream() {
        ByteArrayOutputStream result = new ByteArrayOutputStream(8000*1000);
        return result;
    }

    public ByteArrayInputStream createInputStream(ByteArrayOutputStream data) {
        ByteArrayInputStream result = new ByteArrayInputStream(data.toByteArray());
        return result;
    }

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }

}
