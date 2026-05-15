/*
 * Copyright 2018-2026 Pavel Ponec, https://github.com/pponec
 * https://github.com/pponec/ujorm/blob/master/samples/servlet/src/main/java/org/ujorm/ujoservlet/tools/Html.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ujorm.tools.web;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.web.ao.Column;
import org.ujorm.tools.web.ao.WebUtils;
import org.ujorm.tools.xml.ApiElement;
import org.ujorm.tools.xml.builder.XmlBuilder;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.ao.Injector;
import org.ujorm.tools.xml.builder.XmlPrinter;

/**
 * HTML element builder with convenience methods for common elements and attributes.
 *
 * <h4>Usage</h4>
 * <pre class="pre">
 * var response = HttpContext.of();
 * try (var html = AbstractHtmlElement.of(response)) {
 *     try (var body = html.getBody()) {
 *         body.addHeading("Hello!");
 *         body.addLabel().addText("Active:")
 *             .addCheckBox("active").setCheckBoxValue(true);
 *     }
 * }
 * assertTrue(response.toString().contains("&lt;h1&gt;Hello!&lt;/h1&gt;"));
 * </pre>
 *
 * @see HtmlElement#of(org.ujorm.tools.xml.config.HtmlConfig)
 */
public class Element extends XmlBuilder<Element> implements Html {

    /** No CSS styles */
    public static final String[] NO_CSS = {};

    /** A reusable builder for CSS classes optimization */
    private StringBuilder cssBuilder;

    /** Constructor for a new HTML element */
    public Element(@NotNull final String name, @NotNull final XmlPrinter writer, final int level) {
        super(name, writer, level, true);
    }

    /** Constructor for root elements without immediate print */
    public Element(@NotNull final String name, @NotNull final XmlPrinter writer, final int level, final boolean printName) {
        super(name, writer, level, printName);
    }

    /** Factory method for creating element children to allow method chaining */
    @Override
    @NotNull
    protected Element createChild(@NotNull final String name) {
        return new Element(name, getWriter(), getLevel() + 1, false);
    }

    /**
     * Set an attribute
     * @param name Required element name
     * @param separator Separator for joining values
     * @param value The {@code null} value is silently ignored.
     * @return The current element
     */
    @NotNull
    public Element setAttributes(
            @NotNull final String name,
            @NotNull final CharSequence separator,
            @NotNull final Object... value) {
        if (Check.isEmpty(value)) {
            return this;
        }
        var builder = new StringBuilder(64);
        for (var val : value) {
            if (val != null) {
                if (!builder.isEmpty()) {
                    builder.append(separator);
                }
                builder.append(val);
            }
        }
        if (!builder.isEmpty()) {
            setAttribute(name, builder.toString());
        }
        return this;
    }

    /**
     * Set an attribute with no value
     * @param name Required element name
     * @return The current element
     */
    @NotNull
    public Element setAttribute(@NotNull final String name) {
        return setAttribute(name, "");
    }

    /**
     * A shortcut for the method {@link #setAttribute(java.lang.String, java.lang.Object) }.
     * @param name Required element name
     * @param value The {@code null} value is silently ignored. Formatting is performed by the
     *   {@link XmlPrinter#writeValue(Object, ApiElement, String)}
     *   method, where the default implementation calls a {@code toString()} only.
     * @return The current element
     */
    @NotNull
    public Element setAttr(@NotNull final String name, @Nullable final Object value) {
        return setAttribute(name, value);
    }

    /** Add many texts with no separator
     * @param data Text item
     * @return The current element
     * @see #addAnchor(String, CharSequence...)
     */
    @NotNull
    public Element addText(@NotNull final Object... data) throws IllegalStateException {
        return addTexts("", data);
    }

    /** Add many words separated by a delimiter */
    @NotNull
    public Element addTexts(
            @NotNull final CharSequence separator,
            @NotNull final Object... data)
            throws IllegalStateException {
        for (int i = 0; i < data.length; i++) {
            if (i > 0) {
                super.addRawText(separator);
            }
            super.addText(data[i]);
        }
        return this;
    }

    /** Add raw text items and return the current object. */
    @NotNull
    public Element addRawText(@NotNull final Object... data) throws IllegalStateException {
        for (Object item : data) {
            super.addRawText(item);
        }
        return this;
    }

    /**
     * Add many words separated by a delimiter
     * @param separator The delimiter must contain no special HTML character.
     * @param data Data to print
     * @return The current element
     * @throws IllegalStateException If an error occurs
     */
    public Element addRawTexts(
            @NotNull final CharSequence separator,
            @NotNull final Object... data)
            throws IllegalStateException {
        for (int i = 0; i < data.length; i++) {
            if (i > 0) {
                super.addRawText(separator);
            }
            super.addRawText(data[i]);
        }
        return this;
    }

    // -------------- Add ELEMENT (Overloaded specified methods) -----

    /** Create a new element for a required name and add it to children.
     * <b>WARNING: For performance reasons, this method returns a reusable instance
     * of the child element. Do NOT store the reference to the returned element
     * in a local variable if you intend to create another element at the same level!</b>
     *
     * @param name A name of the new XmlElement is required.
     * @return The new XmlElement (reused instance!)
     */
    @Override
    @NotNull
    public Element addElement(@NotNull final String name) {
        return super.addElement(name);
    }

    /** Create a new element for a required name and add it to children.
     * <b>WARNING: For performance reasons, this method returns a reusable instance
     * of the child element. Do NOT store the reference to the returned element
     * in a local variable if you intend to create another element at the same level!</b>
     *
     * @param name A name of the new XmlElement is required.
     * @return The new XmlElement (reused instance!)
     */
    @NotNull
    public Element addElement(@NotNull final String name, @NotNull final CharSequence... cssClasses) {
        return addElement(name).setClass(cssClasses);
    }

    /** Add new div element with no CSS classes */
    @NotNull
    public Element addDiv() {
        return addElement(DIV);
    }

    /** Add new div element with optional CSS classes */
    @NotNull
    public Element addDiv(@NotNull final CharSequence... cssClasses) {
        return addDiv().setClass(cssClasses);
    }

    /** Add new span element with no CSS classes */
    @NotNull
    public Element addSpan() {
        return addElement(SPAN);
    }

    /** Add new span element with optional CSS classes */
    @NotNull
    public Element addSpan(@NotNull final CharSequence... cssClasses) {
        return addSpan().setClass(cssClasses);
    }

    /** Add new paragraph element with no CSS classes */
    @NotNull
    public Element addParagraph() {
        return addElement(P);
    }

    /** Add new paragraph element with optional CSS classes */
    @NotNull
    public Element addParagraph(@NotNull final CharSequence... cssClasses) {
        return addParagraph().setClass(cssClasses);
    }

    /** Add new label element with no CSS classes */
    @NotNull
    public Element addLabel() {
        return addElement(LABEL);
    }

    /** Add new label element with optional CSS classes */
    @NotNull
    public Element addLabel(@NotNull final CharSequence... cssClasses) {
        return addLabel().setClass(cssClasses);
    }

    /** Add a list item element with no CSS classes */
    @NotNull
    public Element addListItem() {
        return addElement(LI);
    }

    /** Add a list item element with optional CSS classes */
    @NotNull
    public Element addListItem(@NotNull final CharSequence... cssClasses) {
        return addListItem().setClass(cssClasses);
    }

    // -------------- Standard Vararg Methods -----

    /** Add an element according to a condition
     * @param enabled A condition for rendering the element.
     * @param name An element name
     * @param cssClasses CSS classes
     * @return A new nested element (or a hidden element if disabled)
     */
    @NotNull
    public Element addElementIf(final boolean enabled,
                                @NotNull final String name,
                                @NotNull final CharSequence... cssClasses) {
        return addElement(enabled ? name : XmlBuilder.HIDDEN_NAME, cssClasses);
    }

    /** Add new Table */
    @NotNull
    public Element addTable(@NotNull final CharSequence... cssClasses) {
        return addElement(TABLE, cssClasses);
    }

    /** Create a HTML table according to data */
    @NotNull
    public Element addTable(
            @NotNull final Object[][] data,
            @NotNull final CharSequence... cssClass) {
        return addTable(Arrays.asList(data), cssClass);
    }

    /** Create a HTML table according to data */
    @NotNull
    public Element addTable(
            @NotNull final Collection<Object[]> data,
            @NotNull final CharSequence... cssClass) {
        final var result = addTable(cssClass);
        for (final Object[] rowValue : data) {
            if (rowValue != null) {
                final var rowElement = result.addTableRow();
                for (final Object value : rowValue) {
                    rowElement.addTableDetail().addText(value);
                }
            }
        }
        return result;
    }

    /** Create a HTML table according to data
     *
     * <h4>Usage</h4>
     * <pre>
     * element.addTable(getCars(), cssClasses, titles,
     * Car::getId,
     * Car::getName,
     * Car::getEnabled);
     * </pre>
     * @return A new table element
     */
    @SafeVarargs
    @NotNull
    public final <D,V> Element addTable(
            @NotNull final Stream<D> domains,
            @Nullable final CharSequence[] cssClass,
            @Nullable final Object[] headers,
            @NotNull final Function<D,V>... attributes) {

        final var result = addTable(Check.isEmpty(cssClass) ? NO_CSS : cssClass);
        if (Check.hasLength(headers)) {
            final var rowElement = result.addTableHead().addTableRow();
            for (Object value : headers) {
                var th = rowElement.addElement(Html.TH);
                if (value instanceof Injector injector) {
                    injector.write(th);
                } else {
                    th.addText(value);
                }
            }
        }
        try (var tBody = result.addTableBody()) {
            final boolean hasRenderer = WebUtils.isType(Column.class, attributes);
            domains.forEach(value -> {
                final var rowElement = tBody.addTableRow();
                for (var attribute : attributes) {
                    final var td = rowElement.addTableDetail();
                    if (hasRenderer && attribute instanceof Column column) {
                        column.write(td, value);
                    } else {
                        td.addText(attribute.apply(value));
                    }
                }
            });
        }
        return result;
    }

    /** Add an image */
    @NotNull
    public Element addImg(@NotNull final CharSequence... cssClasses)
            throws IllegalStateException {
        return addElement(IMAGE, cssClasses);
    }

    /**
     * Appends a checkbox input element to the current container, accompanied by a hidden fallback field.
     * <p>
     * This method implements a workaround for the standard HTML form submission behavior where
     * unchecked checkboxes are not sent in the request. By prepending an {@code <input type="hidden">}
     * with the same name and a value of {@code false}, this method ensures that the server
     * always receives a boolean value (either {@code true} or {@code false}).
     * </p>
     * <p><b>Usage Note:</b>
     * To set the checked state of the component, use {@link #setCheckBoxValue(boolean)}.
     * </p>
     * @param name       the {@code name} attribute shared by both the checkbox and the hidden input
     * @param cssClasses optional CSS classes to be applied to the visible checkbox element
     * @return A new checkbox element
     * @see #setCheckBoxValue(boolean)
     */
    @NotNull
    public Element addCheckBox(
            @NotNull final CharSequence name,
            @NotNull final CharSequence... cssClasses) {
        addHiddenInput(name, false);
        return addInput(cssClasses).setType(Html.V_CHECKBOX).setName(name);
    }

    /**
     * Add a link to an image
     * @param imageLink A link to image
     * @param alt An alternate text
     * @param cssClasses Optional CSS classes
     * @return A new image element
     * @throws IllegalStateException If an error occurs
     */
    @NotNull
    public Element addImage(
            @NotNull final CharSequence imageLink,
            @NotNull final CharSequence alt,
            @NotNull final CharSequence... cssClasses) throws IllegalStateException {
        return addImg(cssClasses)
                .setAttribute(A_ALT, alt)
                .setAttribute(A_SRC, imageLink);
    }

    /**
     * Add an embedded image
     * @param imageStream Stream provides a PNG image and it will be closed after reading.
     * @param alt An alternate text
     * @param cssClasses Optional CSS classes
     * @return A new image element
     * @throws IllegalStateException If an error occurs
     */
    @NotNull
    public Element addImage(
            @NotNull final InputStream imageStream,
            @NotNull final CharSequence alt,
            @NotNull final CharSequence... cssClasses)
            throws IllegalStateException {
        return addElement(IMAGE, cssClasses)
                .setAttribute(A_ALT, alt)
                .setAttribute(A_SRC, createEmbededImage(imageStream, new StringBuilder(1024)));
    }

    /** Create a content of an embedded image */
    @NotNull
    private CharSequence createEmbededImage(
            @NotNull final InputStream imageStream,
            @NotNull final StringBuilder result) {
        final int bufferSize = 3 * 1024;
        final var encoder = Base64.getEncoder();
        try (var in = new BufferedInputStream(imageStream)) {
            result.append("data:image/png;base64,");
            byte[] chunk = new byte[bufferSize];
            int len;
            while ((len = in.read(chunk)) != -1) {
                if (len == bufferSize) {
                    result.append(encoder.encodeToString(chunk));
                } else {
                    result.append(encoder.encodeToString(Arrays.copyOf(chunk, len)));
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return result;
    }

    /** Add new head element */
    @NotNull
    public Element addHead(@NotNull final CharSequence... cssClasses) {
        return addElement(HEAD).setClass(cssClasses);
    }

    /** Add new body element */
    @NotNull
    public Element addBody(@NotNull final CharSequence... cssClasses) {
        return addElement(BODY).setClass(cssClasses);
    }

    /** Add new title element */
    @NotNull
    public Element addTitle(@NotNull final CharSequence... cssClasses) {
        return addElement(TITLE, cssClasses);
    }

    /** Add new link element */
    @NotNull
    public Element addLink(@NotNull final CharSequence... cssClasses) {
        return addElement(LINK, cssClasses);
    }

    /** Add new style element */
    @NotNull
    public Element addStyle(@NotNull final CharSequence... cssClasses) {
        return addElement(STYLE, cssClasses);
    }

    /** Add new script element */
    @NotNull
    public Element addScript(@NotNull final CharSequence... cssClasses) {
        return addElement(SCRIPT, cssClasses);
    }

    /** Add new fieldset element including a title
     * @param title An optional title
     * @param cssClasses CSS classes
     * @return A new fieldset element
     * @see #LEGEND
     */
    @NotNull
    public Element addFieldset(@Nullable final String title, @NotNull final CharSequence... cssClasses) {
        final var result = addElement(FIELDSET, cssClasses);
        if (Check.hasLength(title)) {
            result.addElement(LEGEND).addText(title);
        }
        return result;
    }

    /** Add new pre element */
    @NotNull
    public Element addPreformatted(@NotNull final CharSequence... cssClasses) {
        return addElement(PRE, cssClasses);
    }

    /** Add new form element */
    @NotNull
    public Element addForm(@NotNull final CharSequence... cssClasses) {
        return addElement(FORM, cssClasses);
    }

    /** Add a top heading (level one) */
    @NotNull
    public Element addHeading(@NotNull CharSequence title, @NotNull final CharSequence... cssClasses) {
        return addHeading(1, title, cssClasses);
    }

    /** Add new heading with the required level */
    @NotNull
    public Element addHeading(int level, @NotNull CharSequence title, @NotNull final CharSequence... cssClasses) {
        return addHeadingX(level, cssClasses).addText(title);
    }

    /** Add new heading with the required level */
    @NotNull
    public Element addHeadingX(int level, @NotNull final CharSequence... cssClasses) {
        Assert.isTrue(level > 0, () -> "Unsupported level " + level);
        return addElement(HEADING_PREFIX + level, cssClasses);
    }

    /** Add new head of table element with CSS */
    @NotNull
    public Element addTableHead(@NotNull final CharSequence... cssClasses) {
        return addElement(THEAD, cssClasses);
    }

    /** Add new table body element with CSS */
    @NotNull
    public Element addTableBody(@NotNull final CharSequence... cssClasses) {
        return addElement(TBODY, cssClasses);
    }

    /** Add new table row element with CSS */
    @NotNull
    public Element addTableRow(@NotNull final CharSequence... cssClasses) {
        return addElement(TR, cssClasses);
    }

    /** Add new detail of table element with CSS */
    @NotNull
    public Element addTableDetail(@NotNull final CharSequence... cssClasses) {
        return addElement(TD, cssClasses);
    }

    /** Add new input element */
    @NotNull
    public Element addInput(@NotNull final CharSequence... cssClasses) {
        return addElement(INPUT, cssClasses);
    }

    /** Add new input element type of text */
    @NotNull
    public Element addTextInput(@NotNull final CharSequence... cssClasses) {
        return addInput(cssClasses).setType(V_TEXT);
    }

    /** Add new input element type of text including attributes */
    @NotNull
    public <V> Element addTextInp(
            @NotNull HttpParameter param,
            @Nullable V value,
            @NotNull CharSequence title,
            @NotNull final CharSequence... cssClasses) {
        return addTextInput(cssClasses)
                .setName(param)
                .setValue(value)
                .setAttribute(Html.A_PLACEHOLDER, title)
                .setAttribute(Html.A_TITLE, title);
    }

    /** Add a new password input element */
    @NotNull
    public Element addPasswordInput(@NotNull final CharSequence... cssClasses) {
        return addInput(cssClasses).setType(V_PASSWORD);
    }

    /** Add a new hidden input element with a name &amp; value */
    @NotNull
    public Element addHiddenInput(
            @Nullable final CharSequence name,
            @Nullable final Object value) {
        return addInput().setType(V_HIDDEN).setNameValue(name, value);
    }

    /** Add new text area element */
    @NotNull
    public Element addTextArea(@NotNull final CharSequence... cssClasses) {
        return addElement(TEXT_AREA, cssClasses);
    }

    /** Add new select element */
    @NotNull
    public Element addSelect(@NotNull final CharSequence... cssClasses) {
        return addElement(SELECT, cssClasses);
    }

    /** Add options from map to current select element
     * @param value Value of a select element
     * @param options Consider an instance of the {@link java.util.LinkedHashMap} class for predictable iteration order.
     * @param cssClasses CSS classes for the options
     * @return The current element
     * @see #addSelect(java.lang.CharSequence...)
     */
    @NotNull
    public Element addSelectOptions(
            @NotNull Object value,
            @NotNull final Map<?,?> options,
            @NotNull final CharSequence... cssClasses) {
        options.forEach((key, val) ->
                this.addElement(Html.OPTION)
                        .setAttribute(Html.A_VALUE, key)
                        .setAttribute(Html.A_SELECTED, Objects.equals(value, key) ? Html.A_SELECTED : null)
                        .setClass(cssClasses)
                        .addText(val)
        );
        return this;
    }

    /** Add new option element */
    @NotNull
    public Element addOption(@NotNull final CharSequence... cssClasses) {
        return addElement(OPTION, cssClasses);
    }

    /** Add new button element */
    @NotNull
    public Element addButton(@NotNull final CharSequence... cssClasses) {
        return addElement(BUTTON, cssClasses);
    }

    /** Add a submit button */
    @NotNull
    public Element addSubmitButton(@NotNull final CharSequence... cssClasses) {
        return addButton(cssClasses).setType(V_SUBMIT);
    }

    /** Add an anchor element with URL and CSS classes */
    @NotNull
    public Element addAnchor(@NotNull final String url, @NotNull final CharSequence... cssClasses) {
        return addElement(A, cssClasses).setHref(url);
    }

    /** Add a linked text */
    @NotNull
    public Element addLinkedText(@NotNull final String url, @NotNull final Object... text) {
        addElement(A).setHref(url).addTexts("", text);
        return this;
    }

    /** Add new unordered list element */
    @NotNull
    public Element addUnorderedlist(@NotNull final CharSequence... cssClasses) {
        return addElement(UL, cssClasses);
    }

    /** Add new ordered list element */
    @NotNull
    public Element addOrderedList(@NotNull final CharSequence... cssClasses) {
        return addElement(OL, cssClasses);
    }

    /** Set a CSS class attribute optionally, the empty attribute is ignored.
     * @param cssClasses Optional CSS classes. The css item is ignored when the value is empty or {@code null}.
     * @return The current element
     */
    @NotNull
    public Element setClass(@NotNull final CharSequence... cssClasses) {
        if (Check.hasLength(cssClasses)) {
            if (this.cssBuilder == null) {
                this.cssBuilder = new StringBuilder(64);
            } else {
                this.cssBuilder.setLength(0);
            }

            for (var cssClass : cssClasses) {
                if (Check.hasLength(cssClass)) {
                    if (!this.cssBuilder.isEmpty()) {
                        this.cssBuilder.append(' ');
                    }
                    this.cssBuilder.append(cssClass);
                }
            }

            if (!this.cssBuilder.isEmpty()) {
                setAttribute(A_CLASS, this.cssBuilder.toString());
            }
        }
        return this;
    }

    /** Add a line break */
    @NotNull
    public Element addBreak(@NotNull final CharSequence... cssClasses) {
        return addElement(BREAK, cssClasses);
    }

    /** Set an identifier of the element */
    @NotNull
    public Element setId(@Nullable final CharSequence value) {
        return setAttribute(A_ID, value);
    }

    /** Set a method of form */
    @NotNull
    public Element setMethod(@Nullable final Object value) {
        return setAttribute(A_METHOD, value);
    }

    /** Set an action type of form */
    @NotNull
    public Element setAction(@Nullable final Object value) {
        return setAttribute(A_ACTION, value);
    }

    /** Set a type of input element */
    @NotNull
    public Element setType(@Nullable final Object value) {
        return setAttribute(A_TYPE, value);
    }

    /** Set an name of input element */
    @NotNull
    public Element setName(@Nullable final CharSequence value) {
        return setAttribute(A_NAME, value);
    }

    /** Set a value of input element. */
    @NotNull
    public Element setValue(@Nullable final Object value) {
        return setAttribute(A_VALUE, value);
    }

    /** Set name &amp; value to the input element */
    @NotNull
    public Element setNameValue(@Nullable final CharSequence name, @Nullable final Object value) {
        return setName(name).setValue(value);
    }

    /** Set a for attribute */
    @NotNull
    public Element setFor(@Nullable final CharSequence value) {
        return setAttribute(A_FOR, value);
    }

    /** Row count of a text area */
    @NotNull
    public Element setRows(@Nullable final int value) {
        return setAttribute(A_ROWS, value);
    }

    /** Column count of a text area */
    @NotNull
    public Element setCols(@Nullable final Object value) {
        return setAttribute(A_COLS, value);
    }

    /** Column span inside the table */
    @NotNull
    public Element setColSpan(@Nullable final int value) {
        return setAttribute(A_COLSPAN, value);
    }

    /** Row span inside the table */
    @NotNull
    public Element setRowSpan(@Nullable final int value) {
        return setAttribute(A_ROWSPAN, value);
    }

    /** Set hyperlink reference */
    @NotNull
    public Element setHref(@Nullable final CharSequence value) {
        return setAttribute(A_HREF, value);
    }

    /** Set a placeholder attribute */
    @NotNull
    public Element setHint(@Nullable final CharSequence value) {
        return setAttribute(A_PLACEHOLDER, value);
    }

    /** Set a title attribute */
    @NotNull
    public Element setTitle(@Nullable final CharSequence value) {
        return setAttribute(A_TITLE, value);
    }

    /** Set a title attribute */
    @NotNull
    public Element setChecked(@Nullable final CharSequence value) {
        return setAttribute(A_CHECKED, value);
    }

    /** Sets the logical value for a CheckBox component. */
    public Element setCheckBoxValue(final boolean value) {
        return setValue(true).setChecked(value ? "" + value : null);
    }

    /** Add nested elements to the element. It is an alternative for try-with-resources. */
    @NotNull
    public ExceptionProvider nest(@NotNull final Consumer<Element> builder) {
        try {
            builder.accept(this);
            return ExceptionProvider.of();
        } catch (Exception e) {
            return ExceptionProvider.of(e);
        } finally {
            close();
        }
    }

    // ---- Static methods ----

    /** Create a root element */
    @NotNull
    public static Element createHtmlRoot(@NotNull final Object title, @Nullable final CharSequence... cssLinks) {
        return createHtmlRoot(title, null, cssLinks);
    }

    /** Create a root element */
    @NotNull
    public static Element createHtmlRoot(
            @NotNull final Object title,
            @Nullable final Charset charset,
            @Nullable final CharSequence... cssLinks) {
        var result = new Element(Html.HTML, XmlPrinter.forHtml(), 0);
        var head = result.addElement(HEAD);
        head.addElement(META).setAttribute(A_CHARSET, charset);
        head.addElement(TITLE).addText(title);

        if (cssLinks != null) {
            for (var cssLink : cssLinks) {
                head.addElement(LINK)
                        .setAttribute(A_HREF, cssLink)
                        .setAttribute(A_REL, "stylesheet");
            }
        }
        return result;
    }

    /** Overtype argument the Element or wrap it to a new instance */
    @NotNull
    public static Element of(@NotNull final ApiElement original) {
        if (original instanceof Element element) {
            return element;
        } else if (original instanceof AbstractHtmlElement htmlElement) {
            return htmlElement.original();
        } else if (original instanceof XmlBuilder builder) {
            return new Element(builder.getName(), builder.getWriter(), builder.getLevel(), false);
        }
        throw new IllegalArgumentException("Unsupported ApiElement type: " + original.getClass().getName());
    }
}