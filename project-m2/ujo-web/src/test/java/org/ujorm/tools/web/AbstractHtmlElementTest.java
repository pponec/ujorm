package org.ujorm.tools.web;

import org.junit.jupiter.api.Test;
import org.ujorm.tools.web.request.HttpContext;

import static org.junit.jupiter.api.Assertions.*;

class AbstractHtmlElementTest {

    @Test
    public void test() {
        HttpContext context = HttpContext.of();
        try (HtmlElement html = AbstractHtmlElement.niceOf(getClass().getSimpleName(), context, "/css/regexp.css")) {
            try (Element body = html.addBody()) {
                body.addHeading("Simple form");
                try (Element form = body.addForm("form-inline")) {
                    form.addLabel("control-label").addText("Note:");
                    form.addSubmitButton("btn", "btn-primary")
                            .addText("Submit");
                }
            }
        }
        assertTrue(context.toString().contains("html"));
    }

}