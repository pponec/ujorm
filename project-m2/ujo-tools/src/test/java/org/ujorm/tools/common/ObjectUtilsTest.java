/*
 * Copyright 2020-2020 Pavel Ponec
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

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Pavel Ponec
 */
public class ObjectUtilsTest {

    /**
     * Test of iof method, of class ObjectUtils.
     */
    @Test
    public void testIof() {
        System.out.println("iof");

        int expResult = 3;
        Object input = "ABC";
        int result = ObjectUtils.iof(input, String.class, v -> v.length()).orElse(0);

        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class ObjectUtils.
     */
    @Test
    public void testCheck() {
        System.out.println("check");
        boolean expResult = true;
        Object input = "ABC";
        boolean result = ObjectUtils.check(input, String.class, v -> v.length() == 3);
        assertEquals(expResult, result);

    }

}
