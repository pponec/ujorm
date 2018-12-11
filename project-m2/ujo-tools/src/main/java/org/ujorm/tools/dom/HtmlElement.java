/*
 * Copyright 2018-2018 Pavel Ponec,
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/HtmlElement.java
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

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import org.ujorm.tools.Assert;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.ujorm.tools.xml.CommonXmlWriter.CHAR_NEW_LINE;

/**
 * XML element model to rendering a HTML file
 * <h3>How to use the class:</h3>
 * <pre class="pre">
 * final HtmlElement html = new HtmlElement("Test");
 * html.getBody().addElement("div")
 *               .addText("Hello word!");
 * html.toResponse(new MockHttpServletResponse(), false);
 * </pre>
 * @since 1.86
 * @author Pavel Ponec
 */
public class HtmlElement extends XmlElement {

    /** XML header */
    public static final String HEADER = "<!DOCTYPE html>";

    /** Head element */
    @Nonnull
    private final XmlElement head;

    /** Body element */
    @Nonnull
    private final XmlElement body;

    /** Charset */
    @Nonnull
    protected final Charset charset;

    /** Create new instance with empty html headers */
    public HtmlElement(@Nonnull final Charset charset) {
        super(Html.HTML);

        Assert.notNull(charset, REQUIRED_MSG, "charset");
        this.charset = charset;
        this.head = addElement(Html.HEAD);
        this.body = addElement(Html.BODY);
    }

    /** Constructor buliding default html headers with codepage UTF-8 */
    public HtmlElement(@Nonnull final Object title) {
        this(title, UTF_8);
    }

    /** Generic constructor buliding default html headers */
    public HtmlElement(@Nonnull final Object title, @Nonnull Charset charset) {
        this(charset);
        head.addElement(Html.META).setAttrib(Html.A_CHARSET, charset);
        head.addElement(Html.TITLE).addText(title);
    }

    /** Returns header element */
    @Nonnull
    public <T extends XmlElement> T getHead() {
        return (T) head;
    }

    /** Returns body element */
    @Nonnull
    public <T extends XmlElement> T getBody() {
        return (T) body;
    }

    /** A shortcut for {@code HtmlList.getHead().addElement(CharSequence) }
     * @param name A name of the new XmlElement is requred.
     * @return The new XmlElement!
     */
    public <T extends XmlElement> T  addElementToHead(@Nonnull final String name) {
        return head.addElement(name);
    }

    /** A shortcut for {@code HtmlList.getBody().addElement(CharSequence) }
     * @param name A name of the new XmlElement is requred.
     * @return The new XmlElement!
     */
    public <T extends XmlElement> T  addElementToBody(@Nonnull final String name) {
        return body.addElement(name);
    }

    /** Create a new CSS element and return it
     * @param css Add a CSS link
     * @return New CSS element
     */
    public <T extends XmlElement> T addCssLink(@Nonnull final CharSequence css) {
        Assert.notNull(name, REQUIRED_MSG, "css");
        return head.addElement(Html.LINK)
                .setAttrib(Html.A_HREF, css)
                .setAttrib(Html.A_REL, "stylesheet")
                .setAttrib(Html.A_TYPE, "text/css");
    }

    /** Create a new CSS element and return it
     * @param css CSS content
     * @return New CSS element
     */
    public <T extends XmlElement> T addCssBody(@Nonnull final CharSequence css) {
        Assert.notNull(name, REQUIRED_MSG, "css");
        return head.addElement(Html.STYLE)
                .setAttrib(Html.A_TYPE, "text/css")
                .addRawText(css);
    }

    /** Render the HTML code including header */
    @Override @Nonnull
    public String toString() throws IllegalStateException {
        try {
            return toWriter(0, new XmlWriter(new StringBuilder(512)
                    .append(HEADER)
                    .append(CHAR_NEW_LINE)))
                    .toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Render the component to a <a href="https://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServletResponse.html">HTML response</a>
     * @param httpServletResponse Argument type of {@code javax.servlet.http.HttpServletResponse} is required.
     * @param noCache Add a header attributes to switch off a cache.
     * @throws IOException An writting error.
     * @throws IllegalArgumentException Wrong argument type
     */
    public void toResponse(@Nonnull final Object httpServletResponse, final boolean noCache) throws IOException, IllegalArgumentException {
        try {
            final Method setEncoding = httpServletResponse.getClass().getMethod("setCharacterEncoding", String.class);
            final Method setHeader = httpServletResponse.getClass().getMethod("setHeader", String.class, String.class);
            final Method getWriter = httpServletResponse.getClass().getMethod("getWriter");

            setEncoding.invoke(httpServletResponse, charset.toString());
            setHeader.invoke(httpServletResponse, "Content-Type", "text/html; charset=" + charset);
            if (noCache) {
                setHeader.invoke(httpServletResponse, "Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
                setHeader.invoke(httpServletResponse, "Pragma", "no-cache"); // HTTP 1.0
                setHeader.invoke(httpServletResponse, "Expires", "0"); // Proxies
            }

            final Writer writer = (Writer) getWriter.invoke(httpServletResponse);
            toWriter(new XmlWriter(writer.append(HtmlElement.HEADER).append(CHAR_NEW_LINE)));
            writer.flush();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Response must be type of HttpServletResponse", e);
        }
    }

    /**
     * Render the component to a {@link XmlWriter}
     * @param xmlWriter An instance of the XmlWriter is required.
     * @throws IOException An writting error.
     * @throws IllegalArgumentException Wrong argument type
     */
    public XmlWriter toWriter(@Nonnull final XmlWriter xmlWriter) throws IOException, IllegalArgumentException {
            toWriter(0, xmlWriter);
            return xmlWriter;
    }

    /** Some HTML constants */
    public static interface Html {

        // --- Element names ---

        /** Body element */
        String HTML = "html";
        /** Head element */
        String HEAD = "head";
        /** Meta element */
        String META = "meta";
        /** Body element */
        String BODY = "body";
        /** Title element */
        String TITLE = "title";
        /** Link element */
        String LINK = "link";
        /** Style element */
        String STYLE = "style";

        // --- Attribute names ---

        String A_CHARSET = "charset";
        String A_HREF = "href";
        String A_REL = "rel";
        String A_TYPE = "type";
    }
}