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
import org.ujoframework.core.UjoIterator;
import org.ujoframework.orm.Session;
import org.ujoframework.orm.OrmHandler;
import org.ujoframework.orm.Query;
import org.ujoframework.tools.criteria.Expression;
import org.ujoframework.tools.criteria.Operator;

/**
 * SampleORM of usages: INSERT, SELECT
 * @author Pavel Ponec
 */
public class SampleORM {

    /** Create database and using INSERT */
    public void useInsert() {

        if (true) {
            OrmHandler.getInstance().createDatabase(Database.class);
        } else {
            OrmHandler.getInstance().loadDatabase(Database.class);
        }
        Session session = OrmHandler.getInstance().getSession();

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
    public void useSelectionOfOrder() {

        Expression<Order> exp1 = Expression.newInstance(Order.DESCR, "John's order");
        Expression<Order> exp2 = Expression.newInstance(Order.DATE, Operator.LE, new Date());
        Expression<Order> expr = exp1.and(exp2);

        Session session = OrmHandler.getInstance().getSession();
        Query<Order> query = session.createQuery(Order.class, expr);
        query.setCountRequest(true);  // need a count of iterator items, a default value is false
        query.setReadOnly(false);     // Read only result;

        for (Order order : query.iterate()) {
            String descr = order.getDescr();
            System.out.println("ORDER ROW: " + order + " // descr: " + descr);
        }
    }

    /** Using SELECT by QUERY */
    public void useSelectionOfItem_1() {
        Session session = OrmHandler.getInstance().getSession();

        Expression<Item> expr = Expression.newInstance(Item.DESCR, Operator.CONTAINS_CASE_INSENSITIVE, "table");
        Query<Item> query = session.createQuery(expr);

        for (Item item : session.iterate( query )) {
            Order order = item.getOrder();
            System.out.println("ITEM ROW: " + item + " ORDER: " + order);
        }
    }

    /** Using SELECT by QUERY */
    public void useSelectionOfItem_2() {
        Session session = OrmHandler.getInstance().getSession();

        Order orderValue = session.load(Order.class, 1L);
        Expression<Item> expr = Expression.newInstance(Item.ORDER, orderValue);
        Query<Item> query = session.createQuery(expr);

        for (Item item : query.iterate()) {
            Order order2 = item.getOrder();
            System.out.println("ITEM ROW: " + item + " ORDER: " + order2);
        }
    }

    /** Using SELECT by QUERY */
    public void useSelectionOfItem_3() {
        Session session = OrmHandler.getInstance().getSession();
        Order orderValue = session.load(Order.class, 1L);

        for (Item item : orderValue.getItems()) {
            Order order2 = item.getOrder();
            System.out.println("ITEM ROW: " + item + " ORDER: " + order2);
        }
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
        Expression<Item> expr = Expression.newInstance(Item.ID, 1);
        int count = session.delete(Item.class, expr);
        session.commit();
        System.out.println("There are DELETED rows: " + count);
    }

    /** Test */
    public static void main(String[] args) {
        try {

            SampleORM sample = new SampleORM();
            sample.useInsert();
            sample.useSelectionOfOrder();
            sample.useSelectionOfItem_1();
            sample.useSelectionOfItem_2();
            sample.useSelectionOfItem_3();
            sample.useRelation();
            sample.useUpdate();
            sample.useDelete();

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
           OrmHandler.getInstance().getSession().close();
        }
    }

}
