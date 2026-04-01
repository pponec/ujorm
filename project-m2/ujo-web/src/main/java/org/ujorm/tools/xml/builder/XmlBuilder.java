/*
 * Copyright 2018-2026 Pavel Ponec,
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

package org.ujorm.tools.xml.builder;

import java.io.Closeable;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.web.Html;
import org.ujorm.tools.xml.AbstractWriter;
import org.ujorm.tools.xml.ApiElement;

/**
 * A XML builder.
 * The main benefits are:
 * <ul>
 *     <li>secure building well-formed XML documents  by the Java code</li>
 *     <li>a simple API built on a single XmlElement class</li>
 *     <li>creating XML components by a subclass is possible</li>
 *     <li>great performance and small memory footprint</li>
 * </ul>
 * <h4>How to use the class:</h4>
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
 * @since 1.86
 * @author Pavel Ponec
 * @param <T> The exact type of the builder to allow fluent method chaining in subclasses.
 */
public class XmlBuilder<T extends XmlBuilder<T>> implements ApiElement<T> {

    /** A name of a hidden element must be a unique instance */
    @Nullable
    public static final String HIDDEN_NAME = "";

    /** The HTML tag name */
    public static final String HTML = Html.HTML;

    /** Assertion message template */
    protected static final String REQUIRED_MSG = "The argument '{}' is required";

    /** Element name (not final to allow recycling) */
    @NotNull
    protected String name;

    /** Node writer */
    @NotNull
    private final XmlPrinter writer;

    /** Element level */
    private final int level;

    /** Last child node */
    @Nullable
    protected T lastChild;

    /** A reusable child for performance optimization */
    @Nullable
    private T reusableChild;

    /** The last child was a text */
    private boolean lastText;

    /** Is Node is filled or it is empty */
    private boolean filled;

    /** The node is closed to writing */
    private boolean closed;

    /** An attribute mode */
    private boolean attributeMode = true;

    /** The new element constructor
     * @param name The element name must not be special HTML characters.
     * The {@code null} value is intended to build a root of AJAX queries.
     * @param writer A XmlPrinter
     * @param level Level of the Element
     */
    public XmlBuilder(
            @NotNull final String name,
            @NotNull final XmlPrinter writer,
            final int level) {
        this(name, writer, level, true);
    }

    /** The new element constructor
     * @param name The element name must not be special HTML characters.
     * The {@code null} value is intended to build a root of AJAX queries.
     * @param writer A XmlPrinter
     * @param level Level of the Element
     * @param printName Print the element name immediately.
     */
    @SuppressWarnings("java:S4973")
    protected XmlBuilder(
            @NotNull final String name,
            @NotNull final XmlPrinter writer,
            final int level,
            final boolean printName
    ) {
        this.name = name;
        this.lastText = name == HIDDEN_NAME;
        this.writer = Assert.required(writer, REQUIRED_MSG, "writer");
        this.level = level;

        if (printName) try {
            writer.writeBeg(this, lastText);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** New element with a parent */
    public XmlBuilder(@NotNull final String name, @NotNull final XmlPrinter writer) {
        this(name, writer, 0);
    }

    /** Returns this instance in the generic type T */
    @NotNull
    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    /** Resets the internal state of the builder to allow object recycling */
    @NotNull @SuppressWarnings("java:S4973")
    protected T reset(@NotNull final String name, final boolean printName) {
        this.name = name;
        this.lastText = name == HIDDEN_NAME;
        this.filled = false;
        this.closed = false;
        this.attributeMode = true;
        this.lastChild = null;

        if (printName) try {
            writer.writeBeg(this, lastText);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return self();
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    /**
     * Factory method for creating children.
     * Subclasses (like Element) must override this to return their own instances.
     * @param name The name of the new child element.
     * @return A new instance of type T.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    protected T createChild(@NotNull final String name) {
        return (T) new XmlBuilder<>(name, writer, level + 1, false);
    }

    /**
     * Setup states
     * @param element A child Node or {@code null} value for a text data
     * @return The child element
     */
    @Nullable
    protected T nextChild(@Nullable final T element) {
        Assert.isFalse(closed, "The node '{}' was closed", this.name);
        if (!filled) try {
            writer.writeMid(this);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        if (lastChild != null) {
            lastChild.close();
            lastChild = null;
        }
        if (element != null) try {
            writer.writeBeg(element, lastText);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        filled = true;
        attributeMode = false;
        lastChild = element;
        lastText = element == null || element.getName() == null;

        return element;
    }

    /** Create a new element for a required name and add it to children.
     * <b>WARNING: For performance reasons, this method returns a reusable instance
     * of the child element. Do NOT store the reference to the returned element
     * in a local variable if you intend to create another element at the same level!</b>
     *
     * @param name A name of the new XmlElement is required.
     * @return The new XmlElement (reused instance!)
     */
    @Override @NotNull
    public T addElement(@NotNull final String name) {
        if (lastChild != null) {
            lastChild.close();
            lastChild = null;
        }
        reusableChild = reusableChild != null
                ? reusableChild.reset(name, false)
                : createChild(name);

        return nextChild(this.reusableChild);
    }

    /**
     * Add an attribute
     * @param name Required element name
     * @param value The {@code null} value is ignored. Formatting is performed by the
     * {@link XmlPrinter#writeValue(Object, ApiElement, String)}
     * method, where the default implementation calls a {@code toString()} only.
     * @return The original element
     */
    @Override @NotNull
    public final T setAttribute(@Nullable final String name, @Nullable final Object value) {
        if (name != null) {
            Assert.hasLength(name, REQUIRED_MSG, "name");
            Assert.isFalse(closed, "The node '{}' was closed", name);
            Assert.isTrue(attributeMode, "Writing attributes to the '{}' node was closed", name);
            if (value != null) try {
                writer.writeAttrib(name, value, this);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return self();
    }

    /**
     * Add a text and escape special character
     * @param value The {@code null} value is allowed. Formatting is performed by the
     * {@link XmlPrinter#writeValue(Object, ApiElement, String)}
     * method, where the default implementation calls a {@code toString()} only.
     * @return This instance */
    @Override
    @NotNull
    public final T addText(@Nullable final Object value) {
        try {
            nextChild(null);
            writer.writeValue(value, this, null);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return self();
    }

    /**
     * Message template with high performance.
     *
     * @param template Message template where parameters are marked by the {@code {}} symbol
     * @param values argument values
     * @return The original builder
     */
    @Override
    @NotNull
    public final T addTextTemplated(@Nullable final CharSequence template, @NotNull final Object... values) {
        try {
            nextChild(null);
            AbstractWriter.FORMATTER.formatMsg(writer.getWriterEscaped(), template, values);
            return self();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Add a native text with no escaped characters, for example: XML code, JavaScript, CSS styles
     * @param value The {@code null} value is ignored.
     * @return This instance */
    @Override @NotNull
    public final T addRawText(@Nullable final Object value) {
        try {
            nextChild(null);
            writer.writeRawValue(value, this);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return self();
    }

    /**
     * Add a <strong>comment text</strong>.
     * @param comment A comment text must not contain a string {@code -->} .
     * @return This instance
     */
    @Override @NotNull
    public T addComment(@Nullable final CharSequence comment) {
        try {
            nextChild(null);
            writer.writeComment(comment, this);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return self();
    }

    /**
     * Add a <strong>character data</strong> in {@code CDATA} format to XML only.
     * The CDATA structure isn't really for HTML at all.
     * @param charData A text including the final DATA sequence. An empty argument is ignored.
     * @return This instance
     */
    @Override @NotNull @Deprecated
    public final T addCDATA(@Nullable final CharSequence charData) {
        throw new UnsupportedOperationException();
    }

    /** Close the Node */
    @Override
    public final void close() {
        if (!closed) try {
            closed = true;
            if (lastChild != null) {
                lastChild.close();
                lastChild = null;
            }
            writer.writeEnd(this);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Is the node closed? */
    public final boolean isClosed() {
        return closed;
    }

    @Override
    public final int getLevel() {
        return level;
    }

    public final boolean isFilled() {
        return filled;
    }

    /** The last child was a text */
    public final boolean isLastText() {
        return lastText;
    }

    /** Writer */
    @NotNull
    public final XmlPrinter getWriter() {
        return writer;
    }

    /** Render the XML code including header */
    @Override @NotNull
    public String toString() {
        return writer.toString();
    }

    // --- Static Methods ---

    /** Create builder for HTML */
    @NotNull
    public static XmlBuilder forHtml(@NotNull Appendable response) {
        return new XmlBuilder(HTML, XmlPrinter.forHtml(response));
    }

    @NotNull
    public static XmlBuilder forNiceHtml(@NotNull Appendable response) {
        return new XmlBuilder(HTML, XmlPrinter.forNiceHtml(response));
    }
}