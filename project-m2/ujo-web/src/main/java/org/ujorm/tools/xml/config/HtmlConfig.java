/*
 * Copyright 2018-2012 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.xml.config;

import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.xml.ApiElement;
import org.ujorm.tools.xml.config.impl.DefaultHtmlConfig;
import static org.ujorm.tools.xml.config.impl.DefaultXmlConfig.*;

/**
 * Configuraion of HtmlPage
 * @author Pavel Ponec
 */
public interface HtmlConfig extends XmlConfig {

    /** Title is a required element by HTML 5 */
    @NotNull
    CharSequence getTitle();

    /** CSS links of a HTML page */
    @NotNull
    CharSequence[] getCssLinks();

    /** Language of a HTML page */
    @NotNull
    Optional<CharSequence> getLanguage();

    /** Get a content type where a recommended value is {@code "text/html"} */
    @NotNull
    String getContentType();

    /** Build a real model or a plain writer with a recommended value {@code false} */
    boolean isDocumentObjectModel();

    /** A request to generate a minimal HTML header */
    boolean isHtmlHeaderRequest();

    /** A raw text for HTML header
     * @deprecated Use method {@link #getHeaderInjector() }
     */
    @Deprecated
    @Nullable
    CharSequence getRawHeaderText();

    @NotNull
    ApiInjector getHeaderInjector();

    /** A name of root element */
    @NotNull
    String getRootElementName();

    @NotNull
    Set<String> getUnpairElements();

    @Override
    default boolean pairElement(@NotNull final ApiElement element) {
        return !getUnpairElements().contains(element.getName());
    }

    /** Clone the config for an AJAX processing */
    default DefaultHtmlConfig cloneForAjax() {
        final DefaultHtmlConfig result = new DefaultHtmlConfig(this);
        result.setRootElementName(null);
        result.setNiceFormat();
        result.setDoctype(EMPTY);
        result.setHtmlHeader(false);
        result.setCacheAllowed(false);
        return result;
    }

    /**
     * Create a new default config
     */
    @NotNull
    static DefaultHtmlConfig ofDefault() {
        return new DefaultHtmlConfig();
    }

    /**
     * No HTML header is generated, no Doctype and no new lines
     *
     * @param rootElementName Element name cannot contain special HTML characters. An undefined value ignores the creation of the root element.
     * @return
     */
    @NotNull
    static DefaultHtmlConfig ofElementName(@Nullable String rootElementName) {
        return ofElement(rootElementName, true);
    }

    /**
     * No HTML header is generated, no Doctype and no new lines
     *
     * @param rootElementName Element name cannot contain special HTML characters.
     * @param enabled Disabled root element ignores the creation of the root element.
     * @return
     */
    @NotNull
    static DefaultHtmlConfig ofElement(@Nullable String rootElementName, boolean enabled) {
        final DefaultHtmlConfig result = ofDefault();
        result.setRootElementName(enabled ? rootElementName : null);
        result.setHtmlHeader(false);
        result.setDoctype(EMPTY);
        return result;
    }

    /**
     * Create a configuration for an AJAX response.
     */
    @NotNull
    static DefaultHtmlConfig ofEmptyElement() {
        final DefaultHtmlConfig result = ofElement(EMPTY, false);
        result.setHtmlHeader(false);
        result.setDoctype(EMPTY);
        result.setNewLine(EMPTY);
        return result;
    }

    /** Clone config form another */
    static DefaultHtmlConfig of(@NotNull final HtmlConfig htmlConfig) {
        return new DefaultHtmlConfig(htmlConfig);
    }

    /**
     * Create a new configuration with a nice format by an HTML title.
     * @param title If the title is null then create an EMPTY element.
     */
    static DefaultHtmlConfig ofTitle(@NotNull String title) {
        return ofDefault()
                    .setTitle(title)
                    .setNiceFormat();
    }
}
