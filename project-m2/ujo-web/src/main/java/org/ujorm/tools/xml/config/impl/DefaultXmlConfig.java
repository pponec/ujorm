/*
 * Copyright 2018-2022 Pavel Ponec, https://github.com/pponec
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.xml.AbstractWriter;
import org.ujorm.tools.xml.config.Formatter;
import org.ujorm.tools.xml.config.XmlConfig;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Configuration of HtmlPage
 * @author Pavel Ponec
 */
public class DefaultXmlConfig implements XmlConfig {

    /** Default intendation per level */
    public static final String DEFAULT_INTENDATION = "\t";

    /** Default string or the new line */
    public static final String DEFAULT_NEW_LINE = "\n";

    /** Default first level of intendation */
    public static final int DEFAULT_FIRST_LEVEL = Integer.MIN_VALUE + 1;

    /** Assertion message template */
    public static final String REQUIRED_MSG = "The argument {} is required";

    /** An empty String */
    public static final String EMPTY = "";

    /** A header declaration of the document or a doctype */
    @Nullable
    protected CharSequence doctype;

    /** Charset */
    @NotNull
    private Charset charset = UTF_8;

    /** Level of the root element, the value may be negative number */
    private int firstLevel = DEFAULT_FIRST_LEVEL;

    /** An indentation space for elements of the next level,
     * where default value is an empty `String` */
    @NotNull
    private CharSequence indentation = EMPTY;

    /** A replacement text instead of the {@code null} value */
    @NotNull
    private CharSequence defaultValue = EMPTY;

    /** A new line sequence */
    @NotNull
    private CharSequence newLine = DEFAULT_NEW_LINE;

    /** Is HTTP cache allowed */
    private boolean cacheAllowed;

    /** A value formatter where a default implemnetation is:
     * <code>
     * {@code Formatter formatter -> value != null ? value.toString() : ""};
     * </code>
     */
    @NotNull
    private Formatter formatter = (value, element, attribute) -> value != null ? value.toString() : EMPTY;

    public DefaultXmlConfig() {
    }

    /** Copy attributes from other config */
    public DefaultXmlConfig(@NotNull final XmlConfig config) {
        this.doctype = config.getDoctype();
        this.charset = config.getCharset();
        this.firstLevel = config.getFirstLevel();
        this.indentation = config.getIndentation();
        this.defaultValue = config.getDefaultValue();
        this.newLine = config.getNewLine();
        this.cacheAllowed = config.isCacheAllowed();
        this.formatter = config.getFormatter();
    }

    /** A header declaration of the document or a doctype */
    @Override
    @NotNull
    public CharSequence getDoctype() {
        return nonnull(doctype, AbstractWriter.XML_HEADER);
    }

    @NotNull
    protected final <T> T nonnull(@Nullable final T value, @NotNull final T defaultValue) {
        return value != null ? value : defaultValue;
    }

    /** A header declaration of the document or a doctype */
    public DefaultXmlConfig setDoctype(@Nullable CharSequence doctype) {
        this.doctype = doctype;
        return this;
    }

    /**
     * Charset
     * @return the charset
     */
    @Override
    @NotNull
    public Charset getCharset() {
        return charset;
    }

    /**
     * Charset
     * @param charset the charset to set
     */
    public DefaultXmlConfig setCharset(@NotNull final Charset charset) {
        this.charset = Assert.notNull(charset, REQUIRED_MSG, "charset");
        return this;
    }

    /**
     * Assign parameters for a nice format of the HTML result
     */
    public final <T extends DefaultXmlConfig> T setNiceFormat() {
        setNiceFormat(DEFAULT_INTENDATION);
        return (T) this;
    }

    /**
     * Assign parameters for a nice format of the HTML result
     * @param indentation An empty String is replaced by a default intendation.
     */
    public final <T extends DefaultXmlConfig> T setNiceFormat(@Nullable final CharSequence indentation) {
        this.firstLevel = 0;
        this.indentation = Check.hasLength(indentation) ? indentation : DEFAULT_INTENDATION;
        this.newLine = DEFAULT_NEW_LINE;
        return (T) this;
    }

    /**
     * Assign parameters for a compressed format of the HTML result
     */
    public final DefaultXmlConfig setCompressedFormat() {
        this.firstLevel = DEFAULT_FIRST_LEVEL;
        this.indentation = EMPTY;
        this.newLine = EMPTY;
        return this;
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
    public DefaultXmlConfig setFirstLevel(int firstLevel) {
        this.firstLevel = firstLevel;
        return this;
    }

    /** An indentation space for elements of the next level,
     * where default value is an empty `String` */
    @NotNull
    @Override
    public CharSequence getIndentation() {
        return nonnull(indentation, EMPTY);
    }

    /** An indentation space for elements of the next level,
     * where default value is an empty `String` */
    public DefaultXmlConfig setIndentationSpace(@NotNull CharSequence indentation) {
        this.indentation = Assert.notNull(indentation, REQUIRED_MSG, "indentation");
        return this;
    }

    /** A replacement text instead of the {@code null} value */
    @Override
    public CharSequence getDefaultValue() {
        return defaultValue;
    }


    /** A default implementation is: {@code String.valueOf(value)} */
    @NotNull
    @Override
    public Formatter getFormatter() {
        return formatter;
    }

    // --- SETTERS ---

    /** A replacement text instead of the {@code null} value */
    public DefaultXmlConfig setDefaultValue(@NotNull String defaultValue) {
        this.defaultValue = Assert.notNull(defaultValue, "defaultValue");
        return this;
    }

    @Override
    public boolean isCacheAllowed() {
        return cacheAllowed;
    }

    public DefaultXmlConfig setCacheAllowed(boolean cacheAllowed) {
        this.cacheAllowed = cacheAllowed;
        return this;
    }

    /** A new line sequence */
    @Override
    public CharSequence getNewLine() {
        return newLine;
    }

    /** A new line sequence */
    public DefaultXmlConfig setNewLine(@NotNull final CharSequence newLine) {
        this.newLine = Assert.notNull(newLine, "newLine");
        return this;
    }

    /** A default value formatter is implemented by the method {@code String.valueOf(value)} */
    public DefaultXmlConfig setFormatter(@NotNull Formatter formatter) {
        this.formatter = Assert.notNull(formatter, "formatter");
        return this;
    }

}
