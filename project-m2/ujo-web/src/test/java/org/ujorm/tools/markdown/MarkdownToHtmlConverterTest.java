package org.ujorm.tools.markdown;

import org.junit.jupiter.api.Test;
import org.ujorm.tools.Lines;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.xml.builder.XmlPrinter;
import org.ujorm.tools.xml.config.HtmlConfig;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the MarkdownToHtmlConverter class.
 */
class MarkdownToHtmlConverterTest {

    /** Tests headings conversion for all six levels. */
    @Test
    void testHeadings() {
        var markdown = """
                # Heading 1
                ## Heading 2
                ### Heading 3
                #### Heading 4
                ##### Heading 5
                ###### Heading 6
                ####### Not a heading
                """;

        var lines = htmlLines(render(markdown));

        assertTrue(lines.findLine("<h1>Heading 1</h1>"));
        assertEquals("<h2>Heading 2</h2>", lines.next());
        assertEquals("<h3>Heading 3</h3>", lines.next());
        assertEquals("<h4>Heading 4</h4>", lines.next());
        assertEquals("<h5>Heading 5</h5>", lines.next());
        assertEquals("<h6>Heading 6</h6>", lines.next());
        assertFalse(lines.findLine("<h7>"));
        assertEquals("<p>####### Not a heading</p>", lines.next());
    }

    /** Tests paragraphs and inline formatting (bold, italic, link, image). */
    @Test
    void testParagraphsAndInlineFormatting() {
        var markdown = """
                This is a paragraph with **bold text**, _italics_, a [link](https://example.com) and an ![logo](https://example.com/img.png).
                
                Another paragraph on a single line.
                """;

        var lines = htmlLines(render(markdown));

        assertTrue(lines.findLine("<strong>bold text</strong>"));
        assertTrue(lines.findLine("<em>italics</em>"));
        assertTrue(lines.findLine("<a href=\"https://example.com\">link</a>"));
        assertTrue(lines.findLine("<img alt=\"logo\" src=\"https://example.com/img.png\"/>"));
        assertTrue(lines.findLine("<p>Another paragraph on a single line.</p>"));
    }

    /** Tests inline code spans delimited by backticks. */
    @Test
    void testInlineCodeSpans() {
        var markdown = """
                Use `code` in text.
                
                Use `` `quoted` `` in text.
                
                Unclosed `marker stays literal.
                """;

        var lines = htmlLines(render(markdown));

        assertTrue(lines.findLine("<p>Use <code>code</code> in text.</p>"));
        assertEquals("<p>Use <code>`quoted`</code> in text.</p>", lines.next());
        assertEquals("<p>Unclosed `marker stays literal.</p>", lines.next());
    }

    /** Tests that inline code escapes HTML and does not parse nested markdown. */
    @Test
    void testInlineCodeEscapesHtmlAndSuppressesFormatting() {
        var markdown = "Inline `<tag>` and `**not bold**`.";

        var lines = htmlLines(render(markdown));

        assertTrue(lines.findLine("<p>Inline <code>&lt;tag&gt;</code> and <code>**not bold**</code>.</p>"));
        assertFalse(lines.findLine("<strong>not bold</strong>"));
    }

    /** Tests unordered and ordered lists. */
    @Test
    void testLists() {
        var markdown = """
                - Item 1
                - Item **2**
                
                1. First
                2. Second
                """;

        var lines = Lines.of(render(markdown));

        assertTrue(lines.findLine("<ul><li>Item 1</li><li>Item <strong>2</strong></li></ul>"));
        assertTrue(lines.findLine("<ol><li>First</li><li>Second</li></ol>"));
    }

    /** Tests unordered lists with alternative markers (* and +). */
    @Test
    void testAlternativeListMarkers() {
        var markdown = """
                * Item A
                + Item B
                - Item C
                """;

        var lines = Lines.of(render(markdown));

        assertTrue(lines.findLine("<ul><li>Item A</li><li>Item B</li><li>Item C</li></ul>"));
    }

    /** Tests wrapping of multi-line paragraphs. */
    @Test
    void testMultiLineParagraph() {
        var markdown = """
                This is line one.
                This is line two.
                This is line three.
                """;

        var lines = htmlLines(render(markdown));

        assertTrue(lines.findLine("<p>This is line one. This is line two. This is line three.</p>"));
    }

    /** Tests that a trailing backslash produces a forced {@code <br/>}. */
    @Test
    void testForcedLineBreak() {
        var markdown = """
                line one\\
                line two
                
                next paragraph
                """;

        var lines = htmlLines(render(markdown));

        assertTrue(lines.findLine("<p>line one<br/>line two</p>"));
        assertEquals("<p>next paragraph</p>", lines.next());
    }

    /** Tests that two trailing spaces no longer produce a forced {@code <br/>}. */
    @Test
    void testTrailingSpacesDoNotForceLineBreak() {
        var markdown = """
                line one\s\s
                line two
                """;

        var lines = htmlLines(render(markdown));

        assertTrue(lines.findLine("<p>line one line two</p>"));
        assertFalse(lines.findLine("<br/>"));
    }

    /** Tests that empty strings and extra new lines do not produce empty paragraphs. */
    @Test
    void testEmptyLinesHandling() {
        var markdown = """
                
                # Title
                
                
                
                Paragraph
                
                """;

        var lines = htmlLines(render(markdown));

        assertTrue(lines.findLine("<h1>Title</h1>"));
        assertEquals("<p>Paragraph</p>", lines.next());
        assertFalse(lines.findLine("<p></p>"), "Empty paragraphs must not be generated");
    }

    /** Tests blockquote rendering. */
    @Test
    void testBlockquote() {
        var markdown = """
                > Quoted line one
                > Quoted line two
                
                Outside.
                """;

        var lines = htmlLines(render(markdown));

        assertTrue(lines.findLine("<blockquote>"));
        assertEquals("<p>Quoted line one Quoted line two</p>", lines.next());
        assertEquals("</blockquote>", lines.next());
        assertEquals("<p>Outside.</p>", lines.next());
    }

    /** Tests fenced code block (triple backticks). */
    @Test
    void testFencedCodeBlock() {
        var markdown = """
                ```
                int x = 1;
                System.out.println(x);
                ```
                """;

        var lines = htmlLines(render(markdown));

        assertTrue(lines.findLine("<pre>"));
        assertEquals("<code>int x = 1;", lines.next());
        assertEquals("System.out.println(x);", lines.next());
        assertEquals("</code>", lines.next());
        assertEquals("</pre>", lines.next());
    }

    /** Tests indented code block (4 spaces or tab). */
    @Test
    void testIndentedCodeBlock() {
        var markdown = """
                Before block.
                
                    line one
                    line two
                
                After block.
                """;

        var lines = htmlLines(render(markdown));

        assertTrue(lines.findLine("<p>Before block.</p>"));
        assertEquals("<pre>", lines.next());
        assertEquals("<code>line one", lines.next());
        assertEquals("line two</code>", lines.next());
        assertEquals("</pre>", lines.next());
        assertEquals("<p>After block.</p>", lines.next());
    }

    /** Tests horizontal rule. */
    @Test
    void testHorizontalRule() {
        var markdown = """
                Above
                
                ---
                
                Below
                """;

        var lines = htmlLines(render(markdown));

        assertTrue(lines.findLine("<p>Above</p>"));
        assertEquals("<hr/>", lines.next());
        assertEquals("<p>Below</p>", lines.next());
    }

    /** Tests alternative horizontal rules (*** and ___). */
    @Test
    void testAlternativeHorizontalRules() {
        var markdown = """
                ***
                ___
                """;

        var lines = htmlLines(render(markdown));

        assertTrue(lines.findLine("<hr/>"));
        assertEquals("<hr/>", lines.next());
        var html = lines.toString();
        assertTrue(html.indexOf("<hr/>") != html.lastIndexOf("<hr/>"));
    }

    /** Tests standard GFM tables. */
    @Test
    void testTable() {
        var markdown = """
                | Name | Value |
                |------|------:|
                | One  | 1     |
                | Two  | **2** |
                """;

        var lines = htmlLines(render(markdown));

        assertTrue(lines.findLine("<table>"));
        assertEquals("<thead>", lines.next());
        assertEquals("<tr>", lines.next());
        assertEquals("<th>Name</th>", lines.next());
        assertEquals("<th>Value</th>", lines.next());
        assertEquals("</tr>", lines.next());
        assertEquals("</thead>", lines.next());
        assertEquals("<tbody>", lines.next());
        assertEquals("<tr>", lines.next());
        assertEquals("<td>One</td>", lines.next());
        assertEquals("<td>1</td>", lines.next());
        assertEquals("</tr>", lines.next());
        assertEquals("<tr>", lines.next());
        assertEquals("<td>Two</td>", lines.next());
        assertEquals("<td>", lines.next());
        assertEquals("<strong>2</strong>", lines.next());
        assertEquals("</td>", lines.next());
        assertEquals("</tr>", lines.next());
        assertEquals("</tbody>", lines.next());
        assertEquals("</table>", lines.next());
    }

    /** Tests GFM tables without outer pipe characters. */
    @Test
    void testTableWithoutOuterPipes() {
        var markdown = """
                Name | Value
                --- | ---
                One | 1
                """;

        var lines = htmlLines(render(markdown));

        assertTrue(lines.findLine("<table>"));
        assertEquals("<thead>", lines.next());
        assertEquals("<tr>", lines.next());
        assertEquals("<th>Name</th>", lines.next());
        assertEquals("<th>Value</th>", lines.next());
        assertEquals("</tr>", lines.next());
        assertEquals("</thead>", lines.next());
        assertEquals("<tbody>", lines.next());
        assertEquals("<tr>", lines.next());
        assertEquals("<td>One</td>", lines.next());
        assertEquals("<td>1</td>", lines.next());
        assertEquals("</tr>", lines.next());
        assertEquals("</tbody>", lines.next());
        assertEquals("</table>", lines.next());
    }

    /** Tests HTML escaping to prevent XSS attacks. */
    @Test
    void testHtmlEscaping() {
        var markdown = "Attack <script>alert('XSS')</script>";

        var lines = htmlLines(render(markdown));

        assertTrue(lines.findLine("<p>Attack &lt;script&gt;alert('XSS')&lt;/script&gt;</p>"));
        assertFalse(lines.findLine("<script>"));
    }

    /** Tests that dangerous URL schemes in links and images are sanitized. */
    @Test
    void testUnsafeUrlSchemesAreSanitized() {
        var markdown = """
                A [click](javascript:alert(1)) link.
                
                Mixed-case [bad](JavaScript:alert(2)) link.
                
                A [data](data:text/html,<script>alert(3)</script>) link.
                
                ![evil](vbscript:msgbox(4))
                
                ![ok](https://example.com/img.png)
                """;

        var lines = Lines.of(render(markdown));

        assertTrue(lines.findLine("<p>A <a href=\"#\">click</a>) link.</p>"));
        assertTrue(lines.findLine("<p>Mixed-case <a href=\"#\">bad</a>) link.</p>"));
        assertTrue(lines.findLine("<p>A <a href=\"#\">data</a>&lt;/script&gt;) link.</p>"));
        assertTrue(lines.findLine("<p><img alt=\"evil\" src=\"\"/>)</p>"));
        assertTrue(lines.findLine("<p><img alt=\"ok\" src=\"https://example.com/img.png\"/></p>"));

        assertFalse(lines.findLine("javascript:"), "javascript: scheme must not appear in output");
        assertFalse(lines.findLine("JavaScript:"));
        assertFalse(lines.findLine("vbscript:"), "vbscript: scheme must not appear in output");
        assertFalse(lines.findLine("data:text/html"), "data: scheme must be blocked in href");
    }

    /** Tests that {@code null} or empty markdown produces no output and never throws. */
    @Test
    void testNullAndEmptyInput() {
        assertEquals("<div></div>", render(null));
        assertEquals("<div></div>", render(""));
        assertThrows(NullPointerException.class, () -> new MarkdownToHtmlConverter().render(null, "x"));
    }

    /** Tests that an unclosed fenced code block at end-of-input is still emitted. */
    @Test
    void testUnclosedFencedCodeBlockIsFlushed() {
        var markdown = """
                ```
                int x = 42;
                """;

        var lines = htmlLines(render(markdown));

        assertTrue(lines.findLine("<pre>"));
        assertEquals("<code>int x = 42;", lines.next());
        assertEquals("</code>", lines.next());
        assertEquals("</pre>", lines.next());
    }

    /** Tests degenerate table inputs do not throw {@link StringIndexOutOfBoundsException}. */
    @Test
    void testDegenerateTableDoesNotCrash() {
        var markdown = """
                |
                |---|
                """;

        // Must not throw – previously caused StringIndexOutOfBoundsException.
        assertDoesNotThrow(() -> render(markdown));
    }

    /** Inserts newlines between adjacent tags so {@link Lines#next()} follows DOM order after {@link Lines#findLine(String)}. */
    private static Lines htmlLines(String html) {
        return Lines.of(html.replace("><", ">\n<"));
    }

    /** Renders the supplied markdown into a {@code <div>} wrapper and returns the produced HTML. */
    private static String render(String markdown) {
        var writer = new StringBuilder();
        var parent = new Element("div", XmlPrinter.forHtml(writer, HtmlConfig.ofEmptyElement()), 0);
        new MarkdownToHtmlConverter().render(parent, markdown);
        parent.close();
        return writer.toString();
    }
}
