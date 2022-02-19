/*
 *  Copyright 2010-2022 Pavel Ponec
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
package org.ujorm.ujo_core;

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.quick.SmartUjo;

/** Simple Company domain class. */
public class Company extends SmartUjo<Company> {
    /** Key factory */
    private static final KeyFactory<Company> f = newCamelFactory(Company.class);

    /** The Primary Key */
    public static final Key<Company, Long> ID = f.newKey();
    /** Company name */
    public static final Key<Company, String> NAME = f.newKey();
    /** City name */
    public static final Key<Company, String> CITY = f.newKey();
    /** Registration date */
    public static final Key<Company, Date> CREATED = f.newKey();

    static { f.lock(); } // Lock the factory;

    // --- An optional implementation of commonly used setters and getters ---

    /** The Primary Key */
    public Long getId() {
        return ID.of(this);
    }

    /** The Primary Key */
    public void setId(Long id) {
        Company.ID.setValue(this, id);
    }

    /** Company name */
    public String getName() {
        return NAME.of(this);
    }

    /** Company name */
    public void setName(String name) {
        Company.NAME.setValue(this, name);
    }

    /** City name */
    public String getCity() {
        return CITY.of(this);
    }

    /** City name */
    public void setCity(String city) {
        Company.CITY.setValue(this, city);
    }

    /** Registration date */
    public Date getCreated() {
        return CREATED.of(this);
    }

    /** Registration date */
    public void setCreated(Date created) {
        Company.CREATED.setValue(this, created);
    }
}
