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
package org.ujorm.orm;

import java.awt.Color;
import java.util.Date;
import junit.framework.TestCase;
import org.ujorm.Key;
import org.ujorm.criterion.*;
import org.ujorm.orm.bo.*;
import org.ujorm.orm_tutorial.sample.Customer;
import static org.ujorm.criterion.Operator.*;

/**
 * The tests of the SQL LIMIT & OFFSET.
 * @author Pavel Ponec
 */
public class FetchTest extends TestCase {

    /** Main Handler */
    private static OrmHandler handler;

    public FetchTest(String testName) {
        super(testName);
    }

    private static Class suite() {
        return FetchTest.class;
    }

    // ---------- TESTS -----------------------

    @SuppressWarnings("deprecation")
    public void testFetch_1() {
        createOrders(1);

        Session session = getHandler().getSession();
        Criterion<XItem> crit = Criterion.where(XItem.ID, GE, 0L);
        Query<XItem> query = session.createQuery(crit);

        // ------ TEST OF BASE ------

        Key<XItem,?> fetchColumn = null;
        int count = 0;
        for (XItem xItem : query) {
            Object orderFk = xItem.readValue(XItem.ORDER);
            assertTrue("Order must be a foreign key", orderFk instanceof ForeignKey);
            assertTrue(xItem.get(XItem.ORDER) instanceof XOrder);
            assertNotNull(xItem.get(XItem.ID));
            assertNotNull(xItem.get(XItem.NOTE));
            assertNotNull(xItem.get(XItem.$ORDER_NOTE));
            //
            assertNotNull(xItem.readSession());
            ++count;
        }
        assertTrue("The one loop at least", count>0);

        // ------ TEST OF BASE ------

        fetchColumn = XItem.NOTE;
        query.setColumn(fetchColumn);
        for (XItem xItem : query) {
            Object orderFk = xItem.readValue(XItem.ORDER);
            assertTrue("Order must be null", orderFk == null);
            assertTrue(xItem.get(XItem.ORDER) == null);
            assertNull(xItem.get(XItem.ID));
            assertNotNull(xItem.get(fetchColumn));
            assertNull(xItem.get(XItem.$ORDER_NOTE));
            //
            assertNotNull(xItem.readSession());
            break; // The one loop is sufficient.
        }

        // ------ TEST OF BASE ------

        fetchColumn = XItem.NOTE;
        query.setColumns(true, fetchColumn);
        for (XItem xItem : query) {
            Object orderFk = xItem.readValue(XItem.ORDER);
            assertTrue("Order must be null", orderFk == null);
            assertTrue(xItem.get(XItem.ORDER) == null);
            assertNotNull(xItem.get(XItem.ID));
            assertNotNull(xItem.get(fetchColumn));
            assertNull(xItem.get(XItem.$ORDER_NOTE));
            //
            assertNotNull(xItem.readSession());
            break; // The one loop is sufficient.
        }

        // ------ TEST OF RELATION 'ORDER' ------

        fetchColumn = XItem.ORDER.add(XOrder.NOTE);
        query.setColumn(fetchColumn);
        for (XItem xItem : query) {
            Object orderFk = xItem.readValue(XItem.ORDER);
            assertTrue("Order instance", orderFk instanceof XOrder);
            assertNull(xItem.get(XItem.ORDER.add(XOrder.CUSTOMER)));
            assertNull(xItem.get(XItem.ID));
            assertNotNull(xItem.get(fetchColumn));
            assertNull(xItem.get(XItem.ORDER.add(XOrder.ID)));
            assertNull(xItem.get(XItem.$ORDER_DATE));
            //
            assertNotNull(xItem.readSession());
            assertNotNull(xItem.getOrder().readSession());
            assertEquals(0, xItem.readChangedProperties(false).length);
            assertEquals(0, xItem.getOrder().readChangedProperties(false).length);
            break; // The one loop is sufficient.
        }

        // ------ TEST OF RELATION 'ORDER' ------

        fetchColumn = XItem.ORDER.add(XOrder.NOTE);
        query.setColumns(true, fetchColumn);
        for (XItem xItem : query) {
            Object orderFk = xItem.readValue(XItem.ORDER);
            assertTrue("Order instance", orderFk instanceof XOrder);
            assertNull(xItem.get(XItem.ORDER.add(XOrder.CUSTOMER)));
            assertNotNull(xItem.get(XItem.ID));
            assertNotNull(xItem.get(fetchColumn));
            assertNull(xItem.get(XItem.ORDER.add(XOrder.ID))); // TODO: fix it or documented it ?
            assertNull(xItem.get(XItem.$ORDER_DATE));
            //
            assertNotNull(xItem.readSession());
            assertNotNull(xItem.getOrder().readSession());
            assertEquals(0, xItem.readChangedProperties(false).length);
            assertEquals(0, xItem.getOrder().readChangedProperties(false).length);
            break; // The one loop is sufficient.
        }


        // ------ TEST OF RELATION 'CUSTOMER' ------

        if (true) {
            // TODO: fix the last test !!!
            session.close();
            return;
        }

        fetchColumn = XItem.ORDER.add(XOrder.CUSTOMER).add(XCustomer.FIRSTNAME);
        query.setColumn(fetchColumn);
        for (XItem xItem : query) {
            Object orderFk = xItem.readValue(XItem.ORDER);
            assertTrue("Order instance", orderFk instanceof XOrder);
            orderFk = xItem.getOrder().readValue(XOrder.CUSTOMER);
            assertTrue("Order instance", orderFk instanceof Customer);
            assertNotNull(xItem.get(XItem.ORDER.add(XOrder.CUSTOMER)));
            assertNull(xItem.get(XItem.ID));
            assertNotNull(xItem.get(fetchColumn));
            assertNull(xItem.get(XItem.ORDER.add(XOrder.CUSTOMER).add(XCustomer.ID))); // FIX IT ?
            assertNull(xItem.get(XItem.ORDER.add(XOrder.CUSTOMER).add(XCustomer.LASTNAME)));
            //
            assertNotNull(xItem.readSession());
            assertNotNull(xItem.getOrder().readSession());
            assertNotNull(xItem.getOrder().getCustomer().readSession());
            assertEquals(0, xItem.readChangedProperties(false).length);
            assertEquals(0, xItem.getOrder().readChangedProperties(false).length);
            assertEquals(0, xItem.getOrder().getCustomer().readChangedProperties(false).length);
            break; // The one loop is sufficient.
        }


        // ------ CLOSE ------
        session.close();

    }

    // ---------- TOOLS -----------------------

    protected OrmHandler getHandler() {
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

        XCustomer customer = new XCustomer();
        XCustomer.FIRSTNAME.setValue(customer, "Lucy");
        XCustomer.LASTNAME.setValue(customer, "Smith" + name);
        XCustomer.CREATED.setValue(customer, new Date());
        XCustomer.PIN.setValue(customer, 1000001);

        XOrder order = new XOrder();
        XOrder.CREATED.setValue(order, new Date());
        XOrder.NOTE.setValue(order, name);
        XOrder.COLOR.setValue(order, Color.BLUE);
        XOrder.CUSTOMER.setValue(order, customer);

        XItem item1 = new XItem();
        XItem.NOTE.setValue(item1, name + "-1");
        XItem.ORDER.setValue(item1, order);

        XItem item2 = new XItem();
        XItem.NOTE.setValue(item2, name + "-2");
        XItem.ORDER.setValue(item2, order);

        XItem item3 = new XItem();
        XItem.NOTE.setValue(item3, name + "-3");
        XItem.ORDER.setValue(item3, order);

        session.save(customer);
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
            createOrder("" + i);
        }
    }

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
}
