/*
 * HUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:00
 */

package org.ujorm.criterion;

import java.util.ArrayList;
import java.util.List;
import junit.framework.*;
import org.ujorm.MyTestCase;
import org.ujorm.extensions.PathProperty;
import static org.ujorm.criterion.Person.*;

/**
 * Criteria test
 * @author Pavel Ponec
 */
public class CriterionTest extends MyTestCase {
    
    PathProperty<Person,Double> MOTHER_CASH  = PathProperty.newInstance(MOTHER, CASH);
    PathProperty<Person,Double> GMOTHER_CASH = PathProperty.newInstance(MOTHER, MOTHER, CASH);
    
    private List<Person> persons;
    
    public CriterionTest(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(CriterionTest.class);
        return suite;
    }
    
    private Person newPerson(String name, Double cash) {
        Person result = new Person();
        result.set(NAME, name);
        result.set(CASH, cash);
        
        persons.add(result);
        return result;
    }

    /** Filled */
    public void testFilled() {
        Person p = new Person();

        boolean expected = true;
        p.set(p.NAME, "aa");
        boolean filled = p.NAME.whereFilled().evaluate(p);
        assertEquals(expected, filled);

        expected = false;
        p.set(p.NAME, "");
        filled = p.NAME.whereFilled().evaluate(p);
        assertEquals(expected, filled);

        expected = false;
        p.set(p.NAME, null);
        filled = p.NAME.whereFilled().evaluate(p);
        assertEquals(expected, filled);

        expected = false;
        p.set(p.NAME, "xxx");
        boolean noFilled = p.NAME.whereNotFilled().evaluate(p);
        assertEquals(expected, noFilled);

        expected = true;
        p.set(p.NAME, "");
        noFilled = p.NAME.whereNotFilled().evaluate(p);
        assertEquals(expected, noFilled);

        expected = true;
        p.set(p.NAME, null);
        noFilled = p.NAME.whereNotFilled().evaluate(p);
        assertEquals(expected, noFilled);


    }
    
    
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
