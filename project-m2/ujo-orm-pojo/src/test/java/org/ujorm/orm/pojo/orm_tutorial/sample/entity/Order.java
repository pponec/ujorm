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

import java.sql.Blob;
import java.sql.Clob;
import java.util.Date;
import java.util.List;
import org.ujorm.core.annot.Transient;
import org.ujorm.extensions.StringWrapper;
import org.ujorm.orm.DbType;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Comment;
import org.ujorm.orm.annot.Table;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, that the Order object has got an collection of Items.
 * @hidden
 */
@Comment("Order table for registering the 'order items'")
@Table(name = "ord_order")
public class Order {

    /** Store the value like VARCHAR. */
    public enum State implements StringWrapper {
        ACTIVE,
        DELETED;

        @Override public String exportToString() {
            return name().substring(0, 1);
        }
    }

    /** The Primary Key */
    @Comment("The Primary Key")
    @Column(pk = true)
    private Long id;
    /** Order STATE, default is ACTIVE */
    @Comment("Order state, default value is ACTIVE")
    private State state = State.ACTIVE;
    /** User key */
    private Integer user_id ;
    /** Description of the Order */
    @Comment("Description of the Order")
    @Column(type = DbType.VARCHAR, name = "NOTE", mandatory = true)
    private String note;
    /** Date of creation */
    private Date created;
    /** Text file */
    @Transient
    private Clob textFile;
    /** Binary file */
    @Transient
    private Blob binaryFile;
    /** Reference to Items */
    private List<Item> items;
    /** Customer */
    @Column(name="fk_customer") private Customer customer;
    @Column(mandatory=true) private Integer newColumn = 777;

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Integer getUser_id() {
        return user_id;
    }

    public void setUser_id(Integer user_id) {
        this.user_id = user_id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Clob getTextFile() {
        return textFile;
    }

    public void setTextFile(Clob textFile) {
        this.textFile = textFile;
    }

    public Blob getBinaryFile() {
        return binaryFile;
    }

    public void setBinaryFile(Blob binaryFile) {
        this.binaryFile = binaryFile;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Integer getNewColumn() {
        return newColumn;
    }

    public void setNewColumn(Integer newColumn) {
        this.newColumn = newColumn;
    }

}
