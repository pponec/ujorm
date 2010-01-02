/*
 *  Copyright 2009-2010 Pavel Ponec
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
package org.ujoframework.orm_tutorial.sample;

import java.util.Date;
import org.ujoframework.UjoProperty;
import org.ujoframework.orm.annot.Column;
import org.ujoframework.implementation.orm.OrmTable;

/**
 * The column mapping to DB table ITEM (a sample of usage).
 * Note, that the Item object have got a reference to a Order object.
 * @hidden
 * @Table=bo_item
 */
public class Item extends OrmTable<Item> {

    /** Unique key */
    @Column(pk = true)
    public static final UjoProperty<Item,Long> ID = newProperty("id", Long.class);
    /** User key */
    public static final UjoProperty<Item,Integer> USER_ID = newProperty("usrId", Integer.class);
    /** Description of Item */
    public static final UjoProperty<Item,String> DESCR = newProperty("description", String.class);
    /** A reference to common Order */
    public static final UjoProperty<Item,Order> ORDER = newProperty("fk_order", Order.class);    
    /** A composed (indirect) property provides a 'created' attribute of the Order */
    public static final UjoProperty<Item,Date> _ORDER_DATE   = Item.ORDER.add(Order.CREATED);

    // --- An optional implementation of commonly used setters and getters ---

    public Long getId() {
        return get(ID);
    }
    public void setId(Long id) {
        set(ID, id);
    }
    public Integer getUsrId() {
        return get(USER_ID);
    }
    public void setUsrId(Integer usrId) {
        set(USER_ID, usrId);
    }
    public String getDescr() {
        return get(DESCR);
    }
    public void setDescr(String descr) {
        set(DESCR, descr);
    }
    public Order getOrder() {
        return get(ORDER);
    }
    public void setOrder(Order descr) {
        set(ORDER, descr);
    }

    /** Example of the composed PATH property */
    public Date getOrderDate() {
        // An alternative solution for: getOrder().getCreated();
        return get(_ORDER_DATE);
    }


}
