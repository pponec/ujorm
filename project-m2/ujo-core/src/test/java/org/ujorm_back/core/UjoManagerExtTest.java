/*
 * UjoManagerExtTest.java
 * JUnit based test
 *
 * Created on 16. June 2007, 15:23
 */

package org.ujorm_back.core;

import junit.framework.*;

/**
 *
 * @author Pavel Ponec
 */
public class UjoManagerExtTest extends TestCase {
    
    public UjoManagerExtTest(String testName) {
        super(testName);
    }
    
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(UjoManagerExtTest.class);
        return suite;
    }      


    /**
     * Test of getItemClass method, of class org.ujorm.core.UjoPersistentManager.
     */
    public void testGetItemClass() {
        System.out.println("getItemClass");
        

        
    }


    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
