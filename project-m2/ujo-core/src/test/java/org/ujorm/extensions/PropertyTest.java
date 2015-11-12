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

    public PropertyTest(String testName) {
        super(testName);
    }

    /**
     * Test of getName method, of class Property.
     */
    public void testGetName() {
        System.out.println("getName");
        assertEquals("id", Person.ID.getName());
        assertEquals("created", Person.CREATED.getName());
        assertEquals("pers", Person.PERS.getName());
    }

    /**
     * Test of getFullName method, of class Property.
     */
    public void testGetFullName() {
        System.out.println("getFullName");
        assertEquals("Person.id", Person.ID.getFullName());
        assertEquals("Person.created", Person.CREATED.getFullName());
        assertEquals("Person.pers", Person.PERS.getFullName());
    }

    /**
     * Test of getType method, of class Property.
     */
    public void testGetType() {
        System.out.println("getType");
        assertEquals(Integer.class, Person.ID.getType());
        assertEquals(Date.class, Person.CREATED.getType());
        assertEquals(List.class, Person.PERS.getType());
    }

    /**
     * Test of getDomainType method, of class Property.
     */
    public void testGetDomainType() {
        System.out.println("getDomainType");
        assertEquals(Person.class, Person.ID.getDomainType());
        assertEquals(Person.class, Person.CREATED.getDomainType());
        assertEquals(Person.class, Person.PERS.getDomainType());
    }

    /**
     * Test of getIndex method, of class Property.
     */
    public void testGetIndex() {
        System.out.println("getIndex");
        assertEquals(0, Person.ID.getIndex());
        assertEquals(1, Person.CREATED.getIndex());
        assertEquals(2, Person.PERS.getIndex());
    }

    /**
     * Test of the {@code getValue} method, from a Property class.
     */
    public void testGetValue() {
        System.out.println("getValue");
        Integer expectedResult = 987;
        Person ujo = new Person(expectedResult);
        assertSame(expectedResult, Person.ID.getValue(ujo));
        assertSame(expectedResult, ujo.get(Person.ID));
    }

    /**
     * Test of the {@code of} method, from a Property class.
     */
    public void testOf() {
        System.out.println("of");
        Integer expectedResult = 980;
        Person ujo = new Person(expectedResult);
        assertSame(expectedResult, Person.ID.of(ujo));
        assertSame(expectedResult, ujo.get(Person.ID));
    }

    /**
     * Test of getDefault method, of class Property.
     */
    public void testGetDefault() {
        System.out.println("getDefault");
        assertNull(Person.ID.getDefault());
        assertNotNull(Person.CREATED.getDefault());
        assertNull(Person.PERS.getDefault());
    }

    /**
     * Test of isDefault method, of class Property.
     */
    public void testIsDefault() {
        System.out.println("isDefault");
        Person ujo = new Person(null);
        assertEquals(null, ujo.get(Person.ID));
        assertSame(null, ujo.get(Person.ID));
        assertSame(Person.ID.getDefault(), ujo.get(Person.ID));
        assertSame(Person.CREATED.getDefault(), ujo.get(Person.CREATED));
    }

    /**
     * Test of isComposite method, of class Property.
     */
    public void testIsComposite() {
        System.out.println("isComposite");
        assertEquals(false, Person.ID.isComposite());
        assertEquals(false, Person.CREATED.isComposite());
    }

    /**
     * Test of isAscending method, of class Property.
     */
    public void testIsAscending() {
        System.out.println("isAscending");
        assertEquals(true, Person.CREATED.isAscending());
        assertEquals(false, Person.CREATED.descending().isAscending());

    }

    /**
     * Test of copy method, of class Property.
     */
    public void testCopy() {
        System.out.println("copy");
        System.out.println("isAscending");
        Person u1 = new Person(1);
        Person u2 = new Person(2);
        Person.ID.copy(u1, u2);
        assertSame(u1.get(Person.ID), u2.get(Person.ID));
    }

    /**
     * Test of isTypeOf method, of class Property.
     */
    public void testIsTypeOf() {
        System.out.println("isTypeOf");
        assertEquals(true , Person.ID.isTypeOf(Object.class));
        assertEquals(true , Person.ID.isTypeOf(Number.class));
        assertEquals(true , Person.ID.isTypeOf(Serializable.class));
        assertEquals(true , Person.ID.isTypeOf(Integer.class));
        assertEquals(false, Person.ID.isTypeOf(String.class));
        assertEquals(false, Person.ID.isTypeOf(Person.class));
        //
        assertEquals(true , Person.CREATED.isTypeOf(Object.class));
        assertEquals(true , Person.CREATED.isTypeOf(Serializable.class));
        assertEquals(true , Person.CREATED.isTypeOf(java.util.Date.class));
        assertEquals(false, Person.CREATED.isTypeOf(java.sql.Date.class));
        assertEquals(false, Person.CREATED.isTypeOf(Number.class));
        assertEquals(false, Person.CREATED.isTypeOf(Integer.class));
        assertEquals(false, Person.CREATED.isTypeOf(Person.class));
        //
        assertEquals(true , Person.PERS.isTypeOf(Object.class));
        assertEquals(true , Person.PERS.isTypeOf(List.class));
        assertEquals(false, Person.PERS.isTypeOf(Integer.class));
        assertEquals(false, Person.PERS.isTypeOf(Person.class));
    }

    /**
     * Test of isDomainOf method, of class Property.
     */
    public void testIsDomainOf() {
        System.out.println("isDomainOf");
        assertEquals(true, Person.CREATED.isDomainOf(Object.class));
        assertEquals(true, Person.CREATED.isDomainOf(SmartUjo.class));
        assertEquals(true, Person.CREATED.isDomainOf(Person.class));
        assertEquals(false, Person.CREATED.isDomainOf(Date.class));
        assertEquals(false, Person.CREATED.isDomainOf(Integer.class));
        assertEquals(false, Person.CREATED.isDomainOf((new Person(1){}).getClass()));
    }

    /**
     * Test of equals method, of class Property.
     */
    public void testEquals() {
        System.out.println("equals");
        Integer id = 22;
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
