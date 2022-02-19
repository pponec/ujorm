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

import java.io.Serializable;
import java.util.Date;
import org.ujorm.Key;
import org.ujorm.orm.DbType;
import org.ujorm.orm.annot.Column;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Table;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, that the Order object has got an collection of Items.
 * @hidden
 */
@Table(name="ord_order")
public class OrderSample extends OrmTable<OrderSample> implements Serializable {

    public enum State {
        ACTIVE,
        DELETED
    }

    /** Unique key */
    @Column(pk=true)
    public static final Key<OrderSample,Long> ID = newKey("id");
    /** Order state, default is ACTIVE */
    public static final Key<OrderSample,State> STATE = newKey("state", State.ACTIVE);
    /** User key */
    public static final Key<OrderSample,Integer> USER_ID = newKey("usrId");
    /** Description of the Order */
    @Column(type=DbType.VARCHAR, name="DESCR", mandatory=true)
    public static final Key<OrderSample,String> NOTE = newKey("description");
    /** Date of creation */
    public static final Key<OrderSample,Date> CREATED = newKey("created");
    

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
    public Date getDate() {
        return get(CREATED);
    }
    public void setDate(Date date) {
        set(CREATED, date);
    }

    public State getState() {
        return get(STATE);
    }
    public void setState(State date) {
        set(STATE, date);
    }

}
