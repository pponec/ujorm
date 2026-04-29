/*
 * Copyright 2026 Pavel Ponec, https://github.com/pponec/demo-ajax
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.tutorial;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ujorm.tools.web.AbstractHtmlElement;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.web.ajax.JavaScriptWriter;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.json.JsonBuilder;
import org.ujorm.tools.web.request.HttpContextImpl;

import java.io.IOException;

import static org.ujorm.tools.web.ajax.JavaScriptWriter.DEFAULT_AJAX_REQUEST_PARAM;

/**
 * A minimalist tutorial example of AJAX using the ujo-web library with CSS classes.
 *
 * @author Pavel Ponec
 */
@WebServlet("/tutorial")
public class TutorialServlet extends HttpServlet {

    /** Handles the HTTP <code>GET</code> method to build the HTML page. */
    @Override
    protected final void doGet(HttpServletRequest request, HttpServletResponse response) {
        var title = "Uppercase Converter";
        var ctx = HttpContextImpl.of(request, response);

        try (var html = AbstractHtmlElement.of(title, ctx)) {
            html.getHead().addStyle().addRawText(Css.styles);
            new JavaScriptWriter().write(html.getHead());

            try (var body = html.addBody()) {
                body.setClass(Css.body);

                try (var container = body.addDiv(Css.container)) {
                    container.addHeading(title, Css.heading);
                    try (var form = container.addForm(Css.form).setMethod(Html.V_POST).setAction("?")) {
                        form.addInput(Css.input)
                                .setAttribute(Html.A_PLACEHOLDER, "Type something quietly...")
                                .setNameValue(Attrib.TEXT, getInputText(ctx));
                        form.addDiv().addButton(Css.btn).addText("YELL!");
                        printResult( form.addDiv(Css.output), ctx);
                    }
                }
            }
        }
    }

    private String getInputText(HttpContextImpl ctx) {
        return ctx.parameter(Attrib.TEXT, "yahoo");
    }

    /** Print the result to required element) */
    private void printResult(Element element, HttpContextImpl ctx) {
        element.addText(getInputText(ctx).toUpperCase());
    }

    protected final void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var ctx = HttpContextImpl.of(request, response);
        if (ctx.parameter(DEFAULT_AJAX_REQUEST_PARAM, Boolean::parseBoolean, false)) {
            try (var json = JsonBuilder.of(ctx)) {
                // Replace a body of element(s) selected by the CSS class:
                json.writeClass(Css.output, e -> printResult(e, ctx));
            }
        } else {
            doGet(request, response);
        }
    }

    /** HTTP parameter(s) */
    enum Attrib implements HttpParameter {
        TEXT;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    /** CSS constants */
    abstract static class Css {
        /** CSS class for body */
        static final String body = "tutorial-body";
        /** CSS class for container */
        static final String container = "tutorial-container";
        /** CSS class for heading */
        static final String heading = "tutorial-heading";
        /** CSS class for form */
        static final String form = "tutorial-form";
        /** CSS class for input */
        static final String input = "tutorial-input";
        /** CSS class for button */
        static final String btn = "tutorial-btn";
        /** CSS class name for the output box targeted by AJAX */
        static final String output = "ajax-output";
        /** Raw CSS rules */
        static final String styles = """
                .tutorial-body { font-family: system-ui, sans-serif; background-color: #f4f6f9; display: flex; justify-content: center; padding-top: 50px; margin: 0; }
                .tutorial-container { background: white; padding: 30px; border-radius: 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); width: 100%; max-width: 400px; }
                .tutorial-heading { text-align: center; color: #333; margin-top: 0; margin-bottom: 20px; }
                .tutorial-form { display: flex; flex-direction: column; gap: 15px; }
                .tutorial-input { padding: 10px; border: 1px solid #ccc; border-radius: 5px; font-size: 16px; width: 100%; box-sizing: border-box; }
                .tutorial-btn { padding: 10px; background-color: #0d6efd; color: white; border: none; border-radius: 5px; font-size: 16px; font-weight: bold; cursor: pointer; }
                .ajax-output { margin-top: 10px; padding: 15px; background-color: #e9ecef; border-radius: 5px; text-align: center; font-size: 20px; font-weight: bold; color: #212529; min-height: 24px; }
                """;
    }
}