/*
 *  Copyright 2009-2010 pavel.
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

package org.ujoframework.orm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import junit.framework.TestCase;
import org.ujoframework.orm.bo.XOrder;

/**
 *
 * @author Pavel Ponec
 */
public class SerializatonTest extends TestCase {
    
    public SerializatonTest(String testName) {
        super(testName);
    }

    /**
     * Test of getValue method, of class UniqueKey.
     */
    public void testKeySerialization() throws Exception {
        System.out.println("testKeySerialization");
        ForeignKey expResult = new ForeignKey(123L);

        ForeignKey result = serialize(expResult);
        assertEquals(expResult, result);
    }

    /**
     * Test of getValue method, of class UniqueKey.
     */
    public void testOrmTableSerialization() throws Exception {
        System.out.println("testOrmTableSerialization");
        XOrder expResult = new XOrder();
        expResult.setId(33L);
        expResult.setDate(new Date());
        expResult.setState(XOrder.State.DELETED);
        //
        XOrder result = serialize(expResult);
        assertEquals(expResult.getId(), result.getId());
        assertEquals(expResult.getDate(), result.getDate());
        assertSame(expResult.getState(), result.getState());
        assertSame(expResult.getDescr(), result.getDescr());
    }


    /** Object serialization */
    @SuppressWarnings("unchecked")
    private <T> T serialize(Serializable object) throws Exception {

        ByteArrayOutputStream dataFile = new ByteArrayOutputStream(8000);

        ObjectOutputStream encoder = null;
        ObjectInputStream decoder = null;

        encoder = new ObjectOutputStream(dataFile);
        encoder.writeObject(object);
        encoder.flush();
        encoder.close();
        //
        InputStream is = new ByteArrayInputStream(dataFile.toByteArray());
        decoder = new ObjectInputStream(is);
        Object result = (Serializable) decoder.readObject();
        decoder.close();

        return (T) result;
    }



}
