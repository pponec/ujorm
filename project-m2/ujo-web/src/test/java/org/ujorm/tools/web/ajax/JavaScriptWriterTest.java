package org.ujorm.tools.web.ajax;

import org.junit.jupiter.api.Test;
import org.ujorm.tools.web.HtmlElement;
import org.ujorm.tools.web.request.HttpContext;

import java.time.Duration;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

/** Tests for the JavaScriptWriter class */
class JavaScriptWriterTest {

    /** Test of default configuration and basic write */
    @Test
    void testDefaultWrite() {
        var context = HttpContext.of();
        try (var html = HtmlElement.of(context)) {
            var writer = new JavaScriptWriter();
            writer.write(html.getHead());
        }

        var result = context.toString();

        // Assert basic script tags and default values
        assertTrue(result.contains("<script"));
        assertTrue(result.contains("</script>"));
        assertTrue(result.contains("ujorm1={"));
        assertTrue(result.contains("delayMs:250"));
        assertTrue(result.contains("document.querySelectorAll(\"form\")"));
        assertTrue(result.contains("input:not([type='button']),textarea,select"));

        // Assert onLoadSubmit is false by default (not generated)
        assertFalse(result.contains("ujorm1.process(null,form)"));
    }

    /** Test custom properties and fluent setters */
    @Test
    void testCustomPropertiesAndSetters() {
        var context = HttpContext.of();
        try (var html = HtmlElement.of(context)) {
            var writer = new JavaScriptWriter()
                    .setFormSelector(".ajax-form")
                    .setIdleDelay(Duration.ofMillis(500))
                    .setOnLoadSubmit(true)
                    .setNewLine("\r\n");

            writer.write(html.getHead());
        }

        var result = context.toString();

        assertTrue(result.contains("delayMs:500"));
        assertTrue(result.contains("document.querySelectorAll(\".ajax-form\")"));

        // Assert onLoadSubmit snippet is generated
        assertTrue(result.contains("document.querySelectorAll('.ajax-form').forEach(form=>{ujorm1.process(null,form)});"));
    }

    /** Test generating Javascript with custom function map */
    @Test
    void testWriteWithFunctionMap() {
        var context = HttpContext.of();
        try (var html = HtmlElement.of(context)) {
            var writer = new JavaScriptWriter();

            // Using LinkedHashMap to guarantee iteration order for the assertion
            var fceMap = new LinkedHashMap<String, String>();
            fceMap.put("onSuccess", "console.log('Success');");
            fceMap.put("onError", "console.error('Error');");

            writer.write(html.getHead(), fceMap);
        }

        var result = context.toString();

        assertTrue(result.contains("fceMap:{ onSuccess(){console.log('Success');}, onError(){console.error('Error');}}"));
    }

    /** Test constructor with custom input selectors */
    @Test
    void testCustomInputSelectors() {
        var context = HttpContext.of();
        try (var html = HtmlElement.of(context)) {
            var writer = new JavaScriptWriter(".custom-input", ".custom-select");
            writer.write(html.getHead());
        }

        var result = context.toString();
        assertTrue(result.contains(".custom-input,.custom-select"));
    }

    /** Test constructor with empty input selectors (edge case) */
    @Test
    void testEmptyInputSelectors() {
        var context = HttpContext.of();
        try (var html = HtmlElement.of(context)) {
            var writer = new JavaScriptWriter(new CharSequence[0]);
            writer.write(html.getHead());
        }

        var result = context.toString();
        assertTrue(result.contains("#!@_")); // Fallback for no selection
    }

    /** Test getters and setters that don't directly impact the core JS template but store state */
    @Test
    void testCoverageOfUnusedGettersAndSetters() {
        var writer = new JavaScriptWriter();

        writer.setAjaxTimeout(Duration.ofSeconds(15));
        assertEquals(Duration.ofSeconds(15), writer.ajaxTimeout);

        writer.setSubtitleSelector(".error-sub");
        assertEquals(".error-sub", writer.errorSelector);

        writer.setErrorMessage("Custom error");
        assertEquals("Custom error", writer.errorMessage);

        writer.setAjaxRequestPath("/my-ajax-endpoint");
        assertEquals("/my-ajax-endpoint", writer.ajaxRequestPath);
        assertEquals(2, writer.version); // setAjaxRequestPath sets version to 2

        writer.setVersion(5);
        assertEquals(5, writer.version);

        writer.setSortable(42);
        assertEquals(42, writer.getFceOrder());

        writer.setAjax(false);
        assertFalse(writer.isAjax());

        writer.setAjax(true);
        assertTrue(writer.isAjax());
    }

    /** Test full constructor */
    @Test
    void testFullConstructor() {
        var writer = new JavaScriptWriter(
                Duration.ofSeconds(1),
                JavaScriptWriter.DEFAULT_AJAX_REQUEST_PARAM,
                JavaScriptWriter.DEFAULT_SORT_REQUEST_PARAM,
                "input.test"
        );

        assertEquals(Duration.ofSeconds(1), writer.idleDelay);
        assertSame(JavaScriptWriter.DEFAULT_AJAX_REQUEST_PARAM, writer.ajaxRequestParam);
        assertSame(JavaScriptWriter.DEFAULT_SORT_REQUEST_PARAM, writer.sortRequestParam);
        assertEquals("input.test", writer.inputCssSelectors[0]);
    }
}