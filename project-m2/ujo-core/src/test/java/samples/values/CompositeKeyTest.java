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

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.junit.jupiter.api.Test;
import org.ujorm.CompositeKey;
import org.ujorm.Key;

/**
 * Test of the CompositeKey iterator:
 * @author Pavel Ponec
 */
public class CompositeKeyTest extends org.junit.jupiter.api.Assertions {

    @Test
    public void testCompositeKey2f() {
        CompositeKey<Person,String> ck = Person.FATHERS_NAME;
        List<Key<?,?>> keys = new ArrayList<>();
        for (Key<?,?> keyItem : ck) {
            keys.add(keyItem);
        }

        assertEquals(2, keys.size());
        assertSame(Person.FATHER, keys.get(0));
        assertSame(Person.NAME, keys.get(1));
    }

    @Test
    public void testCompositeKey3m() {
        CompositeKey<Person,String> ck = Person.GRANDMOTHERS_NAME;
        List<Key> keys = new ArrayList<>();
        for (Key keyItem : ck) {
            keys.add(keyItem);
        }

        assertEquals(3, keys.size());
        assertSame(Person.MOTHER, keys.get(0));
        assertSame(Person.MOTHER, keys.get(1));
        assertSame(Person.NAME, keys.get(2));
    }

}
