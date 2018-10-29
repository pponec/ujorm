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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * XML element model to rendering a HTML file
 * <h3>How to use the class:</h3>
 * <pre class="pre">
 * final HtmlElement html = new HtmlElement("Test");
 * html.getBody().addElement("div")
 *               .addText("Hello word!");
 * HttpServletResponse response = new MockHttpServletResponse();
 * html.toResponse(response, false);
 * </pre>
 * @since 1.86
 * @author Pavel Ponec
 */
public class HtmlElement extends XmlElement {

    /** XML header */
    public static final String HEADER = "<!DOCTYPE html>";

    /** Head element */
    @Nonnull
    private final Element head;

    /** Body element */
    @Nonnull
    private final Element body;

    /** Charset */
    @Nonnull
    protected final Charset charset;

    /** Constructor for codepage UTF-8 */
    public HtmlElement(@Nonnull final CharSequence title) {
        this(title, UTF_8);
    }

    /** Generaic Constructor */
    public HtmlElement(@Nonnull final CharSequence title, @Nonnull Charset charset) {
        super(Html.HTML);
        this.charset = charset;

        head = addElement(Html.HEAD);
        head.addElement(Html.META)
                .addAttrib(Html.A_CHARSET, charset);
        body = addElement(Html.BODY);
        head.addElement(Html.TITLE)
                .addText(title);
    }

    /** Returns header element */
    @Nonnull
    public <T extends Element> T  getHead() {
        return (T) head;
    }

    /** Returns body element */
    @Nonnull
    public <T extends Element> T  getBody() {
        return (T) body;
    }

    /** A shortcut for {@code HtmlList.getHead().addElement(CharSequence) }
     * @param name A name of the new XmlElement is requred.
     * @return The new XmlElement!
     */
    public <T extends Element> T  addElementToHead(@Nonnull final CharSequence name) {
        return head.addElement(name);
    }

    /** A shortcut for {@code HtmlList.getBody().addElement(CharSequence) }
     * @param name A name of the new XmlElement is requred.
     * @return The new XmlElement!
     */
    public <T extends Element> T  addElementToBody(@Nonnull final CharSequence name) {
        return body.addElement(name);
    }

    /** Create a new CSS element and return it
     * @param css Add a CSS link
     * @return New CSS element
     */
    public <T extends Element> T  addCssLink(String css) {
        return head.addElement(Html.LINK)
                .addAttrib(Html.A_HREF, css)
                .addAttrib(Html.A_REL, "stylesheet")
                .addAttrib(Html.A_TYPE, "text/css");
    }

    /** Create a new CSS element and return it
     * @param css CSS content
     * @return New CSS element
     */
    public <T extends Element> T  addCssBody(@Nullable final String css) {
        return head.addElement(Html.STYLE)
                .addAttrib(Html.A_TYPE, "text/css")
                .addRawText(css);
    }

    /** Render the HTML code including header */
    @Override @Nonnull
    public String toString() throws IllegalStateException {
        try {
            return toWriter(new CharArrayWriter(512)
                    .append(HEADER)
                    .append(CHAR_NEW_LINE))
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
            toWriter(writer.append(HtmlElement.HEADER).append(CHAR_NEW_LINE));
            writer.flush();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Response must be type of HttpServletResponse", e);
        }
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
        String A_CONTENT = "content";
        String A_HREF = "href";
        String A_REL = "rel";
        String A_TYPE = "type";
    }
}