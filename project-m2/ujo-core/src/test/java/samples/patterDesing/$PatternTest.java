/*
 * $PatternTest.java
 * JUnit based test
 *
 * Created on 16. June 2007, 15:23
 */

package samples.patterDesing;

import java.util.ArrayList;
import junit.framework.*;
import java.util.List;

/**
 *
 * @author Pavel Ponec
 */
public class $PatternTest extends TestCase {
    
    public $PatternTest(String testName) {
        super(testName);
    }
    
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite($PatternTest.class);
        return suite;
    }      


    /**
     * Test of getItemClass method, of class org.ujorm.core.UjoPersistentManager.
     */
    public void testPattern() {
        new MyMap.UsageTest();
    }


    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
