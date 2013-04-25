/*
 *  Copyright 2007-2013 Pavel Ponec
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
import org.ujorm.extensions.PathProperty;
import org.ujorm.implementation.array.ArrayUjoExt;

/**
 *
 * @author Pavel Ponec
 */
public class Person extends ArrayUjoExt<Person> {

    protected static final KeyFactory<Person> f = KeyFactory.Builder.get(Person.class);

    public static final Key<Person, String> NAME = f.newKey("name");
    public static final Key<Person, Boolean> MALE = f.newKey("male", false);
    public static final Key<Person, Double> CASH = f.newKey("cash", 0d);
    public static final Key<Person, Person> MOTHER = f.newKey("mother");
    public static final Key<Person, String> ADDRESS = f.newKey("address");

    static { f.lock(); }


    public void init() {
        set(NAME, "Jack");
        set(MOTHER, new Person());
        set(MOTHER, NAME, "Jane");
        set(MOTHER, CASH, 200d);

        String name = get(MOTHER, NAME);
        double cash = get(MOTHER, CASH);

        System.out.println(name + " " + cash);
    }

    public void init2() {
        set(NAME, "Jack").set(CASH, 50d);
        set(MOTHER, new Person());
        get(MOTHER).set(NAME, "Jackie").set(CASH, 10D);

        String name = get(MOTHER, NAME);
        double cash = get(MOTHER, CASH);

        System.out.println(name + " " + cash);
    }

    public void init3() {
        set(NAME, "Jack").set(CASH, 50D);
        set(MOTHER, new Person());
        set(MOTHER, MOTHER, new Person());
        set(MOTHER, MOTHER, CASH, 20D);
        set(MOTHER, MOTHER, MOTHER, new Person());
        get(MOTHER, MOTHER, MOTHER).set(NAME, "Jack").set(CASH, 10D);
        get(MOTHER, MOTHER, MOTHER).set(NAME, "Jack").set(CASH, 1.1);

        String name = get(PathProperty.newInstance(MOTHER, MOTHER, MOTHER, NAME));
        Double cash = get(PathProperty.newInstance(MOTHER, MOTHER, MOTHER, CASH));

        String name2 = get(MOTHER, MOTHER, NAME);
        Double cash2 = get(MOTHER, MOTHER, CASH);

        System.out.println(name + " " + cash);
    }

    @Override
    public int readPropertyCount() {
        return f.lockAndSize();
    }
}
