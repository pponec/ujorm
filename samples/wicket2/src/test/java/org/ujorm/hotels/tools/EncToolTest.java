/*
 * Copyright 2018, Pavel Ponec
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
package org.ujorm.hotels.tools;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.ujorm.hotels.tools.EncTool.getHash;

/**
 * EncTool Test
 * @author Pavel Ponec
 */
public class EncToolTest {

    /** Test of getHash method, of class DbServiceImpl. */
    @Test
    public void testGetHash() {
        System.out.println("getHash");
        //
        String text = "demo";
        long expResult = 7808322132654054122L;
        long result = getHash(text);
        assertEquals(expResult, result);
        //
        String text2 = "test";
        long expResult2 = -3360410906529887736L;
        long result2 = getHash(text2);
        assertEquals(expResult2, result2);
    }
}
