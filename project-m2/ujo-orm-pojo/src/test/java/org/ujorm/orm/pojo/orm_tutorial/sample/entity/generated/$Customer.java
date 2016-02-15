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

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.KeyList;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Column;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, how the Keys are created by the KeyFactory.
 * @hidden
 */
public final class $Customer extends OrmTable<$Customer> {
    private static final KeyFactory<$Customer> f = newCamelFactory($Customer.class);

    /** Unique key */
    @Column(pk = true)
    public static final Key<$Customer, Long> ID = f.newKey();
    /** Personal Number */
    public static final Key<$Customer, Integer> PIN = f.newKey();
    /** Firstname */
    @Column(length=50, uniqueIndex="idx_customer_full_name")
    public static final Key<$Customer, String> FIRSTNAME = f.newKey();
    /** Surname */
    @Column(length=50, uniqueIndex="idx_customer_full_name")
    public static final Key<$Customer, String> SURNAME = f.newKey();
    /** Date of creation */
    public static final Key<$Customer, Date> CREATED = f.newKey();
    /** A parent (father or mother) with an alias called {@code "parent"} */
    public static final Key<$Customer, $Customer> PARENT = f.newKeyAlias("customerAlias");

    // Lock the Key factory
    static { f.lock(); }

    /** An optional method for a better performance.
     * @return Return all direct Keys (An implementation from the Ujo API)
     */
    @Override
    public KeyList<$Customer> readKeys() {
        return f.getKeys();
    }

    // --- An optional implementation of commonly used setters and getters ---
    
    public Long getId() {
        return get(ID);
    }

    public void setId(Long _id) {
        set(ID, _id);
    }

    public Integer getPin() {
        return get(PIN);
    }

    public void setPin(Integer _pin) {
        set(PIN, _pin);
    }

}
