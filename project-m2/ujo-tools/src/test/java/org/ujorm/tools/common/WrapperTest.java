/*
 * Copyright 2020-2026 Pavel Ponec
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/jdbc/JdbcBuilder.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.common;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Pavel Poone
 */
public class WrapperTest {

    @Test
    public void testHashAndEquals() {
        Person p1 = new Person(1, "A");
        Person p2 = new Person(1, "B");
        Person p3 = new Person(2, "B");
        Person p4 = new Person(2, "B");

        Wrapper<Person> w1 = Wrapper.of(p1, Person::getId, Person::getName);
        Wrapper<Person> w2 = w1.wrap(p2);
        Wrapper<Person> w3 = w1.wrap(p3);
        Wrapper<Person> w4 = w1.wrap(p4);

        assertNotEquals(w1, w2);
        assertNotEquals(w1, w3);
        assertNotEquals(w2, w3);
        assertEquals(w3, w4);
        assertEquals(w4, w4);

        assertNotEquals(w1.hashCode(), w2.hashCode());
        assertNotEquals(w2.hashCode(), w3.hashCode());
        assertEquals(w3.hashCode(), w3.hashCode());
    }

    @Test
    public void testSort() {
        Person p1 = new Person(1, "A");
        Person p2 = new Person(1, "B");
        Person p3 = new Person(2, "B");
        Person p4 = new Person(2, "B");

        Wrapper<Person> w1 = Wrapper.of(p1, Person::getId, Person::getName);
        Wrapper<Person> w2 = w1.wrap(p2);
        Wrapper<Person> w3 = w1.wrap(p3);
        Wrapper<Person> w4 = w1.wrap(p4);

        Person[] result = Stream.of(w4, w3, w2, w1)
                .distinct()
                .sorted()
                .map(t -> t.getValue())
                .toArray(Person[]::new);

        assertEquals(3, result.length);
        assertTrue(w1.compareTo(w2) < 0);
        assertTrue(w2.compareTo(w3) < 0);
        assertEquals(0, w3.compareTo(w4));
        assertEquals(1, result[0].getId().intValue());
        assertEquals(1, result[1].getId().intValue());
        assertEquals(2, result[2].getId().intValue());
    }

    @Test
    public void testNullSort() {
        Person p1 = new Person(null, "A");
        Person p2 = new Person(2, "B");
        Person p3 = new Person(null, "A");

        Wrapper<Person> w1 = Wrapper.of(p1, Person::getId, Person::getName);
        Wrapper<Person> w2 = w1.wrap(p2);
        Wrapper<Person> w3 = w1.wrap(p3);

        assertTrue(w1.compareTo(w2) < 0);
        assertEquals(0, w1.compareTo(w3));
        assertNotEquals(w1, w2);
        assertEquals(w1, w3);
    }

    class Person {
        private final Integer id;
        private final String name;

        public Person(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        public Integer getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Person{" + "id=" + id + ", name=" + name + '}';
        }

        @Override
        public Person clone() throws CloneNotSupportedException {
            return new Person(id, name);
        }
    }
}
