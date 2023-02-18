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
import org.junit.jupiter.api.Assertions;
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

    /** Filled */
    public void testFilled() {
        Person p = new Person();

        boolean expected = true;
        p.set(NAME, "aa");
        boolean filled = NAME.whereHasLength().evaluate(p);
        Assertions.assertEquals(expected, filled);
        Assertions.assertEquals("Person(name NOT_EQ null) AND (name NOT_EQ \"\")",
                NAME.whereHasLength().toString());

        expected = false;
        p.set(NAME, "");
        filled = NAME.whereHasLength().evaluate(p);
        Assertions.assertEquals(expected, filled);

        expected = false;
        p.set(NAME, null);
        filled = NAME.whereHasLength().evaluate(p);
        Assertions.assertEquals(expected, filled);

        expected = false;
        p.set(NAME, "xxx");
        boolean noFilled = NAME.whereIsEmpty().evaluate(p);
        Assertions.assertEquals(expected, noFilled);

        expected = true;
        p.set(NAME, "");
        noFilled = NAME.whereIsEmpty().evaluate(p);
        assertEquals(expected, noFilled);

        expected = true;
        p.set(NAME, null);
        noFilled = NAME.whereIsEmpty().evaluate(p);
        assertEquals(expected, noFilled);
    }

    /** Test Value fix Join */
    public void testValueFixJoin() {
        final Criterion<Person> crnTrue, crnFalse, crnAny;
        final Person person = new Person();
        Criterion<Person> result;

        crnTrue = Person.NAME.forAll();
        crnFalse = Person.NAME.forNone();
        crnAny = Person.CASH.whereGt(10.00);

        assertEquals("Person(true)", crnTrue.toString());
        assertEquals("Person(false)", crnFalse.toString());
        assertEquals("Person(cash GT 10.0)", crnAny.toString());

        result = crnTrue.or(crnAny);
        assertSame(crnTrue, result);
        assertTrue(result.evaluate(person));
        //
        result = crnFalse.or(crnAny);
        assertSame(crnAny, result);
        assertFalse(result.evaluate(person));
        //
        result = crnTrue.and(crnAny);
        assertSame(crnAny, result);
        assertFalse(result.evaluate(person));
        //
        result = crnFalse.and(crnAny);
        assertSame(crnFalse, result);
        assertFalse(result.evaluate(person));
    }


    /** Test Value Other Join */
    public void testValueOtherJoin() {
        final Criterion<Person> crnTrue, crnFalse, crnAny;
        final Person person = new Person();
        Criterion<Person> result;

        crnTrue = Person.NAME.forAll();
        crnFalse = Person.NAME.forNone();
        crnAny = Person.CASH.whereGt(10.00);
        //
        result = crnAny.or(crnTrue);
        assertSame(crnTrue, result);
        assertTrue(result.evaluate(person));
        //
        result = crnAny.or(crnFalse);
        assertSame(crnAny, result);
        assertFalse(result.evaluate(person));
        //
        result = crnAny.and(crnTrue);
        assertSame(crnAny, result);
        assertFalse(result.evaluate(person));
        //
        result = crnAny.and(crnFalse);
        assertSame(crnFalse, result);
        assertFalse(result.evaluate(person));
    }

    /** Test Binary Join */
    public void testBinaryJoin() {
        final Criterion<Person> crnTrue, crnFalse, crnAny;
        final Person person = new Person();
        Criterion<Person> result;

        crnTrue = Person.NAME.forAll();
        crnFalse = Person.NAME.forNone();
        crnAny = Person.CASH.whereGt(10.00).and(Person.CASH.whereLe(100.00));
        //
        result = crnAny.or(crnTrue);
        assertSame(crnTrue, result);
        assertTrue(result.evaluate(person));
        //
        result = crnAny.or(crnFalse);
        assertSame(crnAny, result);
        assertFalse(result.evaluate(person));
        //
        result = crnAny.and(crnTrue);
        assertSame(crnAny, result);
        assertFalse(result.evaluate(person));
        //
        result = crnAny.and(crnFalse);
        assertSame(crnFalse, result);
        assertFalse(result.evaluate(person));
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
        assertEquals("Person(name EQ address)", expected.toString());
        assertSame(expected.getLeftNode(), result.getLeftNode());
        assertSame(expected.getOperator(), result.getOperator());
        assertEquals(expected.getRightNode(), result.getRightNode());
        assertEquals(expected.toString(), result.toString());
        assertTrue(result.getRightNode() instanceof Key);
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
        assertTrue(result.getLeftNode() instanceof Criterion);
        assertTrue(result.getRightNode() instanceof Criterion);
        //
        final Person person = new Person();
        person.init();
        assertEquals(expected.evaluate(person), result.evaluate(person));
    }

    /** Serialization 1 */
    public void testSerialization_4() throws Exception {
        System.out.println("testSerialization_4: ");

        ProxyValue<String> value = () -> "Lucy";
        Criterion<Person> expected = Person.NAME.whereEq(value);
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

    /** Serialization 3 */
    @SuppressWarnings("unchecked")
    public void testToStringFull() throws IOException, ClassNotFoundException {
        System.out.println("testToStringFull");
        String expected;

        final Criterion<User> crn1, crn2, crn3;

        crn1 = User.LOGIN.whereEq("myLogin");
        crn2 = User.NAME.whereEq("Pavel").cast();
        crn3 = crn2.and(crn1);

        expected = "User(login EQ \"myLogin\")";
        assertEquals(expected, crn1.toStringFull());
        //
        expected = "Person(name EQ \"Pavel\")";
        assertEquals(expected, crn2.toStringFull());
        //
        expected = "User(name EQ \"Pavel\") AND (login EQ \"myLogin\")";
        assertEquals(expected, crn3.toStringFull());
        //
        expected = "User(NOT (login EQ \"myLogin\"))";
        assertEquals(expected, crn1.not().toStringFull());
    }

    /** Object serialization */
    @SuppressWarnings("unchecked")
    private <T extends Serializable> T serialize(T object) throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream(8000);

        try (ObjectOutputStream encoder = new ObjectOutputStream(os)) {
            encoder.writeObject(object);
        }
        //
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        try (ObjectInputStream decoder = new ObjectInputStream(is)) {
            Object result = decoder.readObject();
            return (T) result;
        }
    }
}
