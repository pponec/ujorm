/*
 *  Copyright 2009-2014 Pavel Ponec
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
package org.ujorm.orm_tutorial.sample.entity;

import java.math.BigDecimal;
import java.util.Date;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;

/**
 * The column mapping to DB table ITEM (a sample of usage).
 * Note, that the Item object have got a reference to a Order object.
 * @hidden
 * @Table=bo_item
 */
@Comment("Order item")
public final class Item extends OrmTable<Item> {
    private static final KeyFactory<Item> f = newCamelFactory(Item.class);

    /** Unique key */
    @Column(pk = true)
    public static final Key<Item,Long> ID = f.newKey();
    /** User key */
    public static final Key<Item,Integer> USER_ID = f.newKey();
    /** Description of the Item */
    public static final Key<Item,String> NOTE = f.newKey();
    /** Price of the item */
    @Comment("Price of the item")
    @Column(length=8, precision=2)
    public static final Key<Item,BigDecimal> PRICE = f.newKeyDefault(BigDecimal.ZERO);
    /** A reference to common Order */
    @Comment("A reference to the Order")
    @Column(name="fk_order")
    public static final Key<Item,Order> ORDER = f.newKey();
    /** A composed (or indirect) key provides a 'CREATED' attribute of the Order */
    public static final Key<Item,Date> $ORDER_CREATED = Item.ORDER.add(Order.CREATED);



    // --- An optional implementation of commonly used setters and getters ---

    public Long getId() {
        return get(ID);
    }
    public void setId(Long _id) {
        set(ID, _id);
    }
    public Integer getUsrId() {
        return get(USER_ID);
    }
    public void setUsrId(Integer _id) {
        set(USER_ID, _id);
    }
    public String getNote() {
        return get(NOTE);
    }
    public void setNote(String _descr) {
        set(NOTE, _descr);
    }
    public Order getOrder() {
        return get(ORDER);
    }
    public void setOrder(Order _descr) {
        set(ORDER, _descr);
    }

    /** Example of the composed key */
    public Date getOrderCreated() {
        // An alternative solution for: getOrder().getCreated();
        return get($ORDER_CREATED);
    }


}
