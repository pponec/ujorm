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

package org.ujorm.tools.xml.builder;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.xml.CommonXmlWriter;
import org.ujorm.tools.xml.dom.HtmlElement;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.ujorm.tools.xml.AbstractElement.HTML_DOCTYPE;
import static org.ujorm.tools.xml.AbstractElement.XML_HEADER;
import static org.ujorm.tools.xml.CommonXmlWriter.CHAR_NEW_LINE;

/**
 * If you need special formatting, overwrite responsible methods.
 * @see XmlBuilder
 * @since 1.88
 * @author Pavel Ponec
 */
public class XmlPrinter extends CommonXmlWriter {

    /** Default constructor a zero offset */
    public XmlPrinter() {
        this(new CharArrayWriter(512));
    }

    /** Writer constructor with a zero offset */
    public XmlPrinter(@Nonnull final Appendable out) {
        this(out, "");
    }

    /** Writer constructor with a zero offset */
    public XmlPrinter(@Nonnull final Appendable out, @Nullable final boolean indentation, Object... initTexts) {
        super(out, indentation ? "    " : null);
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
     * @param indentationSpace String for a one level offset.
     */
    public XmlPrinter(@Nonnull final Appendable out, @Nullable final String indentationSpace) {
        super(out, indentationSpace);
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

    /** Create new {@code html} element */
    public XmlBuilder createHtmlElement() throws IOException {
        return createElement(HtmlElement.Html.HTML);
    }

    /** Create any element */
    public XmlBuilder createElement(@Nonnull final String elementName) throws IOException {
        return new XmlBuilder(elementName, this);
    }

    // ------- STATIC METHODS -------

    /** Crete a new instance including a XML_HEADER.
     * The result provides a method {@link #toString() }
     */
    public static XmlPrinter forXml() {
        return new XmlPrinter(new StringBuilder(512), false, XML_HEADER);
    }

    /** Crete a new instance with no header.
     * The result provides a method {@link #toString() }
     * @param indentation An intendation by four spaces.
     * @return New instance of the XmlPrinter
     */
    public static XmlPrinter forXml(boolean indentation) {
        return new XmlPrinter(new StringBuilder(512), indentation, XML_HEADER);
    }

    /** Crete a new instance including a DOCTYPE.
     * The result provides a method {@link #toString() }
     */
    public static XmlPrinter forHtml() {
        return new XmlPrinter(new StringBuilder(512), false, HTML_DOCTYPE);
    }

    /** Crete a new instance including a DOCTYPE */
    public static XmlPrinter forHtml(final Appendable out) {
        return new XmlPrinter(out, false, HTML_DOCTYPE);
    }


    /** Create XmlPrinter for UTF-8 */
    public static XmlPrinter forHtml(@Nonnull final Object httpServletResponse) throws IOException {
        return forHtml(httpServletResponse, UTF_8, true);
    }

    /** Create XmlPrinter for UTF-8 */
    public static XmlPrinter forHtml(@Nonnull final Object httpServletResponse, @Nonnull final Charset charset, final boolean noCache) throws IOException {
        try {
            final Writer writer = createWriter(httpServletResponse, charset, noCache);
            return new XmlPrinter(writer.append(HtmlElement.HTML_DOCTYPE).append(CHAR_NEW_LINE));
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Response must be type of HttpServletResponse", e);
        }
    }
}
