/*
 * Copyright 2021-2022 Pavel Ponec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.common;

import java.util.function.Function;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Pavel Ponec
 */
public class JoinableTest {

    @Test
    public void testOf() {
        Person p3 = new Person(3, "Name-3", null);
        Person p2 = new Person(2, "Name-2", p3);
        Person p1 = new Person(1, "Name-1", p2);
        Person p0 = null;
        
        Function<Person, String> nameProvider1 = Joinable.of(Person::getName);
        Function<Person, String> nameProvider2 = Joinable.of(Person::getBoss).add(Person::getName);
        Function<Person, String> nameProvider3 = Joinable.of(Person::getBoss).add(Person::getBoss).add(Person::getName);
        
        assertEquals("Name-1", nameProvider1.apply(p1));
        assertEquals("Name-2", nameProvider2.apply(p1));
        assertEquals("Name-3", nameProvider3.apply(p1));
        
        assertEquals(null, nameProvider1.apply(p0));
        assertEquals(null, nameProvider2.apply(p3));
        assertEquals(null, nameProvider3.apply(p3));
    }

    final static class Person {

        final Integer id;
        final String name;
        final Person boss;

        public Person(Integer id, String name, Person boss) {
            this.id = id;
            this.name = name;
            this.boss = boss;
        }

        Integer getId() {
            return id;
        }

        String getName() {
            return name;
        }

        Person getBoss() {
            return boss;
        }
    }
}
