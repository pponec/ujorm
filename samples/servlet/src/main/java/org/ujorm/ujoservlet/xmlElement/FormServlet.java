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

package org.ujorm.ujoservlet.xmlElement;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ujorm.tools.Check;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.xml.model.XmlModel;
import org.ujorm.ujoservlet.tools.HtmlElementOrig;
import org.ujorm.ujoservlet.tools.ApplService;

/**
 * A live example of the HtmlElement inside a servlet.
 * @author Pavel Ponec
 */
@WebServlet(FormServlet.URL_PATTERN)
public class FormServlet extends HttpServlet {

    /** URL pattern */
    public static final String URL_PATTERN = "/formServlet";

    /* A common code page form request and response. Try the {@code  Charset.forName("windows-1250")} for example. */
    private final Charset charset = StandardCharsets.UTF_8;

    /** Show the first line of soufce code */
    public static final short SHOW_LINE = 59;

    /** A sign of the POST method */
    private boolean postMethod;

    /**
     * Handles the HTTP <code>GET</code> or <code>POST</code> method.
     * @param input Servlet request
     * @param output Servlet response
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException If an I/O error occurs
     */
    protected void processRequest(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        input.setCharacterEncoding(charset.toString());

        HtmlElementOrig html = createHtmlElement("Simple user form", "css/userForm.css");
        XmlModel form = html.addElementToBody(Html.FORM)
                .setAttribute(Html.A_METHOD, Html.V_POST)
                .setAttribute(Html.A_ACTION, postMethod ? null : input.getRequestURI());
        for (Field field : getFieldDescriptions()) {
            createInputField(field, form, input);
        }

        ApplService.addFooter(html.getBody(), this, SHOW_LINE);
        html.toResponse(output, true); // Render the result
    }

    /** Create new HtmlElement incliding title and CSS style */
    private HtmlElementOrig createHtmlElement(String title, String css) {
        final HtmlElementOrig result = new HtmlElementOrig(title, charset, css);
        result.addElementToBody(Html.H1).addText(title);
        return result;
    }

    /** Create an input field including label and validation message */
    private XmlModel createInputField(Field field, XmlModel form, HttpServletRequest input) {
        XmlModel result = form.addElement(Html.DIV) // An envelope
                .setAttribute(Html.A_CLASS, field.isSubmit() ? "submit" : null);
        result.addElement(Html.LABEL)
                .setAttribute(Html.A_FOR, field.getName())
                .addText(field.getLabel());
        XmlModel inputBox = result.addElement(Html.DIV);
        inputBox.addElement(Html.INPUT)
                .setAttribute(Html.A_TYPE, field.isSubmit() ? Html.V_SUBMIT : Html.V_TEXT)
                .setAttribute(Html.A_ID, field.getName())
                .setAttribute(Html.A_NAME, field.getName())
                .setAttribute(Html.A_VALUE, field.getValue(input));
        field.getErrorMessage(input, postMethod).ifPresent(msg -> inputBox.addElement(Html.SPAN)
                .addText(msg)); // Raw validation message
        return result;
    }

    /** Form field description data */
    private Field[] getFieldDescriptions() {
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

        /** Check the POST value and return an error message */
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
        postMethod = false;
        processRequest(input, output);
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
        postMethod = true;
        processRequest(input, output);
    }
}
