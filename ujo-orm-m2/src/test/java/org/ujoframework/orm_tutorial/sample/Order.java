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

import java.sql.Blob;
import java.sql.Clob;
import java.util.Date;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.UjoIterator;
import org.ujoframework.core.annot.Transient;
import org.ujoframework.extensions.ValueExportable;
import org.ujoframework.orm.DbType;
import org.ujoframework.orm.annot.Column;
import org.ujoframework.implementation.orm.OrmTable;
import org.ujoframework.implementation.orm.RelationToMany;
import org.ujoframework.orm.annot.Table;
import org.ujoframework.orm.utility.OrmTools;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, that the Order object has got an collection of Items.
 * @hidden
 */
@Table(name = "ord_order")
public class Order extends OrmTable<Order> {

    /** Store the value like VARCHAR. */
    public enum State implements ValueExportable {
        ACTIVE,
        DELETED;

        public String exportAsString() {
            return name().substring(0, 1);
        }
    }
    
    /** Unique key */
    @Column(pk = true)
    public static final UjoProperty<Order, Long> ID = newProperty(Long.class);
    /** Order state, default is ACTIVE */
    public static final UjoProperty<Order, State> STATE = newProperty(State.ACTIVE);
    /** User key */
    public static final UjoProperty<Order, Integer> USER_ID = newProperty(Integer.class);
    /** Description of the order */
    @Column(type = DbType.VARCHAR, name = "DESCR", mandatory = true)
    public static final UjoProperty<Order, String> DESCR = newProperty(String.class);
    /** Date of creation */
    public static final UjoProperty<Order, Date> CREATED = newProperty(Date.class);
    /** Text file */
    @Transient
    public static final UjoProperty<Order, Clob> TEXT_FILE = newProperty(Clob.class);
    /** Binary file */
    @Transient
    public static final UjoProperty<Order, Blob> BINARY_FILE = newProperty(Blob.class);
    /** Reference to Items */
    public static final RelationToMany<Order, Item> ITEMS = newRelation(Item.class);
    /** Customer */
    // @Column(name="fk_customer") public static final UjoProperty<Order, Customer> CUSTOMER = newProperty(Customer.class);
    // @Column(mandatory=true) public static final UjoProperty<Order, Integer> NEW_COLUMN = newProperty(777);

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


    public State getState() {
        return get(STATE);
    }

    public void setState(State state) {
        set(STATE, state);
    }

    public String getTextFile() {
        return OrmTools.getClobString(get(TEXT_FILE));
    }

    public void setTextFile(String largeFile) {
        set(TEXT_FILE, OrmTools.createClob(largeFile));
    }

    public byte[] getBinaryFile() {
        return OrmTools.getBlobBytes(get(BINARY_FILE));
    }

    public void setBinaryFile(byte[] binaryFile) {
        set(BINARY_FILE, OrmTools.createBlob(binaryFile));
    }

    public UjoIterator<Item> getItems() {
        return get(ITEMS);
    }
}
