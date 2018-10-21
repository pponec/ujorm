/*
 * Copyright 2018-2018 Pavel Ponec, https://github.com/pponec
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

package org.ujorm.tools;

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
 * @author Pavel Ponec
 */
public class HtmlElement extends XmlElement {

    /** XML header */
    public static final String HEADER = "<!DOCTYPE html>";

    /** Head */
    private final XmlElement head;

    /** Body */
    private final XmlElement body;

    /** Charset */
    @Nonnull
    protected final Charset charset;

    /** Constructor for codepage UTF-8 */
    public HtmlElement(@Nonnull final String title) {
        this(title, UTF_8);
    }

    /** Generaic Constructor */
    public HtmlElement(@Nonnull final String title, @Nonnull Charset charset) {
        super("html");
        this.charset = charset;

        head = addElement("head");
        head.addElement("meta").addAttrib("charset", charset);

        body = addElement("body");
        head.addElement("title").addText(title);
    }

    /** Returns header element */
    @Nonnull
    public XmlElement getHead() {
        return head;
    }

    /** Returns body element */
    @Nonnull
    public XmlElement getBody() {
        return body;
    }

    /** Create a new CSS element and return it
     * @param css Add a CSS link
     * @return New CSS element
     */
    public XmlElement addCssLink(String css) {
        return head.addElement("link")
                .addAttrib("href", css)
                .addAttrib("rel", "stylesheet")
                .addAttrib("type", "text/css");
    }

    /** Create a new CSS element and return it
     * @param css CSS content
     * @return New CSS element
     */
    public XmlElement addCssBody(@Nullable final String css) {
        return head.addElement("style")
                .addAttrib("type", "text/css")
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
            throw new IllegalStateException(e.getMessage(), e);
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
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Response must be type of HttpServletResponse", e);
        }
    }
}