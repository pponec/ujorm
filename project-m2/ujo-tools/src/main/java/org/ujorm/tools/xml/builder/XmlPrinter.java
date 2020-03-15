/*
 * Copyright 2018-2020 Pavel Ponec,
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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.xml.AbstractWriter;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultXmlConfig;
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
        this(new CharArrayWriter(512));
    }

    /** Writer constructor with a zero offset */
    public XmlPrinter(@Nonnull final Appendable out) {
        this(out, "");
    }

    /**
     * A writer constructor
     * @param out A writer
     * @param indentationSpace String for a one level offset.
     */
    public <T> XmlPrinter(@Nonnull final Appendable out, @Nullable final String indentationSpace, @Nonnull final T... prefixes) {
        super(out, indentationSpace);
        try {
            for (Object prefix : prefixes) {
                out.append(String.valueOf(prefix));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
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
    void writeBeg(XmlBuilder element, final boolean lastText) throws IOException {
        if (!lastText) {
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
            if (indentationEnabled && !element.isLastText()) {
                writeNewLine(element.getLevel());
            }
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

    // ------- FACTORY METHODS -------

    /** Create any element */
    public XmlBuilder createElement(@Nonnull final String elementName) throws IOException {
        return new XmlBuilder(elementName, this);
    }

    // ------- STATIC METHODS -------

    /** Crete a new instance including a XML_HEADER.
     * The result provides a method {@link #toString() }
     */
    public static XmlPrinter forXml() {
        return forXml(null, XmlConfig.ofDefault());
    }

    /** Crete a new instance with a formatted output.
     * The result provides a method {@link #toString() }
     * @return New instance of the XmlPrinter
     */
    public static XmlPrinter forNiceXml() {
        DefaultXmlConfig config = XmlConfig.ofDefault();
        config.setNiceFormat(true);
        return forXml(null, config);
    }

    /** A basic XmlPrinter factory method.
     * The result provides a method {@link #toString() }
     * @return New instance of the XmlPrinter
     */
    public static XmlPrinter forXml(
            @Nullable final Appendable out,
            @Nonnull final XmlConfig config
    ) {
        return new XmlPrinter(out != null ? out : new StringBuilder(512),
                config.getIndentation(),
                config.getDoctype());
    }

    // --- HTML ---

    /** Crete a new instance including a DOCTYPE.
     * The result provides a method {@link #toString() }
     */
    public static XmlPrinter forHtml() {
        DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.getDoctype();
        return forXml(null, config);
    }

    /** Crete a new instance including a DOCTYPE */
    public static XmlPrinter forHtml(final Appendable out) {
        DefaultHtmlConfig config = HtmlConfig.ofDefault();
        return forXml(out, config);
    }

    /** Crete a new instance including a DOCTYPE */
    public static XmlPrinter forNiceHtml(final Appendable out) {
        DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat(true);
        return forHtml(out, config);
    }

    /** Create XmlPrinter for UTF-8 */
    public static XmlPrinter forHtml(@Nonnull final Object httpServletResponse) throws IOException {
        DefaultHtmlConfig config = HtmlConfig.ofDefault();
        return forHtml(httpServletResponse, config);
    }

    /** Create XmlPrinter for UTF-8 */
    public static XmlPrinter forNiceHtml(@Nonnull final Object httpServletResponse) throws IOException {
        DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat(true);
        return forHtml(httpServletResponse, config);
    }

    /** Create XmlPrinter for UTF-8 */
    public static <T> XmlPrinter forHtml(
            @Nullable final Appendable out,
            @Nonnull final HtmlConfig config
    ) {
            return new XmlPrinter(out != null ? out : new StringBuilder(512),
                    config.getIndentation(),
                    config.getDoctype());
    }

    /** Create XmlPrinter for UTF-8 */
    public static XmlPrinter forHtml(
            @Nonnull final Object httpServletResponse,
            @Nonnull final Charset charset,
            @Nonnull final String indentationSpace,
            final boolean noCache
    ) throws IOException {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setCharset(charset);
        config.setIndentationSpace(indentationSpace);
        config.setCacheAllowed(!noCache);
        return forHtml(httpServletResponse, config);
    }

    /** Create XmlPrinter for UTF-8.
     * The basic HTML factory.
     */
    public static XmlPrinter forHtml(
            @Nonnull final Object httpServletResponse,
            @Nonnull final HtmlConfig config
    ) throws IOException {
        try {
            final Writer writer = createWriter(
                    httpServletResponse,
                    config.getCharset(),
                    config.isCacheAllowed());
            return new XmlPrinter(writer,
                    config.getIndentation(),
                    config.getDoctype());
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Response must be type of HttpServletResponse", e);
        }
    }
}
