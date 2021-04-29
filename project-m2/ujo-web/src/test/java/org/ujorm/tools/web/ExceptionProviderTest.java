/*
 * Copyright 2021 pavel.
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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;

/**
 *
 * @author pavel
 */
public class ExceptionProviderTest {

    /** Logger */
    private final Logger logger = Logger.getLogger(ElementTest.class.getName());

    /**
     * Test of addSelect method, of class Element.
     */
    @Test
    public void testElementThenCatch() {
        StringBuilder writer = new StringBuilder();
        DefaultHtmlConfig config = HtmlConfig.ofDefault()
                .setTitle("Element-try-catche");

        HtmlElement.of(config, writer).addBody()
                .then(body -> {
                    body.addHeading(config.getTitle());
                })
                .catche(e -> {
                    logger.log(Level.SEVERE, "An error", e);
                });
        String expected = "<h1>Element-try-catche</h1>";
        assertTrue(writer.toString().contains(expected));
    }


    /**
     * Test of addSelect method, of class Element.
     */
    @Test
    public void testElementThenCatch2() {
        StringBuilder writer = new StringBuilder();
        DefaultHtmlConfig config = HtmlConfig.ofDefault()
                .setTitle("Element-try-catche");

        String[] result = {""};
        HtmlElement.of(config, writer).addBody()
                .then(body -> {
                    throw new IllegalArgumentException("test");
                })
                .catche(e -> {
                    result[0] = e.getMessage();
                });
        String expected = "test";
        assertEquals(expected, result[0]);
    }



    /**
     * Test of addSelect method, of class Element.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testElementThenCatch3() {
        StringBuilder writer = new StringBuilder();
        DefaultHtmlConfig config = HtmlConfig.ofDefault()
                .setTitle("Element-try-catche");

        String[] result = {""};
        HtmlElement.of(config, writer).addBody()
                .then(body -> {
                    throw new IllegalArgumentException("test");
                })
                .catche(NullPointerException.class, e -> {
                    result[0] = e.getMessage();
                });
        String expected = "test";
        assertEquals(expected, result[0]);
    }


}
