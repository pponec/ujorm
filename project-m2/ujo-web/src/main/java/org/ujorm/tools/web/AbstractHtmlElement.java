/*
 * Copyright 2018-2026 Pavel Ponec, https://github.com/pponec
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

package org.ujorm.tools.web;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.web.request.HttpContext;
import org.ujorm.tools.xml.ApiElement;
import org.ujorm.tools.xml.builder.XmlBuilder;
import org.ujorm.tools.xml.builder.XmlPrinter;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;
import org.ujorm.tools.xml.model.XmlModel;
import org.ujorm.tools.xml.model.XmlWriter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Consumer;

import static org.ujorm.tools.xml.config.impl.DefaultXmlConfig.REQUIRED_MSG;

/**
 * The root of HTML elements is <b>independent</b> on the Servlet API
 *
 * <h4>Usage</h4>
 * <pre class="pre">
 * var response = HttpContext.of();
 * try (var html = AbstractHtmlElement.of(response)) {
 *     try (var body = html.getBody()) {
 *         body.addHeading("Hello!");
 *         body.addLabel().addText("Active:")
 *             .addCheckBox("active").setCheckBoxValue(true);
 *     }
 * }
 * assertTrue(response.toString().contains("&lt;h1&gt;Hello!&lt;/h1&gt;"));
 * </pre>
 *
 * For more information, see the
 * <a target="_top" href="https://jbook-samples-free.ponec.net/sample?src=net.ponec.jbook.s01_hello.HelloWorldElement">next sample</a>.
 */
public abstract class AbstractHtmlElement implements ApiElement<Element>, Html {

    /** No CSS styles */
    private static final String[] NO_CSS = Element.NO_CSS;

    /** Root element (usually &lt;html&gt;) */
    @NotNull
    protected final Element root;

    /** Head element */
    @Nullable
    private Element head;

    /** Body element */
    @Nullable
    private Element body;

    /** Configuration */
    @NotNull
    private final HtmlConfig config;

    /** Writer */
    @NotNull
    private final Appendable writer;

    /** Assigned html lang */
    @NotNull
    private CharSequence lang = "";

    /** Flag to indicate if the header is already initialized */
    private boolean headerInitialized = false;

    /** Create new instance with empty HTML headers
     * @param config Configuration
     * @param writer Writer
     */
    public AbstractHtmlElement(@NotNull final HtmlConfig config, @NotNull final Appendable writer) {
        this(new XmlModel(Html.HTML), config, writer);
    }

    /** Create new instance with explicit root
     * @param root Root element
     * @param config Configuration
     * @param writer Writer
     */
    public AbstractHtmlElement(@NotNull final ApiElement root, @NotNull final HtmlConfig config, @NotNull final Appendable writer) {
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
    public Element setAttribute(@NotNull String name, @Nullable Object value) {
        if (Html.A_LANG.equals(name)) {
            this.lang = value != null ? value.toString() : "";
        }
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
     */
    @Override @NotNull
    public final Element addElement(@NotNull final String name) {
        return addElement(name, NO_CSS);
    }

    /**
     * Create new Element
     * @param name The element name
     * @param css CSS classes
     * @return New instance of the Element
     */
    public final Element addElement(@NotNull final String name, @NotNull final CharSequence... css) {
        return switch (name) {
            case Html.HEAD -> addHead(css);
            case Html.BODY -> addBody(css);
            default -> {
                initHeader();
                yield root.addElement(name, css);
            }
        };
    }

    /** Returns or creates a head element
     * @param css CSS classes
     * @return Head element
     */
    public Element addHead(@NotNull final CharSequence... css) {
        if (head == null) {
            initHeader();
            if (head == null) {
                head = root.addElement(Html.HEAD, css);
            }
        }
        return head;
    }

    /** Returns a head element
     * @return Head element
     */
    public final Element getHead() {
        return addHead();
    }

    /** Returns or creates a body element
     * @param css CSS classes
     * @return Body element
     */
    @NotNull
    public Element addBody(@NotNull final CharSequence... css) {
        if (body == null) {
            initHeader();
            body = root.addElement(Html.BODY, css);
        }
        return body;
    }

    /** Returns a body element
     * @return Body element
     */
    @NotNull
    public final Element getBody() {
        return addBody();
    }

    /** Lazy initialize the HTML header if requested and not yet initialized */
    protected void initHeader() {
        if (!headerInitialized && config.isHtmlHeaderRequest()) {
            headerInitialized = true;

            // 1. First set root attributes BEFORE adding any child element
            if (lang.isEmpty()) {
                config.getLanguage().ifPresent(
                        lang -> root.setAttribute(A_LANG, lang));
            }

            // 2. Then create or use head element
            final Element headElement = head != null ? head : root.addElement(Html.HEAD);
            if (head == null) {
                head = headElement;
            }

            // 3. Populate head
            headElement.addElement(Html.META).setAttribute(A_CHARSET, config.getCharset());
            headElement.addElement(Html.TITLE).addText(config.getTitle());
            addCssLinks(config.getCssLinks());
            config.getHeaderInjector().write(headElement);

            final var rawHeaderText = config.getRawHeaderText();
            if (Check.hasLength(rawHeaderText)) {
                headElement.addRawText(config.getNewLine());
                headElement.addRawText(rawHeaderText);
            }
        }
    }

    /** Create new Javascript links
     * @param defer A script that will not run until after the page has loaded
     * @param javascriptLinks URL list to Javascript */
    public void addJavascriptLinks(final boolean defer, @NotNull final CharSequence... javascriptLinks) {
        for (var js : javascriptLinks) {
            addJavascriptLink(defer, js);
        }
    }

    /** Create a new Javascript element and return it
     * @param defer A script that will not run until after the page has loaded
     * @param javascriptLink URL to Javascript
     * @return New script element
     */
    public Element addJavascriptLink(final boolean defer, @NotNull final CharSequence javascriptLink) {
        Assert.notNull(javascriptLink, REQUIRED_MSG, "javascriptLink");
        return getHead().addElement(Html.SCRIPT)
                .setAttribute(Html.A_SRC, javascriptLink)
                .setAttribute("defer", defer ? "defer" : null);
    }

    /** Create a new Javascript element and return it.
     * @param javascript Add a javascriptLink link
     * @return New script element
     */
    public Element addJavascriptBody(@Nullable final CharSequence... javascript) {
        if (Check.hasLength(javascript)) {
            final var result = getHead().addElement(Html.SCRIPT)
                    .setAttribute(Html.A_LANGUAGE, "javascript")
                    .setAttribute(Html.A_TYPE, "text/javascript");
            for (int i = 0; i < javascript.length; i++) {
                if (i > 0) result.addRawText("\n");
                result.addRawText(javascript[i]);
            }
            return result;
        }
        return getHead();
    }

    /** Create new CSS links
     * @param css Add a CSS links */
    public void addCssLinks(@NotNull final CharSequence... css) {
        for (var cssLink : css) {
            addCssLink(cssLink);
        }
    }

    /** Create a new CSS link
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
        return getHead().addElement(Html.STYLE).addRawText(css);
    }

    /** Create a new CSS element and return it.
     * @param lineSeparator Row separator
     * @param css CSS content rows
     * @return New CSS element
     */
    public Element addCssBodies(
            @NotNull final CharSequence lineSeparator,
            @NotNull final CharSequence... css) {
        Assert.hasLength(css, REQUIRED_MSG, "css");
        final var result = getHead().addElement(Html.STYLE);
        for (int i = 0; i < css.length; i++) {
            if (i > 0) result.addRawText(lineSeparator);
            result.addRawText(css[i]);
        }
        return result;
    }

    /** Get an original root element
     * @return Original element
     */
    @NotNull
    public Element original() {
        return root;
    }

    /** Returns an Render the HTML code including header.
     * @return HTML code
     */
    @Override @NotNull
    public String toString() {
        return writer.toString();
    }

    @Override
    public void close() throws IllegalStateException {
        initHeader();
        root.close();
        if (root.internalElement instanceof XmlModel xmlElement) {
            try {
                final var doctype = config.getDoctype();
                final var separator = doctype.isEmpty() ? "" : config.getNewLine();
                final var xmlWriter = new XmlWriter(writer.append(doctype).append(separator), config.getIndentation());
                xmlElement.toWriter(config.getFirstLevel() + 1, xmlWriter);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /** Get config
     * @return Configuration
     */
    @NotNull
    public HtmlConfig getConfig() {
        return config;
    }

    /** Get title of configuration
     * @return Title
     */
    public CharSequence getTitle() {
        return getConfig().getTitle();
    }

    /** Apply body of element by a lambda expression.
     * @param builder Builder
     * @return Exception provider
     * @deprecated Use the method {@link #next(Consumer)} rather.
     */
    @Deprecated
    @NotNull
    public final ExceptionProvider then(@NotNull final Consumer<AbstractHtmlElement> builder) {
        return next(builder);
    }

    /** Add nested elements to the element.
     * @param builder Lambda expression
     * @return Exception provider
     */
    @NotNull
    public ExceptionProvider next(@NotNull final Consumer<AbstractHtmlElement> builder) {
        try {
            builder.accept(this);
            return ExceptionProvider.of();
        } catch (RuntimeException e) {
            return ExceptionProvider.of(e);
        } finally {
            close();
        }
    }

    // ------- Static factory methods ----------

    /** Create a root element
     * @param writer Writer
     * @param myConfig Configuration
     * @return HtmlElement instance
     */
    @NotNull
    public static HtmlElement of(@NotNull final Appendable writer, @Nullable final HtmlConfig myConfig) {
        final var config = myConfig != null ? myConfig : new DefaultHtmlConfig();
        final ApiElement rootElement = config.isDocumentObjectModel()
                ? new XmlModel(config.getRootElementName())
                : new XmlBuilder(config.getRootElementName(), new XmlPrinter(writer, config), config.getFirstLevel());

        return new HtmlElement(rootElement, config, writer);
    }

    /** Create root element
     * @param context Context
     * @param myConfig Configuration
     * @return HtmlElement instance
     */
    @NotNull
    public static HtmlElement of(@NotNull final HttpContext context, @Nullable final HtmlConfig myConfig) {
        return of(context.writer(), myConfig);
    }

    /** Create root element for servlet
     * @param httpServletResponse Response
     * @param config Configuration
     * @return HtmlElement instance
     */
    @NotNull
    public static HtmlElement ofServlet(@NotNull final Object httpServletResponse, @Nullable final HtmlConfig config) {
        return of(HttpContext.ofServlet(null, httpServletResponse).writer(), config);
    }

    /** Create new instance
     * @param config Configuration
     * @return HtmlElement instance
     */
    @NotNull
    public static HtmlElement of(@Nullable final HtmlConfig config) {
        return of(new StringBuilder(256), config);
    }

    // --- Convenience Factory Methods (Pairs of: of / niceOf) ---

    // 1. Appendable variants

    /** Create new instance
     * @param title Title
     * @param response Response
     * @param cssLinks CSS links
     * @return HtmlElement instance
     */
    @NotNull
    public static HtmlElement of(@NotNull final CharSequence title, @NotNull final Appendable response, @NotNull final CharSequence... cssLinks) {
        final var config = HtmlConfig.ofDefault();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with nice format
     * @param title Title
     * @param response Response
     * @param cssLinks CSS links
     * @return HtmlElement instance
     */
    @NotNull
    public static HtmlElement niceOf(@NotNull final CharSequence title, @NotNull final Appendable response, @NotNull final CharSequence... cssLinks) {
        final var config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with charset
     * @param title Title
     * @param response Response
     * @param charset Charset
     * @param cssLinks CSS links
     * @return HtmlElement instance
     */
    @NotNull
    public static HtmlElement of(@NotNull final CharSequence title, @NotNull final Appendable response, @NotNull final Charset charset, @NotNull final CharSequence... cssLinks) {
        final var config = HtmlConfig.ofDefault();
        config.setTitle(title);
        config.setCharset(charset);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with nice format and charset
     * @param title Title
     * @param response Response
     * @param charset Charset
     * @param cssLinks CSS links
     * @return HtmlElement instance
     */
    @NotNull
    public static HtmlElement niceOf(@NotNull final CharSequence title, @NotNull final Appendable response, @NotNull final Charset charset, @NotNull final CharSequence... cssLinks) {
        final var config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCharset(charset);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    // 2. HttpContext variants

    /** Create new instance
     * @param title Title
     * @param context Context
     * @param cssLinks CSS links
     * @return HtmlElement instance
     */
    @NotNull
    public static HtmlElement of(
            @NotNull final String title,
            @NotNull final HttpContext context,
            @NotNull final CharSequence... cssLinks) {
        final var config = HtmlConfig.ofDefault();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(context.writer(), config);
    }

    /** Create new instance with nice format
     * @param title Title
     * @param context Context
     * @param cssLinks CSS links
     * @return HtmlElement instance
     */
    @NotNull
    public static HtmlElement niceOf(
            @NotNull final String title,
            @NotNull final HttpContext context,
            @NotNull final CharSequence... cssLinks) {
        final var config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(context.writer(), config);
    }

    // 3. Servlet/Response variants

    /** Create new instance for servlet response
     * @param title Title
     * @param httpServletResponse Response
     * @param cssLinks CSS links
     * @return HtmlElement instance
     */
    @NotNull
    public static HtmlElement ofServlet(@NotNull final String title, @NotNull final Object httpServletResponse, @NotNull final CharSequence... cssLinks) {
        final var config = HtmlConfig.ofDefault();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(HttpContext.ofServlet(null, httpServletResponse).writer(), config);
    }

    /** Create new instance with nice format for servlet response
     * @param title Title
     * @param httpServletResponse Response
     * @param cssLinks CSS links
     * @return HtmlElement instance
     */
    @NotNull
    public static HtmlElement niceOfResponse(@NotNull final String title, @NotNull final Object httpServletResponse, @NotNull final CharSequence... cssLinks) {
        final var config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(HttpContext.ofServlet(null, httpServletResponse).writer(), config);
    }

    /** Create new instance for servlet response
     * @param httpServletResponse Response
     * @param cssLinks CSS links
     * @return HtmlElement instance
     */
    @NotNull
    public static HtmlElement ofServlet(
            @NotNull final Object httpServletResponse,
            @NotNull final CharSequence... cssLinks) {
        final var config = HtmlConfig.ofDefault();
        config.setCssLinks(cssLinks);
        return of(HttpContext.ofServlet(null, httpServletResponse).writer(), config);
    }

    /** Create new instance with nice format for servlet response
     * @param httpServletResponse Response
     * @param cssLinks CSS links
     * @return HtmlElement instance
     */
    @NotNull
    public static HtmlElement niceOfResponse(
            @NotNull final Object httpServletResponse,
            @NotNull final CharSequence... cssLinks) {
        final var config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setCssLinks(cssLinks);
        return of(HttpContext.ofServlet(null, httpServletResponse).writer(), config);
    }

}