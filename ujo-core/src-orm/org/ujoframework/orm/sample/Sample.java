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
import org.ujoframework.orm.DbHandler;
import org.ujoframework.orm.Query;
import org.ujoframework.tools.criteria.Expression;
import org.ujoframework.tools.criteria.Operator;

/**
 *
 * @author pavel
 */
public class Sample {

    /** Using INSERT */
    public void useCreateItem() {

        if (true) {
            DbHandler.getInstance().createDatabase(BoDatabase.class);
        } else {
            DbHandler.getInstance().loadDatabase(BoDatabase.class);

        }
        Session session = DbHandler.getInstance().getSession();

        BoOrder order = new BoOrder();
        BoOrder.DATE.setValue(order, new Date());
        BoOrder.DESCR.setValue(order, "John's order");

        BoItem item1 = new BoItem();
        BoItem.DESCR.setValue(item1, "yellow table");
        BoItem.ORDER.setValue(item1, order);
        BoItem item2 = new BoItem();
        BoItem.DESCR.setValue(item2, "green window");
        BoItem.ORDER.setValue(item2, order);

        System.out.println("order: "  + order.toString());
        System.out.println("item1: "  + item1.toString());
        System.out.println("item2: "  + item2.toString());

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
    public void useSelection() {
        Session session = DbHandler.getInstance().getSession();

        Expression<BoOrder> exp1 = Expression.newInstance(BoOrder.DESCR, "John's order");
        Expression<BoOrder> exp2 = Expression.newInstance(BoOrder.DATE, Operator.LE, new Date());
        Expression<BoOrder> expr = exp1.and(exp2);

        Query<BoOrder> query = session.createQuery(BoOrder.class, expr);
        query.setCountRequest(true);  // need a count of iterator items, a default value is false
        query.setReadOnly(false);     // Read onlyl result;

        for (BoOrder o : session.iterate( query ) ) {
            Long id = BoOrder.ID.of(o);
            String descr = BoOrder.DESCR.of(o);
            System.out.println("Order id: " + id + " descr: " + descr);
        }
    }

    /** Using SELECT by a object relations */
    public void useRelation() {
        Session session = DbHandler.getInstance().getSession();
        BoDatabase db = session.getDatabase();

        UjoIterator<BoOrder> orders  = BoDatabase.ORDERS.of(db);
        for (BoOrder order : orders) {
            Long id = BoOrder.ID.of(order);
            String descr = BoOrder.DESCR.of(order);
            System.out.println("Order id: " + id + " descr: " + descr);

            for (BoItem item : BoOrder.ITEMS.of(order)) {
                Long itemId = BoItem.ID.of(item);
                String itemDescr = BoItem.DESCR.of(item);
                System.out.println(" Item id: " + itemId + " descr: " + itemDescr);
            }
        }
    }

    /** Test */
    public static void main(String[] args) {

        try {
            Sample sample = new Sample();
            sample.useCreateItem();
            sample.useSelection();
            //sample.useRelation();

            //session.close();
        } finally {
           DbHandler.getInstance().getSession().close();
        }

    }

}
