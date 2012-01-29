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

import java.util.Date;
import org.ujorm.UjoProperty;
import org.ujorm.core.UjoIterator;
import org.ujorm.extensions.ValueExportable;
import org.ujorm.orm.DbType;
import org.ujorm.orm.annot.Column;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.annot.Comment;
import org.ujorm.orm.annot.Table;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, that the Order object has got an collection of Items.
 * @hidden
 */
@Comment("Order table for registering the 'order items'")
@Table(name = "ord_order")
public final class Order extends OrmTable<Order> {

    /** Store the value like VARCHAR. */
    public enum State implements ValueExportable {
        ACTIVE,
        DELETED;

        @Override public String exportToString() {
            return name().substring(0, 1);
        }
    }

    /** The Primary Key */
    @Comment("The Primary Key")
    @Column(pk = true)
    public static final UjoProperty<Order, Long> ID = newProperty(Long.class);
    /** Order STATE, default is ACTIVE */
    @Comment("Order state, default value is ACTIVE")
    public static final UjoProperty<Order, State> STATE = newProperty(State.ACTIVE);
    /** User key */
    public static final UjoProperty<Order, Integer> USER_ID = newProperty(Integer.class);
    /** Description of the Order */
    @Comment("Description of the Order")
    @Column(type = DbType.VARCHAR, name = "NOTE", mandatory = true)
    public static final UjoProperty<Order, String> NOTE = newProperty(String.class);
    /** Date of creation */
    public static final UjoProperty<Order, Date> CREATED = newProperty(Date.class);
    /** Reference to Items */
    public static final RelationToMany<Order, Item> ITEMS = newRelation(Item.class);

    // --- Constructors ---

    public Order() {
    }

    public Order(Long id) {
        setId(id);
    }
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

    public void setUsrId(Integer _usrId) {
        set(USER_ID, _usrId);
    }

    public String getNote() {
        return get(NOTE);
    }

    public void setNote(String _note) {
        set(NOTE, _note);
    }

    public Date getCreated() {
        return get(CREATED);
    }

    public void setCreated(Date _created) {
        set(CREATED, _created);
    }

    public State getState() {
        return get(STATE);
    }

    public void setState(State _state) {
        set(STATE, _state);
    }

    public UjoIterator<Item> getItems() {
        return get(ITEMS);
    }
}
