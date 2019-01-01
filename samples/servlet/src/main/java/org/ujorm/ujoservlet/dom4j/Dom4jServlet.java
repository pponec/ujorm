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
package org.ujorm.ujoservlet.dom4j;

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
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.HTMLWriter;
import org.dom4j.io.OutputFormat;
import org.ujorm.tools.Check;
import org.ujorm.tools.web.Html;

/**
 * A live example of the HtmlElement inside a servlet using a Dom4j library.
 * @author Pavel Ponec
 */
@WebServlet(Dom4jServlet.URL_PATTER)
public class Dom4jServlet extends HttpServlet {

    /** URL pattern */
    public static final String URL_PATTER = "/dom4jServlet";

    /** Show the first line of soufce code */
    public static final short SHOW_LINE = 55;

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

        final String title = "Simple user form using a Dom4j library";
        Document document = DocumentHelper.createDocument();
        Element html = document.addElement(Html.HTML);
        Element head = html.addElement(Html.HEAD);
        head.addElement(Html.META)
                .addAttribute(Html.A_CHARSET, charset.toString());
        head.addElement(Html.TITLE)
                .addText(title);
        head.addElement(Html.LINK)
                .addAttribute(Html.A_HREF, "css/userForm.css")
                .addAttribute(Html.A_REL, "stylesheet")
                .addAttribute(Html.A_TYPE, "text/css");
        final Element body = html.addElement(Html.BODY);
        body.addElement(Html.H1).addText(title);
        final Element form = body.addElement(Html.FORM)
                .addAttribute(Html.A_METHOD, Html.V_POST)
                .addAttribute(Html.A_ACTION, postMethod ? null : input.getRequestURI());
        for (Field field : getFieldDescriptions()) {
            final Element row = form.addElement(Html.DIV)
                    .addAttribute(Html.A_CLASS, field.isSubmit() ? "submit" : null);
            row.addElement(Html.LABEL)
                    .addAttribute(Html.A_FOR, field.getName())
                    .addText(field.getLabel());
            Element inputBox = row.addElement(Html.DIV);
            inputBox.addElement(Html.INPUT)
                    .addAttribute(Html.A_TYPE, field.isSubmit() ? Html.V_SUBMIT : Html.V_TEXT)
                    .addAttribute(Html.A_ID, field.getName())
                    .addAttribute(Html.A_NAME, field.getName())
                    .addAttribute(Html.A_VALUE, field.getValue(input));
            field.getErrorMessage(input, postMethod).ifPresent(msg -> inputBox.addElement(Html.SPAN)
                    .addText(msg)); // Raw validation message
        }

        ApplDom4jService.addFooterDom4j(body, this, SHOW_LINE);
        renderHtml(document, output, true);
    }

    /** Rendering the HTML for Dom4j */
    private void renderHtml(Document document, HttpServletResponse output, boolean noCache) throws IOException {
        output.setCharacterEncoding(charset.toString());
        if (noCache) {
            output.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            output.setHeader("Pragma", "no-cache");
            output.setHeader("Expires", "0");
        }
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setNewlines(true);
        format.setEncoding(charset.toString());
        format.setXHTML(true);
        output.getWriter().write( "<!DOCTYPE html>");
        HTMLWriter writer = new HTMLWriter(output.getWriter(), format);
        writer.write(document);
        writer.flush();
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
