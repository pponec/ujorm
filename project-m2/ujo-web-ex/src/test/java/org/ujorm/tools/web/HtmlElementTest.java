/*
 * Copyright 2018-2022 Pavel Ponec, https://github.com/pponec
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

import org.junit.jupiter.api.Test;
import org.ujorm.tools.web.ao.ServletResponse;
import org.ujorm.tools.xml.builder.XmlBuilder;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;
import static org.junit.jupiter.api.Assertions.*;

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
        ServletResponse response = new ServletResponse();
        try (HtmlElement html = HtmlElement.of(response)) {
            html.addBody().addHeading("Hello!");
        }
        assertTrue(response.toString().contains("<h1>Hello!</h1>"));
    }

    /**
     * Test of getName method, of class HtmlElement.
     */
    @Test
    public void sample_1() {
        StringBuilder writer = new StringBuilder();
        DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setRawHedaderCode("<meta name='description' content='Powered by Ujorm'>");

        try (HtmlElement html = HtmlElement.of(config, writer)) {
            html.addBody().addHeading("Hello!");
        }
        String result = String.join("\n",
                "<!DOCTYPE html>",
                "<html lang=\"en\">",
                "<head>",
                "<meta charset=\"UTF-8\"/>",
                "<title>Demo</title>",
                "<meta name='description' content='Powered by Ujorm'></head>",
                "<body>",
                "<h1>Hello!</h1></body></html>");
        assertEquals(result, writer.toString());
    }

    /**
     * Test of getName method, of class HtmlElement.
     */
    @Test
    public void sample_2a() {
        DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setRootElementName(XmlBuilder.HIDDEN_NAME);
        config.setHtmlHeader(false);
        config.setDoctype("");
        config.setDocumentObjectModel(true);

        StringBuilder writer = new StringBuilder();
        try (HtmlElement html = HtmlElement.of(config, writer)) {
            html.original().addText("Hello!");
        }
        assertEquals("Hello!", writer.toString());

        writer.setLength(0);
        try (HtmlElement html = HtmlElement.of(config, writer)) {
            html.original().addHeading("Hello!");
        }
        assertEquals("<h1>Hello!</h1>", writer.toString());

        writer.setLength(0);
        try (HtmlElement html = HtmlElement.of(config, writer)) {
            html.original().setClass("error");
            html.original().addHeading("Hello!");
        }
        assertEquals("<h1>Hello!</h1>", writer.toString());
    }

     /**
     * Test of getName method, of class HtmlElement.
     */
    @Test
    public void sample_2b() {
        DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setRootElementName(XmlBuilder.HIDDEN_NAME);
        config.setHtmlHeader(false);
        config.setDoctype("");

        StringBuilder writer = new StringBuilder();
        try (HtmlElement html = HtmlElement.of(config, writer)) {
            html.original().addText("Hello!");
        }
        assertEquals("Hello!", writer.toString());

        writer.setLength(0);
        try (HtmlElement html = HtmlElement.of(config, writer)) {
            html.original().addHeading("Hello!");
        }
        assertEquals("<h1>Hello!</h1>", writer.toString());

        writer.setLength(0);
        try (HtmlElement html = HtmlElement.of(config, writer)) {
            html.original().setClass("error");
            html.original().addHeading("Hello!");
        }
        assertEquals("<h1>Hello!</h1>", writer.toString());
    }
}
