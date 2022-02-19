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

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Column;

/**
 * The column mapping to DB table ITEM (a sample of usage).
 * Note, that the XItem object have got a reference to a XOrder object.
 * @hidden
 * @Table=bo_item
 */
public class XItem extends OrmTable<XItem> {

    /** Unique key */
    @Column(pk = true)
    public static final Key<XItem,Long> ID = newKey();
    /** User key */
    public static final Key<XItem,Integer> USER_ID = newKey();
    /** Description of XItem */
    public static final Key<XItem,String> NOTE = newKey();
    /** A reference to common XOrder */
    @Column(name="fk_order")
    public static final Key<XItem,XOrder> ORDER = newKey();
    //
    /** A composed (indirect) key provides a 'created' attribute of the XOrder */
    public static final Key<XItem,Date> $ORDER_DATE = XItem.ORDER.add(XOrder.CREATED);
    /** A composed (indirect) key provides an 'name' attribute of the XOrder */
    public static final Key<XItem,String> $ORDER_NOTE = XItem.ORDER.add(XOrder.NOTE);
    /** A composed (indirect) key provides an 'name' attribute of the XOrder */
    public static final Key<XItem,String> $CUST_FIRSTNAME = XItem.ORDER.add(XOrder.CUSTOMER).add(XCustomer.FIRSTNAME);

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
    public XOrder getOrder() {
        return get(ORDER);
    }
    public void setOrder(XOrder order) {
        set(ORDER, order);
    }

    /** Example of the composed PATH key */
    public Date getOrderDate() {
        // An alternative solution for: getOrder().getCreated();
        return get($ORDER_DATE);
    }


}
