/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.implementation.db.sample;

import java.util.Date;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.implementation.db.Session;
import org.ujoframework.implementation.db.DbHandler;
import org.ujoframework.implementation.db.Query;
import org.ujoframework.tools.criteria.Expression;

/**
 *
 * @author pavel
 */
// @SuppressWarnings("unchecked")
public class Sample {

    public void useCreateItem() {

        Session session = DbHandler.getInstance().getSession();

        BoOrder order = new BoOrder();
        BoOrder.DATE.setValue(order, new Date());
        BoOrder.DESCR.setValue(order, "The First Order");


        BoItem item = new BoItem();
        BoItem.DESCR.setValue(item, "Yellow table");
        BoItem.ORDER.setValue(item, order);


        session.save(order);
        session.save(item);

        if (true) {
           session.commit();
        } else {
           session.rollback();
        }
    }

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

        session.commit();
        session.rollback();
    }


    public void useSelection() {

        Session session = DbHandler.getInstance().getSession();


        Expression<BoOrder> exp1 = Expression.newInstance(BoOrder.ID);
        Expression<BoOrder> exp2 = Expression.newInstance(BoOrder.DATE);
        Expression<BoOrder> expA = exp1.and(exp2);

        Query<BoOrder> query = session.createQuery(BoOrder.class, expA);
        query.sizeRequired(true);
        query.readOnly(false);
        query.setParameter(BoOrder.ID, 10L);
        query.setParameter(BoOrder.DATE, new Date());

        BoOrder order = session.single(query);

        for (BoOrder o : session.iterate( query ) ) {
            Long id = BoOrder.ID.of(order);
            String descr = BoOrder.DESCR.of(order);
            System.out.println("Order id: " + id + " descr: " + descr);
        }
    }

}
