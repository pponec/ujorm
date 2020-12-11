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
    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(AjaxServlet.class.getName());
    /** Link to a Bootstrap URL */
    private static final String BOOTSTRAP_CSS = "https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css";
    /** Link to jQuery */
    private static final String JQUERY_JS = "https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js";
    /** A common services */
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
            //html.addJavascriptLink(true, JQUERY_JS);
            html.addCssLink(BOOTSTRAP_CSS);
            html.addCssBody(service.getCss());
            html.getHead().addRawText("\n"
                    + "<script src='https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js'></script>\n"
                    + "<script>\n"
                    + getJavascript("#out")
                    + "\n</script>\n");

            try (Element body = html.getBody()) {
                body.addHeading(html.getTitle());
                try (Element form = body.addForm()
                        .setId("form")
                        .setMethod(Html.V_POST).setAction("?")) {
                    form.addInput("regexp")
                            .setName(REGEXP)
                            .setValue(REGEXP.value(input))
                            .setAttribute(Html.A_PLACEHOLDER, "Regular expression");
                    form.addTextArea("text")
                            .setAttribute(Html.A_PLACEHOLDER, "Plain Text")
                            .setName(TEXT)
                            .addText(TEXT.value(input));
                    form.addDiv().addButton("btn", "btn-primary").addText("Evaluate");
                    Message result = highlight(input);
                    form.addDiv("out", result.isError() ? "error" : null)
                            .setId("out")
                            .addRawText(highlight(input));
                }
                body.addElement(Html.HR);
                body.addTextTemplated("Version <{}.{}.{}>", 1, 2, 3);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Servlet failed", e);
        }
    }

    /** Create a CSS */
    @Nonnull
    public CharSequence getJavascript(String target) {
        return String.join("\n"
                , ""
                , "$(document).ready(function(){"
                , "  $('form').submit(function(event){"
                , "    var data = $('#form').serialize();"
                , "    $.ajax("
                        + "{ url: '?_ajax=y'"
                        + ", type: 'POST'"
                        + ", data: data"
                        + ", success: function(result){"
              //, "      alert('data:' + result);"
                , "      $('" + target + "').html(result);"
                , "    }});"
                , "    event.preventDefault();"
                , "  });"
                , "});"
                , "");
    }

    /** Build a HTML result */
    protected Message highlight(HttpServletRequest input) {
        return service.highlight(REGEXP.value(input, ""), TEXT.value(input, ""));
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
        final boolean ajax = Attrib._AJAX.value(input, "").equals("y");
        if (ajax) {
            doAjax(input, output);
        } else {
            doGet(input, output);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doAjax(
            final HttpServletRequest input,
            final HttpServletResponse output) throws ServletException, IOException {
        String regexp = REGEXP.value(input);
        String text = TEXT.value(input);
        boolean ajax = _AJAX.value(input, "").equals("y");

        HtmlConfig config = HtmlConfig.ofDefault();
        input.setCharacterEncoding(config.getCharset().toString());
        output.setCharacterEncoding(config.getCharset().toString());

        Message msg = highlight(input);
        String out = msg.isError()
                ? "<span class='error'>" + msg + "</span>"
                : msg.getText();
        output.getWriter().append(out);
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
