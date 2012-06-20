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
package org.ujorm.orm.bo;

import java.awt.Color;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Date;
import org.ujorm.UjoProperty;
import org.ujorm.core.UjoIterator;
import org.ujorm.core.annot.Transient;
import org.ujorm.orm.DbType;
import org.ujorm.orm.annot.Column;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.implementation.orm.RelationToMany;
import org.ujorm.orm.annot.Table;
import org.ujorm.orm.utility.OrmTools;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, that the XOrder object has got an collection of Items.
 * @hidden
 */
@Table(name = "ord_order")
public class XOrder extends OrmTable<XOrder> {

    public enum State {
        ACTIVE,
        DELETED
    }
    
    /** Unique key */
    @Column(pk = true)
    public static final UjoProperty<XOrder, Long> ID = newProperty();
    /** XOrder state, default is ACTIVE */
    public static final UjoProperty<XOrder, State> STATE = newProperty(State.ACTIVE);
    /** User key */
    public static final UjoProperty<XOrder, Integer> USER_ID = newProperty();
    /** Description of the Order */
    @Column(type = DbType.VARCHAR, name = "DESCR", mandatory = true, index="idx_description") //
    public static final UjoProperty<XOrder, String> DESCR = newProperty();
    /** Favorite Color */
    public static final UjoProperty<XOrder, Color> COLOR = newProperty(Color.WHITE);
    /** Date of creation */
    public static final UjoProperty<XOrder, Date> CREATED = newProperty();
    /** Text file */
    @Transient
    public static final UjoProperty<XOrder, Clob> TEXT_FILE = newProperty();
    /** Binary file */
    @Transient
    public static final UjoProperty<XOrder, Blob> BINARY_FILE = newProperty();
    /** Reference to Items */
    public static final RelationToMany<XOrder, XItem> ITEMS = newRelation();
    /** Customer */
    @Column(name="fk_customer") 
    public static final UjoProperty<XOrder, XCustomer> CUSTOMER = newProperty();
    //@Column(mandatory=true) public static final UjoProperty<XOrder, Integer> NEW_COLUMN = newProperty(777);

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
        return get(DESCR);
    }

    public void setNote(String note) {
        set(DESCR, note);
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
}
