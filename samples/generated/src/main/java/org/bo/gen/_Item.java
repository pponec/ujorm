/*
 *  Don't modify the generated class.
 *  License the Apache License, Version 2.0
 */
package org.bo.gen;

import org.bo.Item;
import org.bo.Order;
import org.ujorm.UjoProperty;
import org.ujorm.orm.annot.Column;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Comment;

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

    /** The child type */
    final private Item _this = (Item) this;

    // --- Getters / Setters ---

    /** Unique key */
    public Long getId() {
        return id.getValue(_this);
    }

    /** Unique key */
    public void setId(Long _id) {
        id.setValue(_this, _id);
    }

    /** User key */
    public Integer getUsrId() {
        return userId.getValue(_this);
    }

    /** User key */
    public void setUsrId(Integer _id) {
        userId.setValue(_this, _id);
    }

    /** Description of Item */
    public String getDescr() {
        return descr.getValue(_this);
    }

    /** Description of Item */
    public void setDescr(String _descr) {
        descr.setValue(_this, _descr);
    }

    /** A reference to common Order */
    public Order getOrder() {
        return order.getValue(_this);
    }
    
    /** A reference to common Order */
    public void setOrder(Order _order) {
        order.setValue(_this, _order);
    }
}
