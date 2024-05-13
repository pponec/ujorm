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

package org.ujorm.tools.xml.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.xml.AbstractWriter;
import org.ujorm.tools.xml.ApiElement;
import org.ujorm.tools.xml.config.XmlConfig;
import org.ujorm.tools.xml.model.XmlModel.RawEnvelope;
import static org.ujorm.tools.xml.AbstractWriter.*;
import static org.ujorm.tools.xml.config.impl.DefaultXmlConfig.REQUIRED_MSG;

/**
 * XML element <strong>model</strong> to rendering a XML file.
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
 *          .setAttrib("x", 1)
 *          .setAttrib("y", 2);
 *  root.addElement("childB")
 *          .setAttrib("x", 3)
 *          .setAttrib("y", 4)
 *          .addText("A text message &lt;&\"&gt;");
 *  root.addRawText("\n&lt;rawXml/&gt;\n");
 *  root.addCDATA("A character data &lt;&\"&gt;");
 *  String result = root.toString();
 * </pre>
 *
 * @since 2.03
 * @author Pavel Ponec
 */
public class XmlModel implements ApiElement<XmlModel>, Serializable {

    /** Element name */
    @NotNull
    protected final String name;

    /** Attributes */
    @Nullable
    protected Map<String, Object> attributes;

    /** Child elements with a {@code null} items */
    @Nullable
    protected List<Object> children;

     /**
     * @param name The element name must not be special HTML characters.
     * The {@code null} value is intended to build a root of AJAX queries.
     */
    public XmlModel(@NotNull final String name) {
        this.name = name;
    }

    /** New element with a parent */
    public XmlModel(@NotNull final String name, @NotNull final XmlModel parent) {
        this(name);
        parent.addChild(this);
    }

    @Nullable
    @Override
    public String getName() {
        return name;
    }

    /** Return attributes */
    @NotNull
    protected Map<String, Object> getAttribs() {
        if (attributes == null) {
            attributes = new LinkedHashMap<>();
        }
        return attributes;
    }

    /** Add a child entity */
    @NotNull
    protected void addChild(@Nullable final Object child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    /**
     * Add a child element
     * @param element Add a child element is required. An undefined argument is ignored.
     * @return The argument type of XmlElement! */
    @NotNull
    public final XmlModel addElement(@NotNull final XmlModel element) {
        addChild(Assert.notNull(element, REQUIRED_MSG, "element"));
        return element;
    }

    /** Create a new {@link XmlModel} for a required name and add it to children.
     * @param name A name of the new XmlElement is required.
     * @return The new XmlElement!
     */
    @Override @NotNull
    public XmlModel addElement(@NotNull final String name) {
        return new XmlModel(name, this);
    }

    /**
     * Set one attribute
     * @param name Required element name
     * @param value The {@code null} value is ignored. Formatting is performed by the
     *   {@link XmlWriter#writeValue(java.lang.Object, org.ujorm.tools.dom.XmlElement, java.lang.String, java.io.Writer) }
     *   method, where the default implementation calls a {@code toString()} only.
     * @return The original element
     */
    @Override @NotNull
    public final XmlModel setAttribute(@NotNull final String name, @Nullable final Object value) {
        Assert.hasLength(name, REQUIRED_MSG, "name");
        if (value != null) {
            if (attributes == null) {
                attributes = new LinkedHashMap<>();
            }
            attributes.put(name, value);
        }
        return  this;
    }

    /**
     * Add a text and escape special character
     * @param value The {@code null} value is allowed. Formatting is performed by the
     *   {@link XmlWriter#writeValue(java.lang.Object, org.ujorm.tools.dom.XmlElement, java.lang.String, java.io.Writer) }
     *   method, where the default implementation calls a {@code toString()} only.
     * @return This instance */
    @Override @NotNull
    public final XmlModel addText(@Nullable final Object value) {
        addChild(value);
        return  this;
    }

    /**
     * Message template with hight performance.
     *
     * @param template Message template where parameters are marked by the {@code {}} symbol
     * @param values argument values
     * @return The original builder
     */
    @Override @NotNull
    public final XmlModel addTextTemplated(@Nullable final CharSequence template, @NotNull final Object... values) {
        try {
            return addText(AbstractWriter.FORMATTER.formatMsg(null, template, values));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }




    /** Add an native text with no escaped characters, for example: XML code, JavaScript, CSS styles
     * @param value The {@code null} value is ignored.
     * @return This instance */
    @Override @NotNull
    public final XmlModel addRawText(@Nullable final Object value) {
        if (value != null) {
            addChild(new RawEnvelope(value));
        }
        return  this;
    }

    /**
     * Add a <strong>comment text</strong>.
     * The CDATA structure isn't really for HTML at all.
     * @param comment A comment text must not contain a string {@code -->} .
     * @return This instance
     */
    @Override @NotNull
    public final XmlModel addComment(@Nullable final CharSequence comment) {
        if (Check.hasLength(comment)) {
            Assert.isTrue(!comment.toString().contains(COMMENT_END), "The text contains a forbidden string: " + COMMENT_END);
            StringBuilder msg = new StringBuilder
                     ( COMMENT_BEG.length()
                     + COMMENT_END.length()
                     + comment.length() + 2);
            addRawText(msg.append(COMMENT_BEG)
                    .append(SPACE)
                    .append(comment)
                    .append(SPACE)
                    .append(COMMENT_END));
        }
        return  this;
    }

    /**
     * Add a <strong>character data</strong> in {@code CDATA} format to XML only.
     * The CDATA structure isn't really for HTML at all.
     * @param charData A text including the final DATA sequence. An empty argument is ignored.
     * @return This instance
     */
    @Override @NotNull
    public final XmlModel addCDATA(@Nullable final CharSequence charData) {
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
        return  this;
    }

    /** Get an unmodifiable map of attributes */
    @NotNull
    public Map<String, Object> getAttributes() {
        return attributes != null
            ? Collections.unmodifiableMap(attributes)
            : Collections.emptyMap();
    }

    /** Get an unmodifiable list of children */
    @NotNull
    public List<Object> getChildren() {
        return children != null
            ? Collections.unmodifiableList(children)
            : Collections.emptyList();
    }

    /** An empty method */
    @Override
    public final void close() {
    }

    /** Render the XML code including header */
    @NotNull
    @Override
    public String toString() {
        try {
            final XmlConfig config = XmlConfig.ofDefault();
            final XmlWriter writer = new XmlWriter(new StringBuilder(512)
                    .append(AbstractWriter.XML_HEADER)
                    .append(config.getNewLine()));
            return toWriter(0, writer).toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Render the XML code without header */
    @NotNull
    public XmlWriter toWriter(final int level, @NotNull final XmlWriter out) throws IOException {
        return out.write(level, this);
    }

    // -------- Inner class --------

    /** Raw XML code envelope */
    protected static final class RawEnvelope {
        /** XML content */
        @NotNull
        private final Object body;

        public RawEnvelope(@NotNull final Object body) {
            this.body = body;
        }

        /** Get the body */
        @NotNull
        public Object get() {
            return body;
        }
    }
}