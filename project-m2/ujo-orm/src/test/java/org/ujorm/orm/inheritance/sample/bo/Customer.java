/*
 *  Copyright 2009 Pavel Ponec
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
package org.ujorm.orm.inheritance.sample.bo;

import org.ujorm.Key;
import org.ujorm.orm.annot.Column;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Table;

/** Customer extends User */
@Table(name="usr_customer")
public class Customer extends OrmTable<Customer> implements ICustomer {

    /** Primary key */
    @Column(pk=true, name="id")
    public static final Key<Customer, User> user = newKey();
    /** Company */
    public static final Key<Customer, String> company = newKey();
    /** Discount [%] */
    public static final Key<Customer, Integer> discount = newKeyDefault(0);


    // -------- Setters and getters ---------

    public User getUser() {
        return get(user);
    }

    @Override
    public String getLogin() {
        return getUser().getLogin();
    }

    @Override
    public void setLogin(String login) {
        getUser().setLogin(login);
    }

    @Override
    public String getName() {
        return getUser().getName();
    }

    @Override
    public void setName(String name) {
        getUser().setName(name);
    }

    @Override
    public String getPassword() {
        return getUser().getPassword();
    }

    @Override
    public void setPassword(String password) {
        getUser().setPassword(password);
    }

    @Override
    public String getCompany() {
        return get(company);
    }

    @Override
    public void setCompany(String _company) {
        set(company, _company);
    }

    @Override
    public int getDiscount() {
        return get(discount);
    }

    @Override
    public void setDiscount(int _discount) {
        set(discount, _discount);
    }

    // -------- Static methods ---------

    /** Factory method */
    public static Customer newInstance() {
        Customer result = new Customer();
        result.set(user, new User());
        return result;
    }

}
