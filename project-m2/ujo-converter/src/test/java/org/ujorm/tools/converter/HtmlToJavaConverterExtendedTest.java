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

        // Jsoup automatically wraps elements in <body>, so the call is performed on the body instance
        assertTrue(result.contains("body.addHeadingX(1)"));
        assertTrue(result.contains("body.addHeadingX(6, \"sub\")"));
        assertTrue(result.contains(".addText(\"Title 1\")"));
    }

    /** Test special characters escaping */
    @Test
    void testSpecialCharactersEscaping() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<div title='\"quoted\"' data-val='back\\slash'>Line 1\nLine 2</div>";
        var result = converter.convertHtmlToJavaElements(html, false);

        // Attributes use escapeJavaString
        assertTrue(result.contains("setTitle(\"\\\"quoted\\\"\")"));
        assertTrue(result.contains("setAttribute(\"data-val\", \"back\\\\slash\")"));

        // Multiline text is generated as a Java Text Block (""")
        assertTrue(result.contains("\"\"\""));
        assertTrue(result.contains("Line 1"));
        assertTrue(result.contains("Line 2"));
    }

    /** Test multiline text block */
    @Test
    void testMultilineTextBlock() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<div>First line\nSecond line\nThird line</div>";
        var result = converter.convertHtmlToJavaElements(html, true);

        assertTrue(result.contains("\"\"\""));
        assertTrue(result.contains("First line"));
        assertTrue(result.contains("Second line"));
    }

    /** Test tag overrides */
    @Test
    void testTagOverrides() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<ul><li>Item</li></ul><ol><li>Item</li></ol><p>Text</p>";
        var result = converter.convertHtmlToJavaElements(html, false);

        assertTrue(result.contains(".addUnorderedlist()"));
        assertTrue(result.contains(".addOrderedList()")); // Ujorm uses a capital 'L' here
        assertTrue(result.contains(".addListItem()"));
        assertTrue(result.contains(".addParagraph()"));
    }

    /** Test empty head skipping */
    @Test
    void testEmptyHeadSkipping() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<html><head></head><body>Content</body></html>";
        var result = converter.convertHtmlToJavaElements(html, false);

        // Empty head should be skipped
        assertFalse(result.contains("addHead()"));
        assertTrue(result.contains("addBody()"));
    }

    /** Test custom tag handling */
    @Test
    void testCustomTagHandling() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<my-tag class='custom'>Value</my-tag>";
        var result = converter.convertHtmlToJavaElements(html, false);

        // Custom (unknown) tags fall back to the generic addElement method
        assertTrue(result.contains(".addElement(\"my-tag\", \"custom\")"));
        assertTrue(result.contains(".addText(\"Value\")"));
    }

    /** Test anchor with href */
    @Test
    void testAnchorWithHref() throws IOException {
        var converter = new HtmlToJavaConverter();
        var html = "<a href='https://ujorm.org' class='link'>Ujorm</a>";
        var result = converter.convertHtmlToJavaElements(html, false);

        assertTrue(result.contains(".addAnchor(\"https://ujorm.org\", \"link\")"));
    }
}