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
package org.ujorm.orm.bo;

import java.awt.Color;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Date;
import org.ujorm.Key;
import org.ujorm.core.UjoIterator;
import org.ujorm.core.annot.Transient;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.DbType;
import org.ujorm.orm.annot.Column;
import org.ujorm.orm.annot.Table;
import org.ujorm.orm.utility.OrmTools;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, that the XOrder object has got an collection of Items.
 * @hidden
 */
@Table(name = "ord_order")
public class XOrder extends OrmTable<XOrder> {

    /** Simple Index */
    public static final String IDX_NOTE = "idx_note";
    /** Composite Index */
    public static final String IDX_STATE_NOTE = "idx_state_note";

    public enum State {
        ACTIVE,
        DELETED
    }

    /** Unique key */
    @Column(pk = true)
    public static final Key<XOrder, Long> ID = newKey();
    /** XOrder state, default is ACTIVE */
    @Column(index = IDX_STATE_NOTE) //
    public static final Key<XOrder, State> STATE = newKey("state", State.ACTIVE);
    /** User key */
    public static final Key<XOrder, Integer> USER_ID = newKey();
    /** Description of the Order */
    @Column(type = DbType.VARCHAR, name = "NOTE", mandatory = true, index = {IDX_NOTE, IDX_STATE_NOTE})
    public static final Key<XOrder, String> NOTE = newKey("note");
    /** Favorite Color */
    public static final Key<XOrder, Color> COLOR = newKey(Color.WHITE);
    /** Date of creation */
    public static final Key<XOrder, Date> CREATED = newKey();
    /** Text file */
    @Transient
    public static final Key<XOrder, Clob> TEXT_FILE = newKey();
    /** Binary file */
    @Transient
    public static final Key<XOrder, Blob> BINARY_FILE = newKey();
    /** Reference to Items */
    public static final RelationToMany<XOrder, XItem> ITEMS = newRelation();
    /** Customer */
    @Column(name="fk_customer")
    public static final Key<XOrder, XCustomer> CUSTOMER = newKey();
    //@Column(mandatory=true) public static final Key<XOrder, Integer> NEW_COLUMN = newKey(777);

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

    public String getNote() {
        return get(NOTE);
    }

    public void setNote(String note) {
        set(NOTE, note);
    }

    public Date getCreated() {
        return get(CREATED);
    }

    public void setCreated(Date date) {
        set(CREATED, date);
    }

    public Color getColor() {
        return get(COLOR);
    }

    public void setColor(Color color) {
        set(COLOR, color);
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

    public UjoIterator<XItem> getItems() {
        return get(ITEMS);
    }

    public XCustomer getCustomer() {
        return get(CUSTOMER);
    }

    public void setCustomer(XCustomer customer) {
        set(CUSTOMER, customer);
    }

}
