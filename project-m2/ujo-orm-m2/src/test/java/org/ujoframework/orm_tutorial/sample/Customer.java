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

import java.util.Date;
import org.ujoframework.UjoProperty;
import org.ujoframework.orm.annot.Column;
import org.ujoframework.implementation.orm.OrmTable;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, that the Order object has got an collection of Items.
 * @hidden
 */
public class Customer extends OrmTable<Customer> {

    /** Unique key */
    @Column(pk = true)
    public static final UjoProperty<Customer, Long> id = newProperty(Long.class);
    /** Personal Numbr */
    public static final UjoProperty<Customer, Integer> pin = newProperty(Integer.class);
    /** Firstname */
    @Column(length=50, uniqueIndex="idx_customer_full_name")
    public static final UjoProperty<Customer, String> surename = newProperty(String.class);
    /** Lastname */
    @Column(length=50, uniqueIndex="idx_customer_full_name")
    public static final UjoProperty<Customer, String> lastname = newProperty(String.class);
    /** Date of creation */
    public static final UjoProperty<Customer, Date> created = newProperty(Date.class);

    // --- An optional implementation of commonly used setters and getters ---
    public Long getId() {
        return get(id);
    }

    public void setId(Long _id) {
        set(id, _id);
    }

    public Integer getPin() {
        return get(pin);
    }

    public void setPin(Integer _pin) {
        set(pin, _pin);
    }

}
