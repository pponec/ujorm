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

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Pavel Ponec
 */
public class HtmlElementTest {

    /** Simple HTML message building example */
    @Test
    public void testHtmlMessageBuilding() {
        System.out.println("BuildHtmlMessage");

        final HtmlElement html = new HtmlElement("Test");
        html.getBody().addElement("div")
                      .addText("Hello word!");

        String result = html.toString();
        String expected = "<!DOCTYPE html>"
                + "\n<html>"
                + "\n<head>"
                + "\n<meta charset=\"UTF-8\"/>"
                + "\n<title>Test</title></head>"
                + "\n<body>"
                + "\n<div>Hello word!</div></body></html>";
        assertNotNull(result);
        assertEquals(expected, result);
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
        final XmlElement form = html.getBody().addElement("form");
        form.addElement("h1").addText(title);
        final XmlElement table = form.addElement("table");
        for (Field field : fields) {
            final XmlElement row = table.addElement("tr");
            row.addElement("td")
                    .addElement("label")
                    .addAttrib("for", field.getName())
                    .addText(field.getLabel());
            row.addElement("td")
                    .addElement("input")
                    .addAttrib("id", field.getName())
                    .addAttrib("type", field.isSubmit() ? "submit" : "text");
        }

        String result = html.toString();
        String expected = "<!DOCTYPE html>"
                + "\n<html>"
                + "\n<head>"
                + "\n<meta charset=\"UTF-8\"/>"
                + "\n<title>User form</title></head>"
                + "\n<body>"
                + "\n<form>"
                + "\n<h1>User form</h1>"
                + "\n<table>"
                + "\n<tr>"
                + "\n<td>"
                + "\n<label for=\"firstname\">First name</label></td>"
                + "\n<td>"
                + "\n<input id=\"firstname\" type=\"text\"/></td></tr>"
                + "\n<tr>"
                + "\n<td>"
                + "\n<label for=\"lastname\">Last name</label></td>"
                + "\n<td>"
                + "\n<input id=\"lastname\" type=\"text\"/></td></tr>"
                + "\n<tr>"
                + "\n<td>"
                + "\n<label for=\"email\">E-mail</label></td>"
                + "\n<td>"
                + "\n<input id=\"email\" type=\"text\"/></td></tr>"
                + "\n<tr>"
                + "\n<td>"
                + "\n<label for=\"phone\">Phone number</label></td>"
                + "\n<td>"
                + "\n<input id=\"phone\" type=\"text\"/></td></tr>"
                + "\n<tr>"
                + "\n<td>"
                + "\n<label for=\"nick\">Nickname</label></td>"
                + "\n<td>"
                + "\n<input id=\"nick\" type=\"text\"/></td></tr>"
                + "\n<tr>"
                + "\n<td>"
                + "\n<label for=\"submit\"> </label></td>"
                + "\n<td>"
                + "\n<input id=\"submit\" type=\"submit\"/></td></tr>"
               + "</table>"
               + "</form>"
               + "</body>"
               + "</html>";

        assertNotNull(result);
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
