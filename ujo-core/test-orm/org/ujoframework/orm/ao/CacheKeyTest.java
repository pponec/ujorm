/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.orm.ao;

import junit.framework.TestCase;

/**
 *
 * @author pavel
 */
public class CacheKeyTest extends TestCase {
    
    public CacheKeyTest(String testName) {
        super(testName);
    }

    private static Class suite() {
        return CacheKeyTest.class;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // -----------------------------------------

    /**
     * Test of equals method, of class CacheKey.
     */
    public void testEquals() {
        System.out.println("equals");
        Object obj = null;
        CacheKey instance = null;
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    // -----------------------------------------------------


    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
}
