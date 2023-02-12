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
package org.ujorm.core;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Pavel Ponec
 */
public class UjoToolsTest extends TestCase {

    public UjoToolsTest(String testName) {
        super(testName);
    }

    /**
     * Test of isFilled method, of class UjoTools.
     */
    public void testIsFilled_CharSequence() {
        System.out.println("isFilled_CharSequence");
        String text = null;
        assertFalse(UjoTools.isFilled(text));
        //
        text = "";
        assertFalse(UjoTools.isFilled(text));
        //
        text = "ABC";
        assertTrue(UjoTools.isFilled(text));
    }

    /**
     * Test of isFilled method, of class UjoTools.
     */
    public void testIsFilled_Collection() {
        System.out.println("tsFilled_Collection");
        List<String> list = null;
        assertFalse(UjoTools.isFilled(list));
        //
        list = new ArrayList<>();
        assertFalse(UjoTools.isFilled(list));
        //
        list.add("ABC");
        assertTrue(UjoTools.isFilled(list));
    }

}
