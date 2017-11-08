/*
 * Copyright 2017 Pavel Ponec.
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
package org.ujorm.extensions;

import org.junit.Test;
import org.ujorm.UjoDecorator;
import static junit.framework.TestCase.assertEquals;

/**
 * Testing a native database config class
 * @author Pavel Ponec
 */
public class NativeUjoDecoratorTest {

    /** Test the getDbModel method of the class NativeDbConfig. */
    @Test
    public void testGetKeys() {
        System.out.println("NativeUjoDecorator");
        UjoDecorator<Person> instance = NativeUjoDecorator.of(Person.class);
        assertEquals(Person.class, instance.getDomain().getClass());
        assertEquals(Person.ID, instance.getKeys().getFirstKey());

    }

}
