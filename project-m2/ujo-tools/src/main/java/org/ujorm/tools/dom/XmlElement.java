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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.dom.XmlElement.RawEnvelope;
import static org.ujorm.tools.dom.XmlWriter.*;

/**
 * XML element model to rendering a XML file.
 * The main benefits are:
 * <ul>
 *     <li>secure building well-formed XML documents  by the Java code</li>
 *     <li>a simple API built on a single XmlElement class</li>
 *     <li>creating XML components by a subclass is possible</li>
 *     <li>great performance and small memory footprint</li>
 * </ul>¨
 * <h3>How to use the class:</h3>
 * <pre class="pre">
 *  XmlElement root = new XmlElement("root");
 *  root.addElement("childA")
 *          .addAttrib("x", 1)
 *          .addAttrib("y", 2);
 *  root.addElement("childB")
 *          .addAttrib("x", 3)
 *          .addAttrib("y", 4)
 *          .addText("A text message &lt;&\"&gt;");
 *  root.addRawText("\n&lt;rawXml/&gt;\n");
 *  root.addCDATA("A character data &lt;&\"&gt;");
 *  String result = root.toString();
 * </pre>
 * @see HtmlElement
 * @since 1.86
 * @author Pavel Ponec
 */
public class XmlElement {

    /** XML header */
    public static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

    /** Assertion message template */
    protected static final String REQUIRED_MSG = "The {} is required";

    /** Element name */
    @Nonnull
    protected final String name;

    /** Attributes */
    @Nullable
    protected Map<String, Object> attributes;

    /** Child elements */
    @Nullable
    protected List<Object> children;

    /** The new element constructor
     * @param name The parameter must not be empty or contain any special HTML characters.
     */
    public XmlElement(@Nonnull final String name) {
        Assert.notNull(name, REQUIRED_MSG, "name");
        this.name = name;
    }

    /** New element with a parent */
    public XmlElement(@Nonnull final String name, @Nonnull final XmlElement parent) {
        this(name);
        parent.addChild(this);
    }

    /** Return attributes */
    @Nonnull
    protected Map<String, Object> getAttribs() {
        if (attributes == null) {
            attributes = new LinkedHashMap<>();
        }
        return attributes;
    }

    /** Add a child entity */
    @Nonnull
    protected void addChild(@Nonnull final Object child) {
        Assert.notNull(child, REQUIRED_MSG, "child");
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    /**
     * Add a child element
     * @param element Add a child element is required. An undefined argument is ignored.
     * @return The argument type of XmlElement! */
    @Nonnull
    public final <T extends XmlElement> T addElement(@Nonnull final T element) {
        Assert.notNull(element, REQUIRED_MSG, "element");
        addChild(element);
        return element;
    }

    /** Create a new {@link XmlElement} for a required name and add it to children.
     * @param name A name of the new XmlElement is required.
     * @return The new XmlElement!
     */
    @Nonnull
    public final <T extends XmlElement> T addElement(@Nonnull final String name) {
        return (T) new XmlElement(name, this);
    }

    /**
     * Add an attribute
     * @param name Required element name
     * @param data The {@code null} value is ignored. Formatting is performed by the
     *   {@link XmlWriter#writeValue(java.lang.Object, org.ujorm.tools.dom.XmlElement, java.lang.String, java.io.Writer) }
     *   method, where the default implementation calls a {@code toString()} only.
     * @return The original element
     */
    @Nonnull
    public final <T extends XmlElement> T addAttrib(@Nonnull final String name, @Nullable final Object data) {
        Assert.hasLength(name, REQUIRED_MSG, "name");
        if (data != null) {
            if (attributes == null) {
                attributes = new LinkedHashMap<>();
            }
            attributes.put(name, data);
        }
        return (T) this;
    }

    /**
     * Add a text and escape special character
     * @param data The {@code null} value is ignored. Formatting is performed by the
     *   {@link XmlWriter#writeValue(java.lang.Object, org.ujorm.tools.dom.XmlElement, java.lang.String, java.io.Writer) }
     *   method, where the default implementation calls a {@code toString()} only.
     * @return This instance */
    @Nonnull
    public final <T extends XmlElement> T addText(@Nullable final Object data) {
        if (data != null) {
            addChild(data);
        }
        return (T) this;
    }

    /**
     * Add a text including a space (before and after the text)
     * @param data The {@code null} value is ignored.
     * @return This instance */
    @Nonnull
    public final <T extends XmlElement> T addTextWithSpace(@Nullable final Object data) {
        if (data != null) {
            addChild(CHAR_SPACE);
            addChild(data);
            addChild(CHAR_SPACE);
        }
        return (T) this;
    }

    /** Add an native text with no escaped characters, for example: XML code, JavaScript, CSS styles
     * @param data The {@code null} value is ignored.
     * @return This instance */
    @Nonnull
    public final <T extends XmlElement> T addRawText(@Nullable final Object data) {
        if (data != null) {
            addChild(new RawEnvelope(data));
        }
        return (T) this;
    }

    /**
     * Add a <strong>comment text</strong>.
     * The CDATA structure isn't really for HTML at all.
     * @param comment A comment text must not contain a string {@code -->} .
     * @return This instance
     */
    @Nonnull
    public final <T extends XmlElement> T addComment(@Nullable final CharSequence comment) {
        if (Check.hasLength(comment)) {
            Assert.isTrue(!comment.toString().contains(COMMENT_END), "The text contains a forbidden string: " + COMMENT_END);
            StringBuilder msg = new StringBuilder
                     ( COMMENT_BEG.length()
                     + COMMENT_END.length()
                     + comment.length() + 2);
            addRawText(msg.append(COMMENT_BEG)
                    .append(CHAR_SPACE)
                    .append(comment)
                    .append(CHAR_SPACE)
                    .append(COMMENT_END));
        }
        return (T) this;
    }

    /**
     * Add a <strong>character data</strong> in {@code CDATA} format to XML only.
     * The CDATA structure isn't really for HTML at all.
     * @param charData A text including the final DATA sequence. An empty argument is ignored.
     * @return This instance
     */
    @Nonnull
    public final <T extends XmlElement> T addCDATA(@Nullable final CharSequence charData) {
        if (Check.hasLength(charData)) {
            addRawText(CDATA_BEG);
            final String text = charData.toString();
            int i = 0, j;
            while ((j = text.indexOf(CDATA_END, i)) >= 0) {
                j += CDATA_END.length();
                addRawText(text.subSequence(i, j));
                i = j;
                addText(CDATA_END);
                addRawText(CDATA_BEG);
            }
            addRawText(i == 0 ? text : text.substring(i));
            addRawText(CDATA_END);
        }
        return (T) this;
    }

    /** Get an element name */
    @Nonnull
    public String getName() {
        return name.toString();
    }

    /** Get an unmodifiable map of attributes */
    @Nonnull
    public Map<String, Object> getAttributes() {
        return attributes != null
            ? Collections.unmodifiableMap(attributes)
            : Collections.emptyMap();
    }

    /** Get an unmodifiable list of children */
    @Nonnull
    public List<Object> getChildren() {
        return children != null
            ? Collections.unmodifiableList(children)
            : Collections.emptyList();
    }

    /** Render the XML code including header */
    @Override @Nonnull
    public String toString() {
        try {
            return toWriter(0, new XmlWriter(new CharArrayWriter(512)
                    .append(HEADER)
                    .append(CHAR_NEW_LINE))
            ).toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Render the XML code without header */
    @Nonnull
    public XmlWriter toWriter(final int level, @Nonnull final XmlWriter out) throws IOException {
        return out.write(level, this);
    }


    // -------- Inner class --------

    /** Raw XML code envelope */
    protected static final class RawEnvelope {
        /** XML content */
        @Nonnull
        private final Object body;

        public RawEnvelope(@Nonnull final Object body) {
            this.body = body;
        }

        /** Get the body */
        @Nonnull
        public Object get() {
            return body;
        }
    }
}