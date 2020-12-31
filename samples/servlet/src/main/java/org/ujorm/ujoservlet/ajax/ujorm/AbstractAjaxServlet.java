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
package org.ujorm.ujoservlet.ajax.ujorm;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.Check;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.ao.JsonBuilder;

/**
 * A live example of the HtmlElement inside a servlet using a Dom4j library.
 * @author Pavel Ponec
 */
public abstract class AbstractAjaxServlet extends HttpServlet {
    /** Logger */
    private static final Logger LOGGER = Logger.getLogger(AbstractAjaxServlet.class.getName());
    /** Default AJAX request parameter name */
    public static final String DEFAULT_AJAX_REQUEST_PARAM = TableBuilder.DEFAULT_AJAX_REQUEST_PARAM;
    /** Javascript ajax request parameter */
    protected final CharSequence ajaxRequest;
    /** Javascript line separator */
    protected final String newLine;
    /** Input idle delay in millisec */
    protected final int idleDelay;

    /** Default constructor */
    public AbstractAjaxServlet(
            @Nonnull final CharSequence ajaxRequest,
            @Nonnull final String newLine,
            final int idleDelay) {
        this.ajaxRequest = ajaxRequest;
        this.idleDelay = idleDelay;
        this.newLine = newLine;
    }

    public AbstractAjaxServlet() {
        this(DEFAULT_AJAX_REQUEST_PARAM, "\n", 300);
    }
    
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param input servlet request
     * @param output servlet response
     * @param post It is a POST request
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doProcess(final HttpServletRequest input, final HttpServletResponse output, final boolean post) 
            throws ServletException, IOException {
        output.getWriter().append("Implement it!");
    }

    /**
     * Handles the HTTP method.
     * @param input servlet request
     * @param output servlet response
     * @param post It is a POST request
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void doDispatch(final HttpServletRequest input, final HttpServletResponse output, boolean post)
            throws ServletException, IOException {
        try {
            new ReqestDispatcher(input, output)
                    .onParam(getAjaxParam(), jsonBuilder -> doAjax(input, jsonBuilder))
                    .onDefault(() -> doProcess(input, output, post));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "GET error", e);
            output.setStatus(500);
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
    protected final void doGet(
            final HttpServletRequest input,
            final HttpServletResponse output) throws ServletException, IOException {
        doDispatch(input, output, false);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param input servlet request
     * @param output servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected final void doPost(
            final HttpServletRequest input,
            final HttpServletResponse output) throws ServletException, IOException {
        doDispatch(input, output, true);
    }

    /**
     * Implement an AJAX action
     * @param output Simple JSON writer in structure key-value.
     */
    protected void doAjax(HttpServletRequest input, JsonBuilder output)
            throws ServletException, IOException {
    }

    /**
     * Generate a Javascript
     * @param element Root element, where {@code null} value disable the javascript.
     * @param initFormSubmit Submit on the first form on load request
     * @param formSelector A form selector for submit
     * @param inputCssSelectors Array of CSS selector for autosubmit.
     */
    @Nonnull
    protected void writeJavascript(
            @Nullable final Element element,
            final boolean initFormSubmit,
            @Nullable final CharSequence formSelector,
            @Nonnull final CharSequence... inputCssSelectors) {
        if (element == null) {
            return;
        }
        element.addRawTexts(newLine, "", "<script>", "$(document).ready(function(){");
        if (Check.hasLength(inputCssSelectors)) {
                    final String inpSelectors = Stream.of(inputCssSelectors)
              //.map(t -> "." + t)
                .collect(Collectors.joining(", "));

            element.addRawTexts(newLine, ""
                    , "var globalTimeout = null;"
                    , "$('" + inpSelectors + "').keyup(function() {"
                    , "  if (globalTimeout != null) {"
                    , "    clearTimeout(globalTimeout);"
                    , "  }"
                    , "  globalTimeout = setTimeout(function() {"
                    , "    globalTimeout = null;"
                    , "    $('" + formSelector + "').submit();"
                    , "  }, " + idleDelay + ");"
                    , "});"
            );
        }{
            element.addRawTexts(newLine, ""
                    , "$('form').submit(function(event){"
                    , "  var data = $('" + formSelector + "').serialize();"
                    , "  $.ajax("
                          + "{ url: '?" + ajaxRequest + "=true'"
                          + ", type: 'POST'"
                          + ", data: data"
                          + ", timeout: 3000"
                          + ", error: function (xhr, ajaxOptions, thrownError) {"
                          ,  "   $('.subtitle').html('AJAX fails due: ' + thrownError);"
                          +  " }"
                    ,       ", success: function(result){"
                    , "    var jsn = JSON.parse(result);"
                    , "    $.each(jsn, function(key, value){"
                    , "      $(key).html(value);"
                    , "    })"
                    , "  }});"
                    , "  event.preventDefault();"
                    , "});"
                    , initFormSubmit ? "  $('" + formSelector + "').submit();" : ""
                    );
        }
        element.addRawTexts(newLine, "", "});", "</script>");
    }
    
    protected HttpParameter getAjaxParam() {
        return new HttpParameter() {
            @Override
            public String toString() {
                return AbstractAjaxServlet.DEFAULT_AJAX_REQUEST_PARAM;
            }
        };
    }
}
