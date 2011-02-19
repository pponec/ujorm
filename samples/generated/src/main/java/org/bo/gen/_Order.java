/*
 *  Don't modify the generated class.
 *  License the Apache License, Version 2.0
 */
package org.bo.gen;

import java.util.Date;
import org.bo.Item;
import org.bo.Order;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.extensions.ValueExportable;
import org.ujoframework.orm.DbType;
import org.ujoframework.orm.annot.Column;
import org.ujoframework.implementation.orm.OrmTable;
import org.ujoframework.implementation.orm.RelationToMany;
import org.ujoframework.orm.annot.Comment;
import org.ujoframework.orm.annot.Table;

@Table(name = "ord_order")
abstract public class _Order extends OrmTable<Order> {

    /** Store the value like VARCHAR. */
    public enum State implements ValueExportable {
        ACTIVE,
        DELETED;
        @Override public String exportToString() {
            return name().substring(0, 1);
        }
    }
    /** The Unique Key */
    @Comment("The Unique Key")
    @Column(pk = true)
    public static final UjoProperty<Order, Long> id = newProperty(Long.class);
    /** Order state, default is ACTIVE */
    @Comment("Order state, default is ACTIVE")
    public static final UjoProperty<Order, State> state = newProperty(State.ACTIVE);
    /** User key */
    @Comment("User key")
    public static final UjoProperty<Order, Integer> userId = newProperty(Integer.class);
    /** Description of the order */
    @Comment("Description of the order")
    @Column(type = DbType.VARCHAR, name = "DESCR", mandatory = true)
    public static final UjoProperty<Order, String> descr = newProperty(String.class);
    /** Date of creation */
    @Comment("Date of creation")
    public static final UjoProperty<Order, Date> created = newProperty(Date.class);
    /** Reference to Items */
    @Comment("Reference to Items")
    public static final RelationToMany<Order, Item> items = newRelation(Item.class);

    /** The child type */
    final private Order _this = (Order) this;

    // --- Getters / Setters ---

    /** The Unique Key */
    public Long getId() {
        return id.getValue(_this);
    }

    /** The Unique Key */
    public void setId(Long _id) {
        id.setValue(_this,_id);
    }

    /** User key */
    public Integer getUsrId() {
        return userId.getValue(_this);
    }

    /** User key */
    public void setUsrId(Integer _usrId) {
        userId.setValue(_this,_usrId);
    }

    /** Description of the order */
    public String getDescr() {
        return descr.getValue(_this);
    }

    /** Description of the order */
    public void setDescr(String _descr) {
        descr.setValue(_this,_descr);
    }

    /** Date of creation */
    public Date getCreated() {
        return created.getValue(_this);
    }

    /** Date of creation */
    public void setCreated(Date _date) {
        created.setValue(_this,_date);
    }

    /** Order state, default is ACTIVE */
    public State getState() {
        return state.getValue(_this);
    }

    /** Order state, default is ACTIVE */
    public void setState(State _state) {
        state.setValue(_this,_state);
    }

    /** Reference to Items */
    public UjoIterator<Item> getItems() {
        return items.getValue(_this);

    }
}
