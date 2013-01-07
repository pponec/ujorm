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
import java.util.List;
import junit.framework.TestCase;
import org.ujorm.Key;
import org.ujorm.criterion.*;
import org.ujorm.orm.bo.*;
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
        for (XItem item : query) {
            Object orderFk = item.readValue(XItem.ORDER);
            assertTrue("Order must be a foreign key", orderFk instanceof ForeignKey);
            assertTrue(item.get(XItem.ORDER) instanceof XOrder);
            assertNotNull(item.get(XItem.ID));
            assertNotNull(item.get(XItem.NOTE));
            assertNotNull(item.get(XItem.$ORDER_NOTE));
            //
            assertNotNull(item.readSession());
            ++count;
        }
        assertTrue("The one loop at least", count>0);

        // ------ TEST OF BASE ------

        fetchColumn = XItem.NOTE;
        query.setColumn(fetchColumn);
        for (XItem item : query) {
            Object orderFk = item.readValue(XItem.ORDER);
            assertTrue("Order must be null", orderFk == null);
            assertTrue(item.get(XItem.ORDER) == null);
            assertNull(item.get(XItem.ID));
            assertNotNull(item.get(fetchColumn));
            assertNull(item.get(XItem.$ORDER_NOTE));
            //
            assertNotNull(item.readSession());
            break; // The one loop is sufficient.
        }

        // ------ TEST OF BASE ------

        fetchColumn = XItem.NOTE;
        query.setColumns(true, fetchColumn);
        for (XItem item : query) {
            Object orderFk = item.readValue(XItem.ORDER);
            assertTrue("Order must be null", orderFk == null);
            assertTrue(item.get(XItem.ORDER) == null);
            assertNotNull(item.get(XItem.ID));
            assertNotNull(item.get(fetchColumn));
            assertNull(item.get(XItem.$ORDER_NOTE));
            //
            assertNotNull(item.readSession());
            break; // The one loop is sufficient.
        }

        // ------ TEST OF RELATION 'ORDER' ------

        fetchColumn = XItem.ORDER.add(XOrder.NOTE);
        query.setColumn(fetchColumn);
        for (XItem item : query) {
            Object orderFk = item.readValue(XItem.ORDER);
            assertTrue("Order instance", orderFk instanceof XOrder);
            assertNull(item.get(XItem.ORDER.add(XOrder.CUSTOMER)));
            assertNull(item.get(XItem.ID));
            assertNotNull(item.get(fetchColumn));
            assertNull(item.get(XItem.ORDER.add(XOrder.ID)));
            assertNull(item.get(XItem.$ORDER_DATE));
            //
            assertNotNull(item.readSession());
            assertNotNull(item.getOrder().readSession());
            assertEquals(0, item.readChangedProperties(false).length);
            assertEquals(0, item.getOrder().readChangedProperties(false).length);
            break; // The one loop is sufficient.
        }

        // ------ TEST OF RELATION 'ORDER' ------

        fetchColumn = XItem.ORDER.add(XOrder.NOTE);
        query.setColumns(true, fetchColumn);
        for (XItem item : query) {
            Object orderFk = item.readValue(XItem.ORDER);
            assertTrue("Order instance", orderFk instanceof XOrder);
            assertNull(item.get(XItem.ORDER.add(XOrder.CUSTOMER)));
            assertNotNull(item.get(XItem.ID));
            assertNotNull(item.get(fetchColumn));
            assertNull(item.get(XItem.ORDER.add(XOrder.ID))); // TODO: fix it or documented it ?
            assertNull(item.get(XItem.$ORDER_DATE));
            //
            assertNotNull(item.readSession());
            assertNotNull(item.getOrder().readSession());
            assertEquals(0, item.readChangedProperties(false).length);
            assertEquals(0, item.getOrder().readChangedProperties(false).length);
            break; // The one loop is sufficient.
        }

        // ------ TEST OF RELATION 'CUSTOMER' ------

        if (!true) {
            // TODO: fix the last test !!!
            session.close();
            return;
        }

        fetchColumn = XItem.ORDER.add(XOrder.CUSTOMER).add(XCustomer.FIRSTNAME);
        query.setColumn(fetchColumn);
        List<XItem> items = query.list();
        for (XItem item : query) {
            Object objectFk = item.readValue(XItem.ORDER);
            assertTrue("Order instance", objectFk instanceof XOrder);
            objectFk = item.getOrder().readValue(XOrder.CUSTOMER);
            assertTrue("Order instance", objectFk instanceof XCustomer);
            assertNotNull(item.get(XItem.ORDER.add(XOrder.CUSTOMER)));
            assertNull(item.get(XItem.ID));
            assertNotNull(item.get(fetchColumn));
            assertNull(item.get(XItem.ORDER.add(XOrder.CUSTOMER).add(XCustomer.ID))); // FIX IT ?
            assertNull(item.get(XItem.ORDER.add(XOrder.CUSTOMER).add(XCustomer.LASTNAME)));
            //
            assertNotNull(item.readSession());
            assertNotNull(item.getOrder().readSession());
            assertNotNull(item.getOrder().getCustomer().readSession());
            assertEquals(0, item.readChangedProperties(false).length);
            assertEquals(0, item.getOrder().readChangedProperties(false).length);
            assertEquals(0, item.getOrder().getCustomer().readChangedProperties(false).length);
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
