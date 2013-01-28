/*
 * HUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:00
 */

package org.ujorm.criterion;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import junit.framework.*;
import org.ujorm.Key;
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

    /** Serialization 1 */
    public void testSerialization_1() throws IOException, ClassNotFoundException {
        System.out.println("testSerialization_1: ");

        Criterion<Person> expected = Person.NAME.whereEq("Lucy");
        Criterion<Person> result;

        //
        ByteArrayOutputStream out = new ByteArrayOutputStream(256);
        ObjectOutputStream encoder = new ObjectOutputStream(out);
        encoder.writeObject(expected);
        encoder.close();
        //
        ObjectInputStream inp = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
        result = (Criterion) inp.readObject();
        //
        assertSame(expected.getLeftNode(), result.getLeftNode());
        assertSame(expected.getOperator(), result.getOperator());
        assertEquals(expected.getRightNode(), result.getRightNode());
        assertEquals(expected.toString(), result.toString());
        //
        final Person person = new Person();
        person.init();
        assertEquals(expected.evaluate(person), result.evaluate(person));
    }

    /** Serialization 2 */
    public void testSerialization_2() throws IOException, ClassNotFoundException {
        System.out.println("testSerialization_2: ");

        Criterion<Person> expected = Person.NAME.whereEq(Person.ADDRESS);
        Criterion<Person> result;
        //
        ByteArrayOutputStream out = new ByteArrayOutputStream(256);
        ObjectOutputStream encoder = new ObjectOutputStream(out);
        encoder.writeObject(expected);
        encoder.close();
        //
        ObjectInputStream inp = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
        result = (Criterion) inp.readObject();
        //
        assertSame(expected.getLeftNode(), result.getLeftNode());
        assertSame(expected.getOperator(), result.getOperator());
        assertEquals(expected.getRightNode(), result.getRightNode());
        assertEquals(expected.toString(), result.toString());
        assertEquals(true, result.getRightNode() instanceof Key);
        //
        final Person person = new Person();
        person.init();
        assertEquals(expected.evaluate(person), result.evaluate(person));
    }


    /** Serialization 3 */
    public void testSerialization_3() throws IOException, ClassNotFoundException {
        System.out.println("testSerialization_3: ");

        Criterion<Person> expected = Person.NAME.whereEq("Lucy").or(Person.NAME.whereEq(Person.ADDRESS));
        Criterion<Person> result;
        //
        ByteArrayOutputStream out = new ByteArrayOutputStream(256);
        ObjectOutputStream encoder = new ObjectOutputStream(out);
        encoder.writeObject(expected);
        encoder.close();
        //
        ObjectInputStream inp = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
        result = (Criterion) inp.readObject();
        //
        assertEquals(expected.getLeftNode().toString(), result.getLeftNode().toString());
        assertSame  (expected.getOperator(), result.getOperator());
        assertEquals(expected.getRightNode().toString(), result.getRightNode().toString());
        assertEquals(expected.toString(), result.toString());
        assertEquals(true, result.getLeftNode() instanceof Criterion);
        assertEquals(true, result.getRightNode() instanceof Criterion);
        //
        final Person person = new Person();
        person.init();
        assertEquals(expected.evaluate(person), result.evaluate(person));
    }


    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }

}
