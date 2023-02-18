/*
 *  Copyright 2014-2022 Pavel Ponec
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
package samples.values;

import junit.framework.TestCase;
import org.ujorm.Key;
import static samples.values.Person.*;

/**
 * Sample of usage
 * @author Pavel Ponec
 */
public class UsageTest extends org.junit.jupiter.api.Assertions {

    public void testSetGet() {
        Person person = new Person();

        assertNull(person.get(MOTHER));
        assertNull(person.get(MOTHER.add(MOTHER)));

        final Key<Person,String> myName = NAME;
        final Key<Person,String> mothersName = MOTHER.add(NAME);
        final Key<Person,String> grandMothersName = MOTHER.add(MOTHER).add(NAME);

        assertEquals("NAME", myName.toString());
        assertEquals("MOTHER.NAME", mothersName.toString());
        assertEquals("MOTHER.MOTHER.NAME", grandMothersName.toString());

        assertNull(person.get(myName));
        assertNull(person.get(mothersName));   // Mother object is missing !
        assertNull(person.get(grandMothersName)); // Grandmother is missing !

        person.set(myName, "name1");
        person.set(mothersName, "name2");    // Wow, method creates a mother object !
        person.set(grandMothersName, "name3");  // Wow, method creates a grandmother !

        assertEquals("name1", person.get(myName));
        assertEquals("name2", person.get(mothersName));
        assertEquals("name3", person.get(grandMothersName));
    }
}
