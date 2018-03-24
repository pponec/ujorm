/*
 *  Copyright 2009-2016 Pavel Ponec
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
import org.ujorm.core.KeyFactory;
import org.ujorm.extensions.types.UnsignedShort;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.TypeService;
import org.ujorm.orm.annot.Column;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, that the Order object has got an collection of Items.
 * @hidden
 */
public class XCustomer extends OrmTable<XCustomer> {
    private static KeyFactory<XCustomer> f = newCamelFactory(XCustomer.class);

    /** Unique key */
    @Column(pk = true, converter=TypeService.class)
    public static final Key<XCustomer, Long> ID = f.newKey();
    /** Personal Number */
    public static final Key<XCustomer, UnsignedShort> PIN = f.newKey();
    /** Surename */
    @Column(uniqueIndex="idx_xcustomer_full_name")
    public static final Key<XCustomer, String> FIRSTNAME = f.newKey();
    /** Lastname */
    @Column(uniqueIndex="idx_xcustomer_full_name")
    public static final Key<XCustomer, String> LASTNAME = f.newKey();
    /** Date of creation */
    public static final Key<XCustomer, Date> CREATED = f.newKey();
    /** Date of creation */
    public static final Key<XCustomer, XCustomer> SUPERIOR = f.newKey("superior");
    /** Date of creation */
    @Column(mandatory = true)
    public static final Key<XCustomer, UnsignedShort> BENEFIT = f.newKeyDefault(UnsignedShort.of(123));

    static {
        f.lock();
    }

    // --- An optional implementation of commonly used setters and getters ---

    /** Unique key */
    public Long getId() {
        return ID.of(this);
    }

    /** Unique key */
    public void setId(Long id) {
        ID.setValue(this, id);
    }

    /** Personal Number */
    public UnsignedShort getPin() {
        return PIN.of(this);
    }

    /** Personal Number */
    public void setPin(UnsignedShort pin) {
        PIN.setValue(this, pin);
    }

    /** Surename */
    public String getFirstname() {
        return FIRSTNAME.of(this);
    }

    /** Surename */
    public void setFirstname(String firstname) {
        FIRSTNAME.setValue(this, firstname);
    }

    /** Lastname */
    public String getLastname() {
        return LASTNAME.of(this);
    }

    /** Lastname */
    public void setLastname(String lastname) {
        LASTNAME.setValue(this, lastname);
    }

    /** Date of creation */
    public Date getCreated() {
        return CREATED.of(this);
    }

    /** Date of creation */
    public void setCreated(Date created) {
        CREATED.setValue(this, created);
    }

    /** Date of creation */
    public XCustomer getSuperior() {
        return SUPERIOR.of(this);
    }

    /** Date of creation */
    public void setSuperior(XCustomer superior) {
        SUPERIOR.setValue(this, superior);
    }

    /** Date of creation */
    public UnsignedShort getBenefit() {
        return BENEFIT.of(this);
    }

    /** Date of creation */
    public void setBenefit(UnsignedShort benefit) {
        BENEFIT.setValue(this, benefit);
    }

}
