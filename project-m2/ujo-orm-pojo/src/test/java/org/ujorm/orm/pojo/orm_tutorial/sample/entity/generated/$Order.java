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
package org.ujorm.orm.pojo.orm_tutorial.sample.entity.generated;

import java.sql.Blob;
import java.sql.Clob;
import java.util.Date;
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
import org.ujorm.orm.utility.OrmTools;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, that the $Order object has got an collection of Items.
 * @hidden
 */
@Comment("Order table for registering the 'order items'")
@Table(name = "ord_order")
public final class $Order extends OrmTable<$Order> {

    /** Store the value like VARCHAR. */
    public enum State implements StringWrapper {
        ACTIVE,
        DELETED;

        @Override public String exportToString() {
            return name().substring(0, 1);
        }
    }

    /** The Key Factory */
    private static final OrmKeyFactory<$Order> f = newFactory($Order.class);

    /** The Primary Key */
    @Comment("The Primary Key")
    @Column(pk = true)
    public static final Key<$Order, Long> ID = f.newKey();
    /** $Order STATE, default is ACTIVE */
    @Comment("Order state, default value is ACTIVE")
    public static final Key<$Order, State> STATE = f.newKeyDefault(State.ACTIVE);
    /** User key */
    public static final Key<$Order, Integer> USER_ID = f.newKey();
    /** Description of the $Order */
    @Comment("Description of the Order")
    @Column(type = DbType.VARCHAR, name = "NOTE", mandatory = true)
    public static final Key<$Order, String> NOTE = f.newKey();
    /** Date of creation */
    public static final Key<$Order, Date> CREATED = f.newKey();
    /** Text file */
    @Transient
    public static final Key<$Order, Clob> TEXT_FILE = f.newKey();
    /** Binary file */
    @Transient
    public static final Key<$Order, Blob> BINARY_FILE = f.newKey();
    /** Reference to Items */
    public static final RelationToMany<$Order, $Item> ITEMS = f.newRelation();
    /** $Customer */
    @Column(name="fk_customer") public static final Key<$Order, $Customer> CUSTOMER = f.newKey();
    @Column(mandatory=true) public static final Key<$Order, Integer> NEW_COLUMN = f.newKeyDefault(777);

    // Lock the factory:
    static {  f.lock(); }

    // --- Constructors ---

    public $Order() {
    }

    public $Order(Long id) {
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

    public String getTextFile() {
        return OrmTools.getClobString(get(TEXT_FILE));
    }

    public void setTextFile(String _largeFile) {
        set(TEXT_FILE, OrmTools.createClob(_largeFile));
    }

    public byte[] getBinaryFile() {
        return OrmTools.getBlobBytes(get(BINARY_FILE));
    }

    public void setBinaryFile(byte[] _binaryFile) {
        set(BINARY_FILE, OrmTools.createBlob(_binaryFile));
    }

    public UjoIterator<$Item> getItems() {
        return get(ITEMS);
    }
}
