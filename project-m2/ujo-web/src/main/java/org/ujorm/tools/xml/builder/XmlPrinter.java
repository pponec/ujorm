/*
 * Copyright 2018-2026 Pavel Ponec,
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

package org.ujorm.tools.xml.builder;

import java.io.IOException;
import java.nio.charset.Charset;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.xml.AbstractWriter;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.XmlConfig;

/**
 * If you need special formatting, overwrite responsible methods.
 * @see XmlBuilder
 * @since 1.88
 * @author Pavel Ponec
 */
public class XmlPrinter extends AbstractWriter {

    /** Default constructor a zero offset */
    public XmlPrinter() {
        this(new StringBuilder(512));
    }

    /** Writer constructor with a zero offset */
    public XmlPrinter(@NotNull final Appendable out) {
        this(out, XmlConfig.ofDefault());
    }

    /**
     * A writer constructor
     * @param out A writer
     * @param config A configuration object
     */
    public <T> XmlPrinter(@NotNull final Appendable out, @Nullable final XmlConfig config) {
        super(out, config);
        try {
            out.append(config.getDoctype());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Write the content of an envelope
     * @param rawValue A raw value to print
     * @param element An original element
     */
    protected void writeRawValue(@NotNull final Object rawValue, @NotNull final XmlBuilder element) throws IOException {
        out.append(rawValue.toString());
    }

    /** Writes an attribute */
    void writeAttrib(@NotNull String name, Object data, XmlBuilder owner) throws IOException {
        if (owner.getName() != XmlBuilder.HIDDEN_NAME) {
            out.append(SPACE);
            out.append(name);
            out.append('=');
            out.append(XML_2QUOT);
            writeValue(data, owner, name);
            out.append(XML_2QUOT);
        }
    }

    /** Writes raw text */
    void writeRawText(Object rawText) throws IOException {
        out.append(String.valueOf(rawText));
    }

    /** Open the Node */
    void writeBeg(XmlBuilder element, final boolean lastText) throws IOException {
        var name = element.getName();
        if (name != XmlBuilder.HIDDEN_NAME) {
            if (!lastText) {
                writeNewLine(element.getLevel());
            }
            out.append(XML_LT);
            out.append(name);
        }
    }

    /** Middle closing the Node */
    void writeMid(XmlBuilder element) throws IOException {
        if (element.getName() != XmlBuilder.HIDDEN_NAME) {
            out.append(XML_GT);
        }
    }

    /** Close the Node */
    void writeEnd(XmlBuilder element) throws IOException {
        var name = element.getName();
        if (name == XmlBuilder.HIDDEN_NAME) {
            return;
        }

        var filled = element.isFilled();
        var pairElement = config.pairElement(element);

        if (filled || pairElement) {
            if (indentationEnabled && !element.isLastText()) {
                if (pairElement && !filled) {
                    out.append(XML_GT);
                } else {
                    writeNewLine(element.getLevel());
                }
            } else if (!filled) {
                out.append(XML_GT);
            }
            out.append(XML_LT);
            out.append(FORWARD_SLASH);
            out.append(name);
            out.append(XML_GT);
        } else {
            out.append(FORWARD_SLASH);
            out.append(XML_GT);
        }
    }

    @Override @NotNull
    public String toString() {
        var result = out.toString();
        return result != null
                ? result
                : String.valueOf(result);
    }

    // ------- FACTORY METHODS -------

    /** Create any element */
    public XmlBuilder createElement(@NotNull final String name) throws IOException {
        return new XmlBuilder(name, this);
    }

    // ------- STATIC METHODS -------

    /** Create a new instance including a XML_HEADER.
     * The result provides a method {@link #toString() }
     */
    public static XmlPrinter forXml() {
        return forXml(null, XmlConfig.ofDefault());
    }

    /** Create a new instance with a formatted output.
     * The result provides a method {@link #toString() }
     * @return New instance of the XmlPrinter
     */
    public static XmlPrinter forNiceXml() {
        var config = XmlConfig.ofDefault();
        config.setNiceFormat();
        return forXml(null, config);
    }

    /** A basic XmlPrinter factory method.
     * The result provides a method {@link #toString() }
     * @return New instance of the XmlPrinter
     */
    public static XmlPrinter forXml(
            @Nullable final Appendable out,
            @NotNull final XmlConfig config
    ) {
        return new XmlPrinter(out != null ? out : new StringBuilder(512), config);
    }

    // --- HTML ---

    /** Create a new instance including a DOCTYPE.
     * The result provides a method {@link #toString() }
     */
    public static XmlPrinter forHtml() {
        return forXml(null, HtmlConfig.ofDefault());
    }

    /** Create a new instance including a DOCTYPE */
    public static XmlPrinter forHtml(final Appendable out) {
        return forHtml(out, HtmlConfig.ofDefault());
    }

    /** Create a new instance including a DOCTYPE */
    public static XmlPrinter forNiceHtml(final Appendable out) {
        var config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        return forHtml(out, config);
    }

    /** Create XmlPrinter for UTF-8 */
    public static XmlPrinter forHtml(@NotNull final Object httpServletResponse) throws IOException {
        return forHtml(httpServletResponse, HtmlConfig.ofDefault());
    }

    /** Create XmlPrinter for UTF-8 */
    public static XmlPrinter forNiceHtml(@NotNull final Object httpServletResponse) throws IOException {
        var config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        return forHtml(httpServletResponse, config);
    }

    /** Create XmlPrinter for UTF-8 */
    public static <T> XmlPrinter forHtml(
            @Nullable final Appendable out,
            @NotNull final HtmlConfig config
    ) {
        return new XmlPrinter(out != null ? out : new StringBuilder(512), config);
    }

    /** Create XmlPrinter for UTF-8 */
    public static XmlPrinter forHtml(
            @NotNull final Object httpServletResponse,
            @NotNull final Charset charset,
            @NotNull final String indentationSpace,
            final boolean noCache
    ) throws IOException {
        var config = HtmlConfig.ofDefault();
        config.setCharset(charset);
        config.setIndentationSpace(indentationSpace);
        config.setCacheAllowed(!noCache);
        return forHtml(httpServletResponse, config);
    }

    /** Create XmlPrinter for UTF-8.
     * The basic HTML factory.
     */
    public static XmlPrinter forHtml(
            @NotNull final Object httpServletResponse,
            @NotNull final HtmlConfig config
    ) throws IOException {
        try {
            var writer = createWriter(
                    httpServletResponse,
                    config.getCharset(),
                    config.isCacheAllowed());
            return new XmlPrinter(writer, config);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Response must be type of HttpServletResponse", e);
        }
    }
}