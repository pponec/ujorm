package org.ujorm.tools.markdown;

import org.ujorm.tools.Check;
import org.ujorm.tools.web.Element;
import org.ujorm.tools.web.Html;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A lightweight Markdown to HTML converter intended for static project pages,
 * release notes, in-app help, and similar trimmed use cases. The converter
 * builds the HTML output directly into a target {@link Element}, so it
 * naturally integrates with the {@code ujo-web} HTML builder API and inherits
 * its character escaping (no separate sanitization step is required).
 *
 * <h4>Supported block-level syntax</h4>
 * <ul>
 *   <li><b>Headings, level 1 – 6</b>: lines starting with one to six {@code #}
 *       characters followed by whitespace and the title text. A hash count
 *       greater than six is rendered as plain text.</li>
 *   <li><b>Unordered lists</b>: lines starting with {@code -}, {@code *} or
 *       {@code +} followed by whitespace.</li>
 *   <li><b>Ordered lists</b>: lines starting with one or more digits followed
 *       by a dot and whitespace (e.g. {@code 1. First}). The actual numbering
 *       is delegated to the browser via {@code <ol>}.</li>
 *   <li><b>Block quotes</b>: lines prefixed with {@code >}. Consecutive quoted
 *       lines are merged into a single {@code <blockquote>} containing one
 *       paragraph; a blank line ends the quote.</li>
 *   <li><b>Fenced code blocks</b>: blocks delimited by triple back-tick fences
 *       (<code>```</code>). The content is emitted verbatim inside
 *       {@code <pre><code>…</code></pre>} with HTML special characters escaped
 *       and original line breaks preserved.</li>
 *   <li><b>Indented code blocks</b>: contiguous lines indented by four spaces
 *       or a tab character (only when no other block is currently open).</li>
 *   <li><b>Horizontal rules</b>: a line consisting of three or more
 *       {@code -}, {@code *} or {@code _} characters renders as {@code <hr/>}.</li>
 *   <li><b>GFM tables</b>: a header row containing pipe characters followed
 *       by a separator row (e.g. {@code |---|:--:|---:|}) and arbitrary many
 *       data rows. Cells may contain inline formatting.</li>
 *   <li><b>Paragraphs</b>: any other non-empty line is folded into a paragraph
 *       block. Lines are joined with a single space; a trailing backslash
 *       ({@code \}) at the end of a line forces a {@code <br/>} between the
 *       two adjacent lines. A blank line terminates the paragraph.</li>
 * </ul>
 *
 * <h4>Supported inline syntax</h4>
 * <ul>
 *   <li><b>Bold</b>: {@code **text**} → {@code <strong>text</strong>}</li>
 *   <li><b>Italic</b>: {@code _text_} → {@code <em>text</em>}</li>
 *   <li><b>Inline code</b>: {@code `text`} or {@code ``text``} →
 *       {@code <code>text</code>}</li>
 *   <li><b>Hyperlink</b>: {@code [text](url)} → {@code <a href="url">text</a>}</li>
 *   <li><b>Image</b>: {@code ![alt](src)} →
 *       {@code <img alt="alt" src="src"/>}</li>
 * </ul>
 *
 * <h4>Limitations</h4>
 * <p>
 * In order to keep the implementation small the converter intentionally omits
 * several features. In particular it does <em>not</em> support nested lists,
 * setext-style headings ({@code ===}/{@code ---} underlines), reference-style
 * links, HTML pass-through, footnotes, task lists or strike-through.
 * Inline emphasis is detected with a single
 * non-greedy regular expression, so pathological combinations of asterisks
 * and underscores may not be parsed exactly as in CommonMark / GFM.
 *
 * <h4>Thread-safety</h4>
 * <p>
 * The converter itself holds no mutable instance state – each call to
 * {@link #render(Element, String)} works against a fresh, internal state
 * holder – so a single instance can be safely shared between threads, as long
 * as each thread renders into its own {@link Element} target.
 *
 * <h4>Usage</h4>
 * <pre class="pre">
 * String markdown = """
 *         # Release notes
 *
 *         The new version brings the following highlights:
 *
 *         - **Markdown** support for project pages
 *         - GFM _tables_ and fenced code blocks
 *         - More details on the [project page](https://ujorm.org)
 *         """;
 *
 * try (var html = HtmlElement.of("Release notes", response)) {
 *     new MarkdownToHtmlConverter().render(html.getBody(), markdown);
 * }
 * </pre>
 *
 * @see Element
 * @see org.ujorm.tools.web.HtmlElement
 */
public final class MarkdownToHtmlConverter {

    private static final char BREAK_MARKER = '\n';
    /** Cached delimiter for {@link String#split(String, int)} — avoids per-call allocation. */
    private static final String BREAK_SPLIT = String.valueOf(BREAK_MARKER);
    private static final String STRONG = "strong";
    private static final String EM = "em";
    private static final String CODE = "code";
    private static final String BLOCKQUOTE = "blockquote";

    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.+)$");
    /** Ordered ({@code 1.}) or unordered ({@code -}, {@code *}, {@code +}) list item; group 1 is digits only for {@code <ol>}. */
    private static final Pattern LIST = Pattern.compile("^(?:(\\d+)\\.|[*+\\-])\\s+(.+)$");
    private static final Pattern HORIZONTAL_RULE = Pattern.compile("^\\s*([-*_])(\\s*\\1){2,}\\s*$");
    private static final Pattern INDENTED_CODE = Pattern.compile("^(?: {4}|\\t)(.*)$");
    private static final Pattern TABLE_CELL_SEP = Pattern.compile(":?-+:?");
    private static final Pattern INLINE = Pattern.compile(
            "(`+)(.+?)\\1"                         // 1=backtick delimiter, 2=code
                    + "|!\\[([^]]*)]\\(([^)]+)\\)"           // 3=alt, 4=src
                    + "|\\[([^]]+)]\\(([^)]+)\\)"            // 5=text, 6=href
                    + "|\\*\\*(.+?)\\*\\*"                   // 7=bold
                    + "|_(.+?)_"                             // 8=italic
    );

    /** Disallowed URL schemes for hyperlinks (XSS hardening). */
    private static final Pattern UNSAFE_HREF = Pattern.compile(
            "^\\s*(?:javascript|vbscript|data)\\s*:", Pattern.CASE_INSENSITIVE);
    /** Disallowed URL schemes for image sources – data: is left intact for inline base64 images. */
    private static final Pattern UNSAFE_SRC = Pattern.compile(
            "^\\s*(?:javascript|vbscript)\\s*:", Pattern.CASE_INSENSITIVE);
    /** Replacement href used when a link points to an unsafe scheme. */
    private static final String SAFE_FALLBACK_HREF = "#";

    /**
     * Renders the markdown content into the parent HTML element.
     * <p>
     * The input is materialized into a {@link List} of lines because the parser
     * needs random access — table detection peeks one line ahead at the
     * separator row and {@link #renderTable} consumes a contiguous slice of
     * subsequent rows. A plain {@link java.util.stream.Stream Stream} would
     * require a push-back buffer for the same effect.
     *
     * @param parent   target element to append rendered nodes into; must not be {@code null}.
     * @param markdown markdown source text; a {@code null} or empty value is silently ignored.
     */
    public void render(Element parent, String markdown) {
        Objects.requireNonNull(parent, "parent");
        if (Check.isEmpty(markdown)) {
            return;
        }
        var lines = markdown.lines().toList();
        var st = new State(parent);
        for (var i = 0; i < lines.size(); i++) {
            i = step(lines, i, st);
        }
        st.flushAll();
    }

    /** Processes a single line and returns the index of the last consumed line. */
    private int step(List<String> lines, int i, State st) {
        var raw = lines.get(i);
        var line = raw.strip();

        if (st.fence != null) {
            if (line.startsWith("```")) {
                st.flushFence();
            } else {
                st.fence.append(raw).append('\n');
            }
            return i;
        }
        if (line.startsWith("```")) {
            st.flushAll();
            st.fence = new StringBuilder();
            return i;
        }

        var indented = INDENTED_CODE.matcher(raw);
        if (indented.matches() && st.canStartIndentedCode()) {
            st.appendIndentedCode(indented.group(1));
            return i;
        }
        st.flushIndentedCode();

        if (line.isEmpty()) {
            st.flushAll();
            return i;
        }
        if (HORIZONTAL_RULE.matcher(line).matches()) {
            st.flushAll();
            st.parent.addElement(Html.HR);
            return i;
        }
        var heading = HEADING.matcher(line);
        if (heading.matches()) {
            st.flushAll();
            appendInline(st.parent.addHeadingX(heading.group(1).length()), heading.group(2).strip());
            return i;
        }
        if (line.startsWith(">")) {
            st.appendQuote(line.substring(1).stripLeading());
            return i;
        }
        st.flushQuote();

        if (line.contains("|") && i + 1 < lines.size() && isTableSeparator(lines.get(i + 1))) {
            var headers = splitTableRow(line);
            if (!headers.isEmpty()) {
                st.flushAll();
                return renderTable(st.parent, headers, lines, i);
            }
        }

        var list = LIST.matcher(line);
        if (list.matches()) {
            st.appendListItem(list.group(1) != null ? Html.OL : Html.UL, list.group(2).strip());
            return i;
        }
        st.endList();

        if (!st.paragraph.isEmpty()) {
            st.paragraph.append(st.pendingBreak ? BREAK_MARKER : ' ');
        }
        var hardBreak = line.endsWith("\\");
        st.paragraph.append(hardBreak ? line.substring(0, line.length() - 1) : line);
        st.pendingBreak = hardBreak;
        return i;
    }

    // --- Tables ----------------------------------------------------------

    /** A row like {@code |---|:--:|---:|} (with optional alignment colons). */
    private static boolean isTableSeparator(String row) {
        var cells = splitTableRow(row);
        if (cells.isEmpty()) {
            return false;
        }
        for (var c : cells) {
            if (!TABLE_CELL_SEP.matcher(c).matches()) {
                return false;
            }
        }
        return true;
    }

    /** Splits a pipe-separated table row. */
    private static List<String> splitTableRow(String row) {
        var s = row.strip();
        var start = s.startsWith("|") ? 1 : 0;
        var end = s.endsWith("|") ? s.length() - 1 : s.length();
        if (start >= end) {
            return List.of();
        }
        var parts = s.substring(start, end).split("\\|", -1);
        var out = new ArrayList<String>(parts.length);
        for (var p : parts) {
            out.add(p.strip());
        }
        return out;
    }

    /** Renders the GFM table starting at the header row and returns the last consumed line index. */
    private int renderTable(Element parent, List<String> headers, List<String> lines, int start) {
        var table = parent.addTable();
        appendTableCells(table.addTableHead().addTableRow(), headers, true);
        var body = table.addTableBody();
        var i = start + 2; // skip header + separator rows
        while (i < lines.size() && lines.get(i).contains("|")) {
            var cells = splitTableRow(lines.get(i));
            if (!cells.isEmpty()) {
                appendTableCells(body.addTableRow(), cells, false);
            }
            i++;
        }
        return i - 1;
    }

    /** Appends one table row with inline parsing into {@code <th>} or {@code <td>} cells. */
    private void appendTableCells(Element tr, List<String> cells, boolean header) {
        for (var c : cells) {
            appendInline(header ? tr.addElement(Html.TH) : tr.addTableDetail(), c);
        }
    }

    // --- Inline & Block rendering ----------------------------------------

    /** Renders inline content, splitting on the {@link #BREAK_MARKER}. */
    private void appendInline(Element target, String text) {
        var segments = text.split(BREAK_SPLIT, -1);
        for (var j = 0; j < segments.length; j++) {
            if (j > 0) target.addBreak();
            tokenizeSegment(target, segments[j]);
        }
    }

    /** Writes a {@code <pre><code>} block preserving raw line breaks while escaping HTML. */
    private void writeCodeBlock(Element parent, String content) {
        var code = parent.addElement(Html.PRE).addElement(CODE);
        var lines = content.split("\n", -1);
        for (var j = 0; j < lines.length; j++) {
            if (j > 0) code.addRawText("\n");
            code.addText(lines[j]);
        }
    }

    /** Tokenizes a single inline segment producing text, link, image, bold and italic nodes. */
    private void tokenizeSegment(Element target, String text) {
        var matcher = INLINE.matcher(text);
        var from = 0;
        while (matcher.find()) {
            if (matcher.start() > from) {
                target.addText(text.substring(from, matcher.start()));
            }
            if (matcher.group(1) != null) {
                target.addElement(CODE).addText(normalizeCodeSpan(matcher.group(2)));
            } else if (matcher.group(3) != null) {
                target.addImage(safeUrl(matcher.group(4), UNSAFE_SRC, ""), matcher.group(3));
            } else if (matcher.group(5) != null) {
                target.addAnchor(safeUrl(matcher.group(6), UNSAFE_HREF, SAFE_FALLBACK_HREF)).addText(matcher.group(5));
            } else if (matcher.group(7) != null) {
                target.addElement(STRONG).addText(matcher.group(7));
            } else {
                target.addElement(EM).addText(matcher.group(8));
            }
            from = matcher.end();
        }
        if (from < text.length()) {
            target.addText(text.substring(from));
        }
    }

    /** Strips the URL or returns {@code fallback} when it matches {@code unsafe}. */
    private static String safeUrl(String url, Pattern unsafe, String fallback) {
        return unsafe.matcher(url).find() ? fallback : url.strip();
    }

    /** Applies the Markdown rule that trims one balanced surrounding space from code span content. */
    private static String normalizeCodeSpan(String code) {
        if (code.length() > 1 && code.startsWith(" ") && code.endsWith(" ") && !code.isBlank()) {
            return code.substring(1, code.length() - 1);
        }
        return code;
    }

    // --- Mutable state holder --------------------------------------------

    /** Aggregates pending block-level state during a single render call. */
    private final class State {
        final Element parent;
        final StringBuilder paragraph = new StringBuilder();
        Element list;
        String listType;
        Element quote;
        StringBuilder quoteBuffer;
        StringBuilder fence;
        StringBuilder indented;
        boolean pendingBreak;

        State(Element parent) {
            this.parent = parent;
        }

        void flushAll() {
            flushParagraph();
            flushQuote();
            flushIndentedCode();
            flushFence();
            endList();
        }

        boolean canStartIndentedCode() {
            return paragraph.isEmpty() && list == null && quote == null;
        }

        void flushParagraph() {
            if (!paragraph.isEmpty()) {
                appendInline(parent.addParagraph(), paragraph.toString());
                paragraph.setLength(0);
            }
            pendingBreak = false;
        }

        void appendListItem(String type, String content) {
            flushParagraph();
            flushQuote();
            flushIndentedCode();
            if (!type.equals(listType)) {
                list = Html.UL.equals(type) ? parent.addUnorderedlist() : parent.addOrderedList();
                listType = type;
            }
            appendInline(list.addListItem(), content);
        }

        void endList() {
            list = null;
            listType = null;
        }

        void appendQuote(String content) {
            flushParagraph();
            flushIndentedCode();
            endList();
            if (quote == null) {
                quote = parent.addElement(BLOCKQUOTE);
                quoteBuffer = new StringBuilder(content);
            } else {
                quoteBuffer.append(' ').append(content);
            }
        }

        void flushQuote() {
            if (quote != null) {
                appendInline(quote.addParagraph(), quoteBuffer.toString());
                quote = null;
                quoteBuffer = null;
            }
        }

        void flushFence() {
            if (fence != null) {
                writeCodeBlock(parent, fence.toString());
                fence = null;
            }
        }

        void appendIndentedCode(String content) {
            if (indented == null) {
                indented = new StringBuilder(content);
            } else {
                indented.append('\n').append(content);
            }
        }

        void flushIndentedCode() {
            if (indented != null) {
                writeCodeBlock(parent, indented.toString());
                indented = null;
            }
        }
    }
}