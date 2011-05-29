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
 * The column mapping to DB table order (a sample of usage).
 * Note, that the Order object has got an collection of Items.
 * @hidden
 */
@Comment("Order table for registering the 'order items'")
@Table(name = "ord_order")
public class Order extends OrmTable<Order> {

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
    public static final UjoProperty<Order, Long> id = newProperty(Long.class);
    /** Order state, default is ACTIVE */
    @Comment("Order state, default value is ACTIVE")
    public static final UjoProperty<Order, State> state = newProperty(State.ACTIVE);
    /** User key */
    public static final UjoProperty<Order, Integer> userId = newProperty(Integer.class);
    /** Description of the Order */
    @Comment("Description of the Order")
    @Column(type = DbType.VARCHAR, name = "NOTE", mandatory = true)
    public static final UjoProperty<Order, String> note = newProperty(String.class);
    /** Date of creation */
    public static final UjoProperty<Order, Date> created = newProperty(Date.class);
    /** Reference to Items */
    public static final RelationToMany<Order, Item> items = newRelation(Item.class);

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

    public void setUsrId(Integer _usrId) {
        set(userId, _usrId);
    }

    public String getNote() {
        return get(note);
    }

    public void setNote(String _note) {
        set(note, _note);
    }

    public Date getCreated() {
        return get(created);
    }

    public void setCreated(Date _created) {
        set(created, _created);
    }

    public State getState() {
        return get(state);
    }

    public void setState(State _state) {
        set(state, _state);
    }

    public UjoIterator<Item> getItems() {
        return get(items);
    }
}
