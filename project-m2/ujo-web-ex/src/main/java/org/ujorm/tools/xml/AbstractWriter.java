/*
 * Copyright 2018-2022 Pavel Ponec,
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
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.msg.MsgFormatter;
import org.ujorm.tools.xml.config.Formatter;
import org.ujorm.tools.xml.config.XmlConfig;

/**
 * A generic writer
 * @author Pavel Ponec
 */
public abstract class AbstractWriter {

    /** Default XML declaration */
    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    /** Default DOCTYPE of HTML-5 */
    public static final String HTML_DOCTYPE = "<!DOCTYPE html>";

    /** A special XML character */
    public static final char XML_GT = '>';
    /** A special XML character */
    public static final char XML_LT = '<';
    /** A special XML character */
    public static final char XML_AMPERSAND = '&';
    /** A special XML character */
    public static final char XML_APOSTROPHE = '\'';
    /** A special XML character */
    public static final char XML_2QUOT = '"';
    /** A special XML character */
    public static final char SPACE = ' ';
    /** Non-breaking space character */
    public static final char NBSP = '\u00A0';
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

    /** Common formatter */
    public static final MsgFormatter FORMATTER = new MsgFormatter(){};

    /** Output */
    @NotNull
    protected final Appendable out;

    /** XML configuration */
    @NotNull
    protected final XmlConfig config;

    /** Value formatter */
    @NotNull
    private final Formatter formatter;

    /** An indentation request */
    protected final boolean indentationEnabled;

    @NotNull
    private final Appendable writerEscaped = new Appendable() {
            private final boolean attribute = false;

            @NotNull
            @Override
            public Appendable append(@NotNull final CharSequence value) throws IOException {
                write(value, attribute);
                return this;
            }

            @NotNull
            @Override
            public Appendable append(@NotNull final CharSequence value, int start, int end) throws IOException {
                write(value, start, end, attribute);
                return this;
            }

            @NotNull
            @Override
            public Appendable append(final char value) throws IOException {
                write(value, attribute);
                return this;
            }
        };

    /**
     * A writer constructor
     * @param out A writer
     * @param config XML configuration
     */
    public AbstractWriter(@NotNull final Appendable out, @NotNull final XmlConfig config) {
        this.out = Assert.notNull(out, "out");
        this.config = Assert.notNull(config, "config");
        this.indentationEnabled = Check.hasLength(config.getIndentation());
        this.formatter = config.getFormatter();
    }

    /** Write escaped value to the output
     * @param text A value to write
     * @param attribute Write an attribute value
     */
    public final void write(@NotNull final CharSequence text, final boolean attribute) throws IOException {
        write(text, 0, text.length(), attribute);
    }

    /** Write escaped value to the output
     * @param text A value to write
     * @param attribute Write an attribute value
     */
    void write(@NotNull final CharSequence text, final int from, final int max, final boolean attribute) throws IOException {
        for (int i = from; i < max; i++) {
            write(text.charAt(i), attribute);
        }
    }

    /**
     * Write single character to the output
     * @param c Character
     * @param attribute Is it a text to attribute?
     * @throws IOException
     */
    private void write(final char c, final boolean attribute) throws IOException {
        switch (c) {
            case XML_LT:
                out.append(XML_AMPERSAND).append("lt;");
                break;
            case XML_GT:
                out.append(XML_AMPERSAND).append("gt;");
                break;
            case XML_AMPERSAND:
                out.append(XML_AMPERSAND).append("amp;");
                break;
            case XML_2QUOT:
                if (attribute) {
                    out.append(XML_AMPERSAND).append("quot;");
                } else {
                    out.append(c);
                }
                break;
            case XML_APOSTROPHE:
                if (true) {
                    out.append(c);
                } else {
                    out.append(XML_AMPERSAND).append("apos;");
                }
                break;
            case SPACE:
                out.append(c);
                break;
            case NBSP:
                out.append(XML_AMPERSAND).append("#160;");
                break;
            default: {
                if (c > 32) {
                    out.append(c);
                } else {
                    out.append(XML_AMPERSAND).append("#");
                    out.append(Integer.toString(c));
                    out.append(";");
                }
            }
        }
    }

    /** Write escaped value to the output
     * @param value A value to write, where the {@code null} value is ignored silently.
     * @param element The element
     * @param attributeName A name of the XML attribute of {@code null} value for a XML text.
     */
    public void writeValue(
            @Nullable final Object value,
            @NotNull final ApiElement element,
            @Nullable final String attributeName
    ) throws IOException {
        write(formatter.format(value, element, attributeName), attributeName != null);
    }

    /**
     * Write the content of an envelope
     * @param rawValue A raw value to print
     * @param element An original element
     */
    public final void writeRawValue(@NotNull final CharSequence rawValue, @NotNull final ApiElement element) throws IOException {
        out.append(rawValue);
    }

    /** Write a new line with an offset by the current level */
    public void writeNewLine(final int level) throws IOException {
        out.append(config.getNewLine());
        if (indentationEnabled) {
            for (int i = level; i > 0; i--) {
                out.append(config.getIndentation());
            }
        }
    }

    @Override
    public String toString() {
        return out.toString();
    }

    /** For internal usage only */
    @NotNull
    public Appendable getWriter() {
        return out;
    }

    /** Get Writer to escape HTML characters. */
    @NotNull
    public Appendable getWriterEscaped() {
        return writerEscaped;
    }

    // ---- STATIC METHOD(s) ---

    /** Assign a no-cache and an Edge compatibility mode and returns a writer from HttpServletResponse */
    @NotNull
    public static Appendable createWriter(
            @NotNull final Object httpServletResponse,
            @NotNull final Charset charset,
            final boolean noCache
    ) throws ReflectiveOperationException {
        final Method setEncoding = httpServletResponse.getClass().getMethod("setCharacterEncoding", String.class);
        final Method setHeader = httpServletResponse.getClass().getMethod("setHeader", String.class, String.class);
        final Method getWriter = httpServletResponse.getClass().getMethod("getWriter");
        setEncoding.invoke(httpServletResponse, charset.toString());
        setHeader.invoke(httpServletResponse, "Content-Type", "text/html; charset=" + charset);
        if (noCache) {
            setHeader.invoke(httpServletResponse, "Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
            setHeader.invoke(httpServletResponse, "Pragma", "no-cache"); // HTTP 1.0
            setHeader.invoke(httpServletResponse, "Expires", "0"); // Proxies
            setHeader.invoke(httpServletResponse, "X-UA-Compatible", "IE=edge"); // Proxies
        }
        final Appendable writer = (Appendable) getWriter.invoke(httpServletResponse);
        return writer;
    }

//    IT IS A WRONG IDEA:
//    /** Close the an internal writer, if the one is Closeable */
//    @Override
//    public void close() throws IOException {
//        if (this.out instanceof Closeable) {
//            ((Closeable) out).close();
//        }
//    }
}
