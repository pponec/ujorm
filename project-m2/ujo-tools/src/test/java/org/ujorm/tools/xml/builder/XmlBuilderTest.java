/*
 * Copyright 2018 Pavel Ponec
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
package org.ujorm.tools.xml.builder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.ujorm.tools.xml.Html;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

/**
 * A test of the XmlNode class
 * @author Pavel Ponec
 */
public class XmlBuilderTest {

    @Test
    public void testXmlBuilding() throws IOException {
        System.out.println("XmlBuilding");

        final XmlPrinter writer = XmlPrinter.forXml();
        try (XmlBuilder root = writer.createElement("root")) {
            root.addElement("childA")
                    .setAttrib("x", 1)
                    .setAttrib("y", 2);
            root.addElement("childB")
                    .setAttrib("x", 3)
                    .setAttrib("y", 4)
                    .setAttrib("z", "<'&\">")
                    .addText("A text message <'&\">");
            root.addRawText("\n<rawXml/>\n");
         // root.addCDATA("A character data <'&\">");
        }

        String result = writer.toString();
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "\n<root>"
                + "\n<childA x=\"1\" y=\"2\"/>"
                + "\n<childB x=\"3\" y=\"4\" z=\"&lt;'&amp;&quot;&gt;\">A text message &lt;'&amp;\"&gt;</childB>"
                + "\n<rawXml/>"
           //   + "\n<![CDATA[A character data <'&\">]]>"
                + "\n</root>";
        assertEquals(expected, result);
    }

    @Test
    public void testXmlBuildingNice() throws IOException {
        System.out.println("XmlBuildingNice");

        boolean intendation = true;
        final XmlPrinter writer = XmlPrinter.forXml(intendation);
        try (XmlBuilder root = writer.createElement("root")) {
            root.addElement("childA")
                    .setAttrib("x", 1)
                    .setAttrib("y", 2);
            root.addElement("childB")
                    .setAttrib("x", 3)
                    .setAttrib("y", 4)
                    .setAttrib("z", "<'&\">")
                    .addText("A text message <'&\">");
            root.addRawText("\n    <rawXml/>\n");
         // root.addCDATA("A character data <'&\">");
        }

        String result = writer.toString();
        String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "\n<root>"
                + "\n    <childA x=\"1\" y=\"2\"/>"
                + "\n    <childB x=\"3\" y=\"4\" z=\"&lt;'&amp;&quot;&gt;\">A text message &lt;'&amp;\"&gt;</childB>"
                + "\n    <rawXml/>"
           //   + "\n<![CDATA[A character data <'&\">]]>"
                + "\n</root>";
        assertEquals(expected, result);
    }

    @Test
    public void testHtmlBuildingNice1() throws IOException {
        System.out.println("testHtmlBuildingNice1");

        final XmlPrinter writer = XmlPrinter.forNiceHtml((Appendable) new StringBuilder());
        try (XmlBuilder html = writer.createHtmlElement()) {
            html.setAttrib("lang", "en");
            try(XmlBuilder head = html.addElement("head")) {
               head.addElement("meta").setAttrib("charset", StandardCharsets.UTF_8);
               head.addElement("title").addText("Demo");
               head.addElement("link").setAttrib("href", "word.css").setAttrib("rel", "stylesheet");
            }
            try(XmlBuilder body = html.addElement("body")) {
               body.addElement("h1").addText("Hello, World!");
            }
        }

        String result = writer.toString();
        String expected = "<!DOCTYPE html>"
                + "\n<html lang=\"en\">"
                + "\n    <head>"
                + "\n        <meta charset=\"UTF-8\"/>"
                + "\n        <title>Demo</title>"
                + "\n        <link href=\"word.css\" rel=\"stylesheet\"/>"
                + "\n    </head>"
                + "\n    <body>"
                + "\n        <h1>Hello, World!</h1>"
                + "\n    </body>"
                + "\n</html>";
        assertEquals(expected, result);
    }

    @Test
    public void testHtmlBuildingNice2() throws IOException {
        System.out.println("testHtmlBuildingNice2");

        final XmlPrinter writer = XmlPrinter.forNiceHtml((Appendable) new StringBuilder());
        try (XmlBuilder html = writer.createHtmlElement()) {
            html.setAttrib("lang", "en");
            XmlBuilder head = html.addElement("head");
            head.addElement("meta").setAttrib("charset", StandardCharsets.UTF_8);
            head.addElement("title").addText("Demo");
            head.addElement("link").setAttrib("href", "word.css").setAttrib("rel", "stylesheet");

            XmlBuilder body = html.addElement("body");
            body.addElement("h1").addText("Hello, World!");

        }

        String result = writer.toString();
        String expected = "<!DOCTYPE html>"
                + "\n<html lang=\"en\">"
                + "\n    <head>"
                + "\n        <meta charset=\"UTF-8\"/>"
                + "\n        <title>Demo</title>"
                + "\n        <link href=\"word.css\" rel=\"stylesheet\"/>"
                + "\n    </head>"
                + "\n    <body>"
                + "\n        <h1>Hello, World!</h1>"
                + "\n    </body>"
                + "\n</html>";
        assertEquals(expected, result);
    }

    /** Test rendering to the HttpServletResponse */
    @Test
    public void testToResponse() throws IOException {
        System.out.println("HttpServletResponse");

        XmlPrinter writer = XmlPrinter.forHtml();
        try (XmlBuilder html = writer.createHtmlElement()) {
             try (XmlBuilder head = html.addElement(Html.HEAD)) {
                   head.addElement(Html.META).setAttrib(Html.A_CHARSET, UTF_8);
                   head.addElement(Html.TITLE).addText("Test");
             }
             try (XmlBuilder body = html.addElement(Html.BODY)) {
                   body.addElement(Html.H1).addText("Hello word!");
                   body.addElement(Html.DIV).addText(null);
             }
        };

        String result = writer.toString();
        String expected = "<!DOCTYPE html>"
                + "\n<html>"
                + "\n<head>"
                + "\n<meta charset=\"UTF-8\"/>"
                + "\n<title>Test</title></head>"
                + "\n<body>"
                + "\n<h1>Hello word!</h1>"
                + "\n<div>null</div></body></html>";
        assertEquals(expected, result);
    }

}
