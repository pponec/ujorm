/*
 * HUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:00
 */

package org.ujorm.criterion;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ujorm.CompositeKey;
import org.ujorm.AbstractTest;
import org.ujorm.extensions.PathProperty;
import static org.ujorm.criterion.Person.*;

/**
 * Criteria test
 * @author Pavel Ponec
 */
public class CriteriaWeakTest extends AbstractTest {

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

    @SuppressWarnings("unchecked")
    @Test
    public void testInit_01() {
        CriteriaTool uc  = CriteriaTool.newInstance();
        Criterion  ex1 = Criterion.where(CASH, 10.0);
        List<Person> result = uc.select(persons, ex1);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).get(NAME) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInit_02a() {
        CriteriaTool ct  = CriteriaTool.newInstance();
        Criterion crn1 = Criterion.where(CASH, Operator.GT, 10.0);
        List<Person> result = ct.select(persons, crn1);
        assertEquals(3, result.size());
        assertEquals("Marry", result.get(0).get(NAME) );
    }

    /** For documentation (!) */
    @Test
    public void testDoc() {

        // Make a criterion:
        Criterion<Person> crn1 = Criterion.where(CASH, Operator.GT, 10.0);
        Criterion<Person> crn2 = Criterion.where(CASH, Operator.LE, 20.0);
        Criterion<Person> criterion = crn1.and(crn2);

        // Use a criterion (1):
        CriteriaTool<Person> ct = CriteriaTool.newInstance();
        List<Person> result = ct.select(persons, criterion);
        assertEquals(1, result.size());
        assertEquals(20.0, CASH.of(result.get(0)));

        // Use a criterion (2):
        Person person = result.get(0);
        boolean validation = criterion.evaluate(person);
        assertTrue(validation);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInit_02b() {
        CriteriaTool uc  = CriteriaTool.newInstance();
        Criterion  ex1 = Criterion.where(CASH, Operator.LT, 20.0);
        List<Person> result = uc.select(persons, ex1);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).get(NAME) );
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testInit_03a() {
        CriteriaTool uc  = CriteriaTool.newInstance();
        Criterion  ex1 = Criterion.where(MOTHER_CASH, Operator.GT, 20.0);
        List<Person> result = uc.select(persons, ex1);
        assertEquals(2, result.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInit_03b() {
        CriteriaTool uc  = CriteriaTool.newInstance();
        Criterion  ex1 = Criterion.where(MOTHER_CASH, Operator.EQ, 20.0);
        List<Person> result = uc.select(persons, ex1);
        assertEquals(1, result.size());
        assertEquals("John", result.get(0).get(NAME) );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testInit_04a() {
        CriteriaTool uc  = CriteriaTool.newInstance();
        Criterion  ex1 = Criterion.where(CASH, Operator.GT, 10.0);
        Criterion  ex2 = Criterion.where(CASH, Operator.LT, 30.0);
        Criterion  exp = ex1.join(BinaryOperator.AND, ex2);
        List<Person> result = uc.select(persons, exp);
        assertEquals(1, result.size());
        assertEquals("Marry", result.get(0).get(NAME) );
        assertEquals(20.0, result.get(0).get(CASH) );
    }
}
