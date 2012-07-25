/*
 *  Copyright 2010-2011 Pavel Ponec
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
package org;

import java.util.Date;
import org.ujorm.Key;
import org.ujorm.implementation.quick.QuickUjoMid;

/** Simple Company domain class. */
public class Company extends QuickUjoMid<Company> {

    /** The Primary Key */
    public static final Key<Company, Long> ID = newProperty("id", Long.class);
    /** Company name */
    public static final Key<Company, String> NAME = newProperty("name", String.class);
    /** City name */
    public static final Key<Company, String> CITY = newProperty("city", String.class);
    /** Registration date */
    public static final Key<Company, Date> CREATED = newProperty("created", Date.class);

    // --- An optional implementation of commonly used setters and getters ---

    public Long getId() {
        return get(ID);
    }
    public void setId(Long id) {
        set(ID, id);
    }
    public String getName() {
        return get(NAME);
    }
    public void setName(String name) {
        set(NAME, name);
    }
    public String getCity() {
        return get(CITY);
    }
    public void setCity(String street) {
        set(CITY, street);
    }
    public Date getCreated() {
        return get(CREATED);
    }
    public void setCreated(Date created) {
        set(CREATED, created);
    }
}
