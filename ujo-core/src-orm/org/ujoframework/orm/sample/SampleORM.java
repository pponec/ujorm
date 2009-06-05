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

import java.net.URL;
import java.util.Date;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.orm.Session;
import org.ujoframework.orm.OrmHandler;
import org.ujoframework.orm.Query;
import org.ujoframework.orm.metaModel.OrmColumn;
import org.ujoframework.criterion.Criterion;
import org.ujoframework.criterion.Operator;
import org.ujoframework.orm.metaModel.OrmParameters;

/**
 * SampleORM of usages: CREATE TABLE, INSERT, SELECT, UPDATE, DELETE.
 * @author Pavel Ponec
 */
public class SampleORM {

    /** Before the first use you must load a meta-model. */
    public void loadMetaModel() {


        boolean yesIWantChangeDefaultParameters = true;
        if (yesIWantChangeDefaultParameters) {
            OrmParameters params = new OrmParameters();
            OrmParameters.TABLE_ALIAS_SUFFIX.setValue(params, "_alias");
            OrmHandler.getInstance().config(params);
        }

        boolean yesIWantLoadExternalConfig = false;
        if (yesIWantLoadExternalConfig) {

            URL config = getClass().getResource("/org/ujoframework/orm/sample/config.xml");
            OrmHandler.getInstance().config(config, true);
        }

        if (true) {
            OrmHandler.getInstance().createDatabase(Database.class);
        } else {
            OrmHandler.getInstance().loadDatabase(Database.class);
        }
    }

    /** Create database and using INSERT */
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

    /** Using SELECT by QUERY */
    public void useSelectOrders() {

        Criterion<Order> crn1 = Criterion.newInstance(Order.DESCR, "John's order");
        Criterion<Order> crn2 = Criterion.newInstance(Order.DATE, Operator.LE, new Date());
        Criterion<Order> crit = crn1.and(crn2);

        Session session = OrmHandler.getInstance().getSession();
        UjoIterator<Order> orders = session.createQuery(crit).iterate();
        System.out.println("ORDER COUNT: " + orders.count());

        for (Order order : orders) {
            String descr = order.getDescr();
            System.out.println("ORDER ROW: " + order + " // descr: " + descr);
        }
    }

    /** Using SELECT by QUERY */
    public void useSortOrders() {

        Session session = OrmHandler.getInstance().getSession();
        Query<Order> query = session.createQuery(Order.class);
        query.getOrder().add(Order.DESCR);
        query.getOrder().add(Order.DATE.descending());

        UjoIterator<Order> orders = query.iterate();
        System.out.println("VIEW-ORDER COUNT: " + orders.count());
    }

    /** Using SELECT by VIEW QUERY */
    public void useSelectViewOrders() {

        Criterion<ViewOrder> crit = Criterion.newInstance(ViewOrder.ID, Operator.GE, 0);
        Session session = OrmHandler.getInstance().getSession();
        UjoIterator<ViewOrder> orders = session.createQuery(crit).iterate();
        System.out.println("VIEW-ORDER COUNT: " + orders.count());

        for (ViewOrder order : orders) {
            System.out.println("ORDER ROW: " + order);
        }
    }


    /** Using SELECT by QUERY */
    public void useSelectItems_1() {
        Session session = OrmHandler.getInstance().getSession();

        Criterion<Item> crit = Criterion.newInstance(Item.DESCR, Operator.CONTAINS_CASE_INSENSITIVE, "table");
        UjoIterator<Item> items = session.createQuery(crit).setOrder(Item.ID.descending()).iterate();

        for (Item item : items) {
            Order order = item.getOrder();
            System.out.println("ITEM ROW: " + item + " ORDER: " + order);
        }
    }

    /** Using SELECT by QUERY */
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

    /** Using SELECT by QUERY */
    public void useSelectItems_3() {
        Session session = OrmHandler.getInstance().getSession();
        Order orderValue = session.load(Order.class, 1L);

        for (Item item : orderValue.getItems()) {
            Order order2 = item.getOrder();
            System.out.println("ITEM ROW: " + item + " ORDER: " + order2);
        }
    }

    /** Using SELECT by QUERY */
    public void useSelectItems_4() {
        Criterion<Item> crit = Criterion.newInstance(Item.ORDER_DATE, Operator.LE, new Date());
        Session session = OrmHandler.getInstance().getSession();
        UjoIterator<Item> items = session.createQuery(crit).iterate();

        for (Item item : items) {
            System.out.println("Item: " + item);
        }
    }


    /** Using SELECT by QUERY */
    public void useSelectCount() {
        Session session = OrmHandler.getInstance().getSession();
        Criterion<Item> crit = Criterion.newInstance(Item.DESCR, Operator.CONTAINS_CASE_INSENSITIVE, "table");
        Query<Item> query = session.createQuery(crit);

        long count = query.getCount();
        System.out.println("Count of the order items: " + count);
    }

    /** Using SKIP on UjoIterator */
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

    /** Using SELECT by a object relations */
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

    /** Using UPDATE */
    public void useUpdate() {
        Session session = OrmHandler.getInstance().getSession();
        Order order = session.load(Order.class, 1L);
        order.setDate(new Date());

        session.update(order);
        session.commit();
    }

    /** Using DELETE SQL */
    public void useDelete() {
        Session session = OrmHandler.getInstance().getSession();
        Criterion<Item> crit = Criterion.newInstance(Item.ID, 1);
        int count = session.delete(crit);
        session.commit();
        System.out.println("There are DELETED rows: " + count);
    }

    /** Using the column metadata */
    public void useMetadata() {
        OrmColumn c = (OrmColumn) OrmHandler.getInstance().findColumnModel(Order.DESCR);

        System.out.println("** COLUMN METADATA:");
        System.out.println("\t DB name: " + c.getFullName());
        System.out.println("\t Length : " + c.getMaxLength());
        System.out.println("\t NotNull: " + c.isMandatory());
        System.out.println("\t PrimKey: " + c.isPrimaryKey());
    }


    /** Test */
    public static void main(String[] args) {
        try {
            SampleORM sample = new SampleORM();

            sample.loadMetaModel();
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
            sample.useDelete();
            sample.useMetadata();

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
           OrmHandler.getInstance().getSession().close();
        }
    }

}
