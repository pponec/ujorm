/*
 * Copyright 2020 Pavel Ponec
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
package org.ujorm2.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ujorm2.Key;
import org.ujorm2.doman.Item;
import org.ujorm2.metamodel.DomainModelProvider;
import org.ujorm2.metamodel.MetaItem;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author Pavel Ponec
 */
public class MySampleServiceTest {

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of doOrderAccess method, of class MySampleService.
     */
    @Test
    public void testMetaItem() {
        System.out.println("testMetaItem");

        DomainModelProvider provider = new DomainModelProvider();

        MetaItem<Item> item = provider.item();

        Key<Item, Integer> id = item.id();

        assertEquals("id", id.getName());
    }

    /**
     * Test of doOrderAccess method, of class MySampleService.
     */
    @Test
    public void testDoOrderAccess() {
        System.out.println("doOrderAccess");
        MySampleService instance = instance();
        instance.doOrderAccess();
    }

    /**
     * Test of doItemAccess method, of class MySampleService.
     */
    @Test
    public void testDoItemAccess() {
        System.out.println("doItemAccess");
        MySampleService instance = instance();
        instance.doItemAccess();
    }

    /**
     * Test of doItemCondition method, of class MySampleService.
     */
    @Test
    public void testDoItemCondition() {
        System.out.println("doItemCondition");
        MySampleService instance = instance();
        instance.doItemCondition();
    }

    private MySampleService instance() {
        return new MySampleService();
    }

}
