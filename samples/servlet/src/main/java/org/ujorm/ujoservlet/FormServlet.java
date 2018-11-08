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

package org.ujorm.ujoservlet;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.Check;
import org.ujorm.tools.dom.HtmlElement;
import org.ujorm.tools.dom.XmlElement;
import org.ujorm.ujoservlet.tools.ApplService;
import org.ujorm.ujoservlet.tools.Html;

/**
 * A live example of the HtmlElement inside a servlet.
 * @author Pavel Ponec
 */
public class FormServlet extends HttpServlet {

    /** Show the first line of soufce code */
    public static final short SHOW_LINE = 54;

    /* A common code page form request and response. Try the {@code  Charset.forName("windows-1250")} for example. */
    private final Charset charset = StandardCharsets.UTF_8;

    /**
     * Handles the HTTP <code>GET</code> or <code>POST</code> method.
     * @param input servlet request
     * @param output servlet response
     * @param postMethod A sign of the POST method
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest input, HttpServletResponse output, boolean postMethod) throws ServletException, IOException {
        input.setCharacterEncoding(charset.toString());

        final String title = "Simple user form";
        final HtmlElement html = new HtmlElement(title, charset);
        html.addCssLink("welcomeForm.css");
        html.addElementToBody(Html.H1).addText(title);
        final XmlElement form = html.addElementToBody(Html.FORM)
                .addAttrib(Html.A_METHOD, Html.V_POST)
                .addAttrib(Html.A_ACTION, postMethod ? null : input.getRequestURI());
        for (Field field : getFieldDescription()) {
            final XmlElement row = form.addElement(Html.DIV)
                    .addAttrib(Html.A_CLASS, field.isSubmit() ? "submit" : null);
            row.addElement(Html.LABEL)
                    .addAttrib(Html.A_FOR, field.getName())
                    .addText(field.getLabel());
            XmlElement inputBox = row.addElement(Html.DIV);
            inputBox.addElement(Html.INPUT)
                    .addAttrib(Html.A_TYPE, field.isSubmit() ? Html.V_SUBMIT : Html.V_TEXT)
                    .addAttrib(Html.A_ID, field.getName())
                    .addAttrib(Html.A_NAME, field.getName())
                    .addAttrib(Html.A_VALUE, field.getValue(input));
            field.getErrorMessage(input, postMethod).ifPresent(msg -> inputBox.addElement(Html.SPAN)
                    .addText(msg)); // Raw validation message
        }

        ApplService.addFooter(html.getBody(), this, SHOW_LINE);
        html.toResponse(output, true); // Render the result
    }

    /** Form field description data */
    private Field[] getFieldDescription() {
        Field[] reslt = { new Field("First name", "firstname", "^.{2,99}$")
                        , new Field("Last name", "lastname", "^.{2,99}$")
                        , new Field("E-mail", "email", "^[\\w\\.=-]+@[\\w\\.-]+\\.[\\w]{2,3}$")
                        , new Field("Phone number", "phone", "^\\+?[ \\d]{9,15}$")
                        , new Field("Nickname", "nick", "^.{3,10}$")
                        , new Field(" ", "submit", "", true)};
        return reslt;
    }

    /** Form field description class */
    static class Field {
        private final String label;
        private final String name;
        private final String regexp;
        private final boolean submit;

        public Field(String label, String key, String regexp) {
            this(label, key, regexp, false);
        }

        public Field(String label, String name, String regexp, boolean submit) {
            this.label = label;
            this.name = name;
            this.regexp = regexp;
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

        /** Check the POST value and regurn an error message */
        public Optional<String> getErrorMessage(HttpServletRequest input, boolean postMethod) {
            if (postMethod) {
                final String value = input.getParameter(name);
                if (Check.isEmpty(value)) {
                    return Optional.of("Required field");
                }
                if (Check.hasLength(regexp) && !Pattern.matches(regexp, value)) {
                     return Optional.of("Wrong value for: " + regexp); // localiza it!
                }
            }
            return Optional.empty();
        }

        /** Get a request value */
        public String getValue(HttpServletRequest input) {
            return submit ? "Submit" : input.getParameter(name);
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        processRequest(input, output, false);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        processRequest(input, output, true);
    }
}
