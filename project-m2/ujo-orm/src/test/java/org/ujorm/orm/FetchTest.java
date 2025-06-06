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
package org.ujorm.orm;

import java.awt.Color;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.ujorm.Key;
import org.ujorm.criterion.*;
import org.ujorm.extensions.types.UnsignedShort;
import org.ujorm.orm.bo.*;
import org.ujorm.orm.metaModel.MetaParams;
import static org.ujorm.criterion.Operator.*;

/**
 * The tests of the SQL LIMIT & OFFSET.
 * @author Pavel Ponec
 */
public class FetchTest extends org.junit.jupiter.api.Assertions {

    /** Main Handler */
    private static OrmHandler handler;


    @SuppressWarnings("deprecation")
    @Test
    public void testFetch() {
        createOrders(1);

        Session session = getHandler().createSession();
        Criterion<XItem> crit = Criterion.where(XItem.ID, GE, 0L);
        Query<XItem> query = session.createQuery(crit).setLimit(1);

        // ------ LAZY LOADING TEST ------

        Key<XItem, ?> fetchColumn = null;
        int count = 0;
        for (XItem item : query) {
            Object orderFk = item.readValue(XItem.ORDER);
            assertInstanceOf(ForeignKey.class, orderFk, "Order must be a foreign key");
            assertInstanceOf(XOrder.class, item.get(XItem.ORDER));
            assertNotNull(item.get(XItem.ID));
            assertNotNull(item.get(XItem.NOTE));
            assertNotNull(item.get(XItem.$ORDER_NOTE));
            //
            assertNotNull(item.readSession());
            ++count;
        }
        assertEquals(1, count, "The one loop at least");

        // ------ ONE COLUMN TEST ------

        fetchColumn = XItem.NOTE;
        query.setColumn(fetchColumn);
        for (XItem item : query) {
            Object orderFk = item.readValue(XItem.ORDER);
            assertNull(orderFk, "Order must be null");
            assertNull(item.get(XItem.ORDER));
            assertNull(item.get(XItem.ID));
            assertNotNull(item.get(fetchColumn));
            assertNull(item.get(XItem.$ORDER_NOTE));
            //
            assertNotNull(item.readSession());
        }

        // ------ TWO COLUMNS TEST ------

        fetchColumn = XItem.NOTE;
        query.setColumns(true, fetchColumn);
        for (XItem item : query) {
            Object orderFk = item.readValue(XItem.ORDER);
            assertNull(orderFk, "Order must be null");
            assertNull(item.get(XItem.ORDER));
            assertNotNull(item.get(XItem.ID));
            assertNotNull(item.get(fetchColumn));
            assertNull(item.get(XItem.$ORDER_NOTE));
            //
            assertNotNull(item.readSession());
        }

        // ------ FETCH THE COLUMN 'XOrder.NOTE' ------

        fetchColumn = XItem.ORDER.add(XOrder.NOTE);
        query.setColumn(fetchColumn);
        for (XItem item : query) {
            Object orderFk = item.readValue(XItem.ORDER);
            assertInstanceOf(XOrder.class, orderFk, "Order instance");
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
        }

        // ------ FETCH THE COLUMN 'XOrder.NOTE' + 'ID' ------

        fetchColumn = XItem.ORDER.add(XOrder.NOTE);
        query.setColumns(true, fetchColumn);
        for (XItem item : query) {
            Object orderFk = item.readValue(XItem.ORDER);
            assertInstanceOf(XOrder.class, orderFk, "Order instance");
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
        }

        // ------ FETCH THE ALL COLUMNS OF 'XOrder' ------

        fetchColumn = XItem.ORDER;
        query.setColumns(true, fetchColumn);
        query.list();
        query.getColumns();
        for (XItem item : query) {
            Object objectFk = item.readValue(XItem.ORDER);
            assertInstanceOf(XOrder.class, objectFk, "Order instance");
            XOrder order = item.get(XItem.ORDER);
            objectFk = order.readValue(XOrder.CUSTOMER);
            assertInstanceOf(ForeignKey.class, objectFk, "Order instance");
            assertNotNull(item.get(XItem.ORDER.add(XOrder.CUSTOMER)));
            assertNotNull(item.get(XItem.ID));
            assertNotNull(item.get(fetchColumn));
            assertNotNull(item.get(XItem.ORDER.add(XOrder.ID)));
            assertNotNull(item.get(XItem.ORDER.add(XOrder.CREATED)));
            assertNotNull(item.get(XItem.ORDER.add(XOrder.NOTE)));
            assertNotNull(item.get(XItem.$ORDER_DATE));
            //
            assertNotNull(item.readSession());
            assertNotNull(item.getOrder().readSession());
            assertEquals(0, item.readChangedProperties(false).length);
            assertEquals(0, item.getOrder().readChangedProperties(false).length);
            break; // The one loop is sufficient.
        }

        // ------ FETCH THE ONE COLUMN 'XCustomer.FIRSTNAME' ------

        fetchColumn = XItem.ORDER.add(XOrder.CUSTOMER).add(XCustomer.FIRSTNAME);
        query.setColumn(fetchColumn);
        for (XItem item : query) {
            Object objectFk = item.readValue(XItem.ORDER);
            assertInstanceOf(XOrder.class, objectFk, "Order instance");
            objectFk = item.getOrder().readValue(XOrder.CUSTOMER);
            assertInstanceOf(XCustomer.class, objectFk, "Order instance");
            assertNotNull(item.get(XItem.ORDER.add(XOrder.CUSTOMER)));
            assertNull(item.get(XItem.ID));
            assertNotNull(item.get(fetchColumn));
            assertNull(item.get(XItem.ORDER.add(XOrder.CUSTOMER).add(XCustomer.ID)));
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

        // ------ FETCH THE ALL COLUMNS OF THE 'XCustomer' ------

        fetchColumn = XItem.ORDER.add(XOrder.CUSTOMER);
        query.setColumn(fetchColumn);
        for (XItem item : query) {
            Object objectFk = item.readValue(XItem.ORDER);
            assertInstanceOf(XOrder.class, objectFk, "Order instance");
            objectFk = item.getOrder().readValue(XOrder.CUSTOMER);
            assertInstanceOf(XCustomer.class, objectFk, "Order instance");
            assertNotNull(item.get(XItem.ORDER.add(XOrder.CUSTOMER)));
            assertNull(item.get(XItem.ID));
            assertNotNull(item.get(fetchColumn));
            assertNotNull(item.get(XItem.ORDER.add(XOrder.CUSTOMER).add(XCustomer.ID)));
            assertNotNull(item.get(XItem.ORDER.add(XOrder.CUSTOMER).add(XCustomer.LASTNAME)));
            assertNotNull(item.get(XItem.ORDER.add(XOrder.CUSTOMER).add(XCustomer.FIRSTNAME)));
            //
            assertNotNull(item.readSession());
            assertNotNull(item.getOrder().readSession());
            assertNotNull(item.getOrder().getCustomer().readSession());
            assertEquals(0, item.readChangedProperties(false).length);
            assertEquals(0, item.getOrder().readChangedProperties(false).length);
            assertEquals(0, item.getOrder().getCustomer().readChangedProperties(false).length);
            break; // The one loop is sufficient.
        }

        // ------ A ValueWrapper test ------

        XCustomer cust = session.createQuery(XCustomer.BENEFIT.whereEq(XCustomer.BENEFIT.getDefault())).setLimit(1).uniqueResult();
        assertNotNull(cust);
        assertEquals(XCustomer.BENEFIT.getDefault(), cust.getBenefit());


        // ------ CLOSE ------
        session.close();
    }

    @SuppressWarnings("deprecation")
    public void TODO_testFetch_extended() {
        createOrders(1);

        Session session = getHandler().createSession();
        Criterion<XItem> crit = Criterion.where(XItem.ID, GE, 0L);
        Query<XItem> query = session.createQuery(crit);

        // ------ FETCH THE COLUMN 'XOrder.NOTE' + 'ID' ------

        Key<XItem, ?> fetchColumn = XItem.ORDER.add(XOrder.NOTE);
        query.addColumn(fetchColumn);
        for (XItem item : query) {
            Object orderFk = item.readValue(XItem.ORDER);
            assertInstanceOf(XOrder.class, orderFk, "Order instance");
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

        // ------ CLOSE ------
        session.close();
    }

    // ---------- TOOLS -----------------------

    protected OrmHandler getHandler() {
        if (handler == null) {
            MetaParams params = new MetaParams();
            params.set(MetaParams.AUTO_CLOSING_DEFAULT_SESSION, false); // For in-memory database only
            handler = new OrmHandler();
            handler.config(params);

            handler.loadDatabase(XDatabase.class);
        }
        return handler;
    }

    @SuppressWarnings("unchecked")
    protected void deleteAllOrders(Session session) {

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

    protected void createOrder(String name, Session session) {

        XCustomer customer = new XCustomer();
        XCustomer.FIRSTNAME.setValue(customer, "Lucy");
        XCustomer.LASTNAME.setValue(customer, "Smith" + name);
        XCustomer.CREATED.setValue(customer, new Date());
        XCustomer.PIN.setValue(customer, UnsignedShort.of(65_000));
        XCustomer.BENEFIT.setValue(customer, XCustomer.BENEFIT.getDefault());

        XOrder order = new XOrder();
        XOrder.CREATED.setValue(order, new Date());
        XOrder.NOTE.setValue(order, "Note_" + name);
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

        session.insert(customer);
        session.insert(order);
        session.insert(item1);
        session.insert(item2);
        session.insert(item3);

        session.commit();
    }

    /** Remove all orders and create orders by parameter. */
    protected void createOrders(long count) {
        Session session = getHandler().createSession();
        deleteAllOrders(session);
        for (int i = 0; i < count; i++) {
            createOrder("" + i, session);
        }
    }
}
