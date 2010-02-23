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
import org.ujoframework.core.UjoIterator;
import org.ujoframework.orm.DbType;
import org.ujoframework.orm.annot.Column;
import org.ujoframework.implementation.orm.OrmTable;
import org.ujoframework.implementation.orm.RelationToMany;
import org.ujoframework.orm.annot.Table;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, that the Order object has got an collection of Items.
 * @hidden
 */
@Table(name="ord_order")
public class Order extends OrmTable<Order> {

    public enum State {
        ACTIVE,
        DELETED
    }

    /** Unique key */
    @Column(pk=true)
    public static final UjoProperty<Order,Long> ID = newProperty("id", Long.class);
    /** Order state, default is ACTIVE */
    public static final UjoProperty<Order,State> STATE = newProperty("state", State.ACTIVE);
    /** User key */
    public static final UjoProperty<Order,Integer> USER_ID = newProperty("usrId", Integer.class);
    /** Description of the order */
    @Column(type=DbType.VARCHAR, name="DESCR", mandatory=true)
    public static final UjoProperty<Order,String> DESCR = newProperty("description", String.class);
    /** Date of creation */
    public static final UjoProperty<Order,Date> CREATED = newProperty("created", Date.class);
    /** References to Itemsr */
    public static final RelationToMany<Order,Item> ITEMS = newRelation("items", Item.class);
    
    /** Date of creation */
    public static final UjoProperty<Order,String> NEXT_COLUMN = newProperty("next_column", String.class);

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
    public Date getDate() {
        return get(CREATED);
    }
    public void setDate(Date date) {
        set(CREATED, date);
    }
    public UjoIterator<Item> getItems() {
        return get(ITEMS);
    }



}
