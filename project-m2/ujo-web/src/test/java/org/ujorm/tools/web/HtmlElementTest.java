/*
 * Copyright 2018-2018 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.web;

import org.junit.Test;
import org.ujorm.tools.web.ao.MockServletResponse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Pavel Ponec
 */
public class HtmlElementTest {

    /**
     * Test of getName method, of class HtmlElement.
     */
    @Test
    public void sample() {
        MockServletResponse response = new MockServletResponse();
        try (HtmlElement html = HtmlElement.of(response)) {
            html.addBody().addHeading("Hello!");
        }
        assertTrue(response.toString().contains("<h1>Hello!</h1>"));
    }
}
