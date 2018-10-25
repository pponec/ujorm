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

package org.ujorm.tools;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.Assert.assertEquals;

/**
 * @author Pavel Ponec
 */
public class HtmlElementTest {

    /** Test rendering to the HttpServletResponse */
    @Test
    public void testToResponse() throws IOException {
        System.out.println("HttpServletResponse");

        final HtmlElement html = new HtmlElement("Test");
        html.getBody().addElement(Html.H1)
                      .addText("Hello word!");
        MockHttpServletResponse response = new MockHttpServletResponse();
        html.toResponse(response, false);

        String expected = "<!DOCTYPE html>"
                + "\n<html>"
                + "\n<head>"
                + "\n<meta charset=\"UTF-8\"/>"
                + "\n<title>Test</title></head>"
                + "\n<body>"
                + "\n<h1>Hello word!</h1></body></html>";
        assertEquals(expected, response.getContentAsString());
        assertEquals("UTF-8", response.getCharacterEncoding());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
    }

    /** HTML form building example */
    @Test
    public void testHtmlFormBuilding() {
        System.out.println("BuildHtmlForm");

        final Map<String,String> input = new HashMap<>();
        final String title = "User form";
        final Field[] fields = { new Field("First name", "firstname")
                               , new Field("Last name", "lastname")
                               , new Field("E-mail", "email")
                               , new Field("Phone number", "phone")
                               , new Field("Nickname", "nick")
                               , new Field("", "submit", true) };

        final HtmlElement html = new HtmlElement(title);
        html.addCssBody("h1{color:SteelBlue;} td:first-child{text-align:right;}");
        final XmlElement form = html.getBody().addElement(Html.FORM);
        form.addElement(Html.H1).addText(title);
        final XmlElement table = form.addElement(Html.TABLE);
        for (Field field : fields) {
            final XmlElement row = table.addElement(Html.TR);
            row.addElement(Html.TD)
                    .addElement(Html.LABEL)
                    .addAttrib(Html.A_FOR, field.getName())
                    .addText(field.getLabelSeparated());
            row.addElement(Html.TD)
                    .addElement(Html.INPUT)
                    .addAttrib(Html.A_TYPE, field.isSubmit() ? Html.V_SUBMIT : Html.V_TEXT)
                    .addAttrib(Html.A_ID, field.getName())
                    .addAttrib(Html.A_NAME, field.getName())
                    .addAttrib(Html.A_VALUE, input.get(field.getName()));
        }

        String result = html.toString();
        String expected = "<!DOCTYPE html>"
                + "\n<html>"
                + "\n<head>"
                + "\n<meta charset=\"UTF-8\"/>"
                + "\n<title>User form</title>"
                + "\n<style type=\"text/css\">h1{color:SteelBlue;} td:first-child{text-align:right;}</style></head>"
                + "\n<body>"
                + "\n<form>"
                + "\n<h1>User form</h1>"
                + "\n<table>"
                + "\n<tr>"
                + "\n<td>"
                + "\n<label for=\"firstname\">First name:</label></td>"
                + "\n<td>"
                + "\n<input type=\"text\" id=\"firstname\" name=\"firstname\"/></td></tr>"
                + "\n<tr>"
                + "\n<td>"
                + "\n<label for=\"lastname\">Last name:</label></td>"
                + "\n<td>"
                + "\n<input type=\"text\" id=\"lastname\" name=\"lastname\"/></td></tr>"
                + "\n<tr>"
                + "\n<td>"
                + "\n<label for=\"email\">E-mail:</label></td>"
                + "\n<td>"
                + "\n<input type=\"text\" id=\"email\" name=\"email\"/></td></tr>"
                + "\n<tr>"
                + "\n<td>"
                + "\n<label for=\"phone\">Phone number:</label></td>"
                + "\n<td>"
                + "\n<input type=\"text\" id=\"phone\" name=\"phone\"/></td></tr>"
                + "\n<tr>"
                + "\n<td>"
                + "\n<label for=\"nick\">Nickname:</label></td>"
                + "\n<td>"
                + "\n<input type=\"text\" id=\"nick\" name=\"nick\"/></td></tr>"
                + "\n<tr>"
                + "\n<td>"
                + "\n<label for=\"submit\"> </label></td>"
                + "\n<td>"
                + "\n<input type=\"submit\" id=\"submit\" name=\"submit\"/></td></tr>"
               + "</table>"
               + "</form>"
               + "</body>"
               + "</html>";
        assertEquals(expected, result);
    }

    /** Form field description */
    static class Field {

        private final String label;
        private final String name;
        private final boolean submit;

        public Field(String label, String key) {
            this(label, key, false);
        }

        public Field(String label, String name, boolean submit) {
            this.label = label;
            this.name = name;
            this.submit = submit;
        }

        public String getLabelSeparated() {
            char separator = submit || label.isEmpty() ? ' ' : ':';
            return label + separator;
        }

        public String getName() {
            return name;
        }

        public boolean isSubmit() {
            return submit;
        }
    }
}
