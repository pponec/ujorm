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
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.ujorm.tools.HtmlElementTest.Html.*;

/**
 * @author Pavel Ponec
 */
public class HtmlElementTest {

    /** Test rendering to the HttpServletResponse */
    @Test
    public void testToResponse() throws IOException {
        System.out.println("HttpServletResponse");

        final HtmlElement html = new HtmlElement("Test");
        html.getBody().addElement(DIV)
                      .addText("Hello word!");
        MockHttpServletResponse response = new MockHttpServletResponse();
        html.toResponse(response, false);

        String expected = "<!DOCTYPE html>"
                + "\n<html>"
                + "\n<head>"
                + "\n<meta charset=\"UTF-8\"/>"
                + "\n<title>Test</title></head>"
                + "\n<body>"
                + "\n<div>Hello word!</div></body></html>";
        assertEquals(expected, response.getContentAsString());
        assertEquals("UTF-8", response.getCharacterEncoding());
        assertEquals("text/html; charset=UTF-8", response.getContentType());
    }

    /** HTML form building example */
    @Test
    public void testHtmlFormBuilding() {
        System.out.println("BuildHtmlForm");

        final String title = "User form";
        final Field[] fields = { new Field("First name", "firstname")
                               , new Field("Last name", "lastname")
                               , new Field("E-mail", "email")
                               , new Field("Phone number", "phone")
                               , new Field("Nickname", "nick")
                               , new Field(" ", "submit", true) };
        
        final HtmlElement html = new HtmlElement(title);
        html.addCssBody("h1{color:SteelBlue;} td:first-child{text-align:right;}");
        final XmlElement form = html.getBody().addElement(FORM);
        form.addElement(H1).addText(title);
        final XmlElement table = form.addElement(TABLE);
        for (Field field : fields) {
            final XmlElement row = table.addElement(TR);
            row.addElement(TD)
                    .addElement(LABEL)
                    .addAttrib(A_FOR, field.getName())
                    .addText(field.getLabel());
            row.addElement(TD)
                    .addElement(INPUT)
                    .addAttrib(A_ID, field.getName())
                    .addAttrib(A_NAME, field.getName())
                    .addAttrib(A_TYPE, field.isSubmit() ? V_SUBMIT : V_TEXT);
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
                + "\n<label for=\"firstname\">First name</label></td>"
                + "\n<td>"
                + "\n<input id=\"firstname\" name=\"firstname\" type=\"text\"/></td></tr>"
                + "\n<tr>"
                + "\n<td>"
                + "\n<label for=\"lastname\">Last name</label></td>"
                + "\n<td>"
                + "\n<input id=\"lastname\" name=\"lastname\" type=\"text\"/></td></tr>"
                + "\n<tr>"
                + "\n<td>"
                + "\n<label for=\"email\">E-mail</label></td>"
                + "\n<td>"
                + "\n<input id=\"email\" name=\"email\" type=\"text\"/></td></tr>"
                + "\n<tr>"
                + "\n<td>"
                + "\n<label for=\"phone\">Phone number</label></td>"
                + "\n<td>"
                + "\n<input id=\"phone\" name=\"phone\" type=\"text\"/></td></tr>"
                + "\n<tr>"
                + "\n<td>"
                + "\n<label for=\"nick\">Nickname</label></td>"
                + "\n<td>"
                + "\n<input id=\"nick\" name=\"nick\" type=\"text\"/></td></tr>"
                + "\n<tr>"
                + "\n<td>"
                + "\n<label for=\"submit\"> </label></td>"
                + "\n<td>"
                + "\n<input id=\"submit\" name=\"submit\" type=\"submit\"/></td></tr>"
               + "</table>"
               + "</form>"
               + "</body>"
               + "</html>";

        assertNotNull(result);
        assertEquals(expected, result);
    }

    /** HTML constants */
    public interface Html {

        // --- Element names ---

        String BODY = "body";
        String DIV = "div";
        String FORM = "form";
        String H1 = "h1";
        String TABLE = "table";
        String TR = "tr";
        String TD = "td";
        String LABEL = "label";
        String INPUT = "input";

        // --- Attribute names ---

        String A_FOR = "for";
        String A_ID = "id";
        String A_NAME = "name";
        String A_TYPE = "type";

        // --- HTML Values ---

        String V_SUBMIT = "submit";
        String V_TEXT = "text";
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

        public String getLabel() {
            return label;
        }

        public String getName() {
            return name;
        }

        public boolean isSubmit() {
            return submit;
        }
    }
}
