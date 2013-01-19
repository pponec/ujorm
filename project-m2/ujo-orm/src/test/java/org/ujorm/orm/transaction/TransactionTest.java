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
package org.ujorm.orm.transaction;

import junit.framework.TestCase;
import org.ujorm.orm.*;
import org.ujorm.orm.inheritance.sample.Database;
import org.ujorm.orm.inheritance.sample.bo.User;
import org.ujorm.orm.inheritance.sample.bo.Customer;
import org.ujorm.orm.inheritance.sample.bo.Customer.*;

/**
 * The tests of the SQL LIMIT & OFFSET.
 * @author Pavel Ponec
 */
public class TransactionTest extends TestCase {

    public TransactionTest(String testName) {
        super(testName);
    }

    private static Class suite() {
        return TransactionTest.class;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ---------- TESTS -----------------------

    public void testRollbackTranaction() {
        final Session session = new OrmHandler(Database.class).createSession();
        session.delete(Customer.user.forAll()); // TODO ?
        session.delete(User.id.forAll());
        session.commit();
        //
        session.save(new User());
        assertEquals(1L, session.createQuery(User.class).getCount());
        assertEquals(null, session.getTransaction());
        { // 1st Level
            session.beginTransaction();
            session.save(new User());
            assertEquals(2L, session.createQuery(User.class).getCount());
            assertEquals(true, session.getTransaction().isRoot());
            { // 2md Level
                session.beginTransaction();
                session.save(new User());
                assertEquals(3L, session.createQuery(User.class).getCount());
                { // 3rd Level
                    session.beginTransaction();
                    session.save(new User());
                    assertEquals(4L, session.createQuery(User.class).getCount());
                    session.rollbackTransaction();
                }
                assertEquals(3L, session.createQuery(User.class).getCount());
                assertEquals(false, session.getTransaction().isRoot());
                session.rollbackTransaction();
            }
            assertEquals(2L, session.createQuery(User.class).getCount());
            assertEquals(true, session.getTransaction().isRoot());
            session.rollbackTransaction();
        }
        assertEquals(1L, session.createQuery(User.class).getCount());
        assertEquals(null, session.getTransaction());
        session.rollback();
        assertEquals(0L, session.createQuery(User.class).getCount());
        assertEquals(null, session.getTransaction());
    }

    // ---------- TESTS -----------------------

    public void testCommitTranaction() {
        final Session session = new OrmHandler(Database.class).createSession();
        session.delete(Customer.user.forAll()); // TODO ?
        session.delete(User.id.forAll());
        session.commit();
        //
        session.save(new User());
        assertEquals(1L, session.createQuery(User.class).getCount());
        assertEquals(null, session.getTransaction());
        { // 1st Level
            session.beginTransaction();
            session.save(new User());
            assertEquals(2L, session.createQuery(User.class).getCount());
            assertEquals(true, session.getTransaction().isRoot());
            { // 2md Level
                session.beginTransaction();
                session.save(new User());
                assertEquals(3L, session.createQuery(User.class).getCount());
                { // 3rd Level
                    session.beginTransaction();
                    session.save(new User());
                    assertEquals(4L, session.createQuery(User.class).getCount());
                    session.commitTransaction();
                }
                assertEquals(4L, session.createQuery(User.class).getCount());
                assertEquals(false, session.getTransaction().isRoot());
                session.commitTransaction();
            }
            assertEquals(4L, session.createQuery(User.class).getCount());
            assertEquals(true, session.getTransaction().isRoot());
            session.commitTransaction();
        }
        assertEquals(4L, session.createQuery(User.class).getCount());
        assertEquals(null, session.getTransaction());
        session.commit();
        assertEquals(4L, session.createQuery(User.class).getCount());
    }

    // ---------- TESTS -----------------------

    public void testIComplexTranaction() {
        final Session session = new OrmHandler(Database.class).createSession();
        session.delete(Customer.user.forAll()); // TODO ?
        session.delete(User.id.forAll());
        session.commit();
        //
        session.save(new User());
        assertEquals(1L, session.createQuery(User.class).getCount());
        assertEquals(null, session.getTransaction());
        { // 1st Level
            session.beginTransaction();
            session.save(new User());
            assertEquals(2L, session.createQuery(User.class).getCount());
            assertEquals(true, session.getTransaction().isRoot());
            { // 2md Level
                session.beginTransaction();
                session.save(new User());
                assertEquals(3L, session.createQuery(User.class).getCount());
                { // 3rd Level
                    session.beginTransaction();
                    session.save(new User());
                    assertEquals(4L, session.createQuery(User.class).getCount());
                    session.commitTransaction();
                }
                assertEquals(4L, session.createQuery(User.class).getCount());
                assertEquals(false, session.getTransaction().isRoot());
                session.commitTransaction();
            }
            assertEquals(4L, session.createQuery(User.class).getCount());
            assertEquals(true, session.getTransaction().isRoot());
            session.rollbackTransaction();
        }
        assertEquals(1L, session.createQuery(User.class).getCount());
        session.rollback();
        assertEquals(0L, session.createQuery(User.class).getCount());
        assertEquals(null, session.getTransaction());
    }

    // -----------------------------------------------------

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
}
