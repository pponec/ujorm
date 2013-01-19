/*
 *  Copyright 2010-2013 Pavel Ponec
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

import org.ujorm.Key;
import org.ujorm.implementation.quick.QuickUjoMid;

/** Simple Employee domain class */
public class Employee extends QuickUjoMid<Employee> {

    /** Unique key */
    public static final Key<Employee, Long> ID = newKey("id");
    /** User first name, where the default value is {@code null} */
    public static final Key<Employee, String> NAME = newKey("name");
    /** Hourly wage with the default value: 0.0 */
    public static final Key<Employee, Double> WAGE = newKey("wage", 0.0);
    /** A reference to Company */
    public static final Key<Employee, Company> COMPANY = newKey("company");

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
