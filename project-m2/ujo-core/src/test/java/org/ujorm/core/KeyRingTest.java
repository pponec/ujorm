/*
 *  Copyright 2011 pavel.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.ujorm.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import junit.framework.TestCase;
import org.ujorm.CompositeKey;
import org.ujorm.core.KeyRing;
import org.ujorm.core.ujos.UjoCSV;
import org.ujorm.core.ujos.UjoName;
import static org.ujorm.core.ujos.UjoName.*;

/**
 *
 * @author Ponec
 */
public class KeyRingTest extends TestCase {

    public KeyRingTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of getType method, of class KeyRing.
     */
    public void XX_testKeyName() {
        assertEquals("P1", UjoCSV.P1.getName());
        assertEquals("P2", UjoCSV.P2.getName());
        assertEquals("P3", UjoCSV.P3.getName());
        //
        assertEquals("P1", UjoCSV.P1.toString());
        assertEquals("P2", UjoCSV.P2.toString());
        assertEquals("P3", UjoCSV.P3.toString());
    }

    /**
     * Test of getType method, of class KeyRing.
     */
    public void XX_testGetBaseClass() throws Exception {
        System.out.println("getBaseClass");
        UjoCSV ujo = createUjoInstance();

        KeyRing<UjoCSV> props1, props2;
        props1 = KeyRing.of(UjoCSV.class, UjoCSV.P1, UjoCSV.P3);
        props2 = serialize(props1);

        assertEquals(props1.size(), props2.size());
        assertSame(props1.getFirstKey(), props2.getFirstKey());
        assertSame(props1.getFirstKey().of(ujo), props2.getFirstKey().of(ujo));
        assertSame(props1.getFirstKey().isAscending(), props2.getFirstKey().isAscending());
        assertSame(props1.getLastKey(), props2.getLastKey());
        assertSame(props1.getLastKey().of(ujo), props2.getLastKey().of(ujo));
        assertTrue(props2.contains(props1.getFirstKey()));
        assertFalse(props2.contains(UjoCSV.P2));
        assertTrue(props2.equals(props1));
    }

    /**
     * Test of getType method, of class KeyRing.
     */
    public void XX_testGetBaseClass_2() throws Exception {
        System.out.println("getBaseClass");
        UjoCSV ujo = createUjoInstance();

        KeyRing<UjoCSV> props1, props2;
        props1 = KeyRing.of(UjoCSV.P1, UjoCSV.P3);
        props2 = serialize(props1);

        assertEquals(props1.size(), props2.size());
        assertSame(props1.getFirstKey(), props2.getFirstKey());
        assertSame(props1.getFirstKey().of(ujo), props2.getFirstKey().of(ujo));
        assertSame(props1.getFirstKey().isAscending(), props2.getFirstKey().isAscending());
        assertSame(props1.getLastKey(), props2.getLastKey());
        assertSame(props1.getLastKey().of(ujo), props2.getLastKey().of(ujo));
        assertTrue(props2.contains(props1.getFirstKey()));
        assertFalse(props2.contains(UjoCSV.P2));
        assertTrue(props2.equals(props1));
    }

    /**
     * Test of getType method, of class KeyRing.
     */
    public void XX_testGetBaseClassDesc() throws Exception {
        System.out.println("getBaseClass");
        UjoCSV ujo = createUjoInstance();

        KeyRing<UjoCSV> props1, props2;
        props1 = KeyRing.of(UjoCSV.class, UjoCSV.P1, UjoCSV.P3.descending()); // !!!
        props2 = serialize(props1);

        assertEquals(props1.size(), props2.size());
        assertSame(props1.getFirstKey(), props2.getFirstKey());
        assertSame(props1.getFirstKey().of(ujo), props2.getFirstKey().of(ujo));
        assertSame(props1.getFirstKey().isAscending(), props2.getFirstKey().isAscending());
        assertEquals(props1.getLastKey(), props2.getLastKey());
        assertSame(props1.getLastKey().of(ujo), props2.getLastKey().of(ujo));
        assertTrue(props2.contains(props1.getFirstKey()));
        assertFalse(props2.contains(UjoCSV.P2));
        assertTrue(props2.equals(props1));
    }

    /**
     * Test of getType method, of class KeyRing.
     */
    public void XX_testGetBaseClassDesc_2() throws Exception {
        System.out.println("getBaseClass");
        UjoCSV ujo = createUjoInstance();

        KeyRing<UjoCSV> props1, props2;
        props1 = KeyRing.of(UjoCSV.P1, UjoCSV.P3.descending()); // !!!
        props2 = serialize(props1);

        assertEquals(props1.size(), props2.size());
        assertSame(props1.getFirstKey(), props2.getFirstKey());
        assertSame(props1.getFirstKey().of(ujo), props2.getFirstKey().of(ujo));
        assertSame(props1.getFirstKey().isAscending(), props2.getFirstKey().isAscending());
        assertEquals(props1.getLastKey(), props2.getLastKey());
        assertSame(props1.getLastKey().of(ujo), props2.getLastKey().of(ujo));
        assertTrue(props2.contains(props1.getFirstKey()));
        assertFalse(props2.contains(UjoCSV.P2));
        assertTrue(props2.equals(props1));
    }

    private UjoCSV createUjoInstance() {
        UjoCSV ujo = new UjoCSV();
        UjoCSV.P1.setValue(ujo, "text1");
        UjoCSV.P2.setValue(ujo, "text2");
        UjoCSV.P3.setValue(ujo, "text3");
        return ujo;
    }

    // ------------ KEY SPACES ------------

    /**
     * Test of getType method, of class KeyRing.
     */
    public void testKeyAliasName() throws Exception {
        CompositeKey<UjoName,?> cKey;
        KeyRing<UjoName> key1, key2;

        key1 = KeyRing.of(S1.add(S2).add(S1));
        key2 = serialize(key1);
        cKey = (CompositeKey<UjoName,?>) key2.get(0);
        assertEquals(null, cKey.getAlias(0));
        assertEquals(null, cKey.getAlias(1));
        assertEquals(null, cKey.getAlias(2));

        // ---

        key1 = KeyRing.of(S1.add(S2, "A1").add(S1, "A2"));
        key2 = serialize(key1);
        cKey = (CompositeKey<UjoName,?>) key2.get(0);
        assertEquals(null, cKey.getAlias(0));
        assertEquals("A1", cKey.getAlias(1));
        assertEquals("A2", cKey.getAlias(2));

        // ---

        key1 = KeyRing.of(S1.add(S2, "A1").add(S1).add(S1, "A3").add(S1));
        key2 = serialize(key1);
        cKey = (CompositeKey<UjoName,?>) key2.get(0);
        assertEquals(null, cKey.getAlias(0));
        assertEquals("A1", cKey.getAlias(1));
        assertEquals(null, cKey.getAlias(2));
        assertEquals("A3", cKey.getAlias(3));
        assertEquals(null, cKey.getAlias(4));

        // ---

        key1 = KeyRing.of(S1.add(S1, "A1"), S1.add(S2, "A2"));
        key2 = serialize(key1);
        cKey = (CompositeKey<UjoName,?>) key2.get(0);
        assertEquals(null, cKey.getAlias(0));
        assertEquals("A1", cKey.getAlias(1));
        cKey = (CompositeKey<UjoName,?>) key2.get(1);
        assertEquals(null, cKey.getAlias(0));
        assertEquals("A2", cKey.getAlias(1));
    }

    /**
     * Test of getType method, of class KeyRing.
     */
    public void testKeyAliasName2() throws Exception {
        CompositeKey<UjoName,?> cKey;
        KeyRing<UjoName> key1, key2;

        key1 = KeyRing.of(S1.add(S2).add(S1));
        key2 = serialize(key1);
        cKey = (CompositeKey<UjoName,?>) key2.get(0);
        assertEquals(null, cKey.getAlias(0));
        assertEquals(null, cKey.getAlias(1));
        assertEquals(null, cKey.getAlias(2));

        // ---

        key1 = KeyRing.of(S1.alias("A0").add(S2, "A1").add(S1, "A2"));
        key2 = serialize(key1);
        cKey = (CompositeKey<UjoName,?>) key2.get(0);
        assertEquals("A0", cKey.getAlias(0));
        assertEquals("A1", cKey.getAlias(1));
        assertEquals("A2", cKey.getAlias(2));

        // ---

        key1 = KeyRing.of(S1.alias("A0").add(S2, "A1").add(S1).add(S1, "A3").add(S1));
        key2 = serialize(key1);
        cKey = (CompositeKey<UjoName,?>) key2.get(0);
        assertEquals("A0", cKey.getAlias(0));
        assertEquals("A1", cKey.getAlias(1));
        assertEquals(null, cKey.getAlias(2));
        assertEquals("A3", cKey.getAlias(3));
        assertEquals(null, cKey.getAlias(4));

        // ---

        key1 = KeyRing.of(S1.alias("A0").add(S1, "A1"), S1.add(S2, "A2"));
        key2 = serialize(key1);
        cKey = (CompositeKey<UjoName,?>) key2.get(0);
        assertEquals("A0", cKey.getAlias(0));
        assertEquals("A1", cKey.getAlias(1));
        cKey = (CompositeKey<UjoName,?>) key2.get(1);
        assertEquals(null, cKey.getAlias(0));
        assertEquals("A2", cKey.getAlias(1));
    }

    // ------------ HELP ------------

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
}
