/*
 *  Copyright 2007-2026 Pavel Ponec
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
package org.ujorm.criterion;

import org.ujorm.Key;
import org.ujorm.core.KeyFactory;

/**
 *
 * @author Pavel Ponec
 */
public class User<U extends User> extends Person<U> {

    protected static final KeyFactory<User> f2 = KeyFactory.Builder.get(User.class, Person.f.getKeys());

    /** Login */
    public static final Key<User, String> LOGIN = f2.newKey("login");
    /** Password */
    public static final Key<User, String> PASSWORD = f2.newKey("password");

    static { f2.lock(); }


    @Override
    public void init() {
        super.init();

        set(LOGIN, "mylogin");
        set(PASSWORD, "mypassword");
    }

}
