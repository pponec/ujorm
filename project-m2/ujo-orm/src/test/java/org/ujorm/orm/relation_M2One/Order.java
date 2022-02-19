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

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.core.UjoIterator;
import org.ujorm.extensions.StringWrapper;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.DbType;
import org.ujorm.orm.OrmKeyFactory;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Table;

/**
 * The column mapping to DB table order (a sample of usage).
 * Note, that the Order object has got an collection of Items.
 * @hidden
 */
@Table(name = "ord_order")
public class Order extends OrmTable<Order> {

    /** Store the value like VARCHAR. */
    public enum State implements StringWrapper {
        ACTIVE,
        DELETED;

        @Override public String exportToString() {
            return name().substring(0, 1);
        }
    }

    /** The Key Factory */
    private static final OrmKeyFactory<Order> f = newFactory(Order.class);

    /** The Unique Key */
    @Column(pk = true)
    public static final Key<Order, Long> id = f.newKey();
    /** Alternative Unique Key */
    @Column(length=10, uniqueIndex="sid_index")
    public static final Key<Order,String> sid = f.newKeyDefault("");
    /** Order state, default is ACTIVE */
    public static final Key<Order, State> state = f.newKeyDefault(State.ACTIVE);
    /** Date of creation */
    public static final Key<Order, Date> created = f.newKey();
    /** User key */
    public static final Key<Order, Integer> userId = f.newKey();
    /** Description of the Order */
    @Column(type = DbType.VARCHAR, name = "NOTE", mandatory = true)
    public static final Key<Order, String> note = f.newKey();
    /** Reference to Items */
    public static final RelationToMany<Order, Item> items = f.newRelation();

    // Lock the factory:
   static {  f.lock(); }

    // -----------------------------------------------------------------------

    @Override
    public void writeValue(Key key, Object value) {
        super.writeValue(key, value);
        if (key==id && sid.isDefault(this)) {
            super.writeValue(sid, "S"+value);
        }
    }

    // --- An optional implementation of commonly used setters and getters ---
    public Long getId() {
        return get(id);
    }

    public void setId(Long _id) {
        set(id, _id);
    }

    public Date getDate() {
        return get(created);
    }

    public void setDate(Date _date) {
        set(created, _date);
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
