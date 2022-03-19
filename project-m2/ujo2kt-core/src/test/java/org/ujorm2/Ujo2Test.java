/*
 * Copyright 2019-2022 Pavel Ponec.
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

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.ujorm2.metamodel.DomainModelProvider;
import org.ujorm2.service.MySampleService;

/**
 *
 * @author Pavel Ponec
 */
public class Ujo2Test {
    
    /** The main examples & test */
    @Test
    void mainUjo2Test() {    
        try {
            MySampleService instance = new MySampleService();

            instance.doOrderAccess();
            instance.doItemAccess();
            instance.doItemCondition();
        } catch (RuntimeException | Error e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Test of readValue method, of class Ujo.
     */
    @Test
    void testModelContext() {
        System.out.println("ModelContext");

        final DomainModelProvider context = new DomainModelProvider();

        assertNotNull(context.item());
        assertNotNull(context.item().id());
        assertTrue(context.item().id().isDomainOf(Integer.class));

        // TODO
    }

}
