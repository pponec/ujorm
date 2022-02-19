/*
 *  Copyright 2009-2022 Pavel Ponec
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
package org.ujorm.orm_tutorial.sample;

import java.sql.Blob;
import java.sql.Clob;
import java.time.LocalDateTime;
import org.ujorm.Key;
import org.ujorm.core.UjoIterator;
import org.ujorm.core.annot.Transient;
import org.ujorm.extensions.StringWrapper;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.DbType;
import org.ujorm.orm.OrmKeyFactory;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import org.ujorm.orm.annot.Table;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, that the Order object has got an collection of Items.
 * @hidden
 */
@Comment("Order table for registering the 'order items'")
@Table("ord_order")
public final class Order extends OrmTable<Order> {

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

    /** The Primary Key */
    @Comment("The Primary Key")
    @Column(pk = true)
    public static final Key<Order, Long> ID = f.newKey();
    /** Order STATE, default is ACTIVE */
    @Comment("Order state, default value is ACTIVE")
    public static final Key<Order, State> STATE = f.newKeyDefault(State.ACTIVE);
    /** User key */
    public static final Key<Order, Integer> USER_ID = f.newKey();
    /** Description of the Order */
    @Comment("Description of the Order")
    @Column(type = DbType.VARCHAR, name = "NOTE", mandatory = true)
    public static final Key<Order, String> NOTE = f.newKey();
    /** Date of creation */
    public static final Key<Order, LocalDateTime> CREATED = f.newKey();
    /** Text file */
    @Transient
    public static final Key<Order, Clob> TEXT_FILE = f.newKey();
    /** Binary file */
    @Transient
    public static final Key<Order, Blob> BINARY_FILE = f.newKey();
    /** Reference to Items */
    public static final RelationToMany<Order, Item> ITEMS = f.newRelation();
    /** Customer */
    @Column(name="fk_customer")
    public static final Key<Order, Customer> CUSTOMER = f.newKey();
    /** Customer with FK type of String */
    @Column(name="fk_currency", mandatory = false)
    public static final Key<Order, Currency> CURENCY = f.newKey();
    /** New column*/
    @Column(mandatory=true) public static final Key<Order, Integer> NEW_COLUMN = f.newKeyDefault(777);

    // Lock the factory:
    static {  f.lock(); }

    // --- Constructors ---

    public Order() {
    }

    public Order(Long id) {
        setId(id);
    }

    // --- Handy getter for an Iterator

    /** Get all items */
    public UjoIterator<Item> getItems() {
        return get(ITEMS);
    }

    // --- Optional getters and setters are generated by a NetBeans pluggin ---

    /** The Primary Key */
    public Long getId() {
        return ID.of(this);
    }

    /** The Primary Key */
    public void setId(Long id) {
        ID.setValue(this, id);
    }

    /** Order STATE, default is ACTIVE */
    public State getState() {
        return STATE.of(this);
    }

    /** Order STATE, default is ACTIVE */
    public void setState(State state) {
        STATE.setValue(this, state);
    }

    /** User key */
    public Integer getUserId() {
        return USER_ID.of(this);
    }

    /** User key */
    public void setUserId(Integer userId) {
        USER_ID.setValue(this, userId);
    }

    /** Description of the Order */
    public String getNote() {
        return NOTE.of(this);
    }

    /** Description of the Order */
    public void setNote(String note) {
        NOTE.setValue(this, note);
    }

    /** Date of creation */
    public LocalDateTime getCreated() {
        return CREATED.of(this);
    }

    /** Date of creation */
    public void setCreated(LocalDateTime created) {
        CREATED.setValue(this, created);
    }

    /** Text file */
    public Clob getTextFile() {
        return TEXT_FILE.of(this);
    }

    /** Text file */
    public void setTextFile(Clob textFile) {
        TEXT_FILE.setValue(this, textFile);
    }

    /** Binary file */
    public Blob getBinaryFile() {
        return BINARY_FILE.of(this);
    }

    /** Binary file */
    public void setBinaryFile(Blob binaryFile) {
        BINARY_FILE.setValue(this, binaryFile);
    }

    /** Customer */
    public Customer getCustomer() {
        return CUSTOMER.of(this);
    }

    /** Customer */
    public void setCustomer(Customer customer) {
        CUSTOMER.setValue(this, customer);
    }

    /** New column*/
    public Integer getNewColumn() {
        return NEW_COLUMN.of(this);
    }

    /** New column*/
    public void setNewColumn(Integer newColumn) {
        NEW_COLUMN.setValue(this, newColumn);
    }

}
