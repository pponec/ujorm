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

package org.ujorm.tools.dom;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Check;

/**
 * If you need special formatting, overwrite responsible methods.
 * @see XmlElement Default implementation of the XmlWriter
 * @since 1.88
 * @author Pavel Ponec
 */
public class XmlWriter {

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
    protected final Writer out;

    /** An element offset is enabled */
    protected final boolean offsetEnabled;

    /** An offset space */
    @Nullable
    protected final String offsetSpace;

    /** Default constructor a zero offset */
    public XmlWriter() {
        this(new CharArrayWriter(512));
    }

    /** Writer constructor with a zero offset */
    public XmlWriter(@Nonnull final Writer out) {
        this(out, "");
    }

    /**
     * A writer constructor
     * @param out A writer
     * @param offsetSpace String for a one level offset.
     */
    public XmlWriter(@Nonnull final Writer out, @Nullable final String offsetSpace) {
        this.out = out;
        this.offsetEnabled = Check.hasLength(offsetSpace);
        this.offsetSpace = offsetSpace;
    }

    /** Render the XML code without header */
    @Nonnull
    public final XmlWriter write(final int level, @Nonnull final XmlElement element) throws IOException {
        return write(level, element.name, element.attributes, element.children, element);
    }

    /** Render the XML code without header
     * @param level Element nesting level.
     * @param name Name of element
     * @param attributes Attributes of the element
     * @param children Childern of the element
     * @param element Original element
     * @return This
     */
    @Nonnull
    protected XmlWriter write(final int level
            , @Nonnull final CharSequence name
            , @Nullable final Map<String, Object> attributes
            , @Nullable final List<Object> children
            , @Nonnull final XmlElement element) throws IOException {
        out.append(XML_LT);
        out.append(name);

        if (Check.hasLength(attributes)) {
            assert attributes != null; // For static analyzer only
            for (String key : attributes.keySet()) {
                out.append(CHAR_SPACE);
                out.append(key);
                out.append('=');
                out.append(XML_2QUOT);
                writeValue(attributes.get(key), name, true, out);
                out.append(XML_2QUOT);
            }
        }
        if (Check.hasLength(children)) {
            assert children != null; // For static analyzer only
            out.append(XML_GT);
            boolean writeNewLine = true;
            for (Object child : children) {
                if (child instanceof XmlElement) {
                    if (writeNewLine) {
                        newLine(level);
                    } else {
                        writeNewLine = true;
                    }
                    write(level + 1, (XmlElement) child);
                } else if (child instanceof XmlElement.RawEnvelope) {
                    out.append(((XmlElement.RawEnvelope) child).get());
                    writeNewLine = false;
                } else {
                    writeValue(child, name, false, out);
                    writeNewLine = false;
                }
            }
            out.append(XML_LT);
            out.append(FORWARD_SLASH);
            out.append(name);
        } else {
            out.append(FORWARD_SLASH);
        }
        out.append(XML_GT);
        return this;
    }

    /** Write a new line with an offset */
    protected void newLine(final int level) throws IOException {
        out.append(CHAR_NEW_LINE);
        if (offsetEnabled) {
            for (int i = level - 1; i >= 0; i--) {
                out.append(offsetSpace);
            }
        }
    }

    /**
     * Write escaped value to the output
     * @param value A value
     * @param elementName A name of the element
     * @param attribute Render the value to an element attribute, or a text
     * @param out An output writer
     * @throws IOException
     */
    protected void writeValue(@Nonnull final Object value, @Nonnull final CharSequence elementName, final boolean attribute, @Nonnull final Writer out) throws IOException {
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

    @Override @Nonnull
    public String toString() {
        final String result = out.toString();
        return result != null
             ? result
             : String.valueOf(result);
    }

}
