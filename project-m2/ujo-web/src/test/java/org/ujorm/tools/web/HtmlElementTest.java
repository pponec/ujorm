/*
 * Copyright 2018-2026 Pavel Ponec, https://github.com/pponec
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
import org.ujorm.tools.web.request.HttpContext;
import org.ujorm.tools.xml.builder.XmlBuilder;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;
import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        Appendable response = new StringBuilder();
        try (HtmlElement html = HtmlElement.of("Title", response)) {
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

        try (HtmlElement html = HtmlElement.of(writer, config)) {
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
        try (HtmlElement html = HtmlElement.of(writer, config)) {
            html.original().addText("Hello!");
        }
        assertEquals("Hello!", writer.toString());

        writer.setLength(0);
        try (HtmlElement html = HtmlElement.of(writer, config)) {
            html.original().addHeading("Hello!");
        }
        assertEquals("<h1>Hello!</h1>", writer.toString());

        writer.setLength(0);
        try (HtmlElement html = HtmlElement.of(writer, config)) {
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
        try (HtmlElement html = HtmlElement.of(writer, config)) {
            html.original().addText("Hello!");
        }
        assertEquals("Hello!", writer.toString());

        writer.setLength(0);
        try (HtmlElement html = HtmlElement.of(writer, config)) {
            html.original().addHeading("Hello!");
        }
        assertEquals("<h1>Hello!</h1>", writer.toString());

        writer.setLength(0);
        try (HtmlElement html = HtmlElement.of(writer, config)) {
            html.original().setClass("error");
            html.original().addHeading("Hello!");
        }
        assertEquals("<h1>Hello!</h1>", writer.toString());
    }

    /**
     * Test the assigning an attribute to the root element (html).
     */
    @Test
    public void sample_2c() {
        var writer = HttpContext.of();
        try (var html = HtmlElement.niceOf("Hello", writer)) {
            html.setAttribute(Html.A_LANG, "cs");
            try (var body = html.addBody()) {
                body.addHeading(html.getTitle());
            }
        }
        var html = writer.toString();
        assertTrue(html.contains("<html lang=\"cs\">"));
        assertTrue(html.contains("<title>Hello</title>"));
        assertTrue(html.contains("<h1>Hello</h1>"));
    }

    /**
     * Test the assigning an attribute to the root element (html).
     */
    @Test
    public void sample_2d_table() {
        final var data = new Object[][]{{1000, 2000.2f, 3000.3d, new BigDecimal("4000.1415")}};
        var writer = HttpContext.of();
        try (var html = HtmlElement.niceOf("Table", writer)) {
            html.getBody().addHeading(html.getTitle());
            html.getBody().addTable(data);
        }
        var html = writer.toString();
        assertTrue(html.contains("<title>Table</title>"));
        assertTrue(html.contains("2000.2"));
        assertTrue(html.contains("4000.1415"));
    }

    /**
     * Test the assigning an attribute to the root element (html).
     */
    @Test
    public void sample_2e_formatNumbers() {
        var data = new Object[][]{{1000, 2000.2f, 3000.3d, new BigDecimal("4000.1415")}};
        var writer = HttpContext.of();
        try (var html = HtmlElement.niceNumberOf("Table", writer)) {
            html.getBody().addHeading(html.getTitle());
            html.getBody().addTable(data);
        }
        var html = writer.toString();
        assertTrue(html.contains("<title>Table</title>"));
        assertTrue(html.contains("<td>2&#8239;000.2</td>"));
        assertTrue(html.contains("<td>4&#8239;000.1415</td>"));
    }

    /**
     * Test the assigning an attribute to the root element (html).
     */
    @Test
    public void sample_2f_numAtrrib() {
        final var inputValue = 1_000;
        final var lengthAttrib = 2_000;
        final var textValue = 3_000;
        final var nullValue = (Integer) null;
        var writer = HttpContext.of();
        try (var html = HtmlElement.niceNumberOf("Table", writer)) {
            try (var body = html.getBody()) {
                body.addTextInput()
                        .setName("name")
                        .setAttribute(Html.A_MAXLENGTH, lengthAttrib)
                        .setValue(inputValue);
                body.addDiv().addText(textValue);
                body.addParagraph().addText(nullValue);
            }
        }
        var html = writer.toString();
        assertTrue(html.contains("<title>Table</title>"));
        assertTrue(html.contains("1000"));
        assertTrue(html.contains("2000"));
        assertTrue(html.contains("3&#8239;000"));
        assertTrue(html.contains("<p></p>"));
    }

    /**
     * Test of getName method, of class HtmlElement.
     */
    @Test
    public void sample_3_javascript() {
        DefaultHtmlConfig config = HtmlConfig.ofDefault();
        StringBuilder writer = new StringBuilder();
        try (HtmlElement html = HtmlElement.of(writer, config)) {
            html.addJavascriptLink(false, "./prettify.js");
            html.addCssLink("main.css");
            html.getBody().addText("text");
        }
        String result = substring(writer.toString(), "</title>", "<link");
        String expected = "<script src='./prettify.js'></script>"
                .replace('\'', '"');
        assertEquals(expected, result);
    }

    private static String substring(String body, String beg, String end) {
        String regex = String.format("%s(.*?)%s",
                Pattern.quote(beg),
                Pattern.quote(end));
        Matcher matcher = Pattern
                .compile(regex, Pattern.DOTALL)
                .matcher(String.valueOf(body));
        return matcher.find()
                ? matcher.group(1).trim()
                : "";
    }
}
