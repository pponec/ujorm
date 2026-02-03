/*
 *  Copyright 2020-2026 Pavel Ponec.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.ujorm.orm.utility;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author pavel
 */
public class OrmToolsTest {

    /**
     * Test of toCharArray method, of class OrmTools.
     */
    @Test
    public void testToCharArray() {
        System.out.println("toCharArray");
        StringBuilder baos = new StringBuilder();
        baos.append("0123456789");
        char[] result = OrmTools.toCharArray(baos);

        assertEquals(baos.length(), result.length);
        assertEquals(baos.charAt(0), result[0]);
        assertEquals(baos.charAt(1), result[1]);
        assertEquals(baos.charAt(2), result[2]);
        assertEquals(baos.charAt(3), result[3]);
        assertEquals(baos.charAt(4), result[4]);
        assertEquals(baos.charAt(5), result[5]);
        assertEquals(baos.charAt(6), result[6]);
        assertEquals(baos.charAt(7), result[7]);
        assertEquals(baos.charAt(8), result[8]);
        assertEquals(baos.charAt(9), result[9]);

    }

}
