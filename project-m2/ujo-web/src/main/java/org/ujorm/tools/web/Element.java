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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.ujorm.tools.xml.model.XmlWriter;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.web.ao.Column;
import org.ujorm.tools.web.ao.WebUtils;
import org.ujorm.tools.xml.ApiElement;
import org.ujorm.tools.xml.builder.XmlBuilder;
import org.ujorm.tools.xml.model.XmlModel;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.ao.Injector;

/**
 * A HTML Element implements some methods for frequently used elements and attributes
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
public final class Element implements ApiElement<Element>, Html {

    /** No CSS styles */
    protected static final String[] NO_CSS = {};

    /** An original XML element */
    protected final ApiElement internalElement;

    /** New element for an API element
     * @see #of(org.ujorm.tools.xml.ApiElement)
     */
    Element(@NotNull final ApiElement original) {
        this.internalElement = original;
    }

    /** Returns the element name */
    @NotNull
    @Override
    public String getName() {
        return internalElement.getName();
    }

    /**
     * Set an attribute
     * @param name Required element name
     * @param value The {@code null} value is silently ignored. Formatting is performed by the
     *   {@link XmlWriter#writeValue(Object, ApiElement, String)}
     *   method, where the default implementation calls a {@code toString()} only.
     * @return The current element
     */
    @NotNull
    @Override
    public Element setAttribute(@NotNull final String name, @Nullable final Object value) {
        internalElement.setAttribute(name, value);
        return this;
    }

    /**
     * Set an attribute
     * @param name Required element name
     * @param separator Separator for joining values
     * @param value The {@code null} value is silently ignored. Formatting is performed by the
     * {@link XmlWriter#writeValue(Object, ApiElement, String)}
     * method, where the default implementation calls a {@code toString()} only.
     * @return The current element
     */
    @NotNull
    public Element setAttributes(
            @NotNull final String name,
            @NotNull final CharSequence separator,
            @NotNull final Object... value) {
        final String val = Stream.of(value)
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.joining(separator));
        internalElement.setAttribute(name, val);
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
     *   {@link XmlWriter#writeValue(Object, ApiElement, String)}
     *   method, where the default implementation calls a {@code toString()} only.
     * @return The current element
     */
    @NotNull
    public Element setAttr(@NotNull final String name, @Nullable final Object value) {
        return setAttribute(name, value);
    }

    /** Add simple text
     * @param data Text item
     * @return The current element
     * @see #addAnchor(String, CharSequence...)
     */
    @NotNull
    @Override
    public Element addText(final Object data) throws IllegalStateException {
        internalElement.addText(data);
        return this;
    }

    /**
     * Add many texts with <strong>no separator</strong>
     * @param data Text items
     * @return The current element
     * @see #addAnchor(String, CharSequence...)
     */
    @NotNull
    public Element addText(@NotNull final Object... data) throws IllegalStateException {
        return addTexts("", data);
    }

    /**
     * Add a template based text with parameters with high performance.
     *
     * @param template A message template with an ENGLISH locale.
     * See {@link String#format(String, Object...)}) for more parameters.
     * @param values A template parameters
     * @return The current element
     */
    @NotNull
    @Override
    public Element addTextTemplated(CharSequence template, Object... values) {
        internalElement.addTextTemplated(template, values);
        return this;
    }

    /**
     * Add many words separated by a delimiter
     * @param separator The delimiter must contain no special HTML character.
     * @param data Data to print
     * @return The current element
     * @throws IllegalStateException If an error occurs
     */
    public Element addTexts(
            @NotNull final CharSequence separator,
            @NotNull final Object... data)
            throws IllegalStateException {
        for (int i = 0; i < data.length; i++) {
            if (i > 0) {
                internalElement.addRawText(separator);
            }
            internalElement.addText(data[i]);
        }
        return this;
    }

    /** Add raw text
     * @return The current element */
    @NotNull
    @Override
    public Element addRawText(@Nullable final Object data) throws IllegalStateException {
        internalElement.addRawText(data);
        return this;
    }

    /** Add raw text items
     * @return The current element */
    @NotNull
    public Element addRawText(@NotNull final Object... data) throws IllegalStateException {
        for (Object item : data) {
            internalElement.addRawText(item);
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
                internalElement.addRawText(separator);
            }
            internalElement.addRawText(data[i]);
        }
        return this;
    }

    /** Add a comment
     * @return The current element */
    @NotNull
    @Override
    public Element addComment(CharSequence comment) throws IllegalStateException {
        internalElement.addComment(comment);
        return this;
    }

    /** Add CDATA block
     * @return The current element */
    @NotNull
    @Override
    public Element addCDATA(CharSequence charData) throws IllegalStateException {
        internalElement.addCDATA(charData);
        return this;
    }

    /** Close the element */
    @NotNull
    @Override
    public void close() throws IllegalStateException {
        internalElement.close();
    }

    // -------------- Add ELEMENT -----

    /**
     * Create new Element
     * @param name The element name
     * @return A new nested element
     * @throws IllegalStateException An envelope for IO exceptions
     */
    @Override @NotNull
    public Element addElement(@NotNull final String name) throws IllegalStateException {
        return new Element(internalElement.addElement(name));
    }

    /**
     * Add a new Element with optional CSS classes
     * @param name A required name of the element
     * @param cssClasses Optional CSS classes.
     * @return A new nested element
     */
    @NotNull
    public Element addElement(@NotNull final String name, @NotNull final CharSequence... cssClasses) {
        return switch (name) {
            case HEAD -> addHead(cssClasses);
            case BODY -> addBody(cssClasses);
            default -> addElement(name).setClass(cssClasses);
        };
    }

    /**
     * Add an element according to a condition.
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

    /** Add new Table
     * @return A new table element */
    @NotNull
    public Element addTable(@NotNull final CharSequence... cssClasses) {
        return addElement(TABLE, cssClasses);
    }

    /** Create a HTML table according to data
     * @return A new table element */
    @NotNull
    public Element addTable(
            @NotNull final Object[][] data,
            @NotNull final CharSequence... cssClass) {
        return addTable(Arrays.asList(data), cssClass);
    }

    /** Create a HTML table according to data
     * @return A new table element */
    @NotNull
    public Element addTable(
            @NotNull final Collection<Object[]> data,
            @NotNull final CharSequence... cssClass) {
        final var result = addTable(cssClass);
        for (final Object[] rowValue : data) {
            if (rowValue != null) {
                final var rowElement = result.addElement(Html.TR);
                for (final Object value : rowValue) {
                    rowElement.addElement(Html.TD).addText(value);
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
     *         Car::getId,
     *         Car::getName,
     *         Car::getEnabled);
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

        final var result = addTable(cssClass != null ? cssClass : NO_CSS);
        if (Check.hasLength(headers)) {
            final var rowElement = result.addElement(Html.THEAD).addElement(Html.TR);
            for (Object value : headers) {
                var th = rowElement.addElement(Html.TH);
                if (value instanceof Injector injector) {
                    injector.write(th);
                } else {
                    th.addText(value);
                }
            }
        }
        try (var tBody = result.addElement(TBODY)) {
            final boolean hasRenderer = WebUtils.isType(Column.class, attributes);
            domains.forEach(value -> {
                final var rowElement = tBody.addElement(Html.TR);
                for (var attribute : attributes) {
                    final var td = rowElement.addElement(Html.TD);
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

    /** Add an image
     * @return A new image element */
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

    /** Add new head element
     * @return A new head element */
    @NotNull
    public Element addHead(@NotNull final CharSequence... cssClasses) {
        return addElement(HEAD).setClass(cssClasses);
    }

    /** Add new body element
     * @return A new body element */
    @NotNull
    public Element addBody(@NotNull final CharSequence... cssClasses) {
        return addElement(BODY).setClass(cssClasses);
    }

    /** Add new title element
     * @return A new title element */
    @NotNull
    public Element addTitle(@NotNull final CharSequence... cssClasses) {
        return addElement(TITLE, cssClasses);
    }

    /** Add new link element
     * @return A new link element */
    @NotNull
    public Element addLink(@NotNull final CharSequence... cssClasses) {
        return addElement(LINK, cssClasses);
    }

    /** Add new style element
     * @return A new style element */
    @NotNull
    public Element addStyle(@NotNull final CharSequence... cssClasses) {
        return addElement(STYLE, cssClasses);
    }

    /** Add new script element
     * @return A new script element */
    @NotNull
    public Element addScript(@NotNull final CharSequence... cssClasses) {
        return addElement(SCRIPT, cssClasses);
    }

    /** Add new div element
     * @return A new div element */
    @NotNull
    public Element addDiv(@NotNull final CharSequence... cssClasses) {
        return addElement(DIV, cssClasses);
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

    /** Add new pre element
     * @return A new preformatted element */
    @NotNull
    public Element addPreformatted(@NotNull final CharSequence... cssClasses) {
        return addElement(PRE, cssClasses);
    }

    /** Add new span element
     * @return A new span element */
    @NotNull
    public Element addSpan(@NotNull final CharSequence... cssClasses) {
        return addElement(SPAN, cssClasses);
    }

    /** Add new paragraph element
     * @return A new paragraph element */
    @NotNull
    public Element addParagraph(@NotNull final CharSequence... cssClasses) {
        return addElement(P, cssClasses);
    }

    /** Add new form element
     * @return A new form element */
    @NotNull
    public Element addForm(@NotNull final CharSequence... cssClasses) {
        return addElement(FORM, cssClasses);
    }

    /** Add a top heading (level one)
     * @return A new heading element */
    @NotNull
    public Element addHeading(@NotNull CharSequence title, @NotNull final CharSequence... cssClasses) {
        return addHeading(1, title, cssClasses);
    }

    /** Add new heading with the required level where the first level is the one,
     * @return A new heading element */
    @NotNull
    public Element addHeading(int level, @NotNull CharSequence title, @NotNull final CharSequence... cssClasses) {
        return addHeadingX(level, cssClasses).addText(title);
    }

    /** Add new heading with the required level where the first level is the one,
     * @return A new heading element */
    @NotNull
    public Element addHeadingX(int level, @NotNull final CharSequence... cssClasses) {
        Assert.isTrue(level > 0, "Unsupported level {}", level);
        return addElement(HEADING_PREFIX + level, cssClasses);
    }

    /** Add new head of table element
     * @return A new table head element */
    @NotNull
    public Element addTableHead(@NotNull final CharSequence... cssClasses) {
        return addElement(THEAD, cssClasses);
    }

    /** Add new head of table element
     * @return A new table body element */
    @NotNull
    public Element addTableBody(@NotNull final CharSequence... cssClasses) {
        return addElement(TBODY, cssClasses);
    }

    /** Add new table row element
     * @return A new table row element */
    @NotNull
    public Element addTableRow(@NotNull final CharSequence... cssClasses) {
        return addElement(TR, cssClasses);
    }

    /** Add new detail of table element
     * @return A new table detail element */
    @NotNull
    public Element addTableDetail(@NotNull final CharSequence... cssClasses) {
        return addElement(TD, cssClasses);
    }

    /** Add new label element
     * @return A new label element */
    @NotNull
    public Element addLabel(@NotNull final CharSequence... cssClasses) {
        return addElement(LABEL, cssClasses);
    }

    /** Add new input element
     * @return A new input element */
    @NotNull
    public Element addInput(@NotNull final CharSequence... cssClasses) {
        return addElement(INPUT, cssClasses);
    }

    /** Add new input element type of text
     * @return A new text input element */
    @NotNull
    public Element addTextInput(@NotNull final CharSequence... cssClasses) {
        return addInput(cssClasses).setType(V_TEXT);
    }

    /** Add new input element type of text including attributes: name, value, placeholder and title
     * @return A new text input element */
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

    /** Add a new password input element
     * @return A new password input element */
    @NotNull
    public Element addPasswordInput(@NotNull final CharSequence... cssClasses) {
        return addInput(cssClasses).setType(V_PASSWORD);
    }

    /** Add a new hidden input element with a name &amp; value
     * @return A new hidden input element */
    @NotNull
    public Element addHiddenInput(
            @Nullable final CharSequence name,
            @Nullable final Object value) {
        return addInput().setType(V_HIDDEN).setNameValue(name, value);
    }

    /** Add new text area element
     * @return A new text area element */
    @NotNull
    public Element addTextArea(@NotNull final CharSequence... cssClasses) {
        return addElement(TEXT_AREA, cssClasses);
    }

    /** Add new select element
     * @return A new select element
     * @see #addSelectOptions(java.lang.Object, java.util.Map, java.lang.CharSequence...)
     */
    @NotNull
    public Element addSelect(@NotNull final CharSequence... cssClasses) {
        return addElement(SELECT, cssClasses);
    }

    /** Add options from map to current select element
     * @param value Value of a select element
     * @param options Consider an instance of the {@link LinkedHashMap} class for predictable iteration order.
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

    /** Add new option element
     * @return A new option element */
    @NotNull
    public Element addOption(@NotNull final CharSequence... cssClasses) {
        return addElement(OPTION, cssClasses);
    }

    /** Add new button element
     * @return A new button element */
    @NotNull
    public Element addButton(@NotNull final CharSequence... cssClasses) {
        return addElement(BUTTON, cssClasses);
    }

    /** Add a submit button
     * @return A new button element */
    @NotNull
    public Element addSubmitButton(@NotNull final CharSequence... cssClasses) {
        return addButton(cssClasses).setType(V_SUBMIT);
    }

    /** Add an anchor element with URL and CSS classes
     * @return A new anchor element */
    @NotNull
    public Element addAnchor(@NotNull final String url, @NotNull final CharSequence... cssClasses) {
        return addElement(A, cssClasses).setHref(url);
    }

    /**
     * Add a linked text
     * @param url URL
     * @param text Text items
     * @return The original element
     */
    @NotNull
    public Element addLinkedText(@NotNull final String url, @NotNull final Object... text) {
        addElement(A).setHref(url).addTexts("", text);
        return this;
    }

    /** Add new unordered list element
     * @return A new unordered list element */
    @NotNull
    public Element addUnorderedlist(@NotNull final CharSequence... cssClasses) {
        return addElement(UL, cssClasses);
    }

    /** Add new ordered list element
     * @return A new ordered list element */
    @NotNull
    public Element addOrderedList(@NotNull final CharSequence... cssClasses) {
        return addElement(OL, cssClasses);
    }

    /** Add a list item element
     * @return A new list item element */
    @NotNull
    public Element addListItem(@NotNull final CharSequence... cssClasses) {
        return addElement(LI, cssClasses);
    }

    /** Set a CSS class attribute optionally, the empty attribute is ignored.
     * @param cssClasses Optional CSS classes. The css item is ignored when the value is empty or {@code null}.
     * @return The current element
     */
    @NotNull
    public Element setClass(@NotNull final CharSequence... cssClasses) {
        if (Check.hasLength(cssClasses)) {
            var builder = new StringJoiner(" ");
            for (var cssClass : cssClasses) {
                if (Check.hasLength(cssClass)) {
                    builder.add(cssClass);
                }
            }
            var result = builder.toString();
            if (!result.isEmpty()) {
                setAttribute(A_CLASS, result);
            }
        }
        return this;
    }

    /** Add a line break
     * @return A new break element */
    @NotNull
    public Element addBreak(@NotNull final CharSequence... cssClasses) {
        return addElement(BREAK, cssClasses);
    }

    // ---- Static methods ----

    /** Create a root element
     * @param title HTML title
     * @param cssLinks Nullable CSS link array
     * @return A new root element
     */
    @NotNull
    public static Element createHtmlRoot(@NotNull final Object title, @Nullable final CharSequence... cssLinks) {
        return createHtmlRoot(title, null, cssLinks);
    }

    /** Create a root element
     * @param title A HTML title
     * @param charset A charset
     * @param cssLinks Nullable CSS link array
     * @return A new root element
     */
    @NotNull
    public static Element createHtmlRoot(
            @NotNull final Object title,
            @Nullable final Charset charset,
            @Nullable final CharSequence... cssLinks) {
        var result = new XmlModel(HTML);
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
        return new Element(result);
    }

    /** Set an identifier of the element
     * @return The current element */
    @NotNull
    public Element setId(@Nullable final CharSequence value) {
        return setAttribute(A_ID, value);
    }

    /** Set a method of form
     * @return The current element */
    @NotNull
    public Element setMethod(@Nullable final Object value) {
        return setAttribute(A_METHOD, value);
    }

    /** Set an action type of form
     * @return The current element */
    @NotNull
    public Element setAction(@Nullable final Object value) {
        return setAttribute(A_ACTION, value);
    }

    /** Set a type of input element
     * @return The current element */
    @NotNull
    public Element setType(@Nullable final Object value) {
        return setAttribute(A_TYPE, value);
    }

    /** Set an name of input element
     * @return The current element */
    @NotNull
    public Element setName(@Nullable final CharSequence value) {
        return setAttribute(A_NAME, value);
    }

    /** Set a value of input element.
     * @return The current element
     * @see #setCheckBoxValue(boolean) */
    @NotNull
    public Element setValue(@Nullable final Object value) {
        return setAttribute(A_VALUE, value);
    }

    /** Set name &amp; value to the input element
     * @return The current element */
    @NotNull
    public Element setNameValue(@Nullable final CharSequence name, @Nullable final Object value) {
        return setName(name).setValue(value);
    }

    /** Set a for attribute
     * @return The current element */
    @NotNull
    public Element setFor(@Nullable final CharSequence value) {
        return setAttribute(A_FOR, value);
    }

    /** Row count of a text area
     * @return The current element */
    @NotNull
    public Element setRows(@Nullable final int value) {
        return setAttribute(A_ROWS, value);
    }

    /** Column count of a text area
     * @return The current element */
    @NotNull
    public Element setCols(@Nullable final Object value) {
        return setAttribute(A_COLS, value);
    }

    /** Column span inside the table
     * @return The current element */
    @NotNull
    public Element setColSpan(@Nullable final int value) {
        return setAttribute(A_COLSPAN, value);
    }

    /** Row span inside the table
     * @return The current element */
    @NotNull
    public Element setRowSpan(@Nullable final int value) {
        return setAttribute(A_ROWSPAN, value);
    }

    /** Set hyperlink reference
     * @return The current element */
    @NotNull
    public Element setHref(@Nullable final CharSequence value) {
        return setAttribute(A_HREF, value);
    }

    /** Set a placeholder attribute
     * @return The current element */
    @NotNull
    public Element setHint(@Nullable final CharSequence value) {
        return setAttribute(A_PLACEHOLDER, value);
    }


    /** Set a title attribute
     * @return The current element */
    @NotNull
    public Element setTitle(@Nullable final CharSequence value) {
        return setAttribute(A_TITLE, value);
    }

    /**
     * Set a title attribute
     * @param value If the value is {@code null}, the `all` attribute is ignored.
     * @return The current element
     */
    @NotNull
    public Element setChecked(@Nullable final CharSequence value) {
        return setAttribute(A_CHECKED, value);
    }

    /**
     * Sets the logical value for a CheckBox component.
     * <p> This method is designed specifically for CheckBoxes to handle the HTML implementation details.
     * It sets the element's {@code value} attribute to {@code "true"} (ensuring the value is sent
     * in the request) and toggles the {@code checked} attribute based on the provided boolean parameter.
     * </p>
     * @param value The logical value. If {@code true}, the checkbox is marked as checked.
     * @return The current element for method chaining.
     * @see #addCheckBox(CharSequence, CharSequence...)
     */
    public Element setCheckBoxValue(final boolean value) {
        return setValue(true).setChecked(value ? "" + value : null);
    }

    /** Apply body of element by a lambda expression.
     *
     * @deprecated Use the method {@link #next(Consumer)} instead.
     */
    @Deprecated
    @NotNull
    public ExceptionProvider then(@NotNull final Consumer<Element> builder) {
        return next(builder);
    }

    /** Add nested elements to the element.
     *
     * <h4>Usage</h4>
     *
     * <pre class="pre">
     *  HtmlElement.of(config, writer).addBody()
     *      .next(body -> {
     *         body.addHeading(config.getTitle());
     *      })
     *      .catche(e -> {
     *          logger.log(Level.SEVERE, "An error", e);
     *      });
     * </pre>
     */
    @NotNull
    public ExceptionProvider next(@NotNull final Consumer<Element> builder) {
        try {
            builder.accept(this);
            return ExceptionProvider.of();
        } catch (Throwable e) {
            return ExceptionProvider.of(e);
        } finally {
            close();
        }
    }

    /** String value */
    @Override
    @NotNull
    public String toString() {
        return internalElement.toString();
    }

    /** New element for an API element
     * @return A new element wrapper */
    @NotNull
    public static Element of(@NotNull final ApiElement original) {
        return (original instanceof Element element)
                ? element
                : new Element(original);
    }
}