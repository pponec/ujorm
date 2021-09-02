/*
 * Copyright 2019 pavel.
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
package org.ujorm2;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ujorm2.metamodel.ModelProvider;

/**
 *
 * @author Pavel Ponec
 */
public class UjoTest {

    public UjoTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of readValue method, of class Ujo.
     */
    @Test
    public void testModelContext() {
        System.out.println("ModelContext");

        final ModelProvider context = new ModelProvider();

        Assert.assertNotNull(context.item());
        Assert.assertNotNull(context.item().id());
        Assert.assertTrue(context.item().id().isDomainOf(Integer.class));



        // TODO
    }

}
