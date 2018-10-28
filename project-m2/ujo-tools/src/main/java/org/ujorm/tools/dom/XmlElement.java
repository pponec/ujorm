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
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;

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
public class XmlElement implements Element {

    /** XML header */
    public static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

    /** Element name */
    @Nonnull
    protected final CharSequence name;

    /** Attributes */
    @Nullable
    private Map<String, Object> attributes;

    /** Child elements */
    @Nullable
    private List<Object> children;

    /** The new element constructor
     * @param name The parameter must not be empty or contain any special HTML characters.
     */
    public XmlElement(@Nonnull final CharSequence name) {
        this.name = name;
    }

    /** New element with a parent */
    public XmlElement(@Nonnull final CharSequence name, @Nonnull final XmlElement parent) {
        this(name);
        parent.getChildren().add(this);
    }

    /** Return attributes */
    @Nonnull
    protected Map<String, Object> getAttribs() {
        if (attributes == null) {
            attributes = new LinkedHashMap<>();
        }
        return attributes;
    }

    /** Return child entities */
    @Nonnull
    protected List<Object> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    /**
     * Add a child element
     * @param element Add a child element is required. An undefined argument is ignored.
     * @return The argument type of XmlElement! */
    @Override @Nonnull
    public final <T extends Element> T addElement(@Nonnull final T element) {
        Assert.notNull(element, "element");
        getChildren().add(element);
        return element;
    }

    /** Create a new {@link XmlElement} for a required name and add it to children.
     * @param name A name of the new XmlElement is requred.
     * @return The new XmlElement!
     */
    @Override @Nonnull
    public final <T extends Element> T addElement(@Nonnull final CharSequence name) {
        Assert.hasLength(name, "Undefined element name");
        return (T) new XmlElement(name, this);
    }

    /**
     * Add an attribute
     * @param name Required element name
     * @param value The {@code null} value is ignored.
     * @return The original element
     */
    @Override @Nonnull
    public final <T extends Element> T addAttrib(@Nonnull final CharSequence name, @Nullable final Object value) {
        Assert.hasLength(name, "name");
        if (value != null) {
            getAttribs().put(name.toString(), value);
        }
        return (T) this;
    }

    /**
     * Add a text and escape special character
     * @param text text An empty argument is ignored.
     * @return This instance */
    @Override @Nonnull
    public final <T extends Element> T addText(@Nullable final CharSequence text) {
        if (Check.hasLength(text)) {
            getChildren().add(text);
        }
        return (T) this;
    }

    /**
     * Add a text including a space (before and after the text)
     * @param text text An empty argument is ignored.
     * @return This instance */
    @Override @Nonnull
    public final <T extends Element> T addTextWithSpace(@Nullable final CharSequence text) {
        if (Check.hasLength(text)) {
            getChildren().add(CHAR_SPACE);
            getChildren().add(text);
            getChildren().add(CHAR_SPACE);
        }
        return (T) this;
    }

    /** Add an native text with no escaped characters, for example: XML code, JavaScript, CSS styles
     * @param rawText text An empty argument is ignored.
     * @return This instance */
    @Override @Nonnull
    public final <T extends Element> T addRawText(@Nullable final CharSequence rawText) {
        if (Check.hasLength(rawText)) {
            getChildren().add(new RawEnvelope(rawText));
        }
        return (T) this;
    }

    /**
     * Add a <strong>comment text</strong>.
     * The CDATA structure isn't really for HTML at all.
     * @param comment A comment text must not contain a string {@code -->} .
     * @return This instance
     */
    @Override @Nonnull
    public final <T extends Element> T addComment(@Nullable final CharSequence comment) {
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
    @Override @Nonnull
    public final <T extends Element> T addCDATA(@Nullable final CharSequence charData) {
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

    /**
     * Write escaped value to the output
     * @param value A value
     * @param attribute Render the value to an element attribute, or a text
     * @param out An output writer
     * @throws IOException
     */
    protected void writeValue(@Nonnull final Object value, final boolean attribute, @Nonnull final Writer out) throws IOException {
        final CharSequence text = value instanceof CharSequence ? (CharSequence) value : String.valueOf(value);
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

    /** Render the XML code including header */
    @Override @Nonnull
    public String toString() {
        try {
            return toWriter(new CharArrayWriter(512).append(HEADER).append(CHAR_NEW_LINE)).toString();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Render the XML code without header */
    @Override @Nonnull
    public Writer toWriter(@Nonnull final Writer out) throws IOException {
        out.append(XML_LT);
        out.append(name);

        if (Check.hasLength(attributes)) {
            for (String key : attributes.keySet()) {
                out.append(CHAR_SPACE);
                out.append(key);
                out.append('=');
                out.append(XML_2QUOT);
                writeValue(attributes.get(key), true, out);
                out.append(XML_2QUOT);
            }
        }
        if (Check.hasLength(children)) {
            out.append(XML_GT);
            boolean writeNewLine = true;
            for (Object child : children) {
                if (child instanceof XmlElement) {
                    if (writeNewLine) {
                       out.append(CHAR_NEW_LINE);
                    } else {
                        writeNewLine = true;
                    }
                    ((XmlElement)child).toWriter(out);
                } else if (child instanceof RawEnvelope) {
                    out.append(((RawEnvelope) child).get());
                    writeNewLine = false;
                } else {
                    writeValue(child, false, out);
                    writeNewLine = false;
                }
            }
            out.append(XML_LT);
            out.append('/');
            out.append(name);
        } else {
            out.append('/');
        }
        out.append(XML_GT);

        return out;
    }

    // -------- Inner class --------

    /** Raw XML code envelope */
    protected static final class RawEnvelope {
        /** Xml content */
        private final CharSequence body;

        public RawEnvelope(@Nonnull final CharSequence body) {
            this.body = body;
        }

        /** Get the body */
        @Nonnull
        public CharSequence get() {
            return body;
        }
    }
}
