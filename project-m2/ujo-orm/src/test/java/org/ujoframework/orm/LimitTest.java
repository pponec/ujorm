/*
 *  Copyright 2009-2010 Pavel Ponec
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
package org.ujoframework.orm;

import java.util.Date;
import junit.framework.TestCase;
import org.ujoframework.criterion.*;
import org.ujoframework.orm.bo.*;
import static org.ujoframework.criterion.Operator.*;

/**
 * The tests of the SQL LIMIT & OFFSET.
 * @author Pavel Ponec
 */
public class LimitTest extends TestCase {

    static private OrmHandler handler;

    public LimitTest(String testName) {
        super(testName);
    }

    private static Class suite() {
        return LimitTest.class;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    // ---------- TOOLS -----------------------

    static protected OrmHandler getHandler() {
        if (handler == null) {
            handler = new OrmHandler();
            handler.loadDatabase(XDatabase.class);
        }
        return handler;
    }

    @SuppressWarnings("unchecked")
    protected void deleteAllOrders() {

        Session session = getHandler().getSession();
        Criterion crit;
        int count;
        //
        crit = Criterion.constant(XItem.ID, true);
        count = session.delete(crit);
        //
        crit = Criterion.constant(XOrder.ID, true);
        count = session.delete(crit);
        //
        crit = Criterion.constant(XCustomer.ID, true);
        count = session.delete(crit);
    }

    protected void createOrder(String name) {

        Session session = getHandler().getSession();

        XOrder order = new XOrder();
        XOrder.CREATED.setValue(order, new Date());
        XOrder.DESCR.setValue(order, name);

        XItem item1 = new XItem();
        XItem.NOTE.setValue(item1, name + "-1");
        XItem.ORDER.setValue(item1, order);

        XItem item2 = new XItem();
        XItem.NOTE.setValue(item2, name + "-2");
        XItem.ORDER.setValue(item2, order);

        XItem item3 = new XItem();
        XItem.NOTE.setValue(item3, name + "-3");
        XItem.ORDER.setValue(item3, order);

        session.save(order);
        session.save(item1);
        session.save(item2);
        session.save(item3);

        session.commit();
    }

    /** Remove all orders and create orders by parameter. */
    protected void createOrders(long count) {
        deleteAllOrders();
        for (int i = 0; i < count; i++) {
            createOrder(""+i);
        }
    }

    // ---------- TESTS -----------------------

    @SuppressWarnings("deprecation")
    public void testLimit_1() {
        long count = 10;
        int limit = 3;
        int offset = 6;
        long expected;

        createOrders(count);

        Session session = getHandler().getSession();
        Criterion<XOrder> crit = Criterion.where(XOrder.ID, GE, 0L);
        Query<XOrder> query = session.createQuery(crit).orderBy(XOrder.DESCR);

        // ------ BASE ------

        expected = count;
        long myCount = query.getLimitedCount();
        assertEquals(expected, myCount);
        //
        myCount = 0L;
        for (XOrder order : query) {
            order.getId();
            ++myCount;
        }
        assertEquals(expected, myCount);

        // ------ LIMIT ------

        expected = limit;
        query = session.createQuery(crit).setLimit(limit).orderBy(XOrder.DESCR);
        myCount = query.getLimitedCount();
        assertEquals(expected, myCount);
        //
        myCount = 0L;
        for (XOrder order : query) {
            order.getId();
            ++myCount;
        }
        assertEquals(expected, myCount);


        // ------ OFFSET ------

        expected = count - offset;
        query = session.createQuery(crit).setOffset(offset).orderBy(XOrder.DESCR);
        myCount = query.getLimitedCount();
        assertEquals(expected, myCount);
        //
        myCount = 0L;
        for (XOrder order : query) {
            order.getId();
            ++myCount;
        }
        assertEquals(expected, myCount);

        // ------ LIMIT + OFFSET (1) ------

        expected = limit;
        query = session.createQuery(crit).setLimit(limit).setOffset(offset).orderBy(XOrder.DESCR);
        myCount = query.getLimitedCount();
        assertEquals(expected, myCount);
        //
        myCount = 0L;
        for (XOrder order : query) {
            order.getId();
            ++myCount;
        }
        assertEquals(expected, myCount);


        // ------ LIMIT + OFFSET (2) ------

        limit = 10;
        expected = count-offset;
        query = session.createQuery(crit).setLimit(limit).setOffset(offset).orderBy(XOrder.DESCR);
        myCount = query.getLimitedCount();
        assertEquals(expected, myCount);
        //
        myCount = 0L;
        for (XOrder order : query) {
            order.getId();
            ++myCount;
        }
        assertEquals(expected, myCount);


        // ------ LIMIT + OFFSET (3) ------

        offset = 20;
        expected = 0;
        query = session.createQuery(crit).setLimit(limit).setOffset(offset).orderBy(XOrder.DESCR);
        myCount = query.getLimitedCount();
        assertEquals(expected, myCount);
        //
        myCount = 0L;
        for (XOrder order : query) {
            order.getId();
            ++myCount;
        }
        assertEquals(expected, myCount);


        // CLOSE
        session.close();

    }


    // -----------------------------------------------------

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
}
