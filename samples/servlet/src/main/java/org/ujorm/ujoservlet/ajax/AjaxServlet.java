/*
 * Copyright 2020-2020 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.ujoservlet.ajax;

import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.HtmlElement;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;
import org.ujorm.ujoservlet.ajax.ao.HttpParam;
import static org.ujorm.ujoservlet.ajax.AjaxServlet.Attrib.*;

/**
 * A live example of the HtmlElement inside a servlet using a Dom4j library.
 * @author Pavel Ponec
 */
@WebServlet(AjaxServlet.URL_PATTERN)
public class AjaxServlet extends HttpServlet {

    /** URL pattern */
    public static final String URL_PATTERN = "/AjaxServlet";

    /** Link to a Bootstrap URL */
    private static final String BOOTSTRAP_CSS = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css";

    /** Link to jQuery */
    private static final String JQUERY_JS = "https://ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js";

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(AjaxServlet.class.toString());

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {

        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setDocumentObjectModel(false);
        config.setTitle("Ajax Servlet");

        try (HtmlElement html = HtmlElement.of(output, config)) {
            html.addJavascriptLink(true, JQUERY_JS);
            html.addCssLink(BOOTSTRAP_CSS);
            html.addCssBody(getCss());
            try (Element body = html.getBody()) {
                body.addHeading(html.getTitle());
                try (Element form = body.addForm()) {
                    form.addInput("regexp")
                            .setName(REGEXP)
                            .setValue(REGEXP.value(input))
                            .setAttribute(Html.A_PLACEHOLDER, "Regular expression");
                    form.addTextArea("text")
                            .setAttribute(Html.A_PLACEHOLDER, "Test String")
                            .setName(TEXT)
                            .addText(TEXT.value(input));
                    form.addDiv().addSubmitButton("btn", "btn-primary").addText("Submit");
                    form.addDiv("out").addText("");
                }
                body.addElement(Html.HR);
                body.addTextTemplated("Version <{}.{}.{}>", 1, 2, 3);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Servlet failed", e);
        }
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
        doGet(input, output);
    }

    /** Create CSS */
    private CharSequence getCss() {
        return String.join("\n",
                 "body   { margin-left:20px;}",
                 "h1, h2 { color: SteelBlue;}",
                 "form   { width: 500px;}",
                 ".regexp{ width: 100%; margin-bottom: 2px;}",
                 ".text  { width: 100%; height: 100px;}",
                 ".out   { width: 100%; min-height: 100px; border:1px solid gray; margin-top: 10px}"
        );
    }

    enum Attrib implements HttpParam {
        REGEXP,
        TEXT;

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ENGLISH);
        }
    }
}
