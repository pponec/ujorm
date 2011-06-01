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
import org.ujorm.gxt.client.AbstractCujo;
import org.ujorm.gxt.client.CujoProperty;
import org.ujorm.gxt.client.CujoPropertyList;


/**
 * Simple Employee domain class
 */
public class Employee extends AbstractCujo implements Serializable {

    /** Property List */
    private static final CujoPropertyList pList = list(Employee.class);

    /** Unique key */
    public static final CujoProperty<Employee, Long> ID = pList.newProperty("id", Long.class);
    /** User name */
    public static final CujoProperty<Employee, String> NAME = pList.newProperty("name", String.class);
    /** hourly wage */
    public static final CujoProperty<Employee, Double> WAGE = pList.newPropertyDef("wage", 0.0);
    /** A reference to Company */
    public static final CujoProperty<Employee, Company> COMPANY = pList.newProperty("company", Company.class);

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
    public Double getWage() {
        return get(WAGE);
    }
    public void setWage(Double cache) {
        set(WAGE, cache);
    }
    public Company getAddress() {
        return get(COMPANY);
    }
    public void setAddress(Company address) {
        set(COMPANY, address);
    }
    /** Example of the Composed property */
    public String getCompnyCity() {
        return get(COMPANY.add(Company.CITY));
    }
}
