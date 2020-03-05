/*
 * Copyright 2018-2018 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.web;

import java.nio.charset.Charset;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.ujorm.tools.Assert;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Configuraion of HtmlPage
 * @author Pavel Ponec
 */
public class DefaultConfig implements HtmlConfig {

    /** Charset */
    @Nonnull
    private Charset charset =  UTF_8;

    /** Title */
    @Nonnull
    private String doctype;

    /** Title */
    @Nonnull
    private CharSequence title = "Demo";

    /** Level of the root element, the value may be negative number */
    private int firstLevel;

    /** An indentation space for elements of the next level */
    private String indentationSpace = "    ";

    /** Use a DOM model */
    private boolean dom = false;

    /** Css ling witn required order */
    @Nonnull
    private CharSequence[] cssLinks = new CharSequence[0];

    /** Language of the HTML page */
    private CharSequence language = "en";

    public DefaultConfig() {
        setNiceFormat(false);
    }

    public String getDoctype() {
        return doctype;
    }

    /** Html title */
    @Nonnull
    @Override
    public CharSequence getTitle() {
        return title;
    }

    /** Html title */
    public void setTitle(@Nonnull final CharSequence title) {
        Assert.hasLength(title);
        this.title = title;
    }

    /** Get language of the HTML page */
    @Override
    public Optional<CharSequence> getLanguage() {
        return Optional.ofNullable(language);
    }

    /** Set language of the HTML page */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Charset
     * @return the charset
     */
    @Nonnull
    @Override
    public Charset getCharset() {
        return charset;
    }

    /**
     * Charset
     * @param charset the charset to set
     */
    public void setCharset(@Nonnull Charset charset) {
        this.charset = Assert.notNull(charset, "charset");
    }

    /**
     * Nice format or the HTML result
     * @param niceFormat the niceFormat to set
     */
    public final void setNiceFormat(boolean niceFormat) {
        this.firstLevel = niceFormat
                ? 0
                : (Integer.MIN_VALUE + 1);
    }

    /**
     * Level of the root element, the value may be negative.
     * @return the firstLevel
     */
    @Override
    public int getFirstLevel() {
        return firstLevel;
    }

    /**
     * Level of the root element, the value may be negative.
     * @param firstLevel the firstLevel to set
     */
    public void setFirstLevel(int firstLevel) {
        this.firstLevel = firstLevel;
    }

    /**
     * New line
     * @return the newLine
     */
    @Override
    public String getIndentationSpace() {
        return indentationSpace;
    }

    /**
     * New line
     * @param indentationSpace the newLine to set
     */
    public void setIndentationSpace(String indentationSpace) {
        this.indentationSpace = indentationSpace;
    }

    /**
     * Use a DOM model
     * @return the dom
     */
    @Override
    public boolean isDom() {
        return dom;
    }

    /**
     * Use a DOM model
     * @param dom the dom to set
     */
    public void setDom(boolean dom) {
        this.dom = dom;
    }

    /**
     * Css ling witn required order
     * @return the cssLinks
     */
    @Nonnull
    @Override
    public CharSequence[] getCssLinks() {
        return cssLinks;
    }

    /**
     * Css ling witn required order
     * @param cssLinks The cssLinks to set
     */
    public void setCssLinks(@Nonnull final CharSequence... cssLinks) {
        this.cssLinks = Assert.notNull(cssLinks, "cssLinks");
    }

}
