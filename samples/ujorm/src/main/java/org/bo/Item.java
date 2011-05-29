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
package org.bo;

import java.math.BigDecimal;
import java.util.Date;
import org.ujorm.UjoProperty;
import org.ujorm.orm.annot.Column;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Comment;

/**
 * The column mapping to DB table ITEM (a sample of usage).
 * Note, that the Item object have got a reference to a Order object.
 * @hidden
 * @Table=bo_item
 */
@Comment("Order item")
public class Item extends OrmTable<Item> {

    /** Unique key */
    @Column(pk = true)
    public static final UjoProperty<Item,Long> id = newProperty(Long.class);
    /** User key */
    public static final UjoProperty<Item,Integer> userId = newProperty(Integer.class);
    /** Description of the Item */
    public static final UjoProperty<Item,String> note = newProperty(String.class);
    /** Price of the item */
    @Comment("Price of the item")
    @Column(length=8, precision=2)
    public static final UjoProperty<Item,BigDecimal> price = newProperty(BigDecimal.ZERO);
    /** A reference to common Order */
    @Comment("A reference to the Order")
    @Column(name="fk_order")
    public static final UjoProperty<Item,Order> order = newProperty(Order.class);
    /** A composed property provides a 'created' attribute of the Order */
    public static final UjoProperty<Item,Date> $orderCreated = Item.order.add(Order.created);

    // --- An optional implementation of commonly used setters and getters ---

    public Long getId() {
        return get(id);
    }
    public void setId(Long _id) {
        set(id, _id);
    }
    public Integer getUsrId() {
        return get(userId);
    }
    public void setUsrId(Integer _id) {
        set(userId, _id);
    }
    public String getNote() {
        return get(note);
    }
    public void setNote(String _descr) {
        set(note, _descr);
    }
    public Order getOrder() {
        return get(order);
    }
    public void setOrder(Order _descr) {
        set(order, _descr);
    }

    /** Example of the composed property */
    public Date getOrderCreated() {
        // An alternative solution for: getOrder().getCreated();
        return get($orderCreated);
    }


}
