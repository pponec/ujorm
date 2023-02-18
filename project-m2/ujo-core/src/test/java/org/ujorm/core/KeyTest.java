/*
 * UjoManagerTest.java
 * JUnit based test
 *
 * Created on 27. June 2007, 19:21
 */

package org.ujorm.core;

import java.util.ArrayList;
import org.ujorm.Key;
import org.ujorm.MyTestCase;
import org.ujorm.extensions.PathProperty;
import org.ujorm.extensions.PersonExt;
import static org.ujorm.extensions.PersonExt.*;

/**
 *
 * @author Pavel Ponec
 */
public class KeyTest extends MyTestCase {

    /**
     * Test of encodeBytes method, of class org.ujorm.core.UjoManager.
     */
    public void testCopy() {

        PersonExt from = new PersonExt(1);
        PersonExt to = new PersonExt(2);

        PersonExt.ID.copy(from, to);
        assertSame(from.get(ID), to.get(ID));

        // ---

        from.set(PERS, new ArrayList<>());
        PERS.copy(from, to);
        assertSame(from.get(PERS), to.get(PERS));
    }


    /**
     * Test of encodeBytes method, of class org.ujorm.core.UjoManager.
     */
    public void testDescending_1() {

        boolean descending = false;
        Key<PersonExt, Integer> id = PersonExt.ID;
        assertEquals(descending, !id.isAscending());

        descending = true;
        id = id.descending();
        assertEquals(descending, !id.isAscending());

        descending = true;
        id = id.descending(descending);
        assertEquals(descending, !id.isAscending());

        descending = false;
        id = id.descending(descending);
        assertEquals(descending, !id.isAscending());

        descending = false;
        id = id.descending(descending);
        assertEquals(descending, !id.isAscending());

        descending = true;
        id = id.descending(descending);
        assertEquals(descending, !id.isAscending());

    }

    /**
     * Test of encodeBytes method, of class org.ujorm.core.UjoManager.
     */
    public void testDescending_2() {

        boolean descending = false;
        Key<PersonExt, Integer> id = new PathProperty<PersonExt, Integer>(null, PersonExt.ID);
        assertEquals(descending, !id.isAscending());

        descending = true;
        id = id.descending();
        assertEquals(descending, !id.isAscending());

        descending = true;
        id = id.descending(descending);
        assertEquals(descending, !id.isAscending());

        descending = false;
        id = id.descending(descending);
        assertEquals(descending, !id.isAscending());

        descending = false;
        id = id.descending(descending);
        assertEquals(descending, !id.isAscending());

        descending = true;
        id = id.descending(descending);
        assertEquals(descending, !id.isAscending());
    }

    /**
     * Test the toString name
     */
    public void testToStringFullTrue_1() {
        Key<PersonExt, Integer> key = PersonExt.ID;

        String expectedResult = "PersonExt.id {index=0, ascending=true, composite=false, default=null, validator=null, type=class java.lang.Integer, domainType=class org.ujorm.extensions.PersonExt, class=org.ujorm.extensions.Property}";
        String result = key.toStringFull(true);
        assertEquals(expectedResult, result);
    }

    /**
     * Test the toString name
     */
    public void testToStringFullTrue_2() {
        Key<PersonExt, Integer> key = new PathProperty<PersonExt, Integer>(null, PersonExt.PERS);

        String expectedResult = "PersonExt.person {index=-1, ascending=true, composite=true, default=null, validator=null, type=interface java.util.List, domainType=class org.ujorm.extensions.PersonExt, class=org.ujorm.extensions.PathProperty}";
        String result = key.toStringFull(true);
        assertEquals(expectedResult, result);
    }
}
