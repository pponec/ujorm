/*
 * UjoComparatorTestTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:00
 */

package org.ujorm.criterion;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ujorm.Key;
import org.ujorm.AbstractTest;
import org.ujorm.core.UjoComparator;

import java.util.ArrayList;
import java.util.List;

import static org.ujorm.criterion.Person.*;

/**
 * Criteria test
 * @author Pavel Ponec
 */
public class UjoComparatorTest extends AbstractTest {

    final Key<Person,Double> MOTHERS_CASH  = MOTHER.add(CASH);
    final Key<Person,String> MOTHERS_NAME  = MOTHER.add(NAME);
    final Key<Person,Double> GMOTHERS_CASH = MOTHER.add(MOTHER).add(CASH);

    private List<Person> persons;

    /** Create new Person */
    private Person<Person> createPerson(String name, Double cash) {
        final Person<Person> result = new Person<>();
        result.set(NAME, name);
        result.set(CASH, cash);
        return result;
    }

    @BeforeEach
    protected void setUp() throws Exception {
        persons = new ArrayList<>();
        final Person<Person> p,m,g,e;

        persons.add(p = createPerson("John" , 10.0));
        persons.add(m = createPerson("Marry", 20.0));
        persons.add(g = createPerson("Julia", 30.0));
        persons.add(e = createPerson("Eva"  , 40.0));

        p.set(MOTHER, m);
        m.set(MOTHER, g);
        g.set(MOTHER, e);
    }

    @AfterEach
    protected void tearDown() throws Exception {
        persons = null;
    }


    /** No sort for an empty Comparator */
    @SuppressWarnings("unchecked")
    @Test
    public void testInit_00() {
        List<Person> result = UjoComparator.<Person>of().sort(persons);
        assertEquals("John", result.get(0).get(NAME));
    }

    /** Sort by name */
    @SuppressWarnings("unchecked")
    @Test
    public void testInit_01() {
        List<Person> result = UjoComparator.of(NAME).sort(persons);
        assertEquals("Eva", result.get(0).get(NAME) );
    }

    /** Sort by mother's name */
    @SuppressWarnings("unchecked")
    @Test
    public void testInit_02() {
        CriteriaTool<Person> uc  = CriteriaTool.newInstance();
        UjoComparator comp = UjoComparator.of(MOTHERS_NAME);
        List<Person> result = uc.select(persons, comp);

        assertEquals("Eva", result.get(0).get(MOTHERS_NAME) );
    }

}
