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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.Assert;
import org.ujorm.tools.xml.ApiElement;
import org.ujorm.tools.xml.builder.XmlBuilder;
import org.ujorm.tools.xml.builder.XmlPrinter;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;
import org.ujorm.tools.xml.config.HtmlConfig;
import org.ujorm.tools.xml.dom.XmlElement;
import org.ujorm.tools.xml.dom.XmlWriter;
import static org.ujorm.tools.xml.config.impl.DefaultXmlConfig.REQUIRED_MSG;

/** The root of HTML elements */
public class HtmlElement extends Element {

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
    private final Writer writer;

    /** Create new instance with empty html headers */
    public HtmlElement(@Nonnull final HtmlConfig config, @Nonnull final Writer writer) {
        this(new XmlElement(Html.HTML), config, writer);
    }

    /** Create new instance with empty html headers */
    public HtmlElement(@Nonnull final ApiElement root, @Nonnull final HtmlConfig config, @Nonnull final Writer writer) {
        super(root);
        this.config = config;
        this.writer = writer;
    }

    /** Returns a head element */
    public Element getHead() {
        if (head == null) {
            head = addElement(Html.HEAD);
        }
        return head;
    }

    /** Returns a body element */
    @Nonnull
    public Element getBody() {
        if (body == null) {
            body = addElement(Html.BODY);
        }
        return body;
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
                .setAttrib(Html.A_SRC, javascriptLink)
                .setAttrib("defer", defer ? "defer" : null)
                .addText("");
    }

    /** Create a new Javascript element and return it
     * @param javascript Add a javascriptLink link
     * @return New CSS element
     */
    public Element addJavascriptContents(@Nonnull final CharSequence javascript) {
        Assert.notNull(javascript, REQUIRED_MSG, "javascript");
        return getHead().addElement(Html.SCRIPT)
                .setAttrib(Html.A_LANGUAGE, "javascript")
                .setAttrib(Html.A_TYPE, "text/javascript")
                .addText(javascript);
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
                .setAttrib(Html.A_HREF, css)
                .setAttrib(Html.A_REL, "stylesheet");
    }

    /** Create a new CSS element and return it
     * @param css CSS content
     * @return New CSS element
     */
    public Element addCssBody(@Nonnull final CharSequence css) {
        Assert.notNull(css, REQUIRED_MSG, "css");
        return getHead().addElement(Html.STYLE)
                .addRawText(css);
    }

    /** Returns an Render the HTML code including header. Call the close() method before view */
    @Override @Nonnull
    public String toString() throws IllegalStateException {
        return writer.toString();
    }

    @Override
    public void close() throws IllegalStateException {
        super.close();
        if (origElement instanceof XmlElement) {
            try {
                final XmlWriter xmlWriter = new XmlWriter(writer
                        .append(config.getDoctype())
                        .append(config.getNewLine())
                        , config.getIndentation());
                final XmlElement xmlElement = (XmlElement) origElement;
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
     * @throws IllegalStateException IO exceptions */
    @Nonnull
    public static HtmlElement of(@Nonnull final HttpServletResponse response, @Nonnull final CharSequence... cssLinks) {
        return of(response, HtmlConfig.ofDefault());
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions */
    @Nonnull
    public static HtmlElement of(@Nonnull final CharSequence title, @Nonnull final HttpServletResponse response, @Nonnull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions */
    @Nonnull
    public static HtmlElement of(@Nonnull final CharSequence title, @Nonnull final HttpServletResponse response, @Nonnull final Charset charset, @Nonnull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions */
    @Nonnull
    public static HtmlElement niceOf(@Nonnull final CharSequence title, @Nonnull final HttpServletResponse response, @Nonnull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions */
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
     * @throws IllegalStateException IO exceptions */
    @Nonnull
    public static HtmlElement niceOf(@Nonnull final HttpServletResponse response, @Nonnull final CharSequence... cssLinks) {
        final DefaultHtmlConfig config = HtmlConfig.ofDefault();
        config.setNiceFormat();
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** A base method to create new instance with empty html headers
     * @param response HttpREsponse
     * @param config Html configuration
     * @return An instance of the HtmlPage
     * @throws IllegalStateException IO exceptions
     */
    @Nonnull
    public static HtmlElement of(@Nonnull final HttpServletResponse response, @Nonnull final HtmlConfig config) throws IllegalStateException {
        response.setCharacterEncoding(config.getCharset().toString());
        try {
            final ApiElement root = config.isDocumentObjectModel()
                    ? new XmlElement(Html.HTML)
                    : new XmlBuilder(Html.HTML, new XmlPrinter(response.getWriter(), config));
            final HtmlElement result = new HtmlElement(root, config, response.getWriter());
            config.getLanguage().ifPresent(lang -> result.setAttrib(A_LANG, lang));
            result.getHead().addElement(Html.META).setAttrib(A_CHARSET, config.getCharset());
            result.getHead().addElement(Html.TITLE).addText(config.getTitle());
            result.addCssLinks(config.getCssLinks());

            return result;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Create new instance with empty html headers for
     * @param config Html configuration
     * @return An instance of the HtmlPage
     * @throws IllegalStateException IO exceptions
     */
    @Nonnull
    public static HtmlElement of(@Nullable HtmlConfig config) throws IllegalStateException {
        if (config == null) {
            config = new DefaultHtmlConfig();
        }
        final CharArrayWriter writer = new CharArrayWriter(256);
        final ApiElement root = config.isDocumentObjectModel()
                ? new XmlElement(Html.HTML)
                : new XmlBuilder(Html.HTML, new XmlPrinter(writer, config));
        final HtmlElement result = new HtmlElement(root, config, writer);
        config.getLanguage().ifPresent(lang -> result.setAttrib(A_LANG, lang));
        result.getHead().addElement(Html.META).setAttrib(A_CHARSET, config.getCharset());
        result.getHead().addElement(Html.TITLE).addText(config.getTitle());
        result.addCssLinks(config.getCssLinks());
        return result;
    }
}
