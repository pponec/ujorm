/*
 * UjoManagerTest.java
 * JUnit based test
 *
 * Created on 27. June 2007, 19:21
 */

package org.ujorm_back.core;

import org.ujorm.UjoProperty;
import java.util.ArrayList;
import org.ujorm.Key;
import org.ujorm_back.MyTestCase;
import org.ujorm.extensions.PathProperty;
import org.ujorm_back.extensions.PersonExt;
import static org.ujorm_back.extensions.PersonExt.*;

/**
 *
 * @author Pavel Ponec
 */
public class PropertyTest extends MyTestCase {
    
    public PropertyTest(String testName) {
        super(testName);
    }
    
    private static Class suite() {
        return PropertyTest.class;
    }
    
    /**
     * Test of encodeBytes method, of class org.ujorm.core.UjoManager.
     */
    public void testCopy() {

        PersonExt from = new PersonExt(1);
        PersonExt to = new PersonExt(2);

        PersonExt.ID.copy(from, to);
        assertSame(from.get(ID), to.get(ID));

        // ---

        from.set(PERS, new ArrayList<PersonExt>());
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
        Key<PersonExt, Integer> id = new PathProperty<PersonExt, Integer>(PersonExt.ID);
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


    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
    
}
