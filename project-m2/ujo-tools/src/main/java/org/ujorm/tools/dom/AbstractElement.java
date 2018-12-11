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

package org.ujorm.tools.dom;

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

    /** Assertion message template */
    protected static final String REQUIRED_MSG = "The argument {} is required";

    /** Element name */
    @Nonnull
    protected final String name;

    /** Constructor */
    public AbstractElement(String name) {
        Assert.notNull(name, REQUIRED_MSG, "name");
        this.name = name;
    }

    /** Get an element name */
    @Nonnull
    public final String getName() {
        return name;
    }

    /** An empty method */
    @Override
    public void close() throws IOException {
    }

    /**
     * Add a child element
     * @param element Add a child element is required. An undefined argument is ignored.
     * @return The argument type of XmlElement! */
    @Nonnull
    public abstract <T extends E> T addElement(@Nonnull final T element) throws IOException;

    /** Create a new {@link XmlElement} for a required name and add it to children.
     * @param name A name of the new XmlElement is required.
     * @return The new XmlElement!
     */
    @Nonnull
    public abstract <T extends E> T addElement(@Nonnull final String name) throws IOException;

    /** Create a new {@link XmlElement} for a required name and add it to children with many attributes.
     * @param elementName  A name of the new XmlElement is required.
     * @param attributeName An attribute key
     * @param attributeData An attribute value
     * @param attributes Pairs of attribute - value. An attribute with no value is ignored silently.
     * @return The new XmlElement!
     */
    @Nonnull
    public final <T extends E> T addElement
        ( @Nonnull final String elementName
        , @Nonnull final String attributeName
        , @Nullable final Object attributeData
        , @Nonnull final Object... attributes
        ) throws IOException
        {
        final T result = addElement(elementName);
        result.setAttrib(attributeName, attributeData);
        for (int i = 1, max = attributes.length; i < max; i += 2) {
             result.setAttrib((String) attributes[i-1], attributes[i]);
        }
        return result;
    }

    /**
     * Set one attribute
     * @param name Required element name
     * @param data The {@code null} value is ignored. Formatting is performed by the
     *   {@link XmlWriter#writeValue(java.lang.Object, org.ujorm.tools.dom.XmlElement, java.lang.String, java.io.Writer) }
     *   method, where the default implementation calls a {@code toString()} only.
     * @return The original element
     */
    @Nonnull
    public abstract <T extends E> T setAttrib(@Nonnull final String name, @Nullable final Object data) throws IOException;

    /**
     * Add a text and escape special character
     * @param data The {@code null} value is allowed. Formatting is performed by the
     *   {@link XmlWriter#writeValue(java.lang.Object, org.ujorm.tools.dom.XmlElement, java.lang.String, java.io.Writer) }
     *   method, where the default implementation calls a {@code toString()} only.
     * @return This instance */
    @Nonnull
    public abstract <T extends E> T addText(@Nullable final Object data) throws IOException;

    /**
     * Add a text including a space (before and after the text)
     * @param data Anu data
     * @return This instance */
    @Nonnull
    public abstract <T extends E> T addTextWithSpace(@Nullable final Object data) throws IOException;

    /** Add an native text with no escaped characters, for example: XML code, JavaScript, CSS styles
     * @param data The {@code null} value is ignored.
     * @return This instance */
    @Nonnull
    public abstract <T extends E> T addRawText(@Nullable final Object data) throws IOException;
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

    // ----- UTILS -----

    /** Writer XML content by constants */
    public static final class WriterTool {

        /** A special XML character */
        public static final char XML_GT = '>';
        /** A special XML character */
        public static final char XML_LT = '<';
        /** A special XML character */
        public static final char XML_AMP = '&';
        /** A special XML character */
        public static final char XML_QUOT = '\'';
        /** A special XML character */
        public static final char XML_2QUOT = '"';
        /** A special XML character */
        public static final char CHAR_SPACE = ' ';
        /** A new line character */
        public static final char CHAR_NEW_LINE = '\n';
        /** A forward slash character */
        public static final char FORWARD_SLASH = '/';
        /** A CDATA beg markup sequence */
        public static final String CDATA_BEG = "<![CDATA[";
        /** A CDATA end markup sequence */
        public static final String CDATA_END = "]]>";
        /** A comment beg sequence */
        public static final String COMMENT_BEG = "<!--";
        /** A comment end sequence */
        public static final String COMMENT_END = "-->";

        /** Write escaped value to the output
         * @param text A value to write
         * @param out An output
         */
        public static void write(@Nonnull final CharSequence text, @Nonnull Appendable out) throws IOException {
            for (int i = 0, max = text.length(); i < max; i++) {
                final char c = text.charAt(i);
                switch (c) {
                    case XML_LT:
                        out.append(XML_AMP).append("lt;");
                        break;
                    case XML_GT:
                        out.append(XML_AMP).append("gt;");
                        break;
                    case XML_AMP:
                        out.append(XML_AMP).append("#38;");
                        break;
                    case XML_QUOT:
                        out.append(XML_AMP).append("#39;");
                        break;
                    case XML_2QUOT:
                        out.append(XML_AMP).append("#34;");
                        break;
                    default: {
                        if (c > 32 || c == CHAR_SPACE) {
                            out.append(c);
                        } else {
                            out.append(XML_AMP).append("#");
                            out.append(Integer.toString(c));
                            out.append(";");
                        }
                    }
                }
            }
        }
    }

}