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
package org.ujorm.orm.pojo.orm_tutorial.sample.entity.generated;

import java.math.BigDecimal;
import java.util.Date;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import org.ujorm.extensions.UjoMiddle;
import org.ujorm.orm.OrmKeyFactory;
import org.ujorm.orm.pojo.orm_tutorial.sample.entity.*;

/**
 * The column mapping to DB table ITEM (a sample of usage).
 * Note, that the $Item object have got a reference to a $Order object.
 * @hidden
 * @Table=bo_item
 */
@Comment("Order item")
public final class $Item extends OrmTable<$Item> {
    private static final KeyFactory<$Item> f = new OrmKeyFactory($Item.class, true);

    /** Unique key */
    @Column(pk = true)
    public static final Key<$Item,Long> ID = f.newKey();
    /** User key */
    public static final Key<$Item,Integer> USER_ID = f.newKey();
    /** Description of the $Item */
    public static final Key<$Item,String> NOTE = f.newKey();
    /** Price of the item */
    @Comment("Price of the item")
    @Column(length=8, precision=2)
    public static final Key<$Item,BigDecimal> PRICE = f.newKeyDefault(BigDecimal.ZERO);
    /** A reference to common $Order */
    @Comment("A reference to the Order")
    @Column(name="fk_order")
    public static final Key<$Item,$Order> ORDER = f.newKey();
    /** A composed (or indirect) key provides a 'CREATED' attribute of the $Order */
    public static final Key<$Item,Date> $ORDER_CREATED = $Item.ORDER.add($Order.CREATED);

    // Lock the Key factory
    static { f.lock(); }

    // --- Getters and Setters ---

    /** Unique key */
    public Long getId() {
        return ID.of(this);
    }

    /** Unique key */
    public void setId(Long id) {
        ID.setValue(this, id);
    }

    /** User key */
    public Integer getUserId() {
        return USER_ID.of(this);
    }

    /** User key */
    public void setUserId(Integer userId) {
        USER_ID.setValue(this, userId);
    }

    /** Description of the $Item */
    public String getNote() {
        return NOTE.of(this);
    }

    /** Description of the $Item */
    public void setNote(String note) {
        NOTE.setValue(this, note);
    }

    /** Price of the item */
    public BigDecimal getPrice() {
        return PRICE.of(this);
    }

    /** Price of the item */
    public void setPrice(BigDecimal price) {
        PRICE.setValue(this, price);
    }

    /** A reference to common $Order */
    public $Order getOrder() {
        return ORDER.of(this);
    }

    /** A reference to common $Order */
    public void setOrder($Order order) {
        ORDER.setValue(this, order);
    }

    /** A composed (or indirect) key provides a 'CREATED' attribute of the $Order */
    public Date get$orderCreated() {
        return $ORDER_CREATED.of(this);
    }

    /** A composed (or indirect) key provides a 'CREATED' attribute of the $Order */
    public void set$orderCreated(Date $orderCreated) {
        $ORDER_CREATED.setValue(this, $orderCreated);
    }

}
