/*
 * KeyAliasTest.java
 * JUnit based test
 *
 * Created on 27. June 2007, 19:21
 */

package org.ujorm.core;

import org.ujorm.CompositeKey;
import org.ujorm.MyTestCase;
import org.ujorm.extensions.PersonExt;
import static org.ujorm.extensions.PersonExt.*;

/**
 * KeyAliasTest
 * @author Pavel Ponec
 */
public class KeyAliasTest extends MyTestCase {

    public KeyAliasTest(String testName) {
        super(testName);
    }

    private static Class suite() {
        return KeyAliasTest.class;
    }

    /**
     * Test of encodeBytes method, of class org.ujorm.core.UjoManager.
     */
    public void testAlias() {
        final CompositeKey<PersonExt,PersonExt> keys1, keys2, keys3, keys4, keys5, keys6, keys7;
        final String aliasN = null;

        keys1 = PersonExt.SUPERIOR.add(SUPERIOR);
        assertEquals(aliasN, keys1.getAlias(0));
        assertEquals(aliasN, keys1.getAlias(1));
        assertFalse(keys1.hasAlias());

        //--
        String alias1 = "Alias1";
        keys2 = PersonExt.SUPERIOR.add(SUPERIOR, alias1);
        assertEquals(2, keys2.getKeyCount());
        assertEquals(aliasN, keys2.getAlias(0));
        assertEquals(alias1, keys2.getAlias(1));
        assertTrue(keys2.hasAlias());

        //--
        String alias2 = "Alias2";
        String alias3 = "Alias3";
        keys3 = PersonExt.SUPERIOR.add(SUPERIOR, alias2).add(SUPERIOR, alias3);
        assertEquals(3, keys3.getKeyCount());
        assertEquals(aliasN, keys3.getAlias(0));
        assertEquals(alias2, keys3.getAlias(1));
        assertEquals(alias3, keys3.getAlias(2));
        assertTrue(keys3.hasAlias());

        //--
        keys4 = (CompositeKey<PersonExt,PersonExt>) keys3.descending();
        assertEquals(3, keys4.getKeyCount());
        assertEquals(aliasN, keys4.getAlias(0));
        assertEquals(alias2, keys4.getAlias(1));
        assertEquals(alias3, keys4.getAlias(2));
        assertFalse(keys4.isAscending());
        assertTrue(keys4.hasAlias());

        //--
        keys5 = keys2.add(keys4);
        assertEquals(5, keys5.getKeyCount());
        assertEquals(aliasN, keys5.getAlias(0));
        assertEquals(alias1, keys5.getAlias(1));
        assertEquals(aliasN, keys5.getAlias(2));
        assertEquals(alias2, keys5.getAlias(3));
        assertEquals(alias3, keys5.getAlias(4));
        assertFalse(keys5.isAscending());
        assertTrue(keys5.hasAlias());
    }

    /**
     * Test of encodeBytes method, of class org.ujorm.core.UjoManager.
     */
    public void testAlias2() {
        final CompositeKey<PersonExt,PersonExt> keys1, keys2, keys3, keys4, keys5, keys6, keys7;
        final String aliasN = null;

        keys1 = PersonExt.SUPERIOR.add(SUPERIOR);
        assertEquals(aliasN, keys1.getAlias(0));
        assertEquals(aliasN, keys1.getAlias(1));
        assertFalse(keys1.hasAlias());

        //--
        String alias0 = "Alias0";
        String alias1 = "Alias1";
        keys2 = PersonExt.SUPERIOR.alias(alias0).add(SUPERIOR, alias1);
        assertEquals(2, keys2.getKeyCount());
        assertEquals(alias0, keys2.getAlias(0));
        assertEquals(alias1, keys2.getAlias(1));
        assertTrue(keys2.hasAlias());

        //--
        String alias2 = "Alias2";
        String alias3 = "Alias3";
        keys3 = PersonExt.SUPERIOR.alias(alias0).add(SUPERIOR, alias2).add(SUPERIOR, alias3);
        assertEquals(3, keys3.getKeyCount());
        assertEquals(alias0, keys3.getAlias(0));
        assertEquals(alias2, keys3.getAlias(1));
        assertEquals(alias3, keys3.getAlias(2));
        assertTrue(keys3.hasAlias());

        //--
        keys4 = (CompositeKey<PersonExt,PersonExt>) keys3.descending();
        assertEquals(3, keys4.getKeyCount());
        assertEquals(alias0, keys4.getAlias(0));
        assertEquals(alias2, keys4.getAlias(1));
        assertEquals(alias3, keys4.getAlias(2));
        assertFalse(keys4.isAscending());
        assertTrue(keys4.hasAlias());

        //--
        keys5 = keys2.add(keys4);
        assertEquals(5, keys5.getKeyCount());
        assertEquals(alias0, keys5.getAlias(0));
        assertEquals(alias1, keys5.getAlias(1));
        assertEquals(alias0, keys5.getAlias(2));
        assertEquals(alias2, keys5.getAlias(3));
        assertEquals(alias3, keys5.getAlias(4));
        assertFalse(keys5.isAscending());
        assertTrue(keys5.hasAlias());
    }
}
