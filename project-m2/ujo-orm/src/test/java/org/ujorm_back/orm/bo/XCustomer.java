/*
 *  Copyright 2009-2013 Pavel Ponec
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
package org.ujorm_back.orm.bo;

import java.util.Date;
import org.ujorm.UjoProperty;
import org.ujorm.orm.annot.Column;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.TypeService;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, that the Order object has got an collection of Items.
 * @hidden
 */
public class XCustomer extends OrmTable<XCustomer> {

    /** Unique key */
    @Column(pk = true, converter=TypeService.class)
    public static final UjoProperty<XCustomer, Long> ID = newProperty(Long.class);
    /** Personal Numbr */
    public static final UjoProperty<XCustomer, Integer> PIN = newProperty(Integer.class);
    /** Firstname */
    @Column(uniqueIndex="idx_xcustomer_full_name")
    public static final UjoProperty<XCustomer, String> FIRSTNAME = newProperty(String.class);
    /** Lastanme */
    @Column(uniqueIndex="idx_xcustomer_full_name")
    public static final UjoProperty<XCustomer, String> LASTNAME = newProperty(String.class);
    /** Date of creation */
    public static final UjoProperty<XCustomer, Date> CREATED = newProperty(Date.class);

    // --- An optional implementation of commonly used setters and getters ---
    public Long getId() {
        return get(ID);
    }

    public void setId(Long id) {
        set(ID, id);
    }

    public Integer getUsrId() {
        return get(PIN);
    }

    public void setUsrId(Integer usrId) {
        set(PIN, usrId);
    }

}
