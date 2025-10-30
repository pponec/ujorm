/*
 * Copyright 2020-2022 Pavel Ponec, https://github.com/pponec
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.HtmlElement;
import org.ujorm.tools.web.ajax.JavaScriptWriter;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.json.JsonBuilder;
import org.ujorm.tools.web.request.HttpContext;
import org.ujorm.tools.web.request.URequest;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;
import org.ujorm.ujoservlet.ajax.ao.Message;
import org.ujorm.ujoservlet.ajax.ao.Service;
import static org.ujorm.ujoservlet.ajax.RegexpServlet.Attrib.*;
import static org.ujorm.ujoservlet.ajax.RegexpServlet.Css.*;
import static org.ujorm.ujoservlet.ajax.RegexpServlet.Url.*;

/**
 * A live example of the HtmlElement inside a servlet using a Dom4j library.
 * @author Pavel Ponec
 */
@WebServlet(RegexpServlet.URL_PATTERN)
public class RegexpServlet extends HttpServlet {

    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(RegexpServlet.class.getName());
    /** URL pattern */
    public static final String URL_PATTERN = "/RegexpServlet";
    /** Enable AJAX feature */
    private static final boolean AJAX_ENABLED = true;
    /** AJAX param */
    private static final HttpParameter AJAX = JavaScriptWriter.DEFAULT_AJAX_REQUEST_PARAM;
    /** AJAX ready message */
    private static final String AJAX_READY_MSG = "AJAX ready";
    /** A service */
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

        final HttpContext context = HttpContext.ofServlet(input, output);
        try (HtmlElement html = HtmlElement.of(context.writer(), getConfig("Regular expression tester"))) {
            //html.addJavascriptLink(false, JQUERY_JS); // For jQuery implementation only
            html.addCssLink(BOOTSTRAP_CSS);
            html.addCssBodies(html.getConfig().getNewLine(), service.getCss());
            writeJavaScript(html, AJAX_ENABLED);
            Message msg = highlight(context.request());
            try (Element body = html.addBody()) {
                body.addHeading(html.getTitle());
                body.addDiv(SUBTITLE_CSS).addText(AJAX_ENABLED ? AJAX_READY_MSG : "");
                try (Element form = body.addForm()
                        .setMethod(Html.V_POST).setAction("?")) {
                    form.addInput(CONTROL_CSS)
                            .setId(REGEXP)
                            .setName(REGEXP)
                            .setValue(REGEXP.of(context))
                            .setAttribute(Html.A_PLACEHOLDER, "Regular expression");
                    form.addTextArea(CONTROL_CSS)
                            .setId(TEXT)
                            .setName(TEXT)
                            .setAttribute(Html.A_PLACEHOLDER, "Plain Text")
                            .addText(TEXT.of(context));
                    form.addDiv().addButton("btn", "btn-primary").addText("Evaluate");
                    form.addDiv(CONTROL_CSS, OUTPUT_CSS).addRawText(msg);
                }
                body.addElement(Html.HR);
                body.addAnchor(SOURCE_URL).addTextTemplated("Source code <{}.{}.{}>", 1, 2, 3);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Internal server error", e);
            output.setStatus(500);
        }
    }

    @Override
    protected void doPost(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        final HttpContext context = HttpContext.ofServlet(input, output);
        if (AJAX.of(context, false)) {
            doAjax(context.request(), JsonBuilder.of(context.writer(), getConfig("?"))).close();
        } else {
            doGet(input, output);
        }
    }

    /**
     * Return lighlited text in HTML format according a regular expression
     * @param input servlet request
     * @param output A JSON writer
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @NotNull
    protected JsonBuilder doAjax(URequest input, JsonBuilder output) throws IOException {
            final Message msg = highlight(input);
            output.writeClass(OUTPUT_CSS, e -> e.addElementIf(msg.isError(), Html.SPAN, "error")
                    .addRawText(msg));
            output.writeClass(SUBTITLE_CSS, AJAX_READY_MSG);
            return output;
    }

    /** Build a HTML result */
    protected Message highlight(URequest input) {
        return service.highlight(
                REGEXP.of(input, ""),
                TEXT.of(input, ""));
    }

    /** Write a Javascript to a header */
    protected void writeJavaScript(@NotNull final HtmlElement html, final boolean enabled) {
        if (enabled) {
            new JavaScriptWriter(Html.INPUT, Html.TEXT_AREA)
                    .setSubtitleSelector("." + SUBTITLE_CSS)
                    .setFormSelector(Html.FORM)
                    .write(html.getHead());
        }
    }

    /** Create a configuration of HTML model */
    private DefaultHtmlConfig getConfig(@NotNull String title) {
        DefaultHtmlConfig config;
        config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        return config;
    }

    /** URL constants */
    static class Url {
        /** Link to a Bootstrap URL of CDN */
        static final String BOOTSTRAP_CSS = "https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css";
        /** Link to jQuery of CDN */
        static final String JQUERY_JS = "https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js";
        /** Source of the class */
        static final String SOURCE_URL = "https://github.com/pponec/ujorm/blob/"
                + "master"
                + "/samples/servlet/src/main/java/org/ujorm/ujoservlet/ajax/RegexpServlet.java";
    }

    /** CSS constants and identifiers */
    static class Css {
        /** Bootstrap form control CSS class name */
        static final String CONTROL_CSS = "form-control";
        /** CSS class name for the output box */
        static final String OUTPUT_CSS = "out";
        /** CSS class name for the output box */
        static final String SUBTITLE_CSS = "subtitle";
    }

    /** Servlet attributes */
    enum Attrib implements HttpParameter {
        REGEXP,
        TEXT;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
}
