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
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.ao.JsonWriter;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;
import static org.ujorm.ujoservlet.ajax.AjaxServlet.Attrib.*;

/**
 * A live example of the HtmlElement inside a servlet using a Dom4j library.
 * @author Pavel Ponec
 */
@WebServlet(AjaxServlet.URL_PATTERN)
public class AjaxServlet extends HttpServlet {

    /** URL pattern */
    public static final String URL_PATTERN = "/AjaxServlet";
    /** Enable AJAX feature */
    private static final boolean AJAX_ENABLED = true;
    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(AjaxServlet.class.getName());
    /** Link to a Bootstrap URL of CDN */
    private static final String BOOTSTRAP_CSS = "https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css";
    /** Link to jQuery of CDN */
    private static final String JQUERY_JS = "https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js";
    /** Bootstrap form control CSS class name */
    private static final String CSS_CONTROL = "form-control";
    /** CSS class name for the output box */
    private static final String CSS_OUTPUT = "out";
    /** CSS class name for the output box */
    private static final String CSS_SUBTITLE = "subtitle";
    /** Input idle delay in millisec */
    private final int idleDelay = 300;
    /** A common service */
    private final Service service = new Service();

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
        try (HtmlElement html = HtmlElement.of(input, output, getConfig("Regular expression tester"))) {
            html.addJavascriptLink(false, JQUERY_JS);
            html.addCssLink(BOOTSTRAP_CSS);
            html.addCssBody(service.getCss());
            html.addJavascriptBody(service.getJavascript(
                    AJAX_ENABLED, _AJAX, idleDelay, true, REGEXP, TEXT));
            try (Element body = html.getBody()) {
                body.addHeading(html.getTitle());
                body.addDiv(CSS_SUBTITLE).addText("");
                try (Element form = body.addForm()
                        .setId("form")
                        .setMethod(Html.V_POST).setAction("?")) {
                    form.addInput(CSS_CONTROL, "regexp")
                            .setName(REGEXP)
                            .setValue(REGEXP.value(input))
                            .setAttribute(Html.A_PLACEHOLDER, "Regular expression");
                    form.addTextArea(CSS_CONTROL, "text")
                            .setAttribute(Html.A_PLACEHOLDER, "Plain Text")
                            .setName(TEXT)
                            .addText(TEXT.value(input));
                    form.addDiv().addButton("btn", "btn-primary").addText("Evaluate");
                    Message result = highlight(input);
                    form.addDiv(CSS_CONTROL, CSS_OUTPUT)
                            .addRawText(highlight(input));
                }
                body.addElement(Html.HR);
                body.addTextTemplated("Version <{}.{}.{}>", 1, 2, 3);
            }
        } catch (Exception | OutOfMemoryError e) {
            LOGGER.log(Level.SEVERE, "Servlet failed", e);
        }
    }

    /** Build a HTML result */
    protected Message highlight(HttpServletRequest input) {
        return service.highlight(
                REGEXP.value(input, ""),
                TEXT.value(input, ""));
    }

    /** Create a configuration of HTML model */
    private DefaultHtmlConfig getConfig(String title) {
        DefaultHtmlConfig config;
        config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
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
        if (_AJAX.isTrue(input)) {
            doAjax(input, output);
        } else {
            doGet(input, output);
        }
    }

    /**
     * Return lighlited text in HTML format according a regular expression
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doAjax(
            final HttpServletRequest input,
            final HttpServletResponse output) throws ServletException, IOException {
        try (JsonWriter writer = JsonWriter.of(input, output)) {
            Message msg = highlight(input);
            CharSequence[] result = {
                msg.isError() ? "<span class='error'>" : "",
                msg.getText(),
                msg.isError() ? "</span>" : "",
            };
            // Write a selector with a value:
            writer.write("." + CSS_OUTPUT, result);
            writer.write("." + CSS_SUBTITLE, "AJAX ready");
        }
    }

    /** Servlet attributes */
    enum Attrib implements HttpParameter {
        _AJAX,
        REGEXP,
        TEXT;
        @Override
        public String toString() {
            return name().toLowerCase(Locale.ENGLISH);
        }
    }
}
