/*
 *  Copyright 2014-2022 Pavel Ponec
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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import junit.framework.TestCase;
import org.ujorm.Key;
import org.ujorm.orm.bo.XCustomer;
import static org.ujorm.orm.bo.XCustomer.*;

/**
 * The test of space key
 * @author ponec
 */
public class AliasKeyTest extends org.junit.jupiter.api.Assertions {

    /**
     * Test of getKey method, of class Relation.
     */
    public void testKeyName() {
        assertEquals("id", XCustomer.ID.getName());
        assertEquals("firstname", XCustomer.FIRSTNAME.getName());
        assertEquals("lastname", XCustomer.LASTNAME.getName());
    }

    /**
     * Test of getKey method, of class Relation.
     */
    public void testConditions() {
        Set<AliasKey> outSet = new HashSet<>();
        LinkedList<AliasKey> outList = new LinkedList<>();
        //-
        Key key0 = XCustomer.SUPERIOR;
        AliasKey.addKeys(key0, outList);
        AliasKey.addKeys(key0, outSet);
        assertEquals(1, outList.size());
        assertEquals(1, outSet.size());
        outList.clear();
        outSet.clear();
        //-
        Key key1 = XCustomer.SUPERIOR.add(SUPERIOR, "a1").add(SUPERIOR, "a2");
        AliasKey.addKeys(key1, outList);
        AliasKey.addKeys(key1, outSet);
        assertEquals(3, outList.size());
        assertEquals(3, outSet.size());
        assertEquals("a1", outList.getLast().getAliasFrom());
        assertEquals("a2", outList.getLast().getAliasTo());
        //-
        Key key2 = XCustomer.SUPERIOR.add(SUPERIOR, "a1").add(SUPERIOR, "a2");
        AliasKey.addKeys(key2, outList);
        AliasKey.addKeys(key2, outSet);
        assertEquals(6, outList.size());
        assertEquals(3, outSet.size());
        assertEquals("a1", outList.getLast().getAliasFrom());
        assertEquals("a2", outList.getLast().getAliasTo());
        //-
        Key key3 = XCustomer.SUPERIOR.add(SUPERIOR, "b1").add(SUPERIOR, "b2");
        AliasKey.addKeys(key3, outList);
        AliasKey.addKeys(key3, outSet);
        assertEquals(9, outList.size());
        assertEquals(5, outSet.size());
        assertEquals("b1", outList.getLast().getAliasFrom());
        assertEquals("b2", outList.getLast().getAliasTo());
    }

    /**
     * Test of getKey method, of class Relation.
     */
    public void testRelations() {
        Set<AliasKey> outSet = new HashSet<>();
        LinkedList<AliasKey> outList = new LinkedList<>();
        //-
        Key key0 = XCustomer.SUPERIOR;
        AliasKey.addKeys(key0, outList);
        AliasKey.addKeys(key0, outSet);
        assertEquals(1, outList.size());
        assertEquals(1, outSet.size());
        outList.clear();
        outSet.clear();
        //-
        Key key1 = XCustomer.SUPERIOR.add(SUPERIOR, "a1").add(SUPERIOR, "a2");
        AliasKey.addRelations(key1, outList);
        AliasKey.addRelations(key1, outSet);
        assertEquals(2, outList.size());
        assertEquals(2, outSet.size());
        assertNull(outList.getLast().getAliasFrom());
        assertEquals("a1", outList.getLast().getAliasTo());
        //-
        Key key2 = XCustomer.SUPERIOR.add(SUPERIOR, "a1").add(SUPERIOR, "a2");
        AliasKey.addRelations(key2, outList);
        AliasKey.addRelations(key2, outSet);
        assertEquals(4, outList.size());
        assertEquals(2, outSet.size());
        assertNull(outList.getLast().getAliasFrom());
        assertEquals("a1", outList.getLast().getAliasTo());
        //-
        Key key3 = XCustomer.SUPERIOR.add(SUPERIOR, "b1").add(SUPERIOR, "b2");
        AliasKey.addRelations(key3, outList);
        AliasKey.addRelations(key3, outSet);
        assertEquals(6, outList.size());
        assertEquals(3, outSet.size());
        assertNull(outList.getLast().getAliasFrom());
        assertEquals("b1", outList.getLast().getAliasTo());
    }

    /**
     * Test of getKey method, of class Relation.
     */
    public void testLastCondition() {
        Set<AliasKey> outSet = new HashSet<>();
        LinkedList<AliasKey> outList = new LinkedList<>();
        //-
        Key key0 = XCustomer.SUPERIOR;
        AliasKey.addLastKey(key0, outList);
        AliasKey.addLastKey(key0, outSet);
        assertEquals(1, outList.size());
        assertEquals(1, outSet.size());
        outList.clear();
        outSet.clear();
        //-
        Key key1 = XCustomer.SUPERIOR.add(SUPERIOR, "a1").add(SUPERIOR, "a2");
        AliasKey.addLastKey(key1, outList);
        AliasKey.addLastKey(key1, outSet);
        assertEquals(1, outList.size());
        assertEquals(1, outSet.size());
        assertEquals("a1", outList.getLast().getAliasFrom());
        assertEquals("a2", outList.getLast().getAliasTo());
        //-
        Key key2 = XCustomer.SUPERIOR.add(SUPERIOR, "a1").add(SUPERIOR, "a2");
        AliasKey.addLastKey(key2, outList);
        AliasKey.addLastKey(key2, outSet);
        assertEquals(2, outList.size());
        assertEquals(1, outSet.size());
        assertEquals("a1", outList.getLast().getAliasFrom());
        assertEquals("a2", outList.getLast().getAliasTo());
        //-
        Key key3 = XCustomer.SUPERIOR.add(SUPERIOR, "b1").add(SUPERIOR, "b2");
        AliasKey.addLastKey(key3, outList);
        AliasKey.addLastKey(key3, outSet);
        assertEquals(3, outList.size());
        assertEquals(2, outSet.size());
        assertEquals("b1", outList.getLast().getAliasFrom());
        assertEquals("b2", outList.getLast().getAliasTo());
    }


}
