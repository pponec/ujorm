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
import org.ujoframework.extensions.PathProperty;
import org.ujoframework.tools.UjoCriteria;
import org.ujoframework.tools.criteria.Expression;
import org.ujoframework.tools.criteria.OperatorBinary;
import org.ujoframework.tools.criteria.Operator;
import static org.ujoframework.implementation.ujoExtension.ExtPerson.*;

/**
 * Criteria test
 * @author pavel
 */
public class CriteriaWeakTest extends MyTestCase {
    
    PathProperty<ExtPerson,Double> MOTHER_CASH  = PathProperty.create(MOTHER, CASH);
    PathProperty<ExtPerson,Double> GMOTHER_CASH = PathProperty.create(MOTHER, MOTHER, CASH);
    
    private List<ExtPerson> persons;
    
    public CriteriaWeakTest(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(CriteriaWeakTest.class);
        return suite;
    }
    
    private ExtPerson newPerson(String name, Double cash) {
        ExtPerson result = new ExtPerson();
        result.set(NAME, name);
        result.set(CASH, cash);
        
        persons.add(result);
        return result;
    }
    
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
    

    
    @SuppressWarnings("unchecked")
    public void testInit_01() {
        UjoCriteria uc  = UjoCriteria.create();
        Expression  ex1 = Expression.newInstance(CASH, 10.0);
        List<ExtPerson> result = uc.select(persons, ex1);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).get(NAME) );
    }
    
    @SuppressWarnings("unchecked")
    public void testInit_02a() {
        UjoCriteria uc  = UjoCriteria.create();
        Expression  ex1 = Expression.newInstance(CASH, Operator.GT, 10.0);
        List<ExtPerson> result = uc.select(persons, ex1);
        assertEquals(3, result.size());
        assertEquals("Marry", result.get(0).get(NAME) );
    }

    @SuppressWarnings("unchecked")
    public void testInit_02b() {
        UjoCriteria uc  = UjoCriteria.create();
        Expression  ex1 = Expression.newInstance(CASH, Operator.LT, 20.0);
        List<ExtPerson> result = uc.select(persons, ex1);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).get(NAME) );
    }
    
    
    @SuppressWarnings("unchecked")
    public void testInit_03a() {
        UjoCriteria uc  = UjoCriteria.create();
        Expression  ex1 = Expression.newInstance(MOTHER_CASH, Operator.GT, 20.0);
        List<ExtPerson> result = uc.select(persons, ex1);
        assertEquals(2, result.size());
    }
    
    @SuppressWarnings("unchecked")
    public void testInit_03b() {
        UjoCriteria uc  = UjoCriteria.create();
        Expression  ex1 = Expression.newInstance(MOTHER_CASH, Operator.EQ, 20.0);
        List<ExtPerson> result = uc.select(persons, ex1);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).get(NAME) );
    }
    
    @SuppressWarnings("unchecked")
    public void testInit_04a() {
        UjoCriteria uc  = UjoCriteria.create();
        Expression  ex1 = Expression.newInstance(CASH, Operator.GT, 10.0);
        Expression  ex2 = Expression.newInstance(CASH, Operator.LT, 30.0);
        Expression  exp = ex1.join(OperatorBinary.AND, ex2);
        List<ExtPerson> result = uc.select(persons, exp);
        assertEquals(1, result.size());
        assertEquals("Marry", result.get(0).get(NAME) );
        assertEquals(20.0, result.get(0).get(CASH) );
    }
    
    
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
