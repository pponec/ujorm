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

import org.ujorm.ujoservlet.ajax.ao.Service;
import org.ujorm.ujoservlet.ajax.ao.Message;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.HtmlElement;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.json.JsonBuilder;
import org.ujorm.tools.web.ajax.JavaScriptWriter;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;
import static org.ujorm.ujoservlet.ajax.RegexpServlet.Attrib.*;
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
    /** Form identifier */
    private static final String FORM_ID = "form";
    /** Bootstrap form control CSS class name */
    private static final String CONTROL_CSS = "form-control";
    /** CSS class name for the output box */
    private static final String OUTPUT_CSS = "out";
    /** CSS class name for the output box */
    private static final String SUBTITLE_CSS = "subtitle";
    /** AJAX param */
    private static final HttpParameter AJAX = JavaScriptWriter.DEFAULT_AJAX_REQUEST_PARAM;
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
            html.addCssBodies(html.getConfig().getNewLine(), service.getCss());
            if (AJAX_ENABLED) {
                new JavaScriptWriter( 
                        "#" + REGEXP, 
                        "#" + TEXT)
                        .setFormSelector("#" + FORM_ID)
                        .write(html.getHead());   
            }
            Message msg = highlight(input);
            try (Element body = html.getBody()) {
                body.addHeading(html.getTitle());
                body.addDiv(SUBTITLE_CSS).addText("");
                try (Element form = body.addForm()
                        .setId(FORM_ID)
                        .setMethod(Html.V_POST).setAction("?")) {
                    form.addInput(CONTROL_CSS)
                            .setId(REGEXP)
                            .setName(REGEXP)
                            .setValue(REGEXP.of(input))
                            .setAttribute(Html.A_PLACEHOLDER, "Regular expression");
                    form.addTextArea(CONTROL_CSS)
                            .setId(TEXT)
                            .setName(TEXT)
                            .setAttribute(Html.A_PLACEHOLDER, "Plain Text")
                            .addText(TEXT.of(input));
                    form.addDiv().addButton("btn", "btn-primary").addText("Evaluate");
                    form.addDiv(CONTROL_CSS, OUTPUT_CSS).addRawText(msg);
                }
                body.addElement(Html.HR);
                body.addAnchor(SOURCE_URL).addTextTemplated("Version <{}.{}.{}>", 1, 2, 3);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Internal server error", e);
            output.setStatus(500);
        }
    }

    @Override
    protected void doPost(HttpServletRequest input, HttpServletResponse output) throws ServletException, IOException {
        if (AJAX.isTrue(input)) {
            doAjax(input, JsonBuilder.of(input, output, getConfig("?"))).close();
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
    @Nonnull
    protected JsonBuilder doAjax(HttpServletRequest input, JsonBuilder output)
            throws ServletException, IOException {
            final Message msg = highlight(input);
            output.writeClass(OUTPUT_CSS, e -> e.addElementIf(msg.isError(), Html.SPAN, "error")
                    .addRawText(msg));
            output.writeClass(SUBTITLE_CSS, "AJAX ready");
            return output;
    }

    /** Build a HTML result */
    protected Message highlight(HttpServletRequest input) {
        return service.highlight(
                REGEXP.of(input, ""),
                TEXT.of(input, ""));
    }

    /** Create a configuration of HTML model */
    private DefaultHtmlConfig getConfig(@Nonnull String title) {
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
                + "6b71c4611172f91adae17bcf030e982cb785358a"
                + "/samples/servlet/src/main/java/org/ujorm/ujoservlet/ajax/RegexpServlet.java";
    }

    /** Servlet attributes */
    enum Attrib implements HttpParameter {
        REGEXP,
        TEXT;
        
        @Override
        public String toString() {
            return name().toLowerCase(Locale.ENGLISH);
        }
    }
}
