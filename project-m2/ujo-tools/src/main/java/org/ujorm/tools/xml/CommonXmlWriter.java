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

import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Check;
import org.ujorm.tools.Check;

/**
 * A generic writer
 * @author Pavel Ponec
 */
public class CommonXmlWriter {

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

    /** Output */
    @Nonnull
    protected final Appendable out;

    /** An offset space */
    @Nullable
    protected final String offsetSpace;

    /** Is offset enabled */
    private final boolean offsetEnabled;

    /**
     * A writer constructor
     * @param out A writer
     * @param offsetSpace String for a one level offset.
     */
    public CommonXmlWriter(@Nonnull final Appendable out, @Nullable final String offsetSpace) {
        this.out = out;
        this.offsetSpace = offsetSpace;
        this.offsetEnabled = Check.hasLength(offsetSpace);
    }

    /** Write escaped value to the output
     * @param text A value to write
     * @param out An output
     */
    public void write(@Nonnull final CharSequence text) throws IOException {
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

    /** Write escaped value to the output
     * @param value A value to write
     * @param element The element
     * @param attribute A name of the XML attribute of {@code null} value for a XML text.
     */
    public void writeValue(@Nullable final Object value, @Nonnull final AbstractElement element, final @Nullable String attribute) throws IOException {
        final CharSequence text = value instanceof CharSequence ? (CharSequence) value : String.valueOf(value);
        write(text);
    }

    /**
     * Write the content of an envelope
     * @param rawValue A raw value to print
     * @param element An original element
     */
    public void writeRawValue(@Nonnull final Object rawValue, @Nonnull final AbstractElement element) throws IOException {
        out.append(rawValue.toString());
    }

    /** Write a new line with an offset by the current level */
    public void writeNewLine(final int level) throws IOException {
        out.append(CHAR_NEW_LINE);
        if (offsetEnabled) {
            for (int i = level - 1; i >= 0; i--) {
                out.append(offsetSpace);
            }
        }
    }

    @Override
    public String toString() {
        return out.toString();
    }

}
