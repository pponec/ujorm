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
import java.io.Writer;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.Assert;
import org.ujorm.tools.xml.dom.HtmlElement;
import org.ujorm.tools.xml.dom.XmlElement;
import org.ujorm.tools.xml.dom.XmlWriter;
import static org.ujorm.tools.xml.AbstractElement.HTML_DOCTYPE;
import static org.ujorm.tools.xml.CommonXmlWriter.CHAR_NEW_LINE;

/** The root of HTML elements */
public class HtmlPage extends Element {

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
    public HtmlPage(@Nonnull final HtmlConfig config, @Nonnull final Writer writer) {
        super(new XmlElement(Html.HTML));

        this.config = config;
        this.writer = writer;
    }

    /** Returns body element */
    @Nonnull
    public <T extends Element> T getBody() {
        if (body == null) {
            body = addElement(Html.BODY);
        }
        return (T) body;
    }

    /** A shortcut for {@code HtmlList.getHead().addElement(CharSequence) }
     * @param name A name of the new Html is requred.
     * @return The new Html!
     */
    public <T extends Element> T addElementToHead(@Nonnull final String name) {
        if (head == null) {
            head = addElement(Html.HEAD);
        }
        return head.addElement(name);
    }

    /** A shortcut for {@code HtmlList.getBody().addElement(CharSequence) }
     * @param name A name of the new Html is requred.
     * @return The new Html!
     */
    public <T extends Element> T addElementToBody(@Nonnull final String name) {
        return getBody().addElement(name);
    }

    /** Create a new Javascript element and return it
     * @param javascriptLinks URL list to Javascript
     * @param defer A script that will not run until after the page has loaded
     * @return
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
    public <T extends Element> T addJavascriptLink(final boolean defer, @Nonnull final CharSequence javascriptLink) {
        Assert.notNull(javascriptLink, REQUIRED_MSG, "javascriptLink");
        return head.addElement(Html.SCRIPT)
                .setAttrib(Html.A_SRC, javascriptLink)
                .setAttrib(Html.A_TYPE, "text/javascript")
                .setAttrib("defer", defer ? "defer" : null)
                .addText("");
    }

    /** Create a new Javascript element and return it
     * @param javascript Add a javascriptLink link
     * @return New CSS element
     */
    public <T extends Element> T addJavascriptContents(@Nonnull final CharSequence javascript) {
        Assert.notNull(javascript, REQUIRED_MSG, "javascript");
        return head.addElement(Html.SCRIPT)
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
    public <T extends Element> T addCssLink(@Nonnull final CharSequence css) {
        Assert.notNull(css, REQUIRED_MSG, "css");
        return head.addElement(Html.LINK)
                .setAttrib(Html.A_HREF, css)
                .setAttrib(Html.A_REL, "stylesheet")
                .setAttrib(Html.A_TYPE, "text/css");
    }

    /** Create a new CSS element and return it
     * @param css CSS content
     * @return New CSS element
     */
    public <T extends Element> T addCssBody(@Nonnull final CharSequence css) {
        Assert.notNull(css, REQUIRED_MSG, "css");
        return head.addElement(Html.STYLE)
                .setAttrib(Html.A_TYPE, "text/css")
                .addRawText(css);
    }

    /** Render the HTML code including header */
    @Override @Nonnull
    public String toString() throws IllegalStateException {
        return super.toString();
    }

    @Override
    public void close() throws IllegalStateException {
        super.close();
        if (origElement instanceof XmlElement) {
            try {
                final String intendationSpace = config.isNiceFormat() ? config.getIndentationSpace() : "";
                final XmlWriter xmlWriter = new XmlWriter(writer.append(HTML_DOCTYPE).append(CHAR_NEW_LINE), intendationSpace);
                final XmlElement xmlElement = (XmlElement) origElement;
                xmlElement.toWriter(config.getFirstLevel(), xmlWriter);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    // ------- Static methods ----------

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions */
    @Nonnull
    public static HtmlPage of(@Nonnull final HttpServletResponse response) {
        return of(response, new HtmlConfig());
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions */
    @Nonnull
    public static HtmlPage of(@Nonnull final HttpServletResponse response, @Nonnull final CharSequence title, @Nonnull final CharSequence... cssLinks) {
        final HtmlConfig config = new HtmlConfig();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        return of(response, config);
    }

    /** Create new instance with empty html headers
     * @throws IllegalStateException IO exceptions */
    @Nonnull
    public static HtmlPage niceOf(@Nonnull final HttpServletResponse response, @Nonnull final CharSequence title, @Nonnull final CharSequence... cssLinks) {
        final HtmlConfig config = new HtmlConfig();
        config.setTitle(title);
        config.setCssLinks(cssLinks);
        config.setNiceFormat(true);
        return of(response, config);
    }

    /** Create new instance with empty html headers
     * @param response HttpREsponse
     * @param config Html configuration
     * @return An instance of the HtmlPage
     * @throws IllegalStateException IO exceptions
     */
    @Nonnull
    public static HtmlPage of(@Nonnull final HttpServletResponse response, @Nonnull final HtmlConfig config) throws IllegalStateException {
        response.setCharacterEncoding(config.getCharset().toString());
        try {
            final HtmlPage result = new HtmlPage(config, response.getWriter());
            config.getLanguage().ifPresent(lang -> result.setAttrib(A_LANG, lang));
            result.addElementToHead(Html.META).setAttrib(HtmlElement.Html.A_CHARSET, config.getCharset());
            config.getTitle().ifPresent(title -> result.addElementToHead(Html.TITLE).addText(title));
            result.addCssLinks(config.getCssLinks());

            return result;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


}
