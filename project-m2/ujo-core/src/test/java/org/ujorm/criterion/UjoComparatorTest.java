/*
 * HUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:00
 */

package org.ujorm.criterion;

import java.util.ArrayList;
import java.util.List;
import org.ujorm.Key;
import org.ujorm.MyTestCase;
import org.ujorm.core.UjoComparator;
import static org.ujorm.criterion.Person.*;

/**
 * Criteria test
 * @author Pavel Ponec
 */
public class UjoComparatorTest extends MyTestCase {

    Key<Person,Double> MOTHERS_CASH  = MOTHER.add(CASH);
    Key<Person,String> MOTHERS_NAME  = MOTHER.add(NAME);
    Key<Person,Double> GMOTHERS_CASH = MOTHER.add(MOTHER).add(CASH);

    private List<Person> persons;

    public UjoComparatorTest(String testName) {
        super(testName);
    }

    /** Create new Person */
    private Person createPerson(String name, Double cash) {
        Person result = new Person();
        result.set(NAME, name);
        result.set(CASH, cash);

        persons.add(result);
        return result;
    }

    @Override
    protected void setUp() throws Exception {
        persons = new ArrayList<Person>();

        Person p = createPerson("John" , 10.0);
        Person m = createPerson("Marry", 20.0);
        Person g = createPerson("Julia", 30.0);
        Person e = createPerson("Eva"  , 40.0);

        p.set(MOTHER, m);
        m.set(MOTHER, g);
        g.set(MOTHER, e);

    }

    @Override
    protected void tearDown() throws Exception {
        persons = null;
    }


    /** No sort for an empty Comparator */
    public void testInit_00() {
        List<Person> result = UjoComparator.<Person>of().sort(persons);
        assertEquals("John", result.get(0).get(NAME));
    }

    /** Sort by name */
    public void testInit_01() {
        List<Person> result = UjoComparator.<Person>of(NAME).sort(persons);
        assertEquals("Eva", result.get(0).get(NAME) );
    }

    /** Sort by mother's name */
    public void testInit_02() {
        CriteriaTool<Person> uc  = CriteriaTool.newInstance();
        UjoComparator comp = UjoComparator.of(MOTHERS_NAME);
        List<Person> result = uc.select(persons, comp);

        assertEquals("Eva", result.get(0).get(MOTHERS_NAME) );
    }

}
