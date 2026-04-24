/*
 * Copyright 2018-2026 Pavel Ponec,
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/XmlWriter.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.Check;
import org.ujorm.tools.common.StringUtils;
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
    public static final char NBSP = StringUtils.NBSP;
    /** Narrow Non-breaking space character (for numbers) */
    public static final char NARROW_NBSP = StringUtils.NARROW_NBSP;
    /** A forward slash character */
    public static final char FORWARD_SLASH = '/';
    /** A CDATA beg markup sequence */
    public static final String CDATA_BEG = "<![CDATA[";
    /** A CDATA end markup sequence */
    public static final String CDATA_END = "]]>";
    /** A comment beg sequence */
    public static final String COMMENT_BEG = "";

    /** Common formatter */
    public static final MsgFormatter FORMATTER = new MsgFormatter(){};

    private static final String[] CONTROL_ESCAPES = createControlEscapes();

    /** Thread-safe cache for HttpServletResponse reflection methods */
    private static final Map<Class<?>, ResponseMethods> METHOD_CACHE = new ConcurrentHashMap<>();

    /** Output */
    @NotNull
    protected final Appendable out;

    /** XML configuration */
    @NotNull
    protected final XmlConfig config;

    /** New line sequences */
    @NotNull
    protected final String newLine;

    /** An indentation request */
    protected final boolean indentationEnabled;

    @NotNull
    protected final String indentation;

    /** Value formatter */
    @NotNull
    private final Formatter format;

    @NotNull
    private final Appendable writerEscaped = createAppendable();

    /** Lazy cache for indentation prefixes by level. */
    private final List<String> indentationCache = new ArrayList<>();

    /**
     * A writer constructor
     * @param out A writer
     * @param config XML configuration
     */
    public AbstractWriter(@NotNull final Appendable out, @NotNull final XmlConfig config) {
        this.out = Objects.requireNonNull(out, "out");
        this.config = Objects.requireNonNull(config, "config");
        this.format = config.getFormatter();
        this.newLine = config.getNewLine().toString();
        this.indentation = config.getIndentation().toString();
        this.indentationEnabled = Check.hasLength(indentation);
        this.indentationCache.add("");
    }

    /** Write escaped value to the output
     * @param text A value to write
     * @param attribute Write an attribute value
     */
    public final void write(@NotNull final CharSequence text, final boolean attribute) throws IOException {
        write(text, 0, text.length(), attribute);
    }

    /** Writes escaped value to the output in blocks
     * @param text A value to write
     * @param attribute Write an attribute value
     */
    void write(@NotNull final CharSequence text, final int from, final int max, final boolean attribute) throws IOException {
        var start = from;
        for (var i = from; i < max; i++) {
            var c = text.charAt(i);
            var requiresEscape = false;

            switch (c) {
                case XML_LT:
                case XML_GT:
                case XML_AMPERSAND:
                case NBSP:
                case NARROW_NBSP:
                    requiresEscape = true;
                    break;
                case XML_2QUOT:
                    requiresEscape = attribute;
                    break;
                default:
                    requiresEscape = c < 32;
                    break;
            }

            if (requiresEscape) {
                // Append the safe block of text
                if (i > start) {
                    out.append(text, start, i);
                }

                // Escape and write the special character using the existing single-char method
                write(c, attribute);

                // Shift the start pointer
                start = i + 1;
            }
        }

        // Append any remaining safe text
        if (start < max) {
            out.append(text, start, max);
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
                out.append(XML_AMPERSAND + "lt;");
                break;
            case XML_GT:
                out.append(XML_AMPERSAND + "gt;");
                break;
            case XML_AMPERSAND:
                out.append(XML_AMPERSAND + "amp;");
                break;
            case XML_2QUOT:
                if (attribute) {
                    out.append(XML_AMPERSAND + "quot;");
                } else {
                    out.append(c);
                }
                break;
            case XML_APOSTROPHE:
                out.append(c);
                break;
            case SPACE:
                out.append(c);
                break;
            case NBSP:
                out.append(XML_AMPERSAND + "#160;");
                break;
            case NARROW_NBSP:
                out.append(XML_AMPERSAND + "#8239;");
                break;
            default: {
                if (c > 32) {
                    out.append(c);
                } else {
                    out.append(CONTROL_ESCAPES[c]);
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
        write(format.format(value, element, attributeName), attributeName != null);
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
        if (!newLine.isEmpty()) {
            out.append(newLine);
        }
        if (indentationEnabled && level > 0) {
            out.append(getIndentation(level));
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


    private @NotNull Appendable createAppendable() {
        return new Appendable() {
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
    }

    @NotNull
    private String getIndentation(final int level) {
        while (indentationCache.size() <= level) {
            final int lastIndex = indentationCache.size() - 1;
            indentationCache.add(indentationCache.get(lastIndex) + indentation);
        }
        return indentationCache.get(level);
    }

    private static String[] createControlEscapes() {
        final String[] result = new String[33];
        for (int i = 0; i <= 32; i++) {
            result[i] = "&#" + i + ";";
        }
        return result;
    }

    /** Cached reflection methods for HttpServletResponse */
    private static final class ResponseMethods {
        final Method setEncoding;
        final Method setHeader;
        final Method getWriter;

        ResponseMethods(Class<?> clazz) {
            try {
                this.setEncoding = clazz.getMethod("setCharacterEncoding", String.class);
                this.setHeader = clazz.getMethod("setHeader", String.class, String.class);
                this.getWriter = clazz.getMethod("getWriter");
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Failed to initialize HttpServletResponse methods", e);
            }
        }
    }

    // ---- STATIC METHOD(s) ---

    /** Assign a no-cache and an Edge compatibility mode and returns a writer from HttpServletResponse */
    @NotNull
    public static Appendable createWriter(
            @NotNull final Object httpServletResponse,
            @NotNull final Charset charset,
            final boolean noCache
    ) throws ReflectiveOperationException {
        var methods = METHOD_CACHE.computeIfAbsent(httpServletResponse.getClass(), ResponseMethods::new);

        methods.setEncoding.invoke(httpServletResponse, charset.toString());
        methods.setHeader.invoke(httpServletResponse, "Content-Type", "text/html; charset=" + charset);

        if (noCache) {
            methods.setHeader.invoke(httpServletResponse, "Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
            methods.setHeader.invoke(httpServletResponse, "Pragma", "no-cache"); // HTTP 1.0
            methods.setHeader.invoke(httpServletResponse, "Expires", "0"); // Proxies
            methods.setHeader.invoke(httpServletResponse, "X-UA-Compatible", "IE=edge"); // Proxies
        }

        return (Appendable) methods.getWriter.invoke(httpServletResponse);
    }
}