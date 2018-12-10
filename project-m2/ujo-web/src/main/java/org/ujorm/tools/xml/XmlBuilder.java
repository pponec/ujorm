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
import org.ujorm.tools.dom.*;
import static org.ujorm.tools.dom.XmlWriter.*;

/**
 * A XML builder.
 * The main benefits are:
 * <ul>
 *     <li>secure building well-formed XML documents  by the Java code</li>
 *     <li>a simple API built on a single XmlElement class</li>
 *     <li>creating XML components by a subclass is possible</li>
 *     <li>great performance and small memory footprint</li>
 * </ul>¨
 * <h3>How to use the class:</h3>
 * <pre class="pre">
 *  XmlPriter writer = XmlPriter.forXml();
 *  try (XmlBuilder html = new XmlBuilder(Html.HTML, writer)) {
 *      try (XmlBuilder head = html.addElement(Html.HEAD)) {
 *          head.addElement(Html.META, Html.A_CHARSET, UTF_8);
 *          head.addElement(Html.TITLE).addText("Test");
 *      }
 *      ry (XmlBuilder body = html.addElement(Html.BODY)) {
 *          body.addElement(Html.H1).addText("Hello word!");
 *          body.addElement(Html.DIV).addText(null);
 *      }
 *  };
 *  String result = writer.toString();
 * </pre>
 *
 * The XmlElement class implements the {@link Closeable} implementation
 * for an optional highlighting the tree structure in the source code.
 * @see HtmlElement
 * @since 1.86
 * @author Pavel Ponec
 */
public class XmlBuilder implements Closeable {

    /** Assertion message template */
    protected static final String REQUIRED_MSG = "The argument {} is required";

    /** Element name */
    @Nonnull
    protected final String name;

    /** Node writer */
    @Nonnull
    private final XmlPriter writer;

    /** Element level */
    private int level;

    /** Last child node */
    private XmlBuilder lastChild;

    /** The last child was a text */
    private boolean lastText;

    /** Is Node is filled or it is empty */
    private boolean filled;

    /** The node is closed to writing */
    private boolean closed;

    /** An attribsute mode */
    private boolean attributeMode = true;

    /** The new element constructor
     * @param name The element name must not be empty or contain any special HTML characters.
     */
    public XmlBuilder(@Nonnull final String name, @Nonnull final XmlPriter writer, final int level) throws IOException {
        Assert.notNull(name, REQUIRED_MSG, "name");
        Assert.notNull(writer, REQUIRED_MSG, "writer");
        this.name = name;
        this.writer = writer;

        if (level == 0) {
            writer.writeBeg(this);
        }
    }

    /** New element with a parent */
    public XmlBuilder(@Nonnull final String name, @Nonnull final XmlPriter writer) throws IOException {
        this(name, writer, 0);
    }


    /**
     * Settup states
     * @param node A child Node or {@code null} value for a text data
     * @throws IOException
     */
    @Nonnull
    protected <T extends XmlBuilder> T nextChild(@Nullable XmlBuilder node) throws IOException {
        Assert.isFalse(closed, "The node {} was closed", this.name);
        if (!filled) {
            writer.writeMid(this);
        }
        if (lastChild != null) {
            lastChild.close();
        }
        if (node != null) {
            writer.writeBeg(node);
        }

        filled = true;
        attributeMode = false;
        lastChild = node;
        lastText = node == null;

        return (T) node;
    }

    /**
     * Add a child element
     * @param element Add a child element is required. An undefined argument is ignored.
     * @return The argument type of XmlElement! */
    @Nonnull @Deprecated
    public final <T extends XmlBuilder> T addElement(@Nonnull final T element) throws IOException {
        Assert.notNull(element, REQUIRED_MSG, "element");
        nextChild(element);
        return element;
    }

    /** *  Create a new {@link XmlBuilder} for a required name and add it to children.
     * @param name A name of the new XmlElement is required.
     * @return The new XmlElement!
     */
    @Nonnull
    public final <T extends XmlBuilder> T addElement(@Nonnull final String name) throws IOException {
        return nextChild(new XmlBuilder(name, writer, level + 1));
    }

    /** *  Create a new {@link XmlBuilder} for a required name and add it to children with many attributes.
     * @param elementName  A name of the new XmlElement is required.
     * @param attributeName An attribute key
     * @param attributeData An attribute value
     * @param attributes Pairs of attribute - value. An attribute with no value is ignored silently.
     * @return The new XmlElement!
     */
    @Nonnull
    public final <T extends XmlBuilder> T addElement
        ( @Nonnull final String elementName
        , @Nonnull final String attributeName
        , @Nullable final Object attributeData
        , @Nonnull final Object... attributes) throws IOException
        {
        final T result = addElement(elementName);
        result.setAttrib(attributeName, attributeData);
        for (int i = 1, max = attributes.length; i < max; i += 2) {
             result.setAttrib((String) attributes[i-1], attributes[i]);
        }
        return result;
    }

    /**
     * Add an attribute
     * @param name Required element name
     * @param data The {@code null} value is ignored. Formatting is performed by the
     *   {@link XmlPriter#writeValue(java.lang.Object, org.ujorm.tools.dom.XmlElement, java.lang.String, java.io.Writer) }
     *   method, where the default implementation calls a {@code toString()} only.
     * @return The original element
     */
    @Nonnull
    public final <T extends XmlBuilder> T setAttrib(@Nonnull final String name, @Nullable final Object data) throws IOException {
        Assert.hasLength(name, REQUIRED_MSG, "name");
        Assert.isFalse(closed, "The node {} was closed", this.name);
        Assert.isTrue(attributeMode, "Writing attributes to the {} node was closed", this.name);
        if (data != null) {
            writer.writeAttrib(name, data, this);
        }
        return (T) this;
    }

    /**
     * Add a text and escape special character
     * @param data The {@code null} value is allowed. Formatting is performed by the
     *   {@link XmlPriter#writeValue(java.lang.Object, org.ujorm.tools.dom.XmlElement, java.lang.String, java.io.Writer) }
     *   method, where the default implementation calls a {@code toString()} only.
     * @return This instance */
    @Nonnull
    public final <T extends XmlBuilder> T addText(@Nullable final Object data) throws IOException {
        nextChild(null);
        writer.writeValue(data, this, null);
        return (T) this;
    }

    /**
     * Add a text including a space (before and after the text)
     * @param data Anu data
     * @return This instance */
    @Nonnull
    public final <T extends XmlBuilder> T addTextWithSpace(@Nullable final Object data) throws IOException {
        nextChild(null);
        writer.writeRawText(CHAR_SPACE);
        writer.writeValue(data, this, null);
        writer.writeRawText(CHAR_SPACE);
        return (T) this;
    }

    /** Add an native text with no escaped characters, for example: XML code, JavaScript, CSS styles
     * @param data The {@code null} value is ignored.
     * @return This instance */
    @Nonnull
    public final <T extends XmlBuilder> T addRawText(@Nullable final Object data) throws IOException {
        nextChild(null);
        writer.writeRawValue(data, this);
        return (T) this;
    }

//    /**
//     * Add a <strong>comment text</strong>.
//     * The CDATA structure isn't really for HTML at all.
//     * @param comment A comment text must not contain a string {@code -->} .
//     * @return This instance
//     */
//    @Nonnull
//    public final <T extends XmlNode> T addComment(@Nullable final CharSequence comment) {
//        if (Check.hasLength(comment)) {
//            Assert.isTrue(!comment.toString().contains(COMMENT_END), "The text contains a forbidden string: " + COMMENT_END);
//            StringBuilder msg = new StringBuilder
//                     ( COMMENT_BEG.length()
//                     + COMMENT_END.length()
//                     + comment.length() + 2);
//            addRawText(msg.append(COMMENT_BEG)
//                    .append(CHAR_SPACE)
//                    .append(comment)
//                    .append(CHAR_SPACE)
//                    .append(COMMENT_END));
//        }
//        return (T) this;
//    }

//    /**
//     * Add a <strong>character data</strong> in {@code CDATA} format to XML only.
//     * The CDATA structure isn't really for HTML at all.
//     * @param charData A text including the final DATA sequence. An empty argument is ignored.
//     * @return This instance
//     */
//    @Nonnull
//    public final <T extends XmlNode> T addCDATA(@Nullable final CharSequence charData) {
//        if (Check.hasLength(charData)) {
//            addRawText(CDATA_BEG);
//            final String text = charData.toString();
//            int i = 0, j;
//            while ((j = text.indexOf(CDATA_END, i)) >= 0) {
//                j += CDATA_END.length();
//                addRawText(text.subSequence(i, j));
//                i = j;
//                addText(CDATA_END);
//                addRawText(CDATA_BEG);
//            }
//            addRawText(i == 0 ? text : text.substring(i));
//            addRawText(CDATA_END);
//        }
//        return (T) this;
//    }

    /** Get an Node name */
    @Nonnull
    public String getName() {
        return name.toString();
    }

    /** Close the Node */
    @Override
    public final void close() throws IOException {
        if (!closed) {
            closed = true;
            if (lastChild != null) {
                lastChild.close();
            }
            writer.writeEnd(this);
        }
    }

    /** Is the node closed? */
    public boolean isClosed() {
        return closed;
    }

    public int getLevel() {
        return level;
    }

    public boolean isFilled() {
        return filled;
    }

    /** The last child was a text */
    public boolean isLastText() {
        return lastText;
    }

    /** Writer */
    public XmlPriter getWriter() {
        return writer;
    }

    /** Render the XML code including header */
    @Override @Nonnull
    public String toString() {
        return writer.toString();
    }
}