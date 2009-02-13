/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.orm;

import junit.framework.TestCase;

/**
 *
 * @author Ponec
 */
public class OrmSampleTest extends TestCase {
    
    public OrmSampleTest(String testName) {
        super(testName);
    }


    private static Class suite() {
        return OrmSampleTest.class;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }



    /**
     * Test of getItemCount method, of class SuperPropertyList.
     */
    public void testGetItemCount() {
    }
 

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }

}
