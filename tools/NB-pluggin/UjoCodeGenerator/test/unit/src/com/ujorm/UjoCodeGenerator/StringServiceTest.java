/*
 *  Copyright 2012 Pavel Ponec
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
package com.ujorm.UjoCodeGenerator;

import com.ujorm.UjoCodeGenerator.bo.PrefixEnum;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * StringService Test
 * @author Pavel Ponec
 */
public class StringServiceTest {

    public StringServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getGetterName method, of class StringService.
     */
    @Test
    public void testGetGetterName() {
        System.out.println("getGetterName");
        final StringService instance = new StringService();
        final PrefixEnum prefix = PrefixEnum.GET;

        String variable = "ID";
        String expResult = "getId";
        String result = instance.getGetterName(prefix, variable);
        assertEquals(expResult, result);
        //
        variable = "id";
        expResult = "getId";
        result = instance.getGetterName(prefix, variable);
        assertEquals(expResult, result);
        //
        variable = "MY_NAME";
        expResult = "getMyName";
        result = instance.getGetterName(prefix, variable);
        assertEquals(expResult, result);
        //
        variable = "myName";
        expResult = "getMyName";
        result = instance.getGetterName(prefix, variable);
        assertEquals(expResult, result);
        //
        variable = "VERY_LONG_NAME";
        expResult = "getVeryLongName";
        result = instance.getGetterName(prefix, variable);
        assertEquals(expResult, result);
        //
        variable = "veryLongName";
        expResult = "getVeryLongName";
        result = instance.getGetterName(prefix, variable);
        assertEquals(expResult, result);
    }

    /**
     * Test of getGetterName method, of class StringService.
     */
    @Test
    public void testGetSetterName() {
        System.out.println("testGetSetterName");
        final StringService instance = new StringService();

        String variable = "ID";
        String expResult = "setId";
        String result = instance.getSetterName(variable);
        assertEquals(expResult, result);
        //
        variable = "id";
        expResult = "setId";
        result = instance.getSetterName(variable);
        assertEquals(expResult, result);
        //
        variable = "MY_NAME";
        expResult = "setMyName";
        result = instance.getSetterName(variable);
        assertEquals(expResult, result);
        //
        variable = "myName";
        expResult = "setMyName";
        result = instance.getSetterName(variable);
        assertEquals(expResult, result);
        //
        variable = "VERY_LONG_NAME";
        expResult = "setVeryLongName";
        result = instance.getSetterName(variable);
        assertEquals(expResult, result);
        //
        variable = "veryLongName";
        expResult = "setVeryLongName";
        result = instance.getSetterName(variable);
        assertEquals(expResult, result);
    }

    /**
     * Test of getGetterName method, of class StringService.
     */
    @Test
    public void testGetParameterName() {
        System.out.println("testGetParameterName");
        final StringService instance = new StringService();

        String variable = "ID";
        String expResult = "id";
        String result = instance.getParameterName(variable);
        assertEquals(expResult, result);
        //
        variable = "id";
        expResult = "id";
        result = instance.getParameterName(variable);
        assertEquals(expResult, result);
        //
        variable = "MY_NAME";
        expResult = "myName";
        result = instance.getParameterName(variable);
        assertEquals(expResult, result);
        //
        variable = "myName";
        expResult = "myName";
        result = instance.getParameterName(variable);
        assertEquals(expResult, result);
        //
        variable = "VERY_LONG_NAME";
        expResult = "veryLongName";
        result = instance.getParameterName(variable);
        assertEquals(expResult, result);
        //
        variable = "veryLongName";
        expResult = "veryLongName";
        result = instance.getParameterName(variable);
        assertEquals(expResult, result);
    }


}
