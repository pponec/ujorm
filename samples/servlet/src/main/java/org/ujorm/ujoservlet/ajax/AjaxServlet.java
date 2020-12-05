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
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.HtmlElement;
import org.ujorm.tools.xml.builder.XmlPrinter;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.XmlConfig;
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

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(AjaxServlet.class.getName());

    /** Link to a Bootstrap URL */
    private static final String BOOTSTRAP_CSS = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css";

    /** Link to jQuery */
    private static final String JQUERY_JS = "https://ajax.googleapis.com/ajax/libs/jquery/2.1.1/jquery.min.js";

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    private void doProcess(
            final HttpServletRequest input,
            final HttpServletResponse output,
            final boolean isGet) throws ServletException, IOException {
        input.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        try (HtmlElement html = HtmlElement.of(output, getConfig())) {
            html.addJavascriptLink(true, JQUERY_JS);
            html.addCssLink(BOOTSTRAP_CSS);
            html.addCssBody(getCss());
            try (Element body = html.getBody()) {
                body.addHeading(html.getTitle());
                try (Element form = body.addForm().setMethod(Html.V_POST).setAction("?")) {
                    form.addInput("regexp")
                            .setName(REGEXP)
                            .setValue(REGEXP.value(input))
                            .setAttribute(Html.A_PLACEHOLDER, "Regular expression");
                    form.addTextArea("text")
                            .setAttribute(Html.A_PLACEHOLDER, "Test String")
                            .setName(TEXT)
                            .addText(TEXT.value(input));
                    form.addDiv().addSubmitButton("btn", "btn-primary").addText("Submit");
                    form.addDiv("out").addRawText(highlight(input));
                }
                body.addElement(Html.HR);
                body.addTextTemplated("Version <{}.{}.{}>", 1, 2, 3);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Servlet failed", e);
        }
    }

    /** Builde */
    protected String highlight(HttpServletRequest input) {
        return AjaxServlet.this.highlight(REGEXP.value(input), TEXT.value(input));
    }

    /** Build regexp result */
    protected String highlight(String regexp, String text) {
        try {
            SecureRandom random = new SecureRandom();
            String begTag = "_" + random.nextLong();
            String endTag = "_" + random.nextLong();
            Pattern pattern = Pattern.compile("(" + regexp + ")");
            String rawText = pattern
                    .matcher(text)
                    .replaceAll(begTag + "$1" + endTag);

            StringBuilder result = new StringBuilder(256);
            new XmlPrinter(result, XmlConfig.ofDefault()
                    .setDoctype(""))
                    .getWriterEscaped().append(rawText);

            return result.toString()
                    .replaceAll(begTag, "<span>")
                    .replaceAll(endTag, "</span>");
        } catch (Exception e) {
            LOGGER.warning("Regexp error: " + e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }

    /** Create a configuration of HTML model */
    private DefaultHtmlConfig getConfig() {
        DefaultHtmlConfig config;
        config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setDocumentObjectModel(false);
        config.setTitle("Ajax Servlet");
        return config;
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(
            final HttpServletRequest input,
            final HttpServletResponse output) throws ServletException, IOException {
        doProcess(input, output, false);
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(
            final HttpServletRequest input,
            final HttpServletResponse output) throws ServletException, IOException {
        doProcess(input, output, true);
    }

    /** Create CSS */
    private CharSequence getCss() {
        return String.join("\n",
                "body   { margin-left:20px; background-color: #f3f6f7;}",
                "h1, h2 { color: SteelBlue;}",
                "form   { width: 500px;}",
                ".regexp{ width: 100%; margin-bottom: 2px;}",
                ".text  { width: 100%; height: 100px;}",
                ".out   { width: 100%; min-height: 100px; border:1px solid gray; "
                        + "margin-top: 10px; background-color: white;}",
                ".out span { background-color: yellow;}"
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
