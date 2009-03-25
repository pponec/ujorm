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
    public void useCreateItem() {

        if (true) {
            OrmHandler.getInstance().createDatabase(Database.class);
        } else {
            OrmHandler.getInstance().loadDatabase(Database.class);
        }
        Session session = OrmHandler.getInstance().getSession();

        Order order = new Order();
        order.set(Order.DATE, new Date());
        order.set(Order.DESCR, "John's order");

        Item item1 = new Item();
        item1.set(Item.DESCR, "Yellow table");
        item1.set(Item.ORDER, order);

        Item item2 = new Item();
        item2.set(Item.ORDER, order);
        item2.set(Item.DESCR, "Green window");

        System.out.println("order: " + order.toString());
        System.out.println("item1: " + item1.toString());
        System.out.println("item2: " + item2.toString());

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
    public void useOrderSelection() {
        Session session = OrmHandler.getInstance().getSession();

        Expression<Order> exp1 = Expression.newInstance(Order.DESCR, "John's order");
        Expression<Order> exp2 = Expression.newInstance(Order.DATE, Operator.LE, new Date());
        Expression<Order> expr = exp1.and(exp2);

        Query<Order> query = session.createQuery(Order.class, expr);
        query.setCountRequest(true);  // need a count of iterator items, a default value is false
        query.setReadOnly(false);     // Read only result;

        for (Order order : session.iterate( query ) ) {
            Long id = order.get(Order.ID);
            String descr = order.get(Order.DESCR);
            System.out.println("ORDER ROW: " + order);
        }
    }

    /** Using SELECT by QUERY */
    public void useItemSelection() {
        Session session = OrmHandler.getInstance().getSession();

        Expression<Item> expr = Expression.newInstance(Item.DESCR, Operator.CONTAINS_CASE_INSENSITIVE, "table");
        Query<Item> query = session.createQuery(expr);

        for (Item item : session.iterate( query ) ) {

            Order order = Item.ORDER.of(item);
            System.out.println("ITEM ROW: " + item + " ORDER: " + order);
        }
    }



    /** Using SELECT by a object relations */
    public void useRelation() {
        Session session = OrmHandler.getInstance().getSession();
        Database db = session.getDatabase();

        UjoIterator<Order> orders  = Database.ORDERS.of(db);
        for (Order order : orders) {
            Long id = order.get(Order.ID);
            String descr = order.get(Order.DESCR);
            System.out.println("Order id: " + id + " descr: " + descr);

            for (Item item : order.get(Order.ITEMS)) {
                Long itemId = item.get(Item.ID);
                String itemDescr = item.get(Item.DESCR);
                System.out.println(" Item id: " + itemId + " descr: " + itemDescr);
            }
        }
    }

    /** Test */
    public static void main(String[] args) {

        try {
            SampleORM sample = new SampleORM();
            sample.useCreateItem();
            System.out.println(". . . . . . . . . . . . . . . .");
            sample.useOrderSelection();
            //sample.useItemSelection();

            // --------------------------
            //sample.useRelation();
            //session.close();
        } finally {
           OrmHandler.getInstance().getSession().close();
        }
    }

}
