/*
 * Copyright 2021 pavel.
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
import static org.junit.Assert.*;
import org.junit.Test;

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
        
        Function<Person, String> nameProvider = Joinable.of(Person::getParent).add(Person::getName);
        
        
        Joinable<Person, String> ss = new Joinable<Person, String>() {
            @Override
            public String apply(Person person) {
                return person.getName();
            }
        };
        
        assertEquals("Name-1", ss.apply(p1));        
        assertEquals("Name-1", Joinable.of(Person::getParent).apply(p1));
        
        assertEquals("Name-1", Joinable.of(Person::getParent).apply(p1));
        assertEquals("Name-2", nameProvider.apply(p1));
        assertEquals("Name-3", nameProvider.apply(p2));
        assertEquals(null, nameProvider.apply(p3));
    }

    class Person {

        final Integer id;
        final String name;
        final Person parent;

        public Person(Integer id, String name, Person parent) {
            this.id = id;
            this.name = name;
            this.parent = parent;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Person getParent() {
            return parent;
        }
    }
}
