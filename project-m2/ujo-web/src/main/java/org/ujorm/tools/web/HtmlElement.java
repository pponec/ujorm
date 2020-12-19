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

package org.ujorm.tools.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.web.ao.MockServletResponse;
import org.ujorm.tools.xml.ApiElement;
import org.ujorm.tools.xml.builder.XmlBuilder;
import org.ujorm.tools.xml.builder.XmlPrinter;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;
import org.ujorm.tools.xml.model.XmlModel;
import org.ujorm.tools.xml.model.XmlWriter;
import static org.ujorm.tools.xml.config.impl.DefaultXmlConfig.REQUIRED_MSG;

/** The root of HTML elements
 *
 * <h3>Usage</h3>
 *
 * <pre class="pre">
 *    MockServletResponse response = new MockServletResponse();
 *    try (HtmlElement html = HtmlElement.of(response)) {
 *        html.addBody().addHeading("Hello!");
 *    }
 *    assertTrue(response.toString().contains("&lt;h1&gt;Hello!&lt;/h1&gt;"));
 * </pre>
 *
 * For more information see the
 * <a target="_top" href="https://jbook-samples-free.ponec.net/sample?src=net.ponec.jbook.s01_hello.HelloWorldElement">next sample</a>.
 */
public class HtmlElement implements ApiElement<Element>, Html {

    /** Head element */
    @Nonnull
    private final Element root;

    /** Head element */
    @Nonnull
    private Element head;

    /** Body element */
    @Nonnull
    private Element body;

    /** Config */
    @Nonnull
    private final HtmlConfig config;

    /** Config */
    @Nonnull
    private final Appendable writer;

    /** Create new instance with empty html headers */
    public HtmlElement(@Nonnull final HtmlConfig config, @Nonnull final Appendable writer) {
        this(new XmlModel(Html.HTML), config, writer);
    }

    /** Create new instance with empty html headers */
    public HtmlElement(@Nonnull final ApiElement root, @Nonnull final HtmlConfig config, @Nonnull final Appendable writer) {
        this.root = new Element(root);
        this.config = config;
        this.writer = writer;
    }

    @Override
    public String getName() {
        return root.getName();
    }

    @Override
    public Element setAttribute(String name, Object value) {
        return root.setAttribute(name, value);
    }

    @Override
    public Element addText(Object value) {
        return root.addText(value);
    }

    @Override
    public Element addTextTemplated(
            @Nonnull final CharSequence template,
            @Nonnull final Object... values) {
        return root.addTextTemplated(template, values);
    }

    @Override
    public Element addRawText(Object value) {
        return root.addRawText(value);
    }

    @Override
    public Element addComment(CharSequence comment) {
        return root.addComment(comment);
    }

    @Deprecated
    @Override
    public Element addCDATA(CharSequence charData) {
        return root.addCDATA(charData);
    }

    /**
     * Create new Element
     * @param name The element name
     * @return New instance of the Element
     * @throws IllegalStateException An envelope for IO exceptions
     */
    @Override @Nonnull
    public final Element addElement(@Nonnull final String name)
            throws IllegalStateException {
        switch (name) {
            case Html.HEAD:
                return getHead();
            case Html.BODY:
                return getBody();
            default:
                return root.addElement(name);
        }
    }

    /** Returns a head element */
    public Element getHead() {
        if (head == null) {
            head = root.addElement(Html.HEAD);
        }
        return head;
    }

    /** Returns a head element */
    public Element addHead() {
        return getHead();
    }

    /** Returns a body element */
    @Nonnull
    public Element getBody() {
        if (body == null) {
            body = root.addElement(Html.BODY);
        }
        return body;
    }

    /** Returns a body element */
    @Nonnull
    public Element addBody() {
        return getBody();
    }

    /** Create a new Javascript element and return it
     * @param javascriptLinks URL list to Javascript
     * @param defer A script that will not run until after the page has loaded
     */
    public void addJavascriptLinks(final boolean defer, @Nonnull final CharSequence ... javascriptLinks) {
        for (CharSequence js : javascriptLinks) {
            addJavascriptLink(defer, js);
        }
    }

    /** Create a new Javascript element and return it
     * @param javascriptLink URL to Javascript
     * @param defer A script that will not run until after the page has loaded
     * @return
     */
    public Element addJavascriptLink(final boolean defer, @Nonnull final CharSequence javascriptLink) {
        Assert.notNull(javascriptLink, REQUIRED_MSG, "javascriptLink");
        return getHead().addElement(Html.SCRIPT)
                .setAttribute(Html.A_SRC, javascriptLink)
                .setAttribute("defer", defer ? "defer" : null)
                .addText("");
    }

    /** User the method {@link #addJavascriptBody(java.lang.CharSequence...) } rather */
    @Deprecated
    public Element addJavascriptContents(@Nonnull final CharSequence javascript) {
        return addJavascriptBody(javascript);
    }

    /** Create a new Javascript element and return it.
     * Each item is separated by a new line.
     * @param javascript Add a javascriptLink link
     * @return New CSS element
     */
    public Element addJavascriptBody(@Nullable final CharSequence... javascript) {
        if (Check.hasLength(javascript)) {
            final Element result = getHead().addElement(Html.SCRIPT)
                    .setAttribute(Html.A_LANGUAGE, "javascript")
                    .setAttribute(Html.A_TYPE, "text/javascript");
            for (int i = 0, max = javascript.length; i < max; i++) {
                if (i > 0) {
                    result.addRawText("\n");
                }
                result.addRawText(javascript[i]);
            }
            return result;
        }
        return head;
    }

    /** Create a new CSS element and return it
     * @param css Add a CSS link
     */
    public void addCssLinks(@Nonnull final CharSequence... css) {
        for (CharSequence cssLink : css) {
            addCssLink(cssLink);
        }
    }

    /** Create a new CSS element and return it
     * @param css Add a CSS link
     * @return New CSS element
     */
    public Element addCssLink(@Nonnull final CharSequence css) {
        Assert.notNull(css, REQUIRED_MSG, "css");
        return getHead().addElement(Html.LINK)
                .setAttribute(Html.A_HREF, css)
                .setAttribute(Html.A_REL, "stylesheet");
    }

    /** Create a new CSS element and return it.
     * Each item is separated by a new line.
     * @param css CSS content row
     * @return New CSS element
     */
    public Element addCssBody(
            @Nonnull final CharSequence lineSeparator,
            @Nonnull final CharSequence... css) {
        Assert.hasLength(css, REQUIRED_MSG, "css");
        final Element result = getHead().addElement(Html.STYLE);
        for (int i = 0, max = css.length; i < max; i++) {
            if (i > 0) {
                result.addRawText(lineSeparator);
            }
            result.addRawText(css[i]);

        }
        return result;
    }

    /** Get an original root element */
    public Element rootElement() {
        return root;
    }

    /** Returns an Render the HTML code including header. Call the close() method before view */
    @Override @Nonnull
    public String toString() throws IllegalStateException {
        return writer.toString();
    }

    @Override
    public void close() throws IllegalStateException {
        root.close();
        if (root.internalElement instanceof XmlModel) {
            final XmlModel xmlElement = (XmlModel) root.internalElement;
            try {
                final XmlWriter xmlWriter = new XmlWriter(writer
                        .append(config.getDoctype())
                        .append(config.getNewLine())
                        , config.getIndentation());
                xmlElement.toWriter(config.getFirstLevel() + 1, xmlWriter);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /** Get config */
    @Nonnull
    public HtmlConfig getConfig() {
        return config;
    }

    /** Get title of configuration */
    public CharSequence getTitle() {
        return getConfig().getTitle();
    }

    // ------- Static methods ----------

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions
     * @see MockServletResponse
     */
    @Nonnull
    public static HtmlElement of(@Nonnull final HttpServletResponse response, @Nonnull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions
     * @see MockServletResponse
     */
    @Nonnull
    public static HtmlElement of(@Nonnull final CharSequence title, @Nonnull final HttpServletResponse response, @Nonnull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions
     * @see MockServletResponse
     */
    @Nonnull
    public static HtmlElement of(@Nonnull final CharSequence title, @Nonnull final HttpServletResponse response, @Nonnull final Charset charset, @Nonnull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions
     * @see MockServletResponse
     */
    @Nonnull
    public static HtmlElement niceOf(@Nonnull final CharSequence title, @Nonnull final HttpServletResponse response, @Nonnull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions
     * @see MockServletResponse
     */
    @Nonnull
    public static HtmlElement niceOf(@Nonnull final CharSequence title, @Nonnull final HttpServletResponse response, @Nonnull final Charset charset, @Nonnull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCharset(charset);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions
     * @see MockServletResponse
     */
    @Nonnull
    public static HtmlElement niceOf(
            @Nonnull final HttpServletResponse response,
            @Nonnull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** A base method to create new instance
     * @param request The HttpRequest gets the code page from the context only.
     * @param response HttpResponse to write a result
     * @return An instance of the HtmlPage
     * @throws IllegalStateException IO exceptions
     * @see MockServletResponse
     */
    @Nonnull
    public static HtmlElement of(
            @Nonnull final HttpServletRequest request,
            @Nonnull final HttpServletResponse response) throws IllegalStateException, UnsupportedEncodingException {
        return of(request, response, HtmlConfig.ofDefault());
    }

    /** A base method to create new instance
     * @param request The HttpRequest gets the code page from the context only.
     * @param response HttpResponse to write a result
     * @param config Html configuration
     * @return An instance of the HtmlPage
     * @throws IllegalStateException IO exceptions
     * @see MockServletResponse
     */
    @Nonnull
    public static HtmlElement of(
            @Nonnull final HttpServletRequest request,
            @Nonnull final HttpServletResponse response,
            @Nonnull final HtmlConfig config) throws IllegalStateException, UnsupportedEncodingException {
        request.setCharacterEncoding(config.getCharset().toString());
        return of(response, config);
    }

    /** A base method to create new instance with empty html headers
     * @param response HttpResponse to write a result
     * @param config Html configuration
     * @return An instance of the HtmlPage
     * @throws IllegalStateException IO exceptions
     * @see MockServletResponse
     */
    @Nonnull
    public static HtmlElement of(
            @Nonnull final HttpServletResponse response,
            @Nonnull final HtmlConfig config) throws IllegalStateException {
        response.setCharacterEncoding(config.getCharset().toString());
        response.setContentType(config.getContentType());
        try {
            return of(config, response.getWriter());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Create new instance with empty html headers
     * @param config Html configuration
     * @return An instance of the HtmlPage
     * @throws IllegalStateException IO exceptions
     */
    @Nonnull
    public static HtmlElement of(@Nullable HtmlConfig config) throws IllegalStateException {
        return of(HtmlConfig.ofDefault(), new StringBuilder(256));
    }

    /** Create root element for a required element name */
    public static HtmlElement of(
            @Nonnull final HtmlConfig config,
            @Nonnull final Appendable writer
    ) throws IllegalStateException {
        final ApiElement root = config.isDocumentObjectModel()
                ? new XmlModel(config.getRootElementName())
                : new XmlBuilder(config.getRootElementName(), new XmlPrinter(writer, config));
        final HtmlElement result = new HtmlElement(root, config, writer);
        if (config.isHtmlHeaderRequest()) {
            config.getLanguage().ifPresent(lang -> result.setAttribute(A_LANG, lang));
            result.getHead().addElement(Html.META).setAttribute(A_CHARSET, config.getCharset());
            result.getHead().addElement(Html.TITLE).addText(config.getTitle());
            result.addCssLinks(config.getCssLinks());
            final CharSequence rawHeaderText = config.getRowHeaderText();
            if (Check.hasLength(rawHeaderText)) {
                result.addRawText(config.getNewLine());
                result.addRawText(rawHeaderText);
            }
        }
        return result;
    }
}
