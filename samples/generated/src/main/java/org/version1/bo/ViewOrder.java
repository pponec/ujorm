/** Editing of the class is allowed. */

package org.version1.bo;
import org.version1.bo.gen._ViewOrder;
import org.ujorm.orm.annot.View;

@View(select = "SELECT ord_order_alias.id, count(*) AS item_count"
+ " FROM db1.ord_order ord_order_alias"
+ ", db1.ord_item  ord_item_alias"
+ " WHERE ord_order_alias.id = ord_item_alias.fk_order"
+ " GROUP BY ord_order_alias.id"
+ " ORDER BY ord_order_alias.id")
final public class ViewOrder extends _ViewOrder {

}
