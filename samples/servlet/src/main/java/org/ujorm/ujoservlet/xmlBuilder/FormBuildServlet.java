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

package org.ujorm.ujoservlet.xmlBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.Check;
import org.ujorm.tools.xml.dom.HtmlElement;
import org.ujorm.tools.xml.dom.XmlElement;
import org.ujorm.tools.web.Html;
import org.ujorm.ujoservlet.tools.ApplService;

/**
 * A live example of the HtmlElement inside a servlet.
 * @author Pavel Ponec
 */
@WebServlet(FormBuildServlet.URL_PATTERN)
public class FormBuildServlet extends HttpServlet {

    /** URL pattern */
    public static final String URL_PATTERN = "/formBuidServlet";

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

        HtmlElement html = createHtmlElement("Simple user", "css/userForm.css");
        XmlElement form = html.addElementToBody(Html.FORM)
                .setAttrib(Html.A_METHOD, Html.V_POST)
                .setAttrib(Html.A_ACTION, postMethod ? null : input.getRequestURI());
        for (Field field : getFieldDescriptions()) {
            createInputField(field, form, input);
        }

        ApplService.addFooter(html.getBody(), this, SHOW_LINE);
        html.toResponse(output, true); // Render the result
    }

    /** Create new HtmlElement incliding title and CSS style */
    private HtmlElement createHtmlElement(String title, String css) {
        final HtmlElement result = new HtmlElement(title, charset);
        result.addCssLink(css);
        result.addElementToBody(Html.H1).addText(title);
        return result;
    }

    /** Create an input field including label and validation message */
    private XmlElement createInputField(Field field, XmlElement form, HttpServletRequest input) {
        XmlElement result = new XmlElement(Html.DIV) // An envelope
                .setAttrib(Html.A_CLASS, field.isSubmit() ? "submit" : null);
        result.addElement(Html.LABEL)
                .setAttrib(Html.A_FOR, field.getName())
                .addText(field.getLabel());
        XmlElement inputBox = result.addElement(Html.DIV);
        inputBox.addElement(Html.INPUT)
                .setAttrib(Html.A_TYPE, field.isSubmit() ? Html.V_SUBMIT : Html.V_TEXT)
                .setAttrib(Html.A_ID, field.getName())
                .setAttrib(Html.A_NAME, field.getName())
                .setAttrib(Html.A_VALUE, field.getValue(input));
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
