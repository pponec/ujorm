/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org;

import junit.framework.TestCase;

/**
 * Sample core test
 * @author Pavel Ponec
 */
public class SampleCoreTest extends TestCase {

    public SampleCoreTest(String testName) {
        super(testName);
    }

    /** Test of main method, of class SampleCORE. */
    public void testMain() {
        System.out.println("main");
        String[] args = null;
        SampleCORE.main(args);
    }
}
