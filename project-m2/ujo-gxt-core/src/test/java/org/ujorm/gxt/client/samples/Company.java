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
package org.ujorm.gxt.client.samples;

import java.io.Serializable;
import java.util.Date;
import org.ujorm.gxt.client.AbstractCujo;
import org.ujorm.gxt.client.CujoProperty;
import org.ujorm.gxt.client.CujoPropertyList;

/**
 * Simple Company domain class.
 */
public class Company extends AbstractCujo implements Serializable {

    /** Property List */
    private static final CujoPropertyList pList = list(Company.class);

    /** Primary Key */
    public static final CujoProperty<Company,Long> ID = pList.newProperty("id", Long.class);

    public static final CujoProperty<Company, String> NAME = pList.newProperty("name", String.class);
    /** City name */
    public static final CujoProperty<Company, String> CITY = pList.newProperty("city", String.class);
    /** Registration date */
    public static final CujoProperty<Company, Date> CREATED = pList.newProperty("created", Date.class);

    @Override
    public CujoPropertyList readProperties() {
        return pList;
    }

    // --- An optional implementation of commonly used setters and getters ---

    @Override
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
