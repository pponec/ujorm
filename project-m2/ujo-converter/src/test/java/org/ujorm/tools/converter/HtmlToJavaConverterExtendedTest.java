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

        assertEquals("body.addHeadingX(1)", lines.get(5).trim());
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

        assertEquals(".setTitle('\\'quoted\\'')", lines.get(6).trim());
        assertEquals(".setAttribute('data-val', 'back\\\\slash')", lines.next().trim());
        assertEquals("'''", lines.get(9).trim());
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

        assertEquals("'''", lines.get(7).trim());
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

        assertEquals("try (var ul = body.addUnorderedlist()) {", lines.get(5).trim());
        assertEquals("ul.addListItem()", lines.next().trim());
        assertEquals("try (var ol = body.addOrderedList()) {", lines.get(9).trim());
        assertEquals("body.addParagraph()", lines.get(13).trim());
    }

    /** Test empty head skipping */
    @Test
    void testEmptyHeadSkipping() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<html><head></head><body>Content</body></html>";
        var result = converter.convertHtmlToJavaElements(html, false);
        var lines = Lines.ofQuoted(result);

        // Empty head should be skipped
        assertEquals("", lines.get(4).trim().contains("addHead()") ? "addHead()" : "");
        assertEquals("html.addBody()", lines.get(4).trim());
    }

    /** Test custom tag handling */
    @Test
    void testCustomTagHandling() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<my-tag class='custom'>Value</my-tag>";
        var result = converter.convertHtmlToJavaElements(html, false);
        var lines = Lines.ofQuoted(result);

        // Custom (unknown) tags fall back to the generic addElement method
        assertEquals("body.addElement('my-tag', 'custom')", lines.get(5).trim());
        assertEquals(".addText('Value');", lines.next().trim());
    }

    /** Test anchor with href */
    @Test
    void testAnchorWithHref() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<a href='https://ujorm.org' class='link'>Ujorm</a>";
        var result = converter.convertHtmlToJavaElements(html, false);
        var lines = Lines.ofQuoted(result);

        assertEquals("body.addAnchor('https://ujorm.org', 'link')", lines.get(5).trim());
    }

    /** Test default title fallback for missing title tag */
    @Test
    void testDefaultTitleFallback() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<html><body>Hello</body></html>";
        var result = converter.convertHtmlToJavaElements(html, false);
        var lines = Lines.ofQuoted(result);

        assertEquals("try (var html = HtmlElement.niceOf('Demo', result)) {", lines.get(3).trim());
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

        assertEquals("try (var head = html.addHead()) {", lines.get(4).trim());
        assertEquals("head.addElement(Html.META)", lines.get(5).trim());
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

        assertEquals("script.addRawText('\\n' +", lines.get(6).trim());
        assertEquals("const q = \\'\\'\\''';", lines.get(8).trim());
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

}