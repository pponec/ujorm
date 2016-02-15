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
package org.ujorm.orm.pojo.orm_tutorial.sample.entity;

import java.util.Date;
import org.ujorm.orm.annot.Column;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, how the Keys are created by the KeyFactory.
 * @hidden
 */
public class Customer {

    /** Unique key */
    @Column(pk = true)
    private Long id;
    /** Personal Number */
    private Integer pin;
    /** Firstname */
    @Column(length=50, uniqueIndex="idx_customer_full_name")
    private String firstname;
    /** Surname */
    @Column(length=50, uniqueIndex="idx_customer_full_name")
    private String surname;
    /** Date of creation */
    private Date created;
    /** A parent (father or mother) with an alias called {@code "parent"} */
    private Customer parent ; // f.newKeyAlias("customerAlias");

    // --- Generated setters and getters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getPin() {
        return pin;
    }

    public void setPin(Integer pin) {
        this.pin = pin;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Customer getParent() {
        return parent;
    }

    public void setParent(Customer parent) {
        this.parent = parent;
    }

}
