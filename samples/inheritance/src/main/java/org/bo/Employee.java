/*
 *  Copyright 2009 Paul Ponec
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
package org.bo;

import java.math.BigDecimal;
import org.ujoframework.UjoProperty;
import org.ujoframework.orm.annot.Column;
import org.ujoframework.implementation.orm.OrmTable;
import org.ujoframework.orm.annot.Table;


/** Employee */
@Table(name="usr_employee")
public class Employee extends OrmTable<Employee> implements IUser {

    /** Primary key */
    @Column(pk = true)
    public static final UjoProperty<Employee, Long> id = newProperty(Long.class);
    /** User */
    @Column("user_id")
    public static final UjoProperty<Employee, User> user = newProperty(User.class);
    /** Company */
    public static final UjoProperty<Employee, String> company = newProperty(String.class);
    /** Salary */
    public static final UjoProperty<Employee, BigDecimal> salary = newProperty(BigDecimal.ZERO);

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

    /** Factory method */
    public static Employee newInstance() {
        Employee result = new Employee();
        result.set(user, new User());
        return result;
    }
}
