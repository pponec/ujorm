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

package org.ujorm.orm.inheritance.sample.bo;

import org.ujorm.UjoProperty;
import org.ujorm.orm.annot.Column;
import org.ujorm.implementation.orm.OrmTable;
import org.ujorm.orm.annot.Table;

/**
 * The column mapping to DB table ORDER (a sample of usage).
 * Note, that the User object has got an collection of Items.
 * @hidden
 */
@Table(name="usr_user")
public class User extends OrmTable<User> {

    /** Primary key */
    @Column(pk=true)
    public static final UjoProperty<User,Long> id = newProperty(Long.class);
    /** Login */
    @Column(uniqueIndex="idx_login")
    public static final UjoProperty<User,String> login = newProperty(String.class);
    /** Description of the Order */
    public static final UjoProperty<User,String> password = newProperty(String.class);
    /** Full name */
    public static final UjoProperty<User,String> name = newProperty(String.class);

    // ----------- Optional getters/setters -----------

    public Long getId() {
        return get(id);
    }

    public void setId(Long _id) {
        set(id, _id);
    }

    public String getLogin() {
        return get(login);
    }

    public void setLogin(String _login) {
        set(login, _login);
    }

    public String getName() {
        return get(name);
    }

    public void setName(String _name) {
        set(name, _name);
    }

    public String getPassword() {
        return get(password);
    }

    public void setPassword(String _password) {
        set(password, _password);
    }



}
