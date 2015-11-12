/*
 * Copyright 2015 Pavel Ponec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.extensions;

import junit.framework.TestCase;
import org.ujorm.Key;
import org.ujorm.criterion.Criterion;
import org.ujorm.criterion.Operator;
import java.util.Date;
import java.io.Serializable;
import java.util.List;
import org.ujorm.implementation.quick.SmartUjo;

/**
 * PropertyTest
 * @author Pavel Ponec
 */
public class PropertyTest extends TestCase {

    /** Number zero */
    private static final Integer NIL = 0;
    /** Number one */
    private static final Integer ONE = 1;
    /** Number tow */
    private static final Integer TWO = 2;

    public PropertyTest(String testName) {
        super(testName);
    }

    /**
     * Test of getName method, of class Property.
     */
    public void testGetName() {
        System.out.println("getName");
        assertEquals("id", Person.ID.getName());
        assertEquals("born", Person.BORN.getName());
        assertEquals("mother", Person.MOTHER.getName());
        assertEquals("children", Person.CHILDREN.getName());
        assertEquals("mother.born", Person.MOTHER.add(Person.BORN).getName());
    }

    /**
     * Test of getFullName method, of class Property.
     */
    public void testGetFullName() {
        System.out.println("getFullName");
        assertEquals("Person.id", Person.ID.getFullName());
        assertEquals("Person.born", Person.BORN.getFullName());
        assertEquals("Person.mother", Person.MOTHER.getFullName());
        assertEquals("Person.children", Person.CHILDREN.getFullName());
    }

    /**
     * Test of getType method, of class Property.
     */
    public void testGetType() {
        System.out.println("getType");
        assertEquals(Integer.class, Person.ID.getType());
        assertEquals(Date.class, Person.BORN.getType());
        assertEquals(Person.class, Person.MOTHER.getType());
        assertEquals(List.class, Person.CHILDREN.getType());
    }

    /**
     * Test of getDomainType method, of class Property.
     */
    public void testGetDomainType() {
        System.out.println("getDomainType");
        assertEquals(Person.class, Person.ID.getDomainType());
        assertEquals(Person.class, Person.BORN.getDomainType());
        assertEquals(Person.class, Person.MOTHER.getDomainType());
        assertEquals(Person.class, Person.CHILDREN.getDomainType());
    }

    /**
     * Test of getIndex method, of class Property.
     */
    public void testGetIndex() {
        System.out.println("getIndex");
        assertEquals(0, Person.ID.getIndex());
        assertEquals(1, Person.BORN.getIndex());
        assertEquals(2, Person.MOTHER.getIndex());
        assertEquals(3, Person.CHILDREN.getIndex());
    }

    /**
     * Test of the {@code getValue} method, from a Property class.
     */
    public void testGetValue() {
        System.out.println("getValue");
        Integer expectedResult = TWO;
        Person ujo = new Person(expectedResult);
        assertSame(expectedResult, Person.ID.getValue(ujo));
        assertSame(expectedResult, ujo.get(Person.ID));
    }

    /**
     * Test of the {@code of} method, from a Property class.
     */
    public void testOf() {
        System.out.println("of");
        Integer expectedResult = TWO;
        Person ujo = new Person(expectedResult);
        assertSame(expectedResult, Person.ID.of(ujo));
        assertSame(expectedResult, ujo.get(Person.ID));
    }

    /**
     * Test of getDefault method, of class Property.
     */
    public void testGetDefault() {
        System.out.println("getDefault");
        assertSame(NIL, Person.ID.getDefault());
        assertNull(Person.BORN.getDefault());
        assertNull(Person.MOTHER.getDefault());
        assertNull(Person.CHILDREN.getDefault());
    }

    /**
     * Test of isDefault method, of class Property.
     */
    public void testIsDefault() {
        System.out.println("isDefault");
        assertSame(NIL , Person.ID.getDefault());
        assertSame(null, Person.BORN.getDefault());
        assertSame(null, Person.MOTHER.getDefault());
        assertSame(null, Person.CHILDREN.getDefault());
        //
        assertEquals(NIL, new Person(null).getId());
        assertEquals(NIL, new Person(NIL).getId());
        assertEquals(ONE, new Person(ONE).getId());
        assertEquals(TWO, new Person(TWO).getId());
    }

    /**
     * Test of isComposite method, of class Property.
     */
    public void testIsComposite() {
        System.out.println("isComposite");
        assertFalse(Person.ID.isComposite());
        assertFalse(Person.BORN.isComposite());
        assertFalse(Person.MOTHER.isComposite());
        assertTrue (Person.MOTHER.add(Person.MOTHER).isComposite());
    }

    /**
     * Test of isAscending method, of class Property.
     */
    public void testIsAscending() {
        System.out.println("isAscending");
        assertTrue (Person.BORN.isAscending());
        assertFalse(Person.BORN.descending().isAscending());
        assertTrue (Person.MOTHER.isAscending());
        assertFalse(Person.MOTHER.add(Person.MOTHER).descending().isAscending());
    }

    /**
     * Test of copy method, of class Property.
     */
    public void testCopy() {
        System.out.println("copy");
        Person u1 = new Person(ONE);
        Person u2 = new Person(TWO);
        Person.ID.copy(u1, u2);

        assertSame(ONE, u1.getId());
        assertSame(ONE, u2.getId());
        assertTrue(Person.ID.equals(u1,ONE));
        assertTrue(Person.ID.equals(u2,ONE));
        assertSame(u1.getId(), u2.getId());
    }

    /**
     * Test of isTypeOf method, of class Property.
     */
    public void testIsTypeOf() {
        System.out.println("isTypeOf");
        assertTrue (Person.ID.isTypeOf(Object.class));
        assertTrue (Person.ID.isTypeOf(Number.class));
        assertTrue (Person.ID.isTypeOf(Serializable.class));
        assertTrue (Person.ID.isTypeOf(Integer.class));
        assertFalse(Person.ID.isTypeOf(String.class));
        assertFalse(Person.ID.isTypeOf(Person.class));
        //
        assertTrue (Person.BORN.isTypeOf(Object.class));
        assertTrue (Person.BORN.isTypeOf(Serializable.class));
        assertTrue (Person.BORN.isTypeOf(java.util.Date.class));
        assertFalse(Person.BORN.isTypeOf(java.sql.Date.class));
        assertFalse(Person.BORN.isTypeOf(Number.class));
        assertFalse(Person.BORN.isTypeOf(Integer.class));
        assertFalse(Person.BORN.isTypeOf(Person.class));
        //
        assertTrue (Person.MOTHER.isTypeOf(Object.class));
        assertTrue (Person.MOTHER.isTypeOf(Serializable.class));
        assertTrue (Person.MOTHER.isTypeOf(Person.class));
        assertFalse(Person.MOTHER.isTypeOf(java.util.Date.class));
        assertFalse(Person.MOTHER.isTypeOf(java.sql.Date.class));
        assertFalse(Person.MOTHER.isTypeOf(Number.class));
        assertFalse(Person.MOTHER.isTypeOf(Integer.class));
        assertFalse(Person.MOTHER.isTypeOf(new Person(1) {
        }.getClass()));
        //
        assertTrue (Person.CHILDREN.isTypeOf(Object.class));
        assertTrue (Person.CHILDREN.isTypeOf(List.class));
        assertFalse(Person.CHILDREN.isTypeOf(Integer.class));
        assertFalse(Person.CHILDREN.isTypeOf(Person.class));
    }

    /**
     * Test of isDomainOf method, of class Property.
     */
    public void testIsDomainOf() {
        System.out.println("isDomainOf");
        assertTrue (Person.BORN.isDomainOf(Object.class));
        assertTrue (Person.BORN.isDomainOf(SmartUjo.class));
        assertTrue (Person.BORN.isDomainOf(Person.class));
        assertFalse(Person.BORN.isDomainOf(Date.class));
        assertFalse(Person.BORN.isDomainOf(Integer.class));
        assertFalse(Person.BORN.isDomainOf((new Person(1){}).getClass()));
    }

    /**
     * Test of equals method, of class Property.
     */
    public void testEquals() {
        System.out.println("equals");
        Integer id = TWO;
        Person ujo = new Person(id);
        assertTrue(Person.ID.equals(ujo, id));
    }

    /**
     * Test of equalsName method, of class Property.
     */
    public void testEqualsName() {
        System.out.println("equalsName");
        assertTrue(Person.ID.equalsName(Person.ID.getName()));
    }

//    /**
//     * Test of compareTo method, of class Property.
//     */
//    public void testCompareTo() {
//        System.out.println("compareTo");
//        Key p = null;
//        Property instance = null;
//        int expResult = 0;
//        int result = instance.compareTo(p);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of charAt method, of class Property.
//     */
//    public void testCharAt() {
//        System.out.println("charAt");
//        int index = 0;
//        Property instance = null;
//        char expResult = ' ';
//        char result = instance.charAt(index);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of length method, of class Property.
//     */
//    public void testLength() {
//        System.out.println("length");
//        Property instance = null;
//        int expResult = 0;
//        int result = instance.length();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of subSequence method, of class Property.
//     */
//    public void testSubSequence() {
//        System.out.println("subSequence");
//        int start = 0;
//        int end = 0;
//        Property instance = null;
//        CharSequence expResult = null;
//        CharSequence result = instance.subSequence(start, end);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of toString method, of class Property.
//     */
//    public void testToString() {
//        System.out.println("toString");
//        Property instance = null;
//        String expResult = "";
//        String result = instance.toString();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of toStringFull method, of class Property.
//     */
//    public void testToStringFull_0args() {
//        System.out.println("toStringFull");
//        Property instance = null;
//        String expResult = "";
//        String result = instance.toStringFull();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of toStringFull method, of class Property.
//     */
//    public void testToStringFull_boolean() {
//        System.out.println("toStringFull");
//        boolean extended = false;
//        Property instance = null;
//        String expResult = "";
//        String result = instance.toStringFull(extended);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of where method, of class Property.
//     */
//    public void testWhere_Operator_GenericType() {
//        System.out.println("where");
//        Operator operator = null;
//        Object value = null;
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = instance.where(operator, value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of where method, of class Property.
//     */
//    public void testWhere_Operator_Key() {
//        System.out.println("where");
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = null; //instance.where(null);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of whereEq method, of class Property.
//     */
//    public void testWhereEq_GenericType() {
//        System.out.println("whereEq");
//        Object value = null;
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = instance.whereEq(value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of whereIn method, of class Property.
//     */
//    public void testWhereIn_Collection() {
//        System.out.println("whereIn");
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = null; //instance.whereIn(null);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of whereNotIn method, of class Property.
//     */
//    public void testWhereNotIn_Collection() {
//        System.out.println("whereNotIn");
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = null; //instance.whereNotIn(null);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of whereNotIn method, of class Property.
//     */
//    public void testWhereNotIn_GenericType() {
//        System.out.println("whereNotIn");
//        Object[] list = null;
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = instance.whereNotIn(list);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of whereEq method, of class Property.
//     */
//    public void testWhereEq_Key() {
//        System.out.println("whereEq");
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = instance.whereEq(null);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of whereNull method, of class Property.
//     */
//    public void testWhereNull() {
//        System.out.println("whereNull");
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = instance.whereNull();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of whereNotNull method, of class Property.
//     */
//    public void testWhereNotNull() {
//        System.out.println("whereNotNull");
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = instance.whereNotNull();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of whereFilled method, of class Property.
//     */
//    public void testWhereFilled() {
//        System.out.println("whereFilled");
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = instance.whereFilled();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of whereNotFilled method, of class Property.
//     */
//    public void testWhereNotFilled() {
//        System.out.println("whereNotFilled");
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = instance.whereNotFilled();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of whereNeq method, of class Property.
//     */
//    public void testWhereNeq() {
//        System.out.println("whereNeq");
//        Object value = null;
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = instance.whereNeq(value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of whereGt method, of class Property.
//     */
//    public void testWhereGt() {
//        System.out.println("whereGt");
//        Object value = null;
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = instance.whereGt(value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of whereGe method, of class Property.
//     */
//    public void testWhereGe() {
//        System.out.println("whereGe");
//        Object value = null;
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = instance.whereGe(value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of whereLt method, of class Property.
//     */
//    public void testWhereLt() {
//        System.out.println("whereLt");
//        Object value = null;
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = instance.whereLt(value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of whereLe method, of class Property.
//     */
//    public void testWhereLe() {
//        System.out.println("whereLe");
//        Object value = null;
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = instance.whereLe(value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of forSql method, of class Property.
//     */
//    public void testForSql_String() {
//        System.out.println("forSql");
//        String sqlCondition = "";
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = instance.forSql(sqlCondition);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//
//    /**
//     * Test of forSqlUnchecked method, of class Property.
//     */
//    public void testForSqlUnchecked() {
//        System.out.println("forSqlUnchecked");
//        String sqlCondition = "";
//        Object value = null;
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = instance.forSqlUnchecked(sqlCondition, value);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of forAll method, of class Property.
//     */
//    public void testForAll() {
//        System.out.println("forAll");
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = instance.forAll();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of forNone method, of class Property.
//     */
//    public void testForNone() {
//        System.out.println("forNone");
//        Property instance = null;
//        Criterion expResult = null;
//        Criterion result = instance.forNone();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
