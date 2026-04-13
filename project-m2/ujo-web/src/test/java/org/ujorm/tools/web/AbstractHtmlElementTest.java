package org.ujorm.tools.web;

import org.junit.jupiter.api.Test;
import org.ujorm.tools.web.request.HttpContext;
import org.ujorm.tools.xml.config.HtmlConfig;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/** Short comment */
class AbstractHtmlElementTest {

    @Test
    public void testBasicStructure() {
        var context = HttpContext.of();
        try (var html = AbstractHtmlElement.of(context, null)) {
            try (var body = html.getBody()) {
                body.addHeading("Hello World");
            }
        }
        var result = context.toString();
        assertTrue(result.contains("<!DOCTYPE html>"));
        assertTrue(result.contains("<html"));
        assertTrue(result.contains("<head>"));
        assertTrue(result.contains("<h1>Hello World</h1>"));
        assertTrue(result.contains("</body>"));
        assertTrue(result.contains("</html>"));
    }

    @Test
    public void testAttributesAndLang() {
        var context = HttpContext.of();
        try (var html = AbstractHtmlElement.of(context, null)) {
            html.setAttribute(Html.A_LANG, "cs");
            html.getBody().addText("Text");
        }
        var result = context.toString();
        assertTrue(result.contains("lang=\"cs\""));
    }

    @Test
    public void testJavascriptLinks() {
        var context = HttpContext.of();
        try (var html = AbstractHtmlElement.of(context, null)) {
            html.addJavascriptLink(true, "https://example.com/script.js");
            html.addJavascriptBody("console.log('Hello');", "alert('World');");
            html.addBody().addText("Body content");
        }
        var result = context.toString();
        assertTrue(result.contains("<script src=\"https://example.com/script.js\" defer=\"defer\""));
        assertTrue(result.contains("console.log('Hello');\nalert('World');"));
    }

    @Test
    public void testCssLinksAndBodies() {
        var context = HttpContext.of();
        try (var html = AbstractHtmlElement.of(context, null)) {
            html.addCssLink("style.css");
            html.addCssBody("body { color: red; }");
            html.addCssBodies("\n", ".main { margin: 0; }", ".footer { padding: 0; }");
            html.addBody();
        }
        var result = context.toString();
        assertTrue(result.contains("<link href=\"style.css\" rel=\"stylesheet\""));
        assertTrue(result.contains("<style>body { color: red; }</style>"));
        assertTrue(result.contains(".main { margin: 0; }\n.footer { padding: 0; }"));
    }

    @Test
    public void testConfiguration() {
        var config = HtmlConfig.ofDefault();
        config.setTitle("Custom Title");
        config.setCharset(StandardCharsets.UTF_16);

        var context = HttpContext.of();
        try (var html = AbstractHtmlElement.of(context, config)) {
            html.addBody().addText("Content");
        }
        var result = context.toString();
        assertTrue(result.contains("<title>Custom Title</title>"));
        assertTrue(result.contains("<meta charset=\"UTF-16\""));
    }

    @Test
    public void testNesting() {
        var context = HttpContext.of();
        var htmlElement = AbstractHtmlElement.of(context, null);

        htmlElement.nest(html -> {
            html.addHead().addElement(Html.TITLE).addText("Nested Title");
            html.addBody().addText("Nested Content");
        });

        var result = context.toString();
        assertTrue(result.contains("<title>Nested Title</title>"));
        assertTrue(result.contains("Nested Content"));
    }

    @Test
    public void testNiceOfFactory() {
        var context = HttpContext.of();
        try (var html = AbstractHtmlElement.niceOf("Nice Page", context, "style1.css", "style2.css")) {
            html.addBody().addHeading("Title");
        }
        var result = context.toString();

        // Zkoumáme přítomnost nových řádků potvrzujících nice format
        assertTrue(result.contains("\n"));
        assertTrue(result.contains("<body"));
        assertTrue(result.contains("<title>Nice Page</title>"));
        assertTrue(result.contains("href=\"style1.css\""));
        assertTrue(result.contains("href=\"style2.css\""));
    }

    @Test
    public void testLazyHeaderOnClose() {
        var context = HttpContext.of();

        // Nevytváříme explicitně tělo dokumentu ani nevkládáme obsah přímo do elementu html,
        // čímž předejdeme uzavření elementu pro zápis atributů před voláním initHeader.
        try (var html = AbstractHtmlElement.of(context, null)) {
            // Prázdný blok
        }
        var result = context.toString();

        // Hlavička by se měla vygenerovat automaticky při zavolání close()
        assertTrue(result.contains("<head>"));
        assertTrue(result.contains("<meta charset=\"UTF-8\""));
    }
}