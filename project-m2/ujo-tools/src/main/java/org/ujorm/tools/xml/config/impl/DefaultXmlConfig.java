/*
 * Copyright 2018-2020 Pavel Ponec, https://github.com/pponec
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

import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.xml.AbstractWriter;
import static java.nio.charset.StandardCharsets.UTF_8;
import org.ujorm.tools.Check;
import org.ujorm.tools.xml.config.XmlConfig;

/**
 * Configuraion of HtmlPage
 * @author Pavel Ponec
 */
public class DefaultXmlConfig implements XmlConfig {

    /** Default intendation have got 4 spaces per level */
    public static final String DEFAULT_INTENDATION = "    ";

    /** Assertion message template */
    public static final String REQUIRED_MSG = "The argument {} is required";

    public static final int DEFAULT_FIRST_LEVEL = Integer.MIN_VALUE + 1;

    /** Use a DOM model */
    private boolean dom = false;

    /** A header declaration of the document or a doctype */
    @Nullable
    protected String doctype;

    /** Charset */
    @Nonnull
    private Charset charset = UTF_8;

    /** Level of the root element, the value may be negative number */
    private int firstLevel = DEFAULT_FIRST_LEVEL;

    /** An indentation space for elements of the next level,
     * where default value is an empty `String` */
    @Nonnull
    private String indentation = "";

    /** A replacement text instead of the {@code null} value */
    @Nonnull
    private String defaultValue = "";

    /** Is HTTP cache allowed */
    private boolean cacheAllowed;

    /** A header declaration of the document or a doctype */
    @Override
    @Nonnull
    public String getDoctype() {
        return nonnull(doctype, AbstractWriter.XML_HEADER);
    }

    @Nonnull
    protected final <T> T nonnull(@Nullable final T value, @Nonnull final T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /** A header declaration of the document or a doctype */
    public void setDoctype(@Nullable String doctype) {
        this.doctype = doctype;
    }

    /**
     * Charset
     * @return the charset
     */
    @Override
    @Nonnull
    public Charset getCharset() {
        return charset;
    }

    /**
     * Charset
     * @param charset the charset to set
     */
    public void setCharset(@Nonnull final Charset charset) {
        this.charset = Assert.notNull(charset, REQUIRED_MSG, "charset");
    }

    /**
     * Nice format or the HTML result
     * @param niceFormat the niceFormat to set
     */
    public final void setNiceFormat(boolean niceFormat) {
        this.firstLevel = niceFormat ? 0 : DEFAULT_FIRST_LEVEL;
        if (niceFormat && Check.isEmpty(indentation)) {
            indentation = DEFAULT_INTENDATION;
        }
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

    /** An indentation space for elements of the next level,
     * where default value is an empty `String` */
    @Nonnull
    @Override
    public String getIndentation() {
        return nonnull(indentation, "");
    }

    /** An indentation space for elements of the next level,
     * where default value is an empty `String` */
    public void setIndentationSpace(@Nonnull String indentation) {
        this.indentation = Assert.notNull(indentation, REQUIRED_MSG, "indentation");
    }

    /** A replacement text instead of the {@code null} value */
    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    /** A replacement text instead of the {@code null} value */
    public void setDefaultValue(@Nonnull String defaultValue) {
        this.defaultValue = Assert.notNull(defaultValue, "defaultValue");
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

    @Override
    public boolean isCacheAllowed() {
        return cacheAllowed;
    }

    public void setCacheAllowed(boolean cacheAllowed) {
        this.cacheAllowed = cacheAllowed;
    }

}
