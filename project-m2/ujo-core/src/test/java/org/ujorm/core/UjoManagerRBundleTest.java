/*
 * UjoManagerCSVTest.java
 * JUnit based test
 *
 * Created on 3. May 2008, 20:26
 */

package org.ujorm.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.ujorm.MyTestCase;
import org.ujorm.core.ujos.UjoCSV;

/**
 *
 * @author Pavel Ponec
 */
public class UjoManagerRBundleTest extends MyTestCase {

    protected UjoManagerRBundle<UjoCSV> manager;
    protected Object context;
    protected UjoCSV ujo;
    protected String header;


    public UjoManagerRBundleTest(String testName) {
        super(testName);
    }

    private static Class suite() {
        return UjoManagerCSVTest.class;
    }

    @Override
    protected void setUp() throws Exception {
        manager = UjoManagerRBundle.of(UjoCSV.class);
        context = "Bundle-Context";
        header  = "UJO Header";
        ujo = new UjoCSV();
    }

    /**
     * Test of saveBundle method, of class org.ujorm.core.UjoManagerCSV.
     */
    public void testSaveBundle() throws Exception {
        System.out.println("saveBundle");
        //
        UjoCSV.P1.setValue(ujo, "A");
        UjoCSV.P2.setValue(ujo, "B");
        UjoCSV.P3.setValue(ujo, "C");
        //
        ByteArrayOutputStream out = createOS("F1");
        manager.saveResourceBundle(out, ujo, header, context);
        out.close();

        ByteArrayInputStream is = createIS(out);
        UjoCSV ujo2 = manager.loadResourceBundle(is, true, context);

        assertEquals(ujo, ujo2);

        // PrintIt
        // System.out.print("-----\n" + out.toString("windows-1250") + "\n-----\n");
    }

    /**
     * Test of saveBundle method, of class org.ujorm.core.UjoManagerCSV.
     */
    public void testSaveBundle1() throws Exception {
        System.out.println("saveCSV");
        //
        UjoCSV.P1.setValue(ujo, "=");
        UjoCSV.P2.setValue(ujo, "\\");
        UjoCSV.P3.setValue(ujo, " ");
        //
        ByteArrayOutputStream out = createOS("F1");
        manager.saveResourceBundle(out, ujo, header, context);
        out.close();

        ByteArrayInputStream is = createIS(out);
        UjoCSV ujo2 = manager.loadResourceBundle(is, true, context);

        assertEquals(ujo, ujo2);

        // PrintIt
        System.out.print("-----\n" + out.toString("windows-1250") + "\n-----\n");
    }

    /**
     * Test of saveBundle method, of class org.ujorm.core.UjoManagerCSV.
     */
    public void testSaveBundle2A() throws Exception {
        System.out.println("saveBundle2A");
        //
        UjoCSV.P1.setValue(ujo, "A B C D E");
        UjoCSV.P2.setValue(ujo, "");
        UjoCSV.P3.setValue(ujo, null);
        //
        ByteArrayOutputStream out = createOS("F1");
        manager.saveResourceBundle(out, ujo, header, context);
        out.close();

        ByteArrayInputStream is = createIS(out);
        UjoCSV ujo2 = manager.loadResourceBundle(is, true, context);

        assertEquals(ujo, ujo2);

        // PrintIt
        // System.out.print("-----\n" + out.toString("windows-1250") + "\n-----\n");
    }

    /**
     * Test of saveBundle method, of class org.ujorm.core.UjoManagerCSV.
     */
    public void testSaveBundle2B() throws Exception {
        System.out.println("saveBundle2B");
        //
        UjoCSV.P1.setValue(ujo, "A B C D E");
        UjoCSV.P2.setValue(ujo, "");
        UjoCSV.P3.setValue(ujo, null);
        ujo.setOnZeroManager(false);
        //
        ByteArrayOutputStream out = createOS("F1");
        manager.saveResourceBundle(out, ujo, header, context);
        out.close();

        ByteArrayInputStream is = createIS(out);
        UjoCSV ujo2 = manager.loadResourceBundle(is, true, context);
        ujo2.setOnZeroManager(false);

        assertEquals(ujo, ujo2);

        // PrintIt
        System.out.print("-----\n" + out.toString("windows-1250") + "\n-----\n");
    }


    // ----------------------------


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
