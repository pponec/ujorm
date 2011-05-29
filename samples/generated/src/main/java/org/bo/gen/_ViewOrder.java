/*
 *  Don't modify the generated class.
 *  License the Apache License, Version 2.0
 */
package org.bo.gen;

import org.bo.ViewOrder;
import org.ujorm.UjoProperty;
import org.ujorm.orm.annot.Column;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.View;

@View(select = "SELECT ord_order_alias.id, count(*) AS item_count"
+ " FROM db1.ord_order ord_order_alias"
+ ", db1.ord_item  ord_item_alias"
+ " WHERE ord_order_alias.id = ord_item_alias.fk_order"
+ " GROUP BY ord_order_alias.id"
+ " ORDER BY ord_order_alias.id")
abstract public class _ViewOrder extends OrmTable<_ViewOrder> {

    /** Unique key */
    @Column(pk = true)
    public static final UjoProperty<ViewOrder, Long> ID = newProperty(Long.class);
    /** ItemCount */
    public static final UjoProperty<ViewOrder, Integer> ITEM_COUNT = newProperty(0);

    // -------- Getters only ----------
    public Long getId() {
        return ID.getValue((ViewOrder) this);
    }

    public Integer getItemCount() {
        return ITEM_COUNT.getValue((ViewOrder) this);
    }
}
