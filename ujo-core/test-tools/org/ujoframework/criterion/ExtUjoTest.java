/*
 * HUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:00
 */

package org.ujoframework.criterion;

import junit.framework.*;
import org.ujoframework.MyTestCase;
import org.ujoframework.extensions.PathProperty;
import static org.ujoframework.criterion.Person.*;

/**
 * HashMap Unified Data Object Test
 * @author Pavel Ponec
 */
public class ExtUjoTest extends MyTestCase {
    
    public ExtUjoTest(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(ExtUjoTest.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    

    
    public void testInit1() throws Throwable {
        String nameExp1 = "Jack1";
        String nameExp2 = "Jane" ;
        double cashExp  = 200D;
        
        
        Person person = new Person();
        
        person.set(NAME, nameExp1);
        person.set(MOTHER, new Person());
        person.set(MOTHER, NAME, nameExp2);
        person.set(MOTHER, CASH, cashExp);
        
        String name1 = person.get(NAME);
        String name2 = person.get(MOTHER, NAME);
        double cash = person.get(MOTHER, CASH);        
        
        assertEquals(nameExp1, name1);
        assertEquals(nameExp2, name2);
        assertEquals(cashExp, cash);
    }
    
    public void testInit2() throws Throwable {
        
        Person person = new Person();
        person.init();
        
        String name = person.get(MOTHER, NAME);
        assertEquals("Jane", name);
    }

    public void testInit3() throws Throwable {

        Person person = new Person();
        person.set(NAME, "Jack").set(CASH, 50d);
        person.set(MOTHER, new Person());
        person.set(MOTHER, MOTHER, new Person());
        person.set(MOTHER, MOTHER, MOTHER, new Person());
        
        Object o = person.get(MOTHER, MOTHER, MOTHER);
        
        person.get(MOTHER, MOTHER, MOTHER).set(NAME, "Jack").set(CASH, 10D);

        String name1 = person.get(PathProperty.newInstance(MOTHER, MOTHER, MOTHER, NAME));
        Double cash1 = person.get(PathProperty.newInstance(MOTHER, MOTHER, MOTHER, CASH));

        String name2 = person.get(MOTHER, MOTHER, NAME);
        Double cash2 = person.get(MOTHER, MOTHER, CASH);
        
        //
        assertEquals("Jack", name1);
        assertEquals(10D , cash1);
        assertEquals(null, name2);
        assertEquals(0D  , cash2);        
    
    }
        
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
