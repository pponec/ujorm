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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.HtmlElement;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.ao.JsonBuilder;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;
import static org.ujorm.ujoservlet.ajax.RegexpServlet.Attrib.*;

/**
 * A live example of the HtmlElement inside a servlet using a Dom4j library.
 * @author Pavel Ponec
 */
@WebServlet(RegexpServlet.URL_PATTERN)
public class RegexpServlet extends AbstractAjaxServlet {

    /** URL pattern */
    public static final String URL_PATTERN = "/RegexpServlet";
    /** Enable AJAX feature */
    private static final boolean AJAX_ENABLED = true;
    /** Link to a Bootstrap URL of CDN */
    private static final String BOOTSTRAP_CSS = "https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css";
    /** Link to jQuery of CDN */
    private static final String JQUERY_JS = "https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js";
    /** Source of the class */
    private static final String SOURCE_URL = "https://github.com/pponec/ujorm/blob/"
            + "58c0d8a170bfa25b8fdc4d5ebbdcd27dbd19c2dd"
            + "/samples/servlet/src/main/java/org/ujorm/ujoservlet/ajax/RegexpServlet.java";
    /** Form identifier */
    private static final String FORM_ID = "form";
    /** Bootstrap form control CSS class name */
    private static final String CSS_CONTROL = "form-control";
    /** CSS class name for the output box */
    private static final String CSS_OUTPUT = "out";
    /** CSS class name for the output box */
    private static final String CSS_SUBTITLE = "subtitle";
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
    protected void doProcess(
            final HttpServletRequest input,
            final HttpServletResponse output,
            final boolean post) throws ServletException, IOException {
        try (HtmlElement html = HtmlElement.of(input, output, getConfig("Regular expression tester"))) {
            html.addJavascriptLink(false, JQUERY_JS);
            html.addCssLink(BOOTSTRAP_CSS);
            html.addCssBodies(newLine, service.getCss());
            writeJavascript((AJAX_ENABLED ? html.getHead() : null), true,
                    "#" + FORM_ID,
                    "#" + REGEXP,
                    "#" + TEXT);
            Message msg = highlight(input);
            try (Element body = html.getBody()) {
                body.addHeading(html.getTitle());
                body.addDiv(CSS_SUBTITLE).addText("");
                try (Element form = body.addForm()
                        .setId(FORM_ID)
                        .setMethod(Html.V_POST).setAction("?")) {
                    form.addInput(CSS_CONTROL)
                            .setId(REGEXP)
                            .setName(REGEXP)
                            .setValue(REGEXP.of(input))
                            .setAttribute(Html.A_PLACEHOLDER, "Regular expression");
                    form.addTextArea(CSS_CONTROL)
                            .setId(TEXT)
                            .setName(TEXT)
                            .setAttribute(Html.A_PLACEHOLDER, "Plain Text")
                            .addText(TEXT.of(input));
                    form.addDiv().addButton("btn", "btn-primary").addText("Evaluate");
                    form.addDiv(CSS_CONTROL, CSS_OUTPUT).addRawText(msg);
                }
                body.addElement(Html.HR);
                body.addAnchor(SOURCE_URL).addTextTemplated("Version <{}.{}.{}>", 1, 2, 3);
            }
        }
    }

    /**
     * Return lighlited text in HTML format according a regular expression
     * @param input servlet request
     * @param output A JSON writer
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doAjax(HttpServletRequest input, JsonBuilder output)
            throws ServletException, IOException {
            final Message msg = highlight(input);
            output.writeClass(CSS_OUTPUT, e -> e.addElementIf(msg.isError(), Html.SPAN, "error")
                    .addRawText(msg));
            output.writeClass(CSS_SUBTITLE, "AJAX ready");
    }

    /** Build a HTML result */
    protected Message highlight(HttpServletRequest input) {
        return service.highlight(
                REGEXP.of(input, ""),
                TEXT.of(input, ""));
    }

    /** Create a configuration of HTML model */
    private DefaultHtmlConfig getConfig(String title) {
        DefaultHtmlConfig config;
        config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        return config;
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
