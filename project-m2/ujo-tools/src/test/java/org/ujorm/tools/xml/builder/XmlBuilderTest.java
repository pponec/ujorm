/*
 * Copyright 2018-2020 Pavel Ponec
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import org.ujorm.tools.xml.Html;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

/**
 * A test of the XmlNode class
 * @author Pavel Ponec
 */
public class XmlBuilderTest implements Html {

    @Test
    public void testXmlBuilding() throws IOException {
        System.out.println("XmlBuilding");

        final XmlPrinter writer = XmlPrinter.forXml();
        try (XmlBuilder root = writer.createElement("root")) {
            root.addElement("childA")
                    .setAttribute("x", 1)
                    .setAttribute("y", 2);
            root.addElement("childB")
                    .setAttribute("x", 3)
                    .setAttribute("y", 4)
                    .setAttribute("z", "<'&\">")
                    .addText("A text message <'&\">");
            root.addRawText("\n<rawXml/>\n");
         // root.addCDATA("A character data <'&\">");
        }

        String result = writer.toString();
        String expected = String.join("\n"
                , "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                , "<root>"
                , "<childA x=\"1\" y=\"2\"/>"
                , "<childB x=\"3\" y=\"4\" z=\"&lt;'&amp;&quot;&gt;\">A text message &lt;'&amp;\"&gt;</childB>"
                , "<rawXml/>"
           //   , "<![CDATA[A character data <'&\">]]>"
                , "</root>");
        assertEquals(expected, result);
    }

    @Test
    public void testXmlBuildingNice() throws IOException {
        System.out.println("XmlBuildingNice");

        final XmlPrinter writer = XmlPrinter.forNiceXml();
        try (XmlBuilder root = writer.createElement("root")) {
            root.addElement("childA")
                    .setAttribute("x", 1)
                    .setAttribute("y", 2);
            root.addElement("childB")
                    .setAttribute("x", 3)
                    .setAttribute("y", 4)
                    .setAttribute("z", "<'&\">")
                    .addText("A text message <'&\">");
            root.addRawText("\n\t<rawXml/>\n");
         // root.addCDATA("A character data <'&\">");
        }

        String result = writer.toString();
        String expected = String.join("\n"
                , "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                , "<root>"
                , "\t<childA x=\"1\" y=\"2\"/>"
                , "\t<childB x=\"3\" y=\"4\" z=\"&lt;'&amp;&quot;&gt;\">A text message &lt;'&amp;\"&gt;</childB>"
                , "\t<rawXml/>"
           //   , "<![CDATA[A character data <'&\">]]>"
                , "</root>");
        assertEquals(expected, result);
    }

    @Test
    public void testHtmlBuildingNice1() throws IOException {
        System.out.println("testHtmlBuildingNice1");

        final XmlPrinter writer = XmlPrinter.forNiceHtml((Appendable) new StringBuilder());
        try (XmlBuilder html = writer.createElement("html")) {
            html.setAttribute("lang", "en");
            try(XmlBuilder head = html.addElement("head")) {
               head.addElement("meta").setAttribute("charset", StandardCharsets.UTF_8);
               head.addElement("title").addText("Demo");
               head.addElement("link").setAttribute("href", "word.css").setAttribute("rel", "stylesheet");
            }
            try(XmlBuilder body = html.addElement("body")) {
               body.addElement("h1").addText("Hello, World!");
            }
        }

        String result = writer.toString();
        String expected = String.join("\n"
                , "<!DOCTYPE html>"
                , "<html lang=\"en\">"
                , "\t<head>"
                , "\t\t<meta charset=\"UTF-8\"/>"
                , "\t\t<title>Demo</title>"
                , "\t\t<link href=\"word.css\" rel=\"stylesheet\"/>"
                , "\t</head>"
                , "\t<body>"
                , "\t\t<h1>Hello, World!</h1>"
                , "\t</body>"
                , "</html>");
        assertEquals(expected, result);
    }

    @Test
    public void testHtmlBuildingNice2() throws IOException {
        System.out.println("testHtmlBuildingNice2");

        final XmlPrinter writer = XmlPrinter.forNiceHtml((Appendable) new StringBuilder());
        try (XmlBuilder html = writer.createElement("html")) {
            html.setAttribute("lang", "en");
            XmlBuilder head = html.addElement("head");
            head.addElement("meta").setAttribute("charset", StandardCharsets.UTF_8);
            head.addElement("title").addText("Demo");
            head.addElement("link")
                    .setAttribute("href", "word.css")
                    .setAttribute("rel", "stylesheet");

            XmlBuilder body = html.addElement("body");
            body.addElement("h1").addText("Hello, World!");

        }

        String result = writer.toString();
        String expected = String.join("\n"
                , "<!DOCTYPE html>"
                , "<html lang=\"en\">"
                , "\t<head>"
                , "\t\t<meta charset=\"UTF-8\"/>"
                , "\t\t<title>Demo</title>"
                , "\t\t<link href=\"word.css\" rel=\"stylesheet\"/>"
                , "\t</head>"
                , "\t<body>"
                , "\t\t<h1>Hello, World!</h1>"
                , "\t</body>"
                , "</html>");
        assertEquals(expected, result);
    }

    /** Test rendering to the HttpServletResponse */
    @Test
    public void testToResponse() throws IOException {
        System.out.println("HttpServletResponse");

        XmlPrinter writer = XmlPrinter.forHtml();
        try (XmlBuilder html = writer.createElement(Html.HTML)) {
             try (XmlBuilder head = html.addElement(Html.HEAD)) {
                   head.addElement(Html.META).setAttribute(Html.A_CHARSET, UTF_8);
                   head.addElement(Html.TITLE).addText("Test");
             }
             try (XmlBuilder body = html.addElement(Html.BODY)) {
                   body.addElement(Html.H1).addText("Hello word!");
                   body.addElement(Html.DIV).addText(null);
             }
        };

        String result = writer.toString();
        String expected = String.join("\n"
                , "<!DOCTYPE html>"
                , "<html>"
                , "<head>"
                , "<meta charset=\"UTF-8\"/>"
                , "<title>Test</title></head>"
                , "<body>"
                , "<h1>Hello word!</h1>"
                , "<div></div></body></html>");
        assertEquals(expected, result);
    }


    /** Test rendering to the HttpServletResponse */
    @Test
    public void testHtmlResponse() throws IOException {
        System.out.println("HtmlResponse");

        MockHttpServletResponse response = new MockHttpServletResponse();
        try (XmlBuilder html = XmlBuilder.forNiceHtml(response.getWriter())) {
            html.setAttribute("lang", "en");
            try(XmlBuilder head = html.addElement("head")) {
               head.addElement("meta").setAttribute("charset", UTF_8);
               head.addElement("title").addText("Demo");
               head.addElement("link").setAttribute("href", "css/basic.css").setAttribute("rel", "stylesheet");
            }
            try(XmlBuilder body = html.addElement("body")) {
               body.addElement("h1").addText("Hello, World! (extended)");
            }
        }

        String expected = String.join("\n"
                , "<!DOCTYPE html>"
                , "<html lang=\"en\">"
                , "\t<head>"
                , "\t\t<meta charset=\"UTF-8\"/>"
                , "\t\t<title>Demo</title>"
                , "\t\t<link href=\"css/basic.css\" rel=\"stylesheet\"/>"
                , "\t</head>"
                , "\t<body>"
                , "\t\t<h1>Hello, World! (extended)</h1>"
                , "\t</body>"
                , "</html>");
        assertEquals(expected, response.getContentAsString());
    }

    /** Test rendering to the HttpServletResponse */
    @Test
    public void testHtmlResponse4const() throws IOException {
        System.out.println("HtmlResponse4const");
        Charset lang = StandardCharsets.UTF_8;

        MockHttpServletResponse response = new MockHttpServletResponse();
        try (XmlBuilder html = XmlBuilder.forNiceHtml(response.getWriter())) {
            html.setAttribute(A_LANG, "en");
            try(XmlBuilder head = html.addElement(HEAD)) {
               head.addElement(META).setAttribute(A_CHARSET, lang);
               head.addElement(TITLE).addText("Demo");
               head.addElement(LINK).setAttribute(A_HREF, "css/basic.css").setAttribute(A_REL, V_STYLESHEET);
            }
            try(XmlBuilder body = html.addElement(BODY)) {
               body.addElement(H1).addText("Hello, World! (extended)");
            }
        }

        String expected = String.join("\n"
                , "<!DOCTYPE html>"
                , "<html lang=\"en\">"
                , "\t<head>"
                , "\t\t<meta charset=\"UTF-8\"/>"
                , "\t\t<title>Demo</title>"
                , "\t\t<link href=\"css/basic.css\" rel=\"stylesheet\"/>"
                , "\t</head>"
                , "\t<body>"
                , "\t\t<h1>Hello, World! (extended)</h1>"
                , "\t</body>"
                , "</html>");
        assertEquals(expected, response.getContentAsString());
    }

}
