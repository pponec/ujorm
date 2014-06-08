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
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import junit.framework.*;
import org.ujorm.CompositeKey;
import org.ujorm.Key;
import org.ujorm.MyTestCase;
import org.ujorm.extensions.PathProperty;
import static org.ujorm.criterion.Person.*;

/**
 * Criteria test
 * @author Pavel Ponec
 */
public class CriterionTest extends MyTestCase {

    CompositeKey<Person,Double> MOTHER_CASH  = PathProperty.of(MOTHER, CASH);
    CompositeKey<Person,Double> GMOTHER_CASH = PathProperty.of(MOTHER, MOTHER, CASH);

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

    /** Filled */
    public void testJoin() {
        final Criterion<Person> crnTrue, crnFalse, crnOther;
        Person person = new Person();
        Criterion<Person> result;

        crnTrue = Person.NAME.forAll();
        crnFalse = Person.NAME.forNone();
        crnOther = Person.CASH.whereGt(10.00);
        //
        result = crnTrue.or(crnOther);
        assertSame(crnTrue, result);
        assertEquals(true, result.evaluate(person));
        //
        result = crnFalse.or(crnOther);
        assertSame(crnOther, result);
        assertEquals(false, result.evaluate(person));
        //
        result = crnTrue.and(crnOther);
        assertSame(crnOther, result);
        assertEquals(false, result.evaluate(person));
        //
        result = crnFalse.and(crnOther);
        assertSame(crnFalse, result);
        assertEquals(false, result.evaluate(person));
    }


    /** Serialization 1 */
    public void testSerialization_1() throws Exception {
        System.out.println("testSerialization_1: ");

        Criterion<Person> expected = Person.NAME.whereEq("Lucy");
        Criterion<Person> result = serialize(expected);
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
    public void testSerialization_2() throws Exception {
        System.out.println("testSerialization_2: ");

        Criterion<Person> expected = Person.NAME.whereEq(Person.ADDRESS);
        Criterion<Person> result = serialize(expected);
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
    public void testSerialization_3() throws Exception {
        System.out.println("testSerialization_3: ");

        Criterion<Person> expected = Person.NAME.whereEq("Lucy").or(Person.NAME.whereEq(Person.ADDRESS));
        Criterion<Person> result = serialize(expected);
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

    /** Serialization 3 */
    @SuppressWarnings("unchecked")
    public void testToStringFull() throws IOException, ClassNotFoundException {
        System.out.println("testToStringFull");
        String expected;

        final Criterion<User> crn1, crn2, crn3;

        crn1 = User.LOGIN.whereEq("myLogin");
        crn2 = User.NAME.whereEq("Pavel").retype();
        crn3 = crn2.and(crn1);

        expected = "User(login EQ \"myLogin\")";
        assertEquals(expected, crn1.toStringFull());
        //
        expected = "Person(name EQ \"Pavel\")";
        assertEquals(expected, crn2.toStringFull());
        //
        expected = "User(name EQ \"Pavel\") AND (login EQ \"myLogin\")";
        assertEquals(expected, crn3.toStringFull());
    }

    /** Object serialization */
    @SuppressWarnings("unchecked")
    private <T extends Serializable> T serialize(T object) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream(8000);

        ObjectOutputStream encoder = new ObjectOutputStream(os);
        encoder.writeObject(object);
        encoder.close();
        //
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        ObjectInputStream decoder = new ObjectInputStream(is);
        Object result = (Serializable) decoder.readObject();
        decoder.close();

        return (T) result;
    }

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }

}
