/*
 *  Don't modify the generated class.
 *  License the Apache License, Version 2.0
 */
package org.bo.gen;

import org.bo.Item;
import org.bo.Order;
import org.ujoframework.UjoProperty;
import org.ujoframework.orm.annot.Column;
import org.ujoframework.implementation.orm.OrmTable;
import org.ujoframework.orm.annot.Comment;

/**
 * Order Item
 */
abstract public class _Item extends OrmTable<Item> {

    /** Unique key */
    @Comment("Unique key")
    @Column(pk = true)
    public static final UjoProperty<Item, Long> id = newProperty(Long.class);
    /** User key */
    @Comment("User key")
    public static final UjoProperty<Item, Integer> userId = newProperty(Integer.class);
    /** Description of Item */
    @Comment("Description of Item")
    public static final UjoProperty<Item, String> descr = newProperty(String.class);
    /** A reference to common Order */
    @Comment("A reference to common Order")
    @Column(name = "fk_order")
    public static final UjoProperty<Item, Order> order = newProperty(Order.class);

    // --- Getters / Setters ---
    public Long getId() {
        return id.getValue((Item) this);
    }

    public void setId(Long _id) {
        id.setValue((Item) this, _id);
    }

    public Integer getUsrId() {
        return userId.getValue((Item) this);
    }

    public void setUsrId(Integer _id) {
        userId.setValue((Item) this, _id);
    }

    public String getDescr() {
        return descr.getValue((Item) this);
    }

    public void setDescr(String _descr) {
        descr.setValue((Item) this, _descr);
    }

    public Order getOrder() {
        return order.getValue((Item) this);
    }

    public void setOrder(Order _order) {
        order.setValue((Item) this, _order);
    }
}
