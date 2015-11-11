/*
 *  Copyright 2007-2014 Pavel Ponec
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


package org.ujorm.extensions;

import org.ujorm.Key;
import org.ujorm.ListKey;
import org.ujorm.core.KeyFactory;
import org.ujorm.implementation.quick.SmartUjo;
import java.util.Date;

/**
 * Person BO
 * @author Pavel Ponec
 */
public class Person extends SmartUjo<Person> {
    private static final KeyFactory<Person> f = newFactory(Person.class);

    public static final Key<Person, Integer> ID = f.newKey("id");
    public static final Key<Person, Date> CREATED = f.newKey("created", new Date());
    public static final ListKey<Person, Person> PERS = f.newListKey("person");

    static {
        f.lock();
    }

    /** Constructor */
    public Person(Integer id) {
        ID.setValue(this, id);
    }

}
