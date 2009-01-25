/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.implementation.db.sample;

import java.util.Date;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.implementation.db.DbConnection;
import org.ujoframework.implementation.db.DbHandler;

/**
 *
 * @author pavel
 */
public class Sample {

    public void testCreateItem() {

        DbHandler handler = DbHandler.getInstance();

        BoOrder order = new BoOrder();
        BoOrder.DATE.setValue(order, new Date());
        BoOrder.DESCR.setValue(order, "The First Order");


        BoItem item = new BoItem();
        BoItem.DESCR.setValue(item, "Yellow table");
        BoItem.ORDER.setValue(item, order);


        DbConnection connection = handler.getConnection();

        connection.save(order);
        connection.save(item);

        connection.commit();
        connection.rollback();
    }

    public void testRelation() {

        DbHandler handler = DbHandler.getInstance();
        DbConnection connection = handler.getConnection();
        BoDatabase db = connection.getDatabase();

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

        connection.commit();
        connection.rollback();
    }

    public void testSelection() {

        
    }




}
