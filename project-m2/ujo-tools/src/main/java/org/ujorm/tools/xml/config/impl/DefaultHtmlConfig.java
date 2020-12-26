/*
 * Copyright 2018-2019 Pavel Ponec, https://github.com/pponec
 * https://github.com/pponec/ujorm/blob/master/samples/servlet/src/main/java/org/ujorm/ujoservlet/tools/Html.java
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
package org.ujorm.tools.xml.config.impl;

import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.xml.AbstractWriter;
import org.ujorm.tools.xml.builder.XmlBuilder;
import org.ujorm.tools.xml.config.HtmlConfig;

/**
 * Configuraion of HtmlPage
 * @author Pavel Ponec
 */
public class DefaultHtmlConfig extends DefaultXmlConfig implements HtmlConfig {

    /** Title */
    @Nonnull
    private CharSequence title = "Demo";

    /** Css links with a required order */
    @Nonnull
    private CharSequence[] cssLinks = new CharSequence[0];

    /** Language of the HTML page */
    @Nonnull
    private CharSequence language = "en";

    /** Application content type */
    @Nonnull
    private String contentType = "text/html";

    /** Build a real model or a plain writer */
    private boolean buildDom = false;

    /** A request to generate a minimal HTML header */
    private boolean htmlHeaderRequest = true;

    /** Raw text to insert to each HTML header */
    private CharSequence rawHeaderText = null;

    /** A name of root element */
    private CharSequence rootElementName = XmlBuilder.HTML;

    public DefaultHtmlConfig() {
    }

    public DefaultHtmlConfig(@Nonnull final HtmlConfig htmlConfig) {
        super(htmlConfig);
        this.title = htmlConfig.getTitle();
        this.cssLinks = htmlConfig.getCssLinks();
        this.language = htmlConfig.getLanguage().orElse(null);
        this.contentType = htmlConfig.getContentType();
        this.buildDom = htmlConfig.isDocumentObjectModel();
        this.htmlHeaderRequest = htmlConfig.isDocumentObjectModel();
        this.rawHeaderText = htmlConfig.getRawHeaderText();
        this.rootElementName = htmlConfig.getRootElementName();
    }

    @Override
    @Nonnull
    public CharSequence getDoctype() {
        return nonnull(doctype, AbstractWriter.HTML_DOCTYPE);
    }

    @Nonnull
    @Override
    public CharSequence getTitle() {
        return title;
    }

    @Override
    public CharSequence[] getCssLinks() {
        return cssLinks;
    }

    @Override
    @Nonnull
    public Optional<CharSequence> getLanguage() {
        return Optional.ofNullable(language);
    }

    @Override
    @Nonnull
    public String getContentType() {
        return contentType;
    }

    /** Build a real model or a plain writer, the default value is {@code false} */
    @Override
    public boolean isDocumentObjectModel() {
        return buildDom;
    }

    /** A request to generate a minimal HTML header */
    @Override
    public boolean isHtmlHeaderRequest() {
        return htmlHeaderRequest;
    }

    /** A raw text for HTML header */
    @Override
    public CharSequence getRawHeaderText() {
        return rawHeaderText;
    }

    /** A name of root element */
    @Override
    public CharSequence getRootElementName() {
        return rootElementName;
    }

    // --- SETTERS ---

    /** Title is a required element by HTML 5 */
    public void setTitle(@Nonnull CharSequence title) {
        this.title = Assert.hasLength(title, "title");
    }

    public void setCssLinks(@Nonnull CharSequence... cssLinks) {
        this.cssLinks = Assert.notNull(cssLinks, REQUIRED_MSG, "cssLinks");
    }

    public void setLanguage(@Nonnull CharSequence language) {
        this.language = language;
    }

    public void setContentType(@Nonnull String contentType) {
        this.contentType = Assert.notNull(contentType, REQUIRED_MSG, "contentType");
    }

    /** Build a real model or a plain writer, the default value is {@code false}.
     * @deprecated Use the method {@link #setDocumentObjectModel(boolean) }.
     */
    @Deprecated
    public void setDom(final boolean buildDom) {
        setDocumentObjectModel(buildDom);
    }

    /** Build a real model or a plain writer, the default value is {@code false} */
    public void setDocumentObjectModel(final boolean buildDom) {
        this.buildDom = buildDom;
    }

    /** A request to generate a minimal HTML header */
    public void setHtmlHeader(boolean htmlHeaderRequest) {
        this.htmlHeaderRequest = htmlHeaderRequest;
    }

    /**The element name must not be special HTML characters.
     * The {@code null} value is intended to build a root of AJAX queries.
     */
    public void setRootElementName(@Nullable String rootElementName) {
        this.rootElementName = rootElementName;
    }

    public void setRawHedaderCode(@Nullable String rawHeaderText) {
        this.rawHeaderText = Assert.notNull(rawHeaderText, REQUIRED_MSG, "rawHeaderText");
    }
}
