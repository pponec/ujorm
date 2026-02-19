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

import java.time.LocalDate;
import org.ujorm.Key;
import org.ujorm.core.KeyFactory;
import org.ujorm.extensions.PathProperty;
import org.ujorm.implementation.quick.SmartUjo;

/**
 *
 * @author Pavel Ponec
 */
public class Person<U extends Person> extends SmartUjo<U> {

    protected static final KeyFactory<Person> f = KeyFactory.Builder.get(Person.class);

    public static final Key<Person, String> NAME = f.newKey("name");
    public static final Key<Person, Boolean> MALE = f.newKey("male", false);
    public static final Key<Person, Double> CASH = f.newKey("cash", 0d);
    public static final Key<Person, Person> MOTHER = f.newKey("mother");
    public static final Key<Person, String> ADDRESS = f.newKey("address");
    public static final Key<Person, LocalDate> BORN = f.newKey("born", LocalDate.parse("1970-01-01"));

    static {
        f.lock();
    }

    public void init() {
        set(NAME, "Jack");
        set(MOTHER.add(NAME), "Jane");
        set(MOTHER.add(CASH), 200d);

        String name = get(MOTHER.add(NAME));
        double cash = get(MOTHER.add(CASH));

        System.out.println(name + " " + cash);
    }

    public void init2() {
        set(NAME, "Jack");
        set(CASH, 50d);
        get(MOTHER).set(NAME, "Jackie");
        set(CASH, 10D);

        String name = get(MOTHER.add(NAME));
        double cash = get(MOTHER.add(CASH));

        System.out.println(name + " " + cash);
    }

    public void init3() {
        set(NAME, "Jack");
        set(CASH, 50D);
        set(MOTHER.add(MOTHER).add(CASH), 20D);
        set(MOTHER.add(MOTHER).add(MOTHER).add(NAME), "Jack");
        set(MOTHER.add(MOTHER).add(MOTHER).add(CASH), 1.1);

        String name = get(PathProperty.of(MOTHER, MOTHER, MOTHER, NAME));
        Double cash = get(PathProperty.of(MOTHER, MOTHER, MOTHER, CASH));

        String name2 = get(MOTHER.add(MOTHER).add(NAME));
        Double cash2 = get(MOTHER.add(MOTHER).add(CASH));

        System.out.println(name + " " + cash);
        System.out.println(name2 + " " + cash2);
    }

}
