/*
 * HUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:00
 */

package org.ujorm.criterion;

import junit.framework.*;
import org.junit.jupiter.api.Test;
import org.ujorm.MyTestCase;
import org.ujorm.extensions.PathProperty;
import static org.ujorm.criterion.Person.*;

/**
 * HashMap Unified Data Object Test
 * @author Pavel Ponec
 */
public class ExtUjoTest extends MyTestCase {

    @Test
    public void testInit1() throws Throwable {
        String nameExp1 = "Jack1";
        String nameExp2 = "Jane" ;
        double cashExp  = 200D;


        Person<Person> person = new Person();

        person.set(NAME, nameExp1);
        person.set(MOTHER.add(NAME), nameExp2);
        person.set(MOTHER.add(CASH), cashExp);

        String name1 = person.get(NAME);
        String name2 = person.get(MOTHER.add(NAME));
        double cash = person.get(MOTHER.add(CASH));

        assertEquals(nameExp1, name1);
        assertEquals(nameExp2, name2);
        assertEquals(cashExp, cash);
    }

    @Test
    public void testInit2() throws Throwable {

        Person<Person> person = new Person();
        person.init();

        String name = person.get(MOTHER.add(NAME));
        assertEquals("Jane", name);
    }

    @Test
    public void testInit3() throws Throwable {

        Person<Person> person = new Person();
        person.set(NAME, "Jack");
        person.set(CASH, 50d);

        person.set(MOTHER.add(MOTHER).add(MOTHER).add(NAME), "Jack");
        person.set(MOTHER.add(MOTHER).add(MOTHER).add(CASH), 10D);

        String name1 = person.get(PathProperty.of(MOTHER, MOTHER, MOTHER, NAME));
        Double cash1 = person.get(PathProperty.of(MOTHER, MOTHER, MOTHER, CASH));

        String name2 = person.get(MOTHER.add(MOTHER).add(NAME));
        Double cash2 = person.get(MOTHER.add(MOTHER).add(CASH));

        //
        assertEquals("Jack", name1);
        assertEquals(10D , cash1);
        assertNull(name2);
        assertEquals(0D  , cash2);
    }
}
