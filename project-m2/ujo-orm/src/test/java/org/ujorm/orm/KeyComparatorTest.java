/*
 *  Copyright 2009-2013 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.orm;

import junit.framework.TestCase;
import org.ujorm.orm.bo.XItem;

/**
 * The tests of the SQL LIMIT & OFFSET.
 * @author Pavel Ponec
 */
public class KeyComparatorTest extends TestCase {

    /** Main Handler */
    private static OrmHandler handler;

    public KeyComparatorTest(String testName) {
        super(testName);
    }

    private static Class suite() {
        return KeyComparatorTest.class;
    }

    // ---------- TESTS -----------------------

    /** Compare two keys according to inner key sequence count */
    @SuppressWarnings("deprecation")
    public void testKeyComparator() {

        assertEquals(0, Query.INNER_KEY_COMPARATOR.compare(XItem.ID, XItem.NOTE));
        assertEquals(-1, Query.INNER_KEY_COMPARATOR.compare(XItem.ID, XItem.NOTE.descending()));
        assertEquals(1, Query.INNER_KEY_COMPARATOR.compare(XItem.ID.descending(), XItem.NOTE));
        assertEquals(0, Query.INNER_KEY_COMPARATOR.compare(XItem.ID.descending(), XItem.NOTE.descending()));
        assertEquals(0, Query.INNER_KEY_COMPARATOR.compare(XItem.$ORDER_DATE, XItem.$ORDER_DATE));
        assertEquals(0, Query.INNER_KEY_COMPARATOR.compare(XItem.$CUST_FIRSTNAME, XItem.$CUST_FIRSTNAME));
        assertEquals(-1, Query.INNER_KEY_COMPARATOR.compare(XItem.ID, XItem.$ORDER_DATE));
        assertEquals(-1, Query.INNER_KEY_COMPARATOR.compare(XItem.$ORDER_DATE, XItem.$CUST_FIRSTNAME));
        assertEquals(1, Query.INNER_KEY_COMPARATOR.compare(XItem.$ORDER_DATE, XItem.ID));
        assertEquals(1, Query.INNER_KEY_COMPARATOR.compare(XItem.$CUST_FIRSTNAME, XItem.$ORDER_DATE));
        
    }

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
}
