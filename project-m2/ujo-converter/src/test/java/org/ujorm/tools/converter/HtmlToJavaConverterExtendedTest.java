package org.ujorm.tools.converter;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

/** Short comment */
class HtmlToJavaConverterExtendedTest {

    /** Test heading conversion */
    @Test
    void testHeadingConversion() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<h1>Title 1</h1><h6 class='sub'>Title 6</h6>";
        var result = converter.convertHtmlToJavaElements(html, false);
        var lines = Lines.ofQuoted(result);

        assertTrue(lines.findLine("body.addHeadingX(1)"));
        assertEquals(".addText('Title 1');", lines.next().trim());
        assertEquals("body.addHeadingX(6, 'sub')", lines.next().trim());
    }

    /** Test special characters escaping */
    @Test
    void testSpecialCharactersEscaping() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<div title='\"quoted\"' data-val='back\\slash'>Line 1\nLine 2</div>";
        var result = converter.convertHtmlToJavaElements(html, false);
        var lines = Lines.ofQuoted(result);

        assertTrue(lines.findLine(".setTitle('\\'quoted\\'')"));
        assertEquals(".setAttribute('data-val', 'back\\\\slash')", lines.next().trim());
        assertTrue(lines.findLine("'''"));
        assertEquals("Line 1", lines.next().trim());
        assertEquals("Line 2\\", lines.next().trim());
    }

    /** Test multiline text block */
    @Test
    void testMultilineTextBlock() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<div>First line\nSecond line\nThird line</div>";
        var result = converter.convertHtmlToJavaElements(html, true);
        var lines = Lines.ofQuoted(result);

        assertTrue(lines.findLine("\"\"\""));
        assertEquals("First line", lines.next().trim());
        assertEquals("Second line", lines.next().trim());
    }

    /** Test tag overrides */
    @Test
    void testTagOverrides() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<ul><li>Item</li></ul><ol><li>Item</li></ol><p>Text</p>";
        var result = converter.convertHtmlToJavaElements(html, false);
        var lines = Lines.ofQuoted(result);

        assertTrue(lines.findLine("try (var ul = body.addUnorderedlist()) {"));
        assertEquals("ul.addListItem()", lines.next().trim());
        assertTrue(lines.findLine("try (var ol = body.addOrderedList()) {"));
        assertTrue(lines.findLine("body.addParagraph()"));
    }

    /** Test empty head skipping */
    @Test
    void testEmptyHeadSkipping() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<html><head></head><body>Content</body></html>";
        var result = converter.convertHtmlToJavaElements(html, false);
        var lines = Lines.ofQuoted(result);

        // Empty head should be skipped
        assertFalse(result.contains("addHead()"));
        assertTrue(lines.findLine("html.addBody()"));
    }

    /** Test custom tag handling */
    @Test
    void testCustomTagHandling() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<my-tag class='custom'>Value</my-tag>";
        var result = converter.convertHtmlToJavaElements(html, false);
        var lines = Lines.ofQuoted(result);

        // Custom (unknown) tags fall back to the generic addElement method
        assertTrue(lines.findLine("body.addElement('my-tag', 'custom')"));
        assertEquals(".addText('Value');", lines.next().trim());
    }

    /** Test anchor with href */
    @Test
    void testAnchorWithHref() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<a href='https://ujorm.org' class='link'>Ujorm</a>";
        var result = converter.convertHtmlToJavaElements(html, false);
        var lines = Lines.ofQuoted(result);

        assertTrue(lines.findLine("body.addAnchor('https://ujorm.org', 'link')"));
    }

    /** Test default title fallback for missing title tag */
    @Test
    void testDefaultTitleFallback() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<html><body>Hello</body></html>";
        var result = converter.convertHtmlToJavaElements(html, false);
        var lines = Lines.ofQuoted(result);

        assertTrue(lines.findLine("try (var html = HtmlElement.niceOf('Demo', ctx)) {"));
    }

    /** Test non-empty head is preserved */
    @Test
    void testNonEmptyHeadIsGenerated() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = """
                <html>
                  <head><meta name='x' content='y'></head>
                  <body>Content</body>
                </html>
                """;
        var result = converter.convertHtmlToJavaElements(html, false);
        var lines = Lines.ofQuoted(result);

        assertTrue(lines.findLine("try (var head = html.addHead()) {"));
        assertEquals("head.addElement(Html.META)", lines.next().trim());
        assertEquals(".setName('x')", lines.next().trim());
        assertEquals(".setAttribute(Html.A_CONTENT, 'y');", lines.next().trim());
    }

    /** Test tricky raw text content escaping */
    @Test
    void testRawTextEscapingInScript() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = """
                <script>
                const q = "\"\"\"";
                const path = "c:\\\\tmp";
                </script>
                """;
        var result = converter.convertHtmlToJavaElements(html, true);
        var lines = Lines.ofQuoted(result);

        assertTrue(lines.findLine("script.addRawText('\\n' +"));
        assertTrue(lines.findLine("const q = \\'\\'\\''';"));
        assertEquals("const path = 'c:\\\\\\\\tmp';", lines.next().trim());
    }

    /** Test Appendable overload produces same output */
    @Test
    void testAppendableOverload() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<div class='a b'>X</div>";

        var expected = converter.convertHtmlToJavaElements(html, false);
        var writer = new StringBuilder();
        converter.convertHtmlToJavaElements(html, false, writer);

        assertEquals(expected, writer.toString());
    }

    /** Test separated CSS styles constants */
    @Test
    void testSeparatedCssStyles() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<div class='head row'>X</div>";
        var result = converter.convertHtmlToJavaElements(html, false, true);
        var lines = Lines.ofQuoted(result);

        assertTrue(lines.findLine("body.addDiv(Css.head, Css.row)"));
        assertTrue(lines.findLine("static final class Css {"));
        assertEquals("static final String head = 'head';", lines.next().trim());
        assertEquals("static final String row = 'row';", lines.next().trim());
    }

    /** Verify varargs usage and constants for multiple CSS classes */
    @Test
    void testSeparatedCssStylesVarargsWithMultipleElements() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = """
                <div class='head row'>A</div>
                <div class='panel body row'>B</div>
                """;
        var result = converter.convertHtmlToJavaElements(html, false, true);
        var lines = Lines.ofQuoted(result);

        // method call uses separate Css constants (varargs-like usage)
        assertTrue(lines.findLine("body.addDiv(Css.head, Css.row)"));
        assertTrue(lines.findLine("body.addDiv(Css.panel, Css.body, Css.row)"));

        // constants are created for each distinct CSS class
        assertTrue(lines.findLine("static final class Css {"));
        assertEquals("static final String head = 'head';", lines.next().trim());
        assertEquals("static final String row = 'row';", lines.next().trim());
        assertEquals("static final String panel = 'panel';", lines.next().trim());
        assertEquals("static final String body = 'body';", lines.next().trim());
    }
}