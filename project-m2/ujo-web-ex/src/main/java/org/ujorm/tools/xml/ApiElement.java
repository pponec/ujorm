/*
 * Copyright 2018-2022 Pavel Ponec,
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An element model API.
 *
 * The XmlElement class implements the {@link Closeable} implementation
 * for an optional highlighting the tree structure in the source code.
 *
 * @since 1.86
 * @author Pavel Ponec
 */
public interface ApiElement<E extends ApiElement<?>> extends Closeable {

    /** Get an element name */
    @NotNull
    String getName();

    /** Create a new {@link ApiElement} for a required name and add it to children.
     * @param name A name of the new XmlElement is required.
     * @return The new XmlElement!
     */
    @NotNull
    E addElement(@NotNull String name);

    /**
     * Set an attribute
     * @param name Required element name
     * @param value The {@code null} value is silently ignored. Formatting is performed by the
     *   {@link org.ujorm.tools.xml.model.XmlWriter#writeValue(Object, ApiElement, String)}
     *   method, where the default implementation calls a {@code toString()} only.
     * @return The original element
     */
    @NotNull
    E setAttribute(@NotNull String name, @Nullable Object value);

    /**
     * @deprecated Call a method {@link #setAttribute(java.lang.String, java.lang.Object) } rather.
     */
    @Deprecated
    @NotNull
    default E setAttrib(@NotNull String name, @Nullable Object value) {
        return setAttribute(name, value);
    }

    /**
     * Add a text and escape special character
     * @param value The {@code null} value is allowed. Formatting is performed by the
     *   {@link org.ujorm.tools.xml.model.XmlWriter#writeValue(Object, ApiElement, String)}  }
     *   method, where the default implementation calls a {@code toString()} only.
     * @return This instance */
    @NotNull
    E addText(@Nullable Object value);

    /**
     * Message template
     *
     * @param template Message template where parameters are marked by the {@code {}} symbol
     * @param values argument values
     * @return The original builder
     */
    @NotNull
    E addTextTemplated(@Nullable final CharSequence template, @NotNull final Object... values);

    /** Add an native text with no escaped characters, for example: XML code, JavaScript, CSS styles
     * @param value The {@code null} value is ignored.
     * @return This instance */
    @NotNull
    E addRawText(@Nullable Object value);

    /**
     * Add a <strong>comment text</strong>.
     * The CDATA structure isn't really for HTML at all.
     * @param comment A comment text must not contain a string {@code -->} .
     * @return This instance
     */
    @NotNull
    E addComment(@Nullable CharSequence comment);

    /**
     * Add a <strong>character data</strong> in {@code CDATA} format to XML only.
     * The CDATA structure isn't really for HTML at all.
     * @param charData A text including the final DATA sequence. An empty argument is ignored.
     * @return This instance
     */
    @NotNull
    E addCDATA(@Nullable CharSequence charData);

    /** Close the element */
    @Override
    void close();
}