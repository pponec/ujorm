/*
 *  Copyright 2020-2022 Pavel Ponec
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
package org.ujorm.orm.relation_M2One;

import org.ujorm.Key;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.implementation.orm.RelationToOne;
import org.ujorm.orm.annot.Column;

/**
 * The column mapping to DB table ITEM (a sample of usage).
 * Note, that the Item object have got a reference to a Order object.
 * @hidden
 * @Table=bo_item
 */
public class Item extends OrmTable<Item> {

    /** Unique key */
    @Column(pk = true)
    public static final Key<Item,Long> id = newProperty(Long.class);
    /** User key */
    public static final Key<Item,Integer> userId = newProperty(Integer.class);
    /** Description of the Item */
    public static final Key<Item,String> note = newProperty(String.class);
    /** A reference to common Order */
    @Column(name="fk_order")
    //public static final Key<Item,Order> order = newKey(Order.class);
    public static final Key<Item,Order> order= RelationToOne.of(Order.class, Order.sid);

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
    public void setNote(String _note) {
        set(note, _note);
    }
    public Order getOrder() {
        return get(order);
    }
    public void setOrder(Order _descr) {
        set(order, _descr);
    }



}
