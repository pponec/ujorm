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
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import junit.framework.TestCase;
import org.ujorm.core.ujos.UjoCSV;

/**
 *
 * @author Ponec
 */
public class PropertyStoreTest extends TestCase {

    public PropertyStoreTest(String testName) {
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
    public void testKeyName() {
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
    public void testGetBaseClass() throws Exception {
        System.out.println("getBaseClass");
        UjoCSV ujo = createUjoInstance();

        KeyRing<UjoCSV> props1, props2;
        props1 = KeyRing.of(UjoCSV.class, UjoCSV.P1, UjoCSV.P3);
        props2 = null;

        try {
            ByteArrayOutputStream dataFile = new ByteArrayOutputStream();
            ObjectOutput encoder = new ObjectOutputStream(dataFile);
            encoder.writeObject(props1);
            encoder.close();
            InputStream is = new ByteArrayInputStream(dataFile.toByteArray());
            ObjectInput decoder = new ObjectInputStream(is);
            props2 = (KeyRing<UjoCSV>) decoder.readObject();
        } catch (Throwable e) {
            e.printStackTrace();
            assertNull(e);
        }

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
    public void testGetBaseClass_2() throws Exception {
        System.out.println("getBaseClass");
        UjoCSV ujo = createUjoInstance();

        KeyRing<UjoCSV> props1, props2;
        props1 = KeyRing.of(UjoCSV.P1, UjoCSV.P3);
        props2 = null;

        try {
            ByteArrayOutputStream dataFile = new ByteArrayOutputStream();
            ObjectOutput encoder = new ObjectOutputStream(dataFile);
            encoder.writeObject(props1);
            encoder.close();
            InputStream is = new ByteArrayInputStream(dataFile.toByteArray());
            ObjectInput decoder = new ObjectInputStream(is);
            props2 = (KeyRing<UjoCSV>) decoder.readObject();
        } catch (Throwable e) {
            e.printStackTrace();
            assertNull(e);
        }

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
    public void testGetBaseClassDesc() throws Exception {
        System.out.println("getBaseClass");
        UjoCSV ujo = createUjoInstance();

        KeyRing<UjoCSV> props1, props2;
        props1 = KeyRing.of(UjoCSV.class, UjoCSV.P1, UjoCSV.P3.descending()); // !!!
        props2 = null;

        try {
            ByteArrayOutputStream dataFile = new ByteArrayOutputStream();
            ObjectOutput encoder = new ObjectOutputStream(dataFile);
            encoder.writeObject(props1);
            encoder.close();
            InputStream is = new ByteArrayInputStream(dataFile.toByteArray());
            ObjectInput decoder = new ObjectInputStream(is);
            props2 = (KeyRing<UjoCSV>) decoder.readObject();
        } catch (Throwable e) {
            assertNull(e);
        }

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
    public void testGetBaseClassDesc_2() throws Exception {
        System.out.println("getBaseClass");
        UjoCSV ujo = createUjoInstance();

        KeyRing<UjoCSV> props1, props2;
        props1 = KeyRing.of(UjoCSV.P1, UjoCSV.P3.descending()); // !!!
        props2 = null;

        try {
            ByteArrayOutputStream dataFile = new ByteArrayOutputStream();
            ObjectOutput encoder = new ObjectOutputStream(dataFile);
            encoder.writeObject(props1);
            encoder.close();
            InputStream is = new ByteArrayInputStream(dataFile.toByteArray());
            ObjectInput decoder = new ObjectInputStream(is);
            props2 = (KeyRing<UjoCSV>) decoder.readObject();
        } catch (Throwable e) {
            assertNull(e);
        }

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
}
