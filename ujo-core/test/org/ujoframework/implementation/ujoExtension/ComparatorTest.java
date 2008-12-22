/*
 * HUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. èerven 2007, 23:00
 */

package org.ujoframework.implementation.ujoExtension;

import java.util.ArrayList;
import java.util.List;
import junit.framework.*;
import org.ujoframework.MyTestCase;
import org.ujoframework.core.UjoComparator;
import org.ujoframework.extensions.PathProperty;
import org.ujoframework.tools.UjoCriteria;
import static org.ujoframework.implementation.ujoExtension.ExtPerson.*;

/**
 * Criteria test
 * @author pavel
 */
public class ComparatorTest extends MyTestCase {
    
    PathProperty<ExtPerson,Double> MOTHERS_CASH  = PathProperty.create(MOTHER, CASH);
    PathProperty<ExtPerson,String> MOTHERS_NAME  = PathProperty.create(MOTHER, NAME);
    PathProperty<ExtPerson,Double> GMOTHERS_CASH = PathProperty.create(MOTHER, MOTHER, CASH);
    
    private List<ExtPerson> persons;
    
    public ComparatorTest(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(ComparatorTest.class);
        return suite;
    }
    
    private ExtPerson newPerson(String name, Double cash) {
        ExtPerson result = new ExtPerson();
        result.set(NAME, name);
        result.set(CASH, cash);
        
        persons.add(result);
        return result;
    }
    
    @Override
    protected void setUp() throws Exception {
        persons = new ArrayList<ExtPerson>();
        
        ExtPerson p = newPerson("John" , 10.0);
        ExtPerson m = newPerson("Marry", 20.0);
        ExtPerson g = newPerson("Julia", 30.0);
        ExtPerson e = newPerson("Eva"  , 40.0);
        
        p.set(MOTHER, m);
        m.set(MOTHER, g);
        g.set(MOTHER, e);
        
    }
    
    @Override
    protected void tearDown() throws Exception {
        persons = null;
    }
    

    
    public void testInit_01() {
        UjoCriteria<ExtPerson> uc  = UjoCriteria.create();
        UjoComparator comp = UjoComparator.create(true, NAME);
        List<ExtPerson> result = uc.select(persons, comp);
        
        assertEquals("Eva", result.get(0).get(NAME) );
    }
    
    public void testInit_02() {
        UjoCriteria<ExtPerson> uc  = UjoCriteria.create();
        UjoComparator comp = UjoComparator.create(true, MOTHERS_NAME);
        List<ExtPerson> result = uc.select(persons, comp);
        
        assertEquals("Eva", result.get(0).get(MOTHERS_NAME) );
    }
    
    
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
