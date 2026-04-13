/*
 * Copyright 2018-2026 Pavel Ponec, https://github.com/pponec
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
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.xml.AbstractWriter;
import org.ujorm.tools.xml.builder.XmlBuilder;
import org.ujorm.tools.xml.config.ApiInjector;
import org.ujorm.tools.xml.config.HtmlConfig;

/**
 * Configuration of HtmlPage
 * @author Pavel Ponec
 */
public class DefaultHtmlConfig extends DefaultXmlConfig implements HtmlConfig {

    /** Title */
    @NotNull
    private CharSequence title = "Demo";

    /** Css links with a required order */
    @NotNull
    private CharSequence[] cssLinks = new CharSequence[0];

    /** Language of the HTML page */
    @Nullable
    private CharSequence language = "en";

    /** Application content type */
    @NotNull
    private String contentType = "text/html";

    /** A request to generate a minimal HTML header */
    private boolean htmlHeaderRequest = true;

    /** Raw text to insert to each HTML header */
    @Deprecated
    @Nullable
    private CharSequence rawHeaderText = null;

    /** Header injector */
    @NotNull
    private ApiInjector headerInjector = e -> {};

    /** A name of root element */
    @NotNull
    private String rootElementName = XmlBuilder.HTML;

    /** Unpair HTML element names (Replaced Double Brace Initialization with efficient Set.of) */
    @NotNull
    private Set<String> unpairElements = Set.of(
            "area",
            "base",
            Html.BREAK,
            "col",
            "embed",
            Html.HR,
            Html.IMAGE,
            Html.INPUT,
            "keygen",
            Html.LINK,
            Html.META,
            "param",
            // Html.SCRIPT // The script is unpair element commonly
            "source",
            Html.STYLE,
            "track"
    );

    public DefaultHtmlConfig() {
    }

    public DefaultHtmlConfig(@NotNull final HtmlConfig htmlConfig) {
        super(htmlConfig);
        this.title = htmlConfig.getTitle();
        this.cssLinks = htmlConfig.getCssLinks();
        this.language = htmlConfig.getLanguage().orElse(null);
        this.contentType = htmlConfig.getContentType();
        this.rawHeaderText = htmlConfig.getRawHeaderText();
        this.headerInjector = htmlConfig.getHeaderInjector();
        this.rootElementName = htmlConfig.getRootElementName();
        this.htmlHeaderRequest = htmlConfig.isHtmlHeaderRequest();
        this.unpairElements = Set.copyOf(htmlConfig.getUnpairElements());
    }

    @Override
    @NotNull
    public CharSequence getDoctype() {
        return nonnull(doctype, AbstractWriter.HTML_DOCTYPE);
    }

    @NotNull
    @Override
    public CharSequence getTitle() {
        return title;
    }

    @Override
    @NotNull
    public CharSequence[] getCssLinks() {
        return cssLinks;
    }

    @Override
    @NotNull
    public Optional<CharSequence> getLanguage() {
        return Optional.ofNullable(language);
    }

    @Override
    @NotNull
    public String getContentType() {
        return contentType;
    }

    /** A request to generate a minimal HTML header */
    @Override
    public boolean isHtmlHeaderRequest() {
        return htmlHeaderRequest;
    }

    /** A raw text for HTML header */
    @Override
    @Nullable
    public CharSequence getRawHeaderText() {
        return rawHeaderText;
    }

    /** Return a header injector */
    @Override
    @NotNull
    public ApiInjector getHeaderInjector() {
        return headerInjector;
    }

    /** A name of root element */
    @Override
    @NotNull
    public String getRootElementName() {
        return rootElementName;
    }

    @Override
    @NotNull
    public Set<String> getUnpairElements() {
        return unpairElements;
    }

    // --- SETTERS ---

    /** Title is a required element by HTML 5 */
    public DefaultHtmlConfig setTitle(@NotNull final CharSequence title) {
        this.title = Assert.required(title, "title");
        return this;
    }

    public DefaultHtmlConfig setCssLinks(@NotNull final CharSequence... cssLinks) {
        this.cssLinks = Assert.required(cssLinks, REQUIRED_MSG, "cssLinks");
        return this;
    }

    public DefaultHtmlConfig setLanguage(@NotNull final CharSequence language) {
        this.language = language;
        return this;
    }

    public DefaultHtmlConfig setContentType(@NotNull final String contentType) {
        this.contentType = Assert.required(contentType, REQUIRED_MSG, "contentType");
        return this;
    }

    /** A request to generate a minimal HTML header */
    public DefaultHtmlConfig setHtmlHeader(final boolean htmlHeaderRequest) {
        this.htmlHeaderRequest = htmlHeaderRequest;
        return this;
    }

    /** The element name must not be special HTML characters.
     * The {@code null} value is intended to build a root of AJAX queries.
     */
    public DefaultHtmlConfig setRootElementName(@Nullable final String rootElementName) {
        this.rootElementName = rootElementName != null
                ? rootElementName
                : XmlBuilder.HIDDEN_NAME;
        return this;
    }

    /** Set Unpair element names */
    public DefaultHtmlConfig setUnpairElements(@NotNull final Set<String> unpairElements) {
        this.unpairElements = Assert.required(unpairElements, REQUIRED_MSG, "unpairElements");
        return this;
    }

    /**
     * Use the {@link #setHeaderInjector(org.ujorm.tools.xml.config.ApiInjector) } method rather.
     * @param rawHeaderText Header text
     * @return This config
     * @deprecated
     */
    @Deprecated
    public DefaultHtmlConfig setRawHeaderText(@NotNull final String rawHeaderText) {
        this.rawHeaderText = Assert.required(rawHeaderText, REQUIRED_MSG, "rawHeaderText");
        return this;
    }

    /** Assign a new header injector */
    public DefaultHtmlConfig setHeaderInjector(@NotNull final ApiInjector headerInjector) {
        this.headerInjector = Assert.required(headerInjector, REQUIRED_MSG, "headerInjector");
        return this;
    }
}