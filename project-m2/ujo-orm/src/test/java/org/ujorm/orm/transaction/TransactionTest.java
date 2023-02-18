/*
 *  Copyright 2020-2022 Pavel Ponec
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
package org.ujorm.orm.transaction;

import junit.framework.TestCase;
import org.ujorm.orm.*;
import org.ujorm.orm.inheritance.sample.Database;
import org.ujorm.orm.inheritance.sample.bo.Customer;
import org.ujorm.orm.inheritance.sample.bo.User;
import org.ujorm.orm.metaModel.MetaParams;

/**
 * The tests of the SQL LIMIT & OFFSET.
 * @author Pavel Ponec
 */
public class TransactionTest extends org.junit.jupiter.api.Assertions {


    // ---------- TESTS -----------------------

    public void testRollbackTranaction() {
        try (Session session = getHandler().createSession()) {
            session.delete(Customer.USER.forAll()); // TODO ?
            session.delete(User.id.forAll());
            session.commit();
            //
            session.insert(new User());
            assertEquals(1L, session.createQuery(User.class).getCount());
            assertNull(session.getTransaction());
            { // 1st Level
                session.beginTransaction();
                session.insert(new User());
                assertEquals(2L, session.createQuery(User.class).getCount());
                assertTrue(session.getTransaction().isRoot());
                { // 2md Level
                    session.beginTransaction();
                    session.insert(new User());
                    assertEquals(3L, session.createQuery(User.class).getCount());
                    { // 3rd Level
                        session.beginTransaction();
                        session.insert(new User());
                        assertEquals(4L, session.createQuery(User.class).getCount());
                        session.rollbackTransaction();
                    }
                    assertEquals(3L, session.createQuery(User.class).getCount());
                    assertFalse(session.getTransaction().isRoot());
                    session.rollbackTransaction();
                }
                assertEquals(2L, session.createQuery(User.class).getCount());
                assertTrue(session.getTransaction().isRoot());
                session.rollbackTransaction();
            }
            assertEquals(1L, session.createQuery(User.class).getCount());
            assertNull(session.getTransaction());
            session.rollback();
            assertEquals(0L, session.createQuery(User.class).getCount());
            assertNull(session.getTransaction());
        }
    }

    // ---------- TESTS -----------------------

    public void testCommitTranaction() {
        try (Session session = getHandler().createSession()) {
            session.delete(Customer.USER.forAll()); // TODO ?
            session.delete(User.id.forAll());
            session.commit();
            //
            session.insert(new User());
            assertEquals(1L, session.createQuery(User.class).getCount());
            assertNull(session.getTransaction());
            { // 1st Level
                session.beginTransaction();
                session.insert(new User());
                assertEquals(2L, session.createQuery(User.class).getCount());
                assertTrue(session.getTransaction().isRoot());
                { // 2md Level
                    session.beginTransaction();
                    session.insert(new User());
                    assertEquals(3L, session.createQuery(User.class).getCount());
                    { // 3rd Level
                        session.beginTransaction();
                        session.insert(new User());
                        assertEquals(4L, session.createQuery(User.class).getCount());
                        session.commitTransaction();
                    }
                    assertEquals(4L, session.createQuery(User.class).getCount());
                    assertFalse(session.getTransaction().isRoot());
                    session.commitTransaction();
                }
                assertEquals(4L, session.createQuery(User.class).getCount());
                assertTrue(session.getTransaction().isRoot());
                session.commitTransaction();
            }
            assertEquals(4L, session.createQuery(User.class).getCount());
            assertNull(session.getTransaction());
            session.commit();
            assertEquals(4L, session.createQuery(User.class).getCount());
        }
    }

    // ---------- TESTS -----------------------

    public void testIComplexTranaction() {
        try (Session session = getHandler().createSession()) {
            session.delete(Customer.USER.forAll()); // TODO ?
            session.delete(User.id.forAll());
            session.commit();
            //
            session.insert(new User());
            assertEquals(1L, session.createQuery(User.class).getCount());
            assertNull(session.getTransaction());
            { // 1st Level
                session.beginTransaction();
                session.insert(new User());
                assertEquals(2L, session.createQuery(User.class).getCount());
                assertTrue(session.getTransaction().isRoot());
                { // 2md Level
                    session.beginTransaction();
                    session.insert(new User());
                    assertEquals(3L, session.createQuery(User.class).getCount());
                    { // 3rd Level
                        session.beginTransaction();
                        session.insert(new User());
                        assertEquals(4L, session.createQuery(User.class).getCount());
                        session.commitTransaction();
                    }
                    assertEquals(4L, session.createQuery(User.class).getCount());
                    assertFalse(session.getTransaction().isRoot());
                    session.commitTransaction();
                }
                assertEquals(4L, session.createQuery(User.class).getCount());
                assertTrue(session.getTransaction().isRoot());
                session.rollbackTransaction();
            }
            assertEquals(1L, session.createQuery(User.class).getCount());
            session.rollback();
            assertEquals(0L, session.createQuery(User.class).getCount());
            assertNull(session.getTransaction());
        }
    }

    // -----------------------------------------------------

    /** Create new Handler */
    private OrmHandler getHandler() {
        OrmHandler result = new OrmHandler();
        MetaParams params = new MetaParams();
        params.set(MetaParams.AUTO_CLOSING_DEFAULT_SESSION, false); // For in-memory database only
        result.config(params);
        result.loadDatabase(Database.class);
        return result;
    }
}
