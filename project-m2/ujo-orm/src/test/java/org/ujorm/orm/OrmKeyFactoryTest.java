/*
 *  Copyright 2018-2022 Pavel Ponec
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

import org.junit.Test;
import org.ujorm.extensions.types.UnsignedShort;
import org.ujorm.orm.bo.ACustomerExtended;
import org.ujorm.orm.bo.XCustomerExtended;
import static junit.framework.TestCase.assertEquals;

/**
 * A test of the OrmKeyFactory class.
 * @author Pavel Ponec
 */
public class OrmKeyFactoryTest {

    /** Test of createKey method, of class OrmKeyFactory. */
    @Test
    public void testCreateOrmKey() {
        System.out.println("createOrmKey");

        final Long iBase = 1L;
        final Long idExte = 2L;
        final XCustomerExtended cust = new XCustomerExtended();

        assertEquals(0, XCustomerExtended.ID.getIndex());
        assertEquals(7, XCustomerExtended.ID_EXTENDED.getIndex());

        assertEquals("id", XCustomerExtended.ID.getName());
        assertEquals("idExtended", XCustomerExtended.ID_EXTENDED.getName());

        cust.setId(iBase);
        cust.setIdExtended(idExte);
        cust.setPin(UnsignedShort.of(1234));

        assertEquals(iBase, cust.getId());
        assertEquals(idExte, cust.getIdExtended());
    }

    /** Test of createKey method, of class KeyFactory. */
    @Test
    public void testCreateKey() {
        System.out.println("createCoreKey");

        final Long iBase = 1L;
        final Long idExte = 2L;
        final ACustomerExtended cust = new ACustomerExtended();

        assertEquals(0, ACustomerExtended.ID.getIndex());
        assertEquals(6, ACustomerExtended.ID_EXTENDED.getIndex());

        assertEquals("id", ACustomerExtended.ID.getName());
        assertEquals("idExtended", ACustomerExtended.ID_EXTENDED.getName());

        cust.setId(iBase);
        cust.setIdExtended(idExte);

        assertEquals(iBase, cust.getId());
        assertEquals(idExte, cust.getIdExtended());
    }
}
