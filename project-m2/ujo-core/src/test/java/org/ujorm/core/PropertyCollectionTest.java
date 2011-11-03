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
import org.ujorm.implementation.xmlSpeed.PojoTree;
import org.ujorm.implementation.xmlSpeed.ZCounter;

/**
 *
 * @author Ponec
 */
public class PropertyCollectionTest extends TestCase {

    public PropertyCollectionTest(String testName) {
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
     * Test of getBaseClass method, of class PropertyCollection.
     */
    public void testGetBaseClass() throws Exception {
        System.out.println("getBaseClass");
        UjoCSV ujo = createUjoInstance();

        PropertyCollection<UjoCSV> props1, props2;
        props1 = PropertyCollection.newInstance(UjoCSV.class, UjoCSV.P1, UjoCSV.P3);
        props2 = null;

        try {
            ByteArrayOutputStream dataFile = new ByteArrayOutputStream();
            ObjectOutput encoder = new ObjectOutputStream(dataFile);
            encoder.writeObject(props1);
            encoder.close();
            InputStream is = new ByteArrayInputStream(dataFile.toByteArray());
            ObjectInput decoder = new ObjectInputStream(is);
            props2 = (PropertyCollection<UjoCSV>) decoder.readObject();
        } catch (Throwable e) {
            assertNull(e);
        }

        assertEquals(props1.size(), props2.size());
        assertSame(props1.getFirstProperty(), props2.getFirstProperty());
        assertSame(props1.getFirstProperty().getValue(ujo), props2.getFirstProperty().getValue(ujo));
        assertSame(props1.getLastProperty(), props2.getLastProperty());
        assertSame(props1.getLastProperty().getValue(ujo), props2.getLastProperty().getValue(ujo));
        assertTrue(props2.contains(props1.getFirstProperty()));
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
