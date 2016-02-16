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
package org.ujorm.orm.pojo.orm_tutorial.sample.entity;

import java.math.BigDecimal;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import org.ujorm.orm.pojo.orm_tutorial.sample.entity.generated.*;

/**
 * The column mapping to DB table ITEM (a sample of usage).
 * Note, that the Item object have got a reference to a Order object.
 * @hidden
 * @Table=bo_item
 */
@Comment("Order item")
public class Item {

    /** Unique key */
    @Column(pk = true)
    private Long id;
    /** User key */
    private Integer userId;
    /** Description of the $Item */
    private String note;
    /** Price of the item */
    @Comment("Price of the item")
    @Column(length=8, precision=2)
    private BigDecimal price = BigDecimal.ZERO;
    /** A reference to common $Order */
    @Comment("A reference to the Order")
    @Column(name="fk_order")
    private $Order order;

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public $Order getOrder() {
        return order;
    }

    public void setOrder($Order order) {
        this.order = order;
    }

}
