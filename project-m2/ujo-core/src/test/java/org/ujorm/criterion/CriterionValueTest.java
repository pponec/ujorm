/*
 * HUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:00
 */

package org.ujorm.criterion;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.ujorm.CompositeKey;
import org.ujorm.AbstractTest;
import org.ujorm.extensions.PathProperty;
import static org.ujorm.criterion.Person.*;

/**
 * Criteria test
 * @author Pavel Ponec
 */
public class CriterionValueTest extends AbstractTest {

    CompositeKey<Person,Double> MOTHER_CASH  = PathProperty.of(MOTHER, CASH);
    CompositeKey<Person,Double> GMOTHER_CASH = PathProperty.of(MOTHER, MOTHER, CASH);

    private List<Person> persons;

    private Person newPerson(String name, Double cash) {
        Person result = new Person();
        result.set(NAME, name);
        result.set(CASH, cash);

        persons.add(result);
        return result;
    }

    @BeforeEach
    protected void setUp() throws Exception {
        persons = new ArrayList<>();

        Person p = newPerson("John" , 10.0);
        Person m = newPerson("Marry", 20.0);
        Person g = newPerson("Julia", 30.0);
        Person e = newPerson("Eva"  , 40.0);

        p.set(MOTHER, m);
        m.set(MOTHER, g);
        g.set(MOTHER, e);

    }

    @AfterEach
    protected void tearDown() throws Exception {
        persons = null;
    }

    @Test
    public void testInit_01() {
        CriteriaTool<Person> uc  = CriteriaTool.newInstance();
        Criterion<Person>  ex1 = Criterion.where(CASH, 10.0);
        List<Person> result = uc.select(persons, ex1);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).get(NAME) );
    }

    @Test
    public void testInit_02a() {
        CriteriaTool<Person> uc  = CriteriaTool.newInstance();
        Criterion<Person>  ex1 = Criterion.where(CASH, Operator.GT, 10.0);
        List<Person> result = uc.select(persons, ex1);
        assertEquals(3, result.size());
        assertEquals("Marry", result.get(0).get(NAME) );
    }

    @Test
    public void testInit_02b() {
        CriteriaTool<Person> uc  = CriteriaTool.newInstance();
        Criterion<Person>  ex1 = Criterion.where(CASH, Operator.LT, 20.0);
        List<Person> result = uc.select(persons, ex1);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).get(NAME) );
    }


    @Test
    public void testInit_03a() {
        CriteriaTool<Person> uc  = CriteriaTool.newInstance();
        Criterion<Person>  ex1 = Criterion.where(MOTHER_CASH, Operator.GT, 20.0);
        List<Person> result = uc.select(persons, ex1);
        assertEquals(2, result.size());
    }

    @Test
    public void testInit_03b() {
        CriteriaTool<Person> uc  = CriteriaTool.newInstance();
        Criterion<Person>  ex1 = Criterion.where(MOTHER_CASH, Operator.EQ, 20.0);
        List<Person> result = uc.select(persons, ex1);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).get(NAME) );
    }

    @Test
    public void testInit_04a() {
        CriteriaTool<Person> uc  = CriteriaTool.newInstance();
        Criterion<Person>  ex1 = Criterion.where(CASH, Operator.GT, 10.0);
        Criterion<Person>  ex2 = Criterion.where(CASH, Operator.LT, 30.0);
        Criterion<Person>  exp = ex1.join(BinaryOperator.AND, ex2);
        List<Person> result = uc.select(persons, exp);
        assertEquals(1, result.size());
        assertEquals("Marry", result.get(0).get(NAME) );
        assertEquals(20.0, result.get(0).get(CASH) );
    }

    @Test
    public void testInit_05a() {
        persons.get(0).set(NAME, null);

        CriteriaTool<Person> uc  = CriteriaTool.newInstance();
        Criterion<Person>  exp = Criterion.where(NAME, Operator.EQ, (String) null);
        List<Person> result = uc.select(persons, exp);
        assertEquals(1, result.size());
        assertEquals(10.0, result.get(0).get(CASH) );
    }

    @Test
    public void testInit_in_01() {
        Criterion<Employee> crn = Criterion.whereIn(Employee.CASH, persons, Person.CASH);
        boolean ok = crn.evaluate(new Employee(10.0));
        assertTrue(ok);
    }

    @Test
    public void testInit_in_02() {
        Criterion<Employee> crn = Criterion.whereIn(Employee.CASH, persons, Person.CASH);
        boolean ok = !crn.evaluate(new Employee(15.0));
        assertTrue(ok);
    }


    @Test
    public void testInit_in_03() {
        Criterion<Employee> crn = Criterion.whereIn(Employee.NAME, persons, Person.NAME);
        boolean ok = crn.evaluate(new Employee("Julia"));
        assertTrue(ok);
    }

    @Test
    public void testInit_in_04() {
        Criterion<Employee> crn = Criterion.whereIn(Employee.NAME, persons, Person.NAME);
        boolean ok = !crn.evaluate(new Employee("Julya"));
        assertTrue(ok);
    }

    @Test
    public void testInit_in_06() {
        Criterion<Employee> crn = Criterion.whereIn(Employee.NAME, persons, Person.NAME);
        boolean ok = !crn.evaluate(new Employee("Julya"));
        assertTrue(ok);
    }

    @Test
    public void testInit_in_07() {
        Criterion<Employee> crn = Criterion.whereNotIn(Employee.NAME, new ArrayList<>(), Person.NAME);
        boolean ok = crn.evaluate(new Employee("Julia"));
        assertTrue(ok);
    }

    @Test
    public void testInit_in_08() {
        Criterion<Employee> crn = Criterion.whereIn(Employee.NAME, new ArrayList<>(), Person.NAME);
        boolean ok = !crn.evaluate(new Employee("Julya"));
        assertTrue(ok);
    }
}
