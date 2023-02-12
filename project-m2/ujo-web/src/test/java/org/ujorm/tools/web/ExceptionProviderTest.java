/*
 * Copyright 2021-2022 Pavel Ponec.
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.junit.Assert.*;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;

/**
 *
 * @author pavel
 */
public class ExceptionProviderTest {

    /**
     * Logger
     */
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
                .next(body -> {
                    body.addHeading(config.getTitle());
                })
                .catchEx(e -> {
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
                .catchEx(e -> {
                    result[0] = e.getMessage();
                });
        String expected = "test";
        assertEquals(expected, result[0]);
    }

    /**
     * Test of addSelect method, of class Element.
     */
    @Test
    public void testElementThenCatch3a() {
        StringBuilder writer = new StringBuilder();
        DefaultHtmlConfig config = HtmlConfig.ofDefault()
                .setTitle("Element-try-catche");

        String[] result = {""};
        HtmlElement.of(config, writer).addBody()
                .then(body -> {
                    throw new IllegalArgumentException("test");
                })
                .catchEx(NullPointerException.class, e -> {
                    result[0] = e.getMessage();
                });
        String expected = "test";
        assertEquals(expected, result[0]);
    }
    /**
     * Test of addSelect method, of class Element.
     */
    @Test()
    public void testElementThenCatch3b() {
        StringBuilder writer = new StringBuilder();
        DefaultHtmlConfig config = HtmlConfig.ofDefault()
                .setTitle("Element-try-catche");

        String[] result = {""};
        HtmlElement.of(config, writer).addBody()
                .then(body -> {
                    throw new NullPointerException("test");
                })
                .catchEx(NullPointerException.class, e -> {
                    result[0] = e.getMessage();
                });
        String expected = "test";
        assertEquals(expected, result[0]);
    }

    /**
     * Test of addSelect method, of class Element.
     */
    @Test
    public void testElementThenCatch4a() {
        Assertions.assertThrows(OutOfMemoryError.class, -> {
            StringBuilder writer = new StringBuilder();
            DefaultHtmlConfig config = HtmlConfig.ofDefault()
                    .setTitle("Element-try-catche");

            String[] result = {""};
            HtmlElement.of(config, writer).addBody()
                    .then(body -> {
                        throw new OutOfMemoryError("test");
                    })
                    .catchEx(NullPointerException.class, e -> {
                        result[0] = e.getMessage();
                    });
            String expected = "test";
            assertEquals(expected, result[0]);
        });
    }

    /**
     * Test of addSelect method, of class Element.
     */
    @Test
    public void testElementThenCatch4b() {
        StringBuilder writer = new StringBuilder();
        DefaultHtmlConfig config = HtmlConfig.ofDefault()
                .setTitle("Element-try-catche");

        String[] result = {""};
        HtmlElement.of(config, writer).addBody()
                .next(body -> {
                    throw new OutOfMemoryError("test");
                })
                .catchEx(OutOfMemoryError.class, e -> {
                    result[0] = e.getMessage();
                });
        String expected = "test";
        assertEquals(expected, result[0]);
    }

}
