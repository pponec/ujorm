/*
 * Copyright 2018-2018 Pavel Ponec, https://github.com/pponec
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

import java.io.IOException;
import java.io.Writer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * XML element model to rendering a XML file.
 * The main benefits are:
 * <ul>
 *     <li>secure building well-formed XML documents  by the Java code</li>
 *     <li>a simple API built on a single XmlElement class</li>
 *     <li>creating XML components by a subclass is possible</li>
 * </ul>¨
 * @see XmlElement
 * @since 1.86
 * @author Pavel Ponec
 */
public interface Element {

    /** A special XML character */
    char XML_GT = '>';
    /** A special XML character */
    char XML_LT = '<';
    /** A special XML character */
    char XML_AMP = '&';
    /** A special XML character */
    char XML_QUOT = '\'';
    /** A special XML character */
    char XML_2QUOT = '"';
    /** A special XML character */
    char CHAR_SPACE = ' ';
    /** A new line character */
    char CHAR_NEW_LINE = '\n';
    /** A CDATA beg markup sequence */
    String CDATA_BEG = "<![CDATA[";
    /** A CDATA end markup sequence */
    String CDATA_END = "]]>";
    /** A comment beg sequence */
    String COMMENT_BEG = "<!--";
    /** A comment end sequence */
    String COMMENT_END = "-->";

    /**
     * Add a child element
     * @param element Add a child element is required. An undefined argument is ignored.
     * @return The argument type of XmlElement! */
    @Nonnull
    public <T extends Element> T addElement(@Nonnull T element);

    /** Create a new {@link XmlElement} for a required name and add it to children.
     * @param name A name of the new XmlElement is requred.
     * @return The new XmlElement!
     */
    @Nonnull
    public <T extends Element> T addElement(@Nonnull CharSequence name);

    /**
     * Add an attribute
     * @param name Required element name
     * @param value The {@code null} value is ignored.
     * @return The original element
     */
    @Nonnull
    public  <T extends Element> T addAttrib(@Nonnull CharSequence name, @Nullable  Object value);

    /**
     * Add a text and escape special character
     * @param text text An empty argument is ignored.
     * @return This instance */
    @Nonnull
    public  <T extends Element> T addText(@Nullable CharSequence text);

    /**
     * Add a text including a space (before and after the text)
     * @param text text An empty argument is ignored.
     * @return This instance */
    @Nonnull
    public  <T extends Element> T addTextWithSpace(@Nullable CharSequence text);

    /** Add an native text with no escaped characters, for example: XML code, JavaScript, CSS styles
     * @param rawText text An empty argument is ignored.
     * @return This instance */
    @Nonnull
    public  <T extends Element> T addRawText(@Nullable CharSequence rawText);

    /**
     * Add a <strong>comment text</strong>.
     * The CDATA structure isn't really for HTML at all.
     * @param comment A comment text must not contain a string {@code -->} .
     * @return This instance
     */
    @Nonnull
    public  <T extends Element> T addComment(@Nullable CharSequence comment) ;

    /**
     * Add a <strong>character data</strong> in {@code CDATA} format to XML only.
     * The CDATA structure isn't really for HTML at all.
     * @param charData A text including the  DATA sequence. An empty argument is ignored.
     * @return This instance
     */
    @Nonnull
    public  <T extends Element> T addCDATA(@Nullable CharSequence charData);

    /** Render the XML code without header */
    @Nonnull
    public Writer toWriter(@Nonnull Writer out) throws IOException;


}
