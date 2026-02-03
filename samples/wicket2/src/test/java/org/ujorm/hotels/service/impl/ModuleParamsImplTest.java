/*
 *  Copyright 2014-2026 Pavel Ponec
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
package org.ujorm.hotels.service.impl;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ujorm.hotels.entity.Customer;
import org.ujorm.hotels.service.AuthService;
import org.ujorm.hotels.service.ParamService;
import org.ujorm.hotels.service.param.ApplicationParams;

import jakarta.inject.Inject;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ModuleParamsImplTest
 * @author Pavel Ponec
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = SpringContext.class)
//@WebAppConfiguration
public class ModuleParamsImplTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModuleParamsImplTest.class);

    @Inject
    private ApplicationParams appParams;
    @Inject
    @Named(ParamService.CACHED)
    private ParamService paramService;
    @Inject
    private AuthService authService;
    /** Wicket tester  */
    protected WicketTester tester;

    @BeforeEach
    public void before() {
        tester = new WicketTester();
    }

    /** Test of readValue method, of class AbstractModuleParams. */
    @Disabled
    @Test
    public void testReadValue() {
        LOGGER.info("testReadValue");
        assertEquals(ApplicationParams.TEST1.getDefault(), appParams.getTest1());
        //
        boolean logged = authService.authenticate(getLoggedUser());
        assertTrue(logged);
    }

    /** Logged User */
    private Customer getLoggedUser() {
        Customer result = new Customer();
        result.setLogin("test");
        result.setPassword("test");
        result.setEmail("test@test.tst");
        return result;
    }

}
