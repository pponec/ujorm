/*
 *  Copyright 2009 Paul Ponec
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

package org.ujoframework.orm.sample;

import java.util.Date;
import java.util.logging.*;
import org.ujoframework.Ujo;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.orm.*;
import org.ujoframework.orm.metaModel.MetaColumn;
import org.ujoframework.criterion.*;
import org.ujoframework.orm.metaModel.MetaParams;

/**
 * The tutorial in the class for the Ujorm<br>
 * ---------------------------------------<br>
 * Learn the basic skills in 15 minutes.
 * The next several methods demonstrates the use of statements:
 *     CREATE TABLE, INSERT, SELECT, UPDATE or DELETE.
 *
 * Copyright 2009, Pavel Ponec
 */
public class SampleORM {

    /** Before the first use load a meta-model.
     * Database tables will be created in the first time.
     */
    public void loadMetaModel(boolean createDb) {

        Logger.getLogger(Ujo.class.getPackage().getName()).setLevel(Level.ALL);

        boolean yesIWantChangeDefaultParameters = true;
        if (yesIWantChangeDefaultParameters) {
            MetaParams params = new MetaParams();
            MetaParams.TABLE_ALIAS_SUFFIX.setValue(params, "_alias");
            MetaParams.SEQUENCE_CACHE.setValue(params, 1);
            OrmHandler.getInstance().config(params);
        }

        boolean yesIWantLoadExternalConfig = false;
        if (yesIWantLoadExternalConfig) {
            java.net.URL config = getClass().getResource("/org/ujoframework/orm/sample/config.xml");
            OrmHandler.getInstance().config(config, true);
        }

        OrmHandler.getInstance().loadDatabase(Database.class);
    }

    /** Insert one Order and two Items into database. */
    public void useInsert() {

        Order order = new Order();
        order.setDate(new Date());
        order.setDescr("John's order");

        Item item1 = new Item();
        item1.setOrder(order);
        item1.setDescr("Yellow table");

        Item item2 = new Item();
        item2.setOrder(order);
        item2.setDescr("Green window");

        System.out.println("order: " + order);
        System.out.println("item1: " + item1);
        System.out.println("item2: " + item2);

        Session session = OrmHandler.getInstance().getSession();
        session.save(order);
        session.save(item1);
        session.save(item2);

        if (true) {
           session.commit();
        } else {
           session.rollback();
        }
    }

    /** Now, how to select Orders from the database by the Criterion? */
    public void useSelectOrders() {

        Criterion<Order> cn1 = Criterion.newInstance(Order.DESCR, "John's order");
        Criterion<Order> cn2 = Criterion.newInstance(Order.CREATED, Operator.LE, new Date());
        Criterion<Order> crit = cn1.and(cn2);

        Session session = OrmHandler.getInstance().getSession();
        UjoIterator<Order> orders = session.createQuery(crit).iterate();
        System.out.println("ORDER COUNT: " + orders.count());

        for (Order order : orders) {
            String descr = order.getDescr();
            System.out.println("ORDER ROW: " + order + " // descr: " + descr);
        }
    }

    /** Sort orders by DESCR and CREATED (descending). */
    public void useSortOrders() {

        Session session = OrmHandler.getInstance().getSession();
        Query<Order> query = session.createQuery(Order.class);
        query.getOrder().add(Order.DESCR);
        query.getOrder().add(Order.CREATED.descending());

        UjoIterator<Order> orders = query.iterate();
        System.out.println("VIEW-ORDER COUNT: " + orders.count());
    }

    /** Use a 'native query' where the query is created
     * by a special entity signed by the @View annotation.
     */
    public void useSelectViewOrders() {

        Criterion<ViewOrder> crit = Criterion.newInstance(ViewOrder.ID, Operator.GE, 0L);
        Session session = OrmHandler.getInstance().getSession();
        UjoIterator<ViewOrder> orders = session.createQuery(crit).iterate();
        System.out.println("VIEW-ORDER COUNT: " + orders.count());

        for (ViewOrder order : orders) {
            System.out.println("ORDER ROW: " + order);
        }
    }

    /** Select all items with a description with the 'table' insensitive text. */
    public void useSelectItems_1() {
        Session session = OrmHandler.getInstance().getSession();

        Criterion<Item> crit = Criterion.newInstance(Item.DESCR, Operator.CONTAINS_CASE_INSENSITIVE, "table");
        UjoIterator<Item> items = session.createQuery(crit).setOrder(Item.ID.descending()).iterate();

        for (Item item : items) {
            Order order = item.getOrder();
            System.out.println("ITEM ROW: " + item + " ORDER: " + order);
        }
    }

    /** Select one Order by ID and print its Items by a criterion */
    public void useSelectItems_2() {
        Session session = OrmHandler.getInstance().getSession();

        Order orderValue = session.load(Order.class, 1L);
        Criterion<Item> crit = Criterion.newInstance(Item.ORDER, orderValue);
        UjoIterator<Item> items = session.createQuery(crit).iterate();

        for (Item item : items) {
            Order order2 = item.getOrder();
            System.out.println("ITEM ROW: " + item + " ORDER: " + order2);
        }
    }

    /** Select an Order by ID and print its Items 
     * by a 'one to many' relation property
     */
    public void useSelectItems_3() {
        Session session = OrmHandler.getInstance().getSession();
        Order order = session.load(Order.class, 1L);

        for (Item item : order.getItems()) {
            Order order2 = item.getOrder();
            System.out.println("ITEM ROW: " + item + " ORDER: " + order2);
        }
    }

    /** Select items by a related order date property Item._ORDER_DATE.
     * It is a sample of multi-table query.
     */
    public void useSelectItems_4() {
        Criterion<Item> crit = Criterion.newInstance(Item._ORDER_DATE, Operator.LE, new Date());
        Session session = OrmHandler.getInstance().getSession();
        UjoIterator<Item> items = session.createQuery(crit).iterate();

        for (Item item : items) {
            System.out.println("Item: " + item);
        }
    }

    /** How to count items ? */
    public void useSelectCount() {
        Session session = OrmHandler.getInstance().getSession();
        Criterion<Item> crit = Criterion.newInstance(Item.DESCR, Operator.CONTAINS_CASE_INSENSITIVE, "table");
        Query<Item> query = session.createQuery(crit);

        long count = query.getCount();
        System.out.println("Count of the order items: " + count);
    }

    /** How to skip items? */
    public void useIteratorSkip() {
        Session session = OrmHandler.getInstance().getSession();
        Criterion<Item> crit = Criterion.newInstance(Item.DESCR, Operator.NOT_EQ, "XXXXX");
        UjoIterator<Item> iterator = session.createQuery(crit).iterate();
        
        boolean skip = iterator.skip(1);
        if (iterator.hasNext()) {
            Item item = iterator.next();
            System.out.println("Item : " + item);
        }

        skip = iterator.skip(1);
        boolean isNext =  iterator.hasNext();
        System.out.println("Next: " + isNext);
    }

    /** Sample for 'one to many' relation.
     * 	Note that it is possible to use a Database configuration object too.
     */
    public void useRelation() {
        Session session = OrmHandler.getInstance().getSession();
        Database db = session.getDatabase(Database.class);

        UjoIterator<Order> orders = db.get(Database.ORDERS);
        for (Order order : orders) {
            String descr = order.getDescr();
            System.out.println("Order: " + order + " // descr: " + descr);

            for (Item item : order.getItems()) {
                Long itemId = item.getId();
                String itemDescr = item.getDescr();
                System.out.println(" Item id: " + itemId + " descr: " + itemDescr);
            }
        }
    }

    /** Using the UPDATE */
    public void useUpdate() {
        Session session = OrmHandler.getInstance().getSession();
        Order order = session.load(Order.class, 1L);
        order.setDate(new Date());

        session.update(order);
        session.commit();
    }

    /** How to delete the one loaded object? */
    public void useDelete_1() {
        Session session = OrmHandler.getInstance().getSession();
        Item item = session.createQuery(Item.class).iterate().toList().get(0);

        session.delete(item);
        session.commit();
        System.out.println("There is DELETED object: " + item);
    }

    /** How to use a batch DELETE? */
    public void useDelete_2() {
        Session session = OrmHandler.getInstance().getSession();
        Criterion<Item> crit = Criterion.newInstance(Item.ID, 1L);
        int count = session.delete(crit);
        session.commit();
        System.out.println("There are DELETED rows: " + count);
    }

    /** Print some meta-data of the propery Order.DESCR. */
    public void useMetadata() {
        MetaColumn c = (MetaColumn) OrmHandler.getInstance().findColumnModel(Order.DESCR);

        StringBuilder msg = new StringBuilder()
            .append("** METADATA OF COLUMN: " + Order.DESCR)
            .append("\n\t Length : " + c.getMaxLength())
            .append("\n\t NotNull: " + c.isMandatory())
            .append("\n\t PrimKey: " + c.isPrimaryKey())
            .append("\n\t DB name: " + c.getFullName())
            .append("\n\t Dialect: " + c.getDialectClass().getSimpleName())
            ;
        System.out.println(msg);
    }

    /** Close Ujorm session to clear a session cache include 
     * a database connection(s) 
     */
    public void useCloseSession() {
        OrmHandler.getInstance().getSession().close();
    }

    /** Run the tutorial */
    public static void main(String[] args) {
        SampleORM sample = new SampleORM();
        
        try {
            sample.loadMetaModel(true);
            sample.useInsert();
            sample.useSelectOrders();
            sample.useSortOrders();
            sample.useSelectViewOrders();
            sample.useSelectItems_1();
            sample.useSelectItems_2();
            sample.useSelectItems_3();
            sample.useSelectItems_4();
            sample.useSelectCount();
            sample.useIteratorSkip();
            sample.useRelation();
            sample.useUpdate();
            sample.useDelete_1();
            sample.useDelete_2();
            sample.useMetadata();

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            sample.useCloseSession();
        }
    }
}
