/*
 * Copyright 2011-2022 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;
import org.ujorm.CompositeKey;
import org.ujorm.core.ujos.UjoCSV;
import org.ujorm.core.ujos.UjoName;
import static org.ujorm.core.ujos.UjoName.*;

/**
 *
 * @author Ponec
 */
public class KeyRingTest extends org.junit.jupiter.api.Assertions {

    /**
     * Test of getType method, of class KeyRing.
     */
    @Test
    public void testKeyName() {
        assertEquals("P1", UjoCSV.P1.getName());
        assertEquals("P2", UjoCSV.P2.getName());
        assertEquals("P3", UjoCSV.P3.getName());
        //
        assertEquals("P1"    , UjoCSV.P1.toString());
        assertEquals("P2"    , UjoCSV.P2.toString());
        assertEquals("P3[a3]", UjoCSV.P3.toString());
    }

    /**
     * Test of the excluded keys
     */
    @Test
    public void testExcludedKeys() throws Exception {
        KeyRing<UjoCSV> ring = KeyRing.ofExcluding(UjoCSV.class, UjoCSV.P1, UjoCSV.P2);
        assertEquals(1, ring.size());
        assertEquals(UjoCSV.P3.getName(), ring.getFirstKey().getName());
    }

    /**
     * Test of getType method, of class KeyRing.
     */
    @Test
    public void testGetBaseClass() throws Exception {
        System.out.println("getBaseClass");
        UjoCSV ujo = createUjoInstance();

        KeyRing<UjoCSV> ring1, ring2;
        ring1 = KeyRing.of(UjoCSV.class, UjoCSV.P1, UjoCSV.P3);
        ring2 = serialize(ring1);

        assertEquals(ring1.size(), ring2.size());
        assertSame(ring1.getFirstKey(), ring2.getFirstKey());
        assertSame(ring1.getFirstKey().of(ujo), ring2.getFirstKey().of(ujo));
        assertSame(ring1.getFirstKey().isAscending(), ring2.getFirstKey().isAscending());
        assertEquals(ring1.getLastKey(), ring2.getLastKey());
        assertEquals(ring1.getLastKey(), ring2.getLastKey());
        assertEquals(((CompositeKey)ring1.getLastKey()).getAlias(0), ((CompositeKey)ring2.getLastKey()).getAlias(0));
        assertSame(ring1.getLastKey().of(ujo), ring2.getLastKey().of(ujo));
        assertTrue(ring2.contains(ring1.getFirstKey()));
        assertFalse(ring2.contains(UjoCSV.P2));
        assertEquals(ring2, ring1);
    }

    /**
     * Test of getType method, of class KeyRing.
     */
    @Test
    public void testGetBaseClass_2() throws Exception {
        System.out.println("getBaseClass");
        UjoCSV ujo = createUjoInstance();

        KeyRing<UjoCSV> props1, props2;
        props1 = KeyRing.of(UjoCSV.P1, UjoCSV.P3);
        props2 = serialize(props1);

        assertEquals(props1.size(), props2.size());
        assertSame(props1.getFirstKey(), props2.getFirstKey());
        assertSame(props1.getFirstKey().of(ujo), props2.getFirstKey().of(ujo));
        assertSame(props1.getFirstKey().isAscending(), props2.getFirstKey().isAscending());
        assertEquals(props1.getLastKey(), props2.getLastKey());
        assertSame(props1.getLastKey().of(ujo), props2.getLastKey().of(ujo));
        assertTrue(props2.contains(props1.getFirstKey()));
        assertFalse(props2.contains(UjoCSV.P2));
        assertEquals(props2, props1);
    }

    /**
     * Test of getType method, of class KeyRing.
     */
    @Test
    public void testGetBaseClassDesc() throws Exception {
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
        assertEquals(((CompositeKey)props1.getLastKey()).getAlias(0), ((CompositeKey)props2.getLastKey()).getAlias(0));
        assertSame(props1.getLastKey().of(ujo), props2.getLastKey().of(ujo));
        assertTrue(props2.contains(props1.getFirstKey()));
        assertFalse(props2.contains(UjoCSV.P2));
        assertEquals(props2, props1);
    }

    /**
     * Test of getType method, of class KeyRing.
     */
    @Test
    public void testGetBaseClassDesc_2() throws Exception {
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
        assertEquals(props2, props1);
    }

    // ------------ KEY SPACES ------------

    /**
     * Test of getType method, of class KeyRing.
     */
    @Test
    public void testKeyAliasName() throws Exception {
        CompositeKey<UjoName,?> cKey;
        KeyRing<UjoName> key1, key2;

        key1 = KeyRing.of(S1.add(S2).add(S1));
        key2 = serialize(key1);
        cKey = (CompositeKey<UjoName,?>) key2.get(0);
        assertNull(cKey.getAlias(0));
        assertNull(cKey.getAlias(1));
        assertNull(cKey.getAlias(2));

        // ---

        key1 = KeyRing.of(S1.add(S2, "A1").add(S1, "A2"));
        key2 = serialize(key1);
        cKey = (CompositeKey<UjoName,?>) key2.get(0);
        assertNull(cKey.getAlias(0));
        assertEquals("A1", cKey.getAlias(1));
        assertEquals("A2", cKey.getAlias(2));

        // ---

        key1 = KeyRing.of(S1.add(S2, "A1").add(S1).add(S1, "A3").add(S1));
        key2 = serialize(key1);
        cKey = (CompositeKey<UjoName,?>) key2.get(0);
        assertNull(cKey.getAlias(0));
        assertEquals("A1", cKey.getAlias(1));
        assertNull(cKey.getAlias(2));
        assertEquals("A3", cKey.getAlias(3));
        assertNull(cKey.getAlias(4));

        // ---

        key1 = KeyRing.of(S1.add(S1, "A1"), S1.add(S2, "A2"));
        key2 = serialize(key1);
        cKey = (CompositeKey<UjoName,?>) key2.get(0);
        assertNull(cKey.getAlias(0));
        assertEquals("A1", cKey.getAlias(1));
        cKey = (CompositeKey<UjoName,?>) key2.get(1);
        assertNull(cKey.getAlias(0));
        assertEquals("A2", cKey.getAlias(1));
    }

    /**
     * Test of getType method, of class KeyRing.
     */
    @Test
    public void testKeyAliasName2() throws Exception {
        CompositeKey<UjoName,?> cKey;
        KeyRing<UjoName> key1, key2;

        key1 = KeyRing.of(S1.add(S2).add(S1));
        key2 = serialize(key1);
        cKey = (CompositeKey<UjoName,?>) key2.get(0);
        assertNull(cKey.getAlias(0));
        assertNull(cKey.getAlias(1));
        assertNull(cKey.getAlias(2));

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
        assertNull(cKey.getAlias(2));
        assertEquals("A3", cKey.getAlias(3));
        assertNull(cKey.getAlias(4));

        // ---

        key1 = KeyRing.of(S1.alias("A0").add(S1, "A1"), S1.add(S2, "A2"));
        key2 = serialize(key1);
        cKey = (CompositeKey<UjoName,?>) key2.get(0);
        assertEquals("A0", cKey.getAlias(0));
        assertEquals("A1", cKey.getAlias(1));
        cKey = (CompositeKey<UjoName,?>) key2.get(1);
        assertNull(cKey.getAlias(0));
        assertEquals("A2", cKey.getAlias(1));
    }

    // ------------ HELP ------------

    private UjoCSV createUjoInstance() {
        UjoCSV ujo = new UjoCSV();
        UjoCSV.P1.setValue(ujo, "text1");
        UjoCSV.P2.setValue(ujo, "text2");
        UjoCSV.P3.setValue(ujo, "text3");
        return ujo;
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
            return (T) decoder.readObject();
        }
    }
}
