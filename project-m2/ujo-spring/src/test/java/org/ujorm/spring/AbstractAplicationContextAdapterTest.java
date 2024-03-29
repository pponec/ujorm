/*
 * Copyright 2012-2022 Pavel Ponec
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
package org.ujorm.spring;
/*
 *  Copyright 2012-2012 Pavel Ponec
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


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.ujorm.spring.AplicationContextAdapter.*;

/**
 * Test for AplicationContextAdapter
 * @author Pavel Ponec
 */
public class AbstractAplicationContextAdapterTest extends org.junit.jupiter.api.Assertions {

    @Autowired
    private AplicationContextAdapter context;

    /**
     * Test of getBean method, of class AbstractAplicationContextAdapter.
     */
    public void compilationTestGetBean() {

        DummySpringService service = context.getBean(dummySpringService);
        DummySpringController controller = context.getBean(springController);

        assertNotNull(service);
        assertNotNull(controller);
    }

    /**
     * A dummy test.
     */
    @Test
    public void testDummy() {
    }

}
