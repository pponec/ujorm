/*
 * Copyright 2018-2018 Pavel Ponec,
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/XmlWriter.java
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

package org.ujorm.tools.xml;

import java.io.CharArrayWriter;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.Check;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * If you need special formatting, overwrite responsible methods.
 * @see XmlBuilder
 * @since 1.88
 * @author Pavel Ponec
 */
public class XmlPriter {

    /** A special XML character */
    public static final char XML_GT = '>';
    /** A special XML character */
    public static final char XML_LT = '<';
    /** A special XML character */
    public static final char XML_AMP = '&';
    /** A special XML character */
    public static final char XML_QUOT = '\'';
    /** A special XML character */
    public static final char XML_2QUOT = '"';
    /** A special XML character */
    public static final char CHAR_SPACE = ' ';
    /** A new line character */
    public static final char CHAR_NEW_LINE = '\n';
    /** A forward slash character */
    public static final char FORWARD_SLASH = '/';
    /** A CDATA beg markup sequence */
    public static final String CDATA_BEG = "<![CDATA[";
    /** A CDATA end markup sequence */
    public static final String CDATA_END = "]]>";
    /** A comment beg sequence */
    public static final String COMMENT_BEG = "<!--";
    /** A comment end sequence */
    public static final String COMMENT_END = "-->";

    /** Writer */
    @Nonnull
    protected final Appendable out;

    /** An element offset is enabled */
    protected final boolean offsetEnabled;

    /** An offset space */
    @Nullable
    protected final String offsetSpace;

    /** Default constructor a zero offset */
    public XmlPriter() {
        this(new CharArrayWriter(512));
    }

    /** Writer constructor with a zero offset */
    public XmlPriter(@Nonnull final Appendable out) {
        this(out, "");
    }

    /** Writer constructor with a zero offset */
    public XmlPriter(@Nonnull final Appendable out, @Nullable final boolean offset, Object... initTexts) {
        this(out, offset ? "\t" : null);
        try {
            for (Object text : initTexts) {
                out.append(String.valueOf(text));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * A writer constructor
     * @param out A writer
     * @param offsetSpace String for a one level offset.
     */
    public XmlPriter(@Nonnull final Appendable out, @Nullable final String offsetSpace) {
        this.out = out;
        this.offsetEnabled = Check.hasLength(offsetSpace);
        this.offsetSpace = offsetSpace;
    }

    /** Write a new line with an offset by the current level */
    protected void writeNewLine(final int level) throws IOException {
        out.append(CHAR_NEW_LINE);
        if (offsetEnabled) {
            for (int i = level - 1; i >= 0; i--) {
                out.append(offsetSpace);
            }
        }
    }

    /** Write escaped value to the output
     * @param value A value to write
     * @param element The element
     * @param attribute A name of the XML attribute of {@code null} value for a XML text.
     */
    protected void writeValue(@Nullable final Object value, @Nonnull final XmlBuilder element, final @Nullable String attribute) throws IOException {
        final CharSequence text = value instanceof CharSequence ? (CharSequence) value : String.valueOf(value);
        for (int i = 0, max = text.length(); i < max; i++) {
            final char c = text.charAt(i);
            switch (c) {
                case XML_LT:
                    out.append(XML_AMP).append("lt;");
                    break;
                case XML_GT:
                    out.append(XML_AMP).append("gt;");
                    break;
                case XML_AMP:
                    out.append(XML_AMP).append("#38;");
                    break;
                case XML_QUOT:
                    out.append(XML_AMP).append("#39;");
                    break;
                case XML_2QUOT:
                    out.append(XML_AMP).append("#34;");
                    break;
                default: {
                    if (c > 32 || c == CHAR_SPACE) {
                        out.append(c);
                    } else {
                        out.append(XML_AMP).append("#");
                        out.append(Integer.toString(c));
                        out.append(";");
                    }
                }
            }
        }
    }

    /**
     * Write the content of an envelope
     * @param rawValue A raw value to print
     * @param element An original element
     */
    protected void writeRawValue(@Nonnull final Object rawValue, @Nonnull final XmlBuilder element) throws IOException {
        out.append(rawValue.toString());
    }

    void writeAttrib(String name, Object data, XmlBuilder owner) throws IOException {
        out.append(CHAR_SPACE);
        out.append(name);
        out.append('=');
        out.append(XML_2QUOT);
        writeValue(data, owner, name);
        out.append(XML_2QUOT);
    }

    void writeRawText(Object rawText) throws IOException {
        out.append(String.valueOf(rawText));
    }

    /** Open the Node */
    void writeBeg(XmlBuilder element) throws IOException {
        if (!element.isLastText()) {
            writeNewLine(element.getLevel());
        }
        out.append(XML_LT);
        out.append(element.getName());
    }

    /** Middle closing the Node */
    void writeMid(XmlBuilder element) throws IOException {
        out.append(XML_GT);
    }

    /** Close the Node */
    void writeEnd(XmlBuilder element) throws IOException {
        if (element.isFilled()) {
            out.append(XML_LT);
            out.append(FORWARD_SLASH);
            out.append(element.getName());
            out.append(XML_GT);
        } else {
            out.append(FORWARD_SLASH);
            out.append(XML_GT);
        }
    }

    @Override @Nonnull
    public String toString() {
        final String result = out.toString();
        return result != null
             ? result
             : String.valueOf(result);
    }

    // ------- STATIC METHODS -------

    /** Crete a new instance including a XML_HEADER.
     * The result provides a method {@link #toString() }
     */
    public static XmlPriter forXml() {
        return new XmlPriter(new StringBuilder(512), false, Html.XML_HEADER);
    }

    /** Crete a new instance including a DOCTYPE.
     * The result provides a method {@link #toString() }
     */
    public static XmlPriter forHtml() {
        return new XmlPriter(new StringBuilder(512), false, Html.DOCTYPE);
    }

    /** Crete a new instance including a DOCTYPE */
    public static XmlPriter forHtml(final Appendable out) {
        return new XmlPriter(out, false, Html.DOCTYPE);
    }

    /** Create XmlPrinter for UTF-8 */
    public static XmlPriter forHtml(HttpServletResponse output) throws IOException {
        output.setCharacterEncoding(UTF_8.toString());
        return new XmlPriter(output.getWriter(), false, Html.DOCTYPE);

    }
}
