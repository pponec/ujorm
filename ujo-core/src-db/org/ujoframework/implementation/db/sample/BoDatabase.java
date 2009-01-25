/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ujoframework.implementation.db.sample;

import org.ujoframework.implementation.db.TableUjo;
import org.ujoframework.implementation.db.UjoRelative;

/**
 * An table definition of the one database.
 * @database=JDBC/MY_DATABASE
 */
public class BoDatabase extends TableUjo {

    /** Customer order */
    public static final UjoRelative<BoDatabase,BoOrder> ORDERS = newRelation("ORD_ORDER", BoOrder.class);

    /** Items of the Customer order */
    public static final UjoRelative<BoDatabase,BoItem> ORDER_ITEMS = newRelation("ORD_ITEMS", BoItem.class);



}
