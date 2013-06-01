/*
 * Copyright 2013 No company.
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
package org.ujorm.hotels.services.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.ujorm.hotels.services.DbService;
import static org.junit.Assert.*;

/**
 *
 * @author Pavel Ponec
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext.xml"})
public class DbServiceImplTest {
    
    /** Has service */
    @Autowired
    private DbService service;
    
    /**
     * Test of getHash method, of class DbServiceImpl.
     */
    @Test
    public void testGetHash() {
        System.out.println("getHash");
        //
        String text = "demo";
        long expResult = 7808322132654054122L;
        long result = service.getHash(text);
        assertEquals(expResult, result);
        //
        String text2 = "test";
        long expResult2 = -3360410906529887736L;
        long result2 = service.getHash(text2);
        assertEquals(expResult2, result2);
    }
}
