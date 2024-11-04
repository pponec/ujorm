/*
 * Copyright 2018-2022 Pavel Ponec, https://github.com/pponec
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
import java.nio.charset.Charset;
import java.util.function.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.web.request.RContext;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
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
 *    ServletResponse response = new ServletResponse();
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
    @NotNull
    private final Element root;

    /** Head element */
    @NotNull
    private Element head;

    /** Body element */
    @NotNull
    private Element body;

    /** Config */
    @NotNull
    private final HtmlConfig config;

    /** Config */
    @NotNull
    private final Appendable writer;

    /** Create new instance with empty html headers */
    public HtmlElement(@NotNull final HtmlConfig config, @NotNull final Appendable writer) {
        this(new XmlModel(Html.HTML), config, writer);
    }

    /** Create new instance with empty html headers */
    public HtmlElement(@NotNull final ApiElement root, @NotNull final HtmlConfig config, @NotNull final Appendable writer) {
        this.root = new Element(root);
        this.config = config;
        this.writer = writer;
    }

    @NotNull
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
            @NotNull final CharSequence template,
            @NotNull final Object... values) {
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
    @Override @NotNull
    public final Element addElement(@NotNull final String name)
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
    @NotNull
    public Element getBody() {
        if (body == null) {
            body = root.addElement(Html.BODY);
        }
        return body;
    }

    /** Returns a body element */
    @NotNull
    public Element addBody() {
        return getBody();
    }

    /** Create a new Javascript element and return it
     * @param javascriptLinks URL list to Javascript
     * @param defer A script that will not run until after the page has loaded
     */
    public void addJavascriptLinks(final boolean defer, @NotNull final CharSequence ... javascriptLinks) {
        for (CharSequence js : javascriptLinks) {
            addJavascriptLink(defer, js);
        }
    }

    /** Create a new Javascript element and return it
     * @param javascriptLink URL to Javascript
     * @param defer A script that will not run until after the page has loaded
     * @return
     */
    public Element addJavascriptLink(final boolean defer, @NotNull final CharSequence javascriptLink) {
        Assert.notNull(javascriptLink, REQUIRED_MSG, "javascriptLink");
        return getHead().addElement(Html.SCRIPT)
                .setAttribute(Html.A_SRC, javascriptLink)
                .setAttribute("defer", defer ? "defer" : null);
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
    public void addCssLinks(@NotNull final CharSequence... css) {
        for (CharSequence cssLink : css) {
            addCssLink(cssLink);
        }
    }

    /** Create a new CSS element and return it
     * @param css Add a CSS link
     * @return New CSS element
     */
    public Element addCssLink(@NotNull final CharSequence css) {
        Assert.notNull(css, REQUIRED_MSG, "css");
        return getHead().addElement(Html.LINK)
                .setAttribute(Html.A_HREF, css)
                .setAttribute(Html.A_REL, "stylesheet");
    }

    /** Create a new CSS element and return it
     * @param css CSS content
     * @return New CSS element
     */
    public Element addCssBody(@NotNull final CharSequence css) {
        Assert.notNull(css, REQUIRED_MSG, "css");
        return getHead().addElement(Html.STYLE)
                .addRawText(css);
    }

    /** Create a new CSS element and return it.
     * Each item is separated by a new line.
     * @param lineSeparator Row separator
     * @param css CSS content rows
     * @return New CSS element
     */
    public Element addCssBodies(
            @NotNull final CharSequence lineSeparator,
            @NotNull final CharSequence... css) {
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
    @NotNull
    public Element original() {
        return root;
    }

    /** Returns an Render the HTML code including header. Call the close() method before view */
    @Override @NotNull
    public String toString() throws IllegalStateException {
        return writer.toString();
    }

    @Override
    public void close() throws IllegalStateException {
        root.close();
        if (root.internalElement instanceof XmlModel) {
            final XmlModel xmlElement = (XmlModel) root.internalElement;
            try {
                final CharSequence doctype = config.getDoctype();
                final XmlWriter xmlWriter = new XmlWriter(writer
                        .append(doctype)
                        .append(doctype.length() == 0 ? "" : config.getNewLine())
                        , config.getIndentation());
                xmlElement.toWriter(config.getFirstLevel() + 1, xmlWriter);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /** Get config */
    @NotNull
    public HtmlConfig getConfig() {
        return config;
    }

    /** Get title of configuration */
    public CharSequence getTitle() {
        return getConfig().getTitle();
    }

    // ------- Static methods ----------

    /** Create root element for a required element name. The MAIN factory method. */
    @NotNull
    public static HtmlElement of(
            @NotNull final Appendable writer,
            @NotNull final HtmlConfig myConfig
    ) throws IllegalStateException {
        HtmlConfig config = myConfig != null ? myConfig : new DefaultHtmlConfig();
        //config.setNiceFormat();
        //config.setCssLinks(cssLinks);

        final ApiElement root = config.isDocumentObjectModel()
                ? new XmlModel(config.getRootElementName())
                : new XmlBuilder(config.getRootElementName(), new XmlPrinter(writer, config), config.getFirstLevel());
        final HtmlElement result = new HtmlElement(root, config, writer);
        if (config.isHtmlHeaderRequest()) {
            config.getLanguage().ifPresent(lang -> result.setAttribute(A_LANG, lang));
            result.getHead().addElement(Html.META).setAttribute(A_CHARSET, config.getCharset());
            result.getHead().addElement(Html.TITLE).addText(config.getTitle());
            result.addCssLinks(config.getCssLinks());
            config.getHeaderInjector().write(result.getHead());

            // A deprecated solution:
            final CharSequence rawHeaderText = config.getRawHeaderText();
            if (Check.hasLength(rawHeaderText)) {
                result.getHead().addRawText(config.getNewLine());
                result.getHead().addRawText(rawHeaderText);
            }
        }
        return result;
    }

    /** Create root element for a required element name. The MAIN factory method. */
    @NotNull
    public static HtmlElement of(
            @NotNull final RContext context,
            @NotNull final HtmlConfig myConfig) {
        return of(context.writer(), myConfig);
    }


    /** Create new instance with empty html headers, The MAIN servlet factory method.
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement ofServlet(
            @NotNull final Object htmlServletResponse,
            @Nullable final HtmlConfig config) {
        return of(RContext.ofServlet(null, htmlServletResponse).writer(), config);
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement ofServlet(
            @NotNull final String title,
            @NotNull final Object htmlServletResponse,
            @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(RContext.ofServlet(null, htmlServletResponse).writer(), config);
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement of(@NotNull final CharSequence title, @NotNull final Appendable response, @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement of(@NotNull final CharSequence title, @NotNull final Appendable response, @NotNull final Charset charset, @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOf(@NotNull final CharSequence title, @NotNull final Appendable response, @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOf(@NotNull final CharSequence title, @NotNull final Appendable response, @NotNull final Charset charset, @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCharset(charset);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }



    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOfResponse(
            @NotNull final String title,
            @NotNull final Object httpServletResponse,
            @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(RContext.ofServlet(null, httpServletResponse).writer(), config);
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOfResponse(
            @NotNull final Object httpServletResponse,
            @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setCssLinks(cssLinks);
        return of(RContext.ofServlet(null, httpServletResponse).writer(), config);
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOf(
            @NotNull final String title,
            @NotNull final RContext context,
            @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(context.writer(), config);
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions
     * @see Appendable
     */
    @NotNull
    public static HtmlElement niceOf(
            @NotNull final String title,
            @NotNull final Appendable response,
            @NotNull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with empty html headers
     * @param config Html configuration
     * @return An instance of the HtmlPage
     * @throws IllegalStateException IO exceptions
     */
    @NotNull
    public static HtmlElement of(@Nullable final HtmlConfig config) throws IllegalStateException {
        return of(new StringBuilder(256), config);
    }

    /** Apply body of element by a lambda expression.
     *
     * @deprecated Use the method {@link #next(Consumer)} rather.
     */
    @NotNull
    public final ExceptionProvider then(@NotNull final Consumer<HtmlElement> builder) {
        return next(builder);
    }

    /** Add nested elements to the element.
     *
     * <h3>Usage</h3>
     *
     * <pre class="pre">
     *  HtmlElement.of(config, writer).addBody()
     *      .next(body -> {
     *         body.addHeading(config.getTitle());
     *      })
     *      .catche(e -> {
     *          logger.log(Level.SEVERE, "An error", e);
     *      });
     * </pre>
     */
    @NotNull
    public ExceptionProvider next(@NotNull final Consumer<HtmlElement> builder) {
        try {
            builder.accept(this);
            return ExceptionProvider.of();
        } catch (RuntimeException e) {
            return ExceptionProvider.of(e);
        } finally {
            close();
        }
    }
}
