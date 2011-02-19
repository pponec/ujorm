/** Editing of the class is allowed. */

package org.bo;
import org.bo.gen._ViewOrder;
import org.ujoframework.orm.annot.View;

@View(select = "SELECT ord_order_alias.id, count(*) AS item_count"
+ " FROM db1.ord_order ord_order_alias"
+ ", db1.ord_item  ord_item_alias"
+ " WHERE ord_order_alias.id = ord_item_alias.fk_order"
+ " GROUP BY ord_order_alias.id"
+ " ORDER BY ord_order_alias.id")
final public class ViewOrder extends _ViewOrder {

}
