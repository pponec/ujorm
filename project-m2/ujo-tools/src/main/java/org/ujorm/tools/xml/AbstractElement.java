/*
 * Copyright 2018-2018 Pavel Ponec,
 * https://github.com/pponec/ujorm/blob/master/project-m2/ujo-tools/src/main/java/org/ujorm/tools/XmlElement.java
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

package org.ujorm.tools.xml;

import java.io.Closeable;
import java.io.IOException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;

/**
 * Abstrac element model.
 * The main benefits are:
 * @see HtmlElement
 * @since 1.86
 * @author Pavel Ponec
 */
public abstract class AbstractElement<E extends AbstractElement> implements Closeable {

    /** XML header */
    public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    /** HTML doctype */
    public static final String HTML_DOCTYPE = "<!DOCTYPE html>";

    /** Default intendation have got 4 spaces per level */
    public static final String DEFAULT_INTENDATION = "    ";

    /** Assertion message template */
    protected static final String REQUIRED_MSG = "The argument {} is required";

    /** Element name */
    @Nonnull
    protected final String name;

    /** Constructor */
    public AbstractElement(@Nonnull final CharSequence name) {
        Assert.notNull(name, REQUIRED_MSG, "name");
        this.name = name.toString();
    }

    /** Get an element name */
    @Nonnull
    public final String getName() {
        return name;
    }

    /** Create a new {@link XmlElement} for a required name and add it to children.
     * @param name A name of the new XmlElement is required.
     * @return The new XmlElement!
     */
    @Nonnull
    public abstract <T extends E> T addElement(@Nonnull final String name) throws IOException;

    /**
     * Set one attribute
     * @param name Required element name
     * @param value The {@code null} value is silently ignored. Formatting is performed by the
     *   {@link XmlWriter#writeValue(java.lang.Object, org.ujorm.tools.dom.XmlElement, java.lang.String, java.io.Writer) }
     *   method, where the default implementation calls a {@code toString()} only.
     * @return The original element
     */
    @Nonnull
    public abstract <T extends E> T setAttrib(@Nonnull final String name, @Nullable final Object value) throws IOException;

    /**
     * Add a text and escape special character
     * @param value The {@code null} value is allowed. Formatting is performed by the
     *   {@link XmlWriter#writeValue(java.lang.Object, org.ujorm.tools.dom.XmlElement, java.lang.String, java.io.Writer) }
     *   method, where the default implementation calls a {@code toString()} only.
     * @return This instance */
    @Nonnull
    public abstract <T extends E> T addText(@Nullable final Object value) throws IOException;

    /**
     * Add a text including a space (before and after the text)
     * @param value Anu data
     * @return This instance */
    @Nonnull
    public abstract <T extends E> T addTextWithSpace(@Nullable final Object value) throws IOException;

    /** Add an native text with no escaped characters, for example: XML code, JavaScript, CSS styles
     * @param value The {@code null} value is ignored.
     * @return This instance */
    @Nonnull
    public abstract <T extends E> T addRawText(@Nullable final Object value) throws IOException;
    /**
     * Add a <strong>comment text</strong>.
     * The CDATA structure isn't really for HTML at all.
     * @param comment A comment text must not contain a string {@code -->} .
     * @return This instance
     */
    @Nonnull
    public abstract <T extends E> T addComment(@Nullable final CharSequence comment) throws IOException;

    /**
     * Add a <strong>character data</strong> in {@code CDATA} format to XML only.
     * The CDATA structure isn't really for HTML at all.
     * @param charData A text including the final DATA sequence. An empty argument is ignored.
     * @return This instance
     */
    @Nonnull
    public abstract <T extends E> T addCDATA(@Nullable final CharSequence charData) throws IOException;

}