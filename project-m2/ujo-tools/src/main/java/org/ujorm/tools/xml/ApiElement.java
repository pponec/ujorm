/*
 * Copyright 2018-2020 Pavel Ponec,
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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An element model API.
 * 
 * The XmlElement class implements the {@link Closeable} implementation
 * for an optional highlighting the tree structure in the source code.
*
 * @see HtmlElement
 * @since 1.86
 * @author Pavel Ponec
 */
public interface ApiElement<E extends ApiElement<?>> extends Closeable {

    /** Get an element name */
    @Nonnull
    String getName();

    /** Create a new {@link XmlElement} for a required name and add it to children.
     * @param name A name of the new XmlElement is required.
     * @return The new XmlElement!
     */
    @Nonnull
    E addElement(@Nonnull final String name);

    /**
     * Set an attribute
     * @param name Required element name
     * @param value The {@code null} value is silently ignored. Formatting is performed by the
     *   {@link XmlWriter#writeValue(java.lang.Object, org.ujorm.tools.dom.XmlElement, java.lang.String, java.io.Writer) }
     *   method, where the default implementation calls a {@code toString()} only.
     * @return The original element
     */
    @Nonnull
    E setAttrib(@Nonnull final String name, @Nullable final Object value);

    /**
     * Add a text and escape special character
     * @param value The {@code null} value is allowed. Formatting is performed by the
     *   {@link XmlWriter#writeValue(java.lang.Object, org.ujorm.tools.dom.XmlElement, java.lang.String, java.io.Writer) }
     *   method, where the default implementation calls a {@code toString()} only.
     * @return This instance */
    @Nonnull
    E addText(@Nullable final Object value);

    /** Add an native text with no escaped characters, for example: XML code, JavaScript, CSS styles
     * @param value The {@code null} value is ignored.
     * @return This instance */
    @Nonnull
    E addRawText(@Nullable final Object value);
    /**
     * Add a <strong>comment text</strong>.
     * The CDATA structure isn't really for HTML at all.
     * @param comment A comment text must not contain a string {@code -->} .
     * @return This instance
     */
    @Nonnull
    E addComment(@Nullable final CharSequence comment);

    /**
     * Add a <strong>character data</strong> in {@code CDATA} format to XML only.
     * The CDATA structure isn't really for HTML at all.
     * @param charData A text including the final DATA sequence. An empty argument is ignored.
     * @return This instance
     */
    @Nonnull
    E addCDATA(@Nullable final CharSequence charData);

}