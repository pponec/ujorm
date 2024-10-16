/*
 * Copyright 2018-2022 Pavel Ponec, https://github.com/pponec
 * https://github.com/pponec/ujorm/blob/master/samples/servlet/src/main/java/org/ujorm/ujoservlet/tools/Html.java
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

import javax.sql.rowset.spi.XmlWriter;
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
 * <h3>Usage</h3>
 *
 * <pre class="pre">
 *    ServletResponse response = new ServletResponse();
 *    try (HtmlElement html = HtmlElement.of(response)) {
 *        try (Element body = html.getBody()) {
 *            body.addHeading("Hello!");
 *        }
 *    }
 *    assertTrue(response.toString().contains("&lt;h1&gt;Hello!&lt;/h1&gt;"));
 * </pre>
 *
 * @see HtmlElement#of(org.ujorm.tools.xml.config.HtmlConfig)
 */
public final class Element implements ApiElement<Element>, Html {

    /** An original XML element */
    final ApiElement internalElement;

    /** New element for an API element
     * @see #of(org.ujorm.tools.xml.ApiElement)
     */
    Element(@NotNull final ApiElement original) {
        this.internalElement = original;
    }

    @NotNull
    @Override
    public String getName() {
        return internalElement.getName();
    }

    /**
     * Set an attribute
     * @param name Required element name
     * @param value The {@code null} value is silently ignored. Formatting is performed by the
     *   {@link XmlWriter#writeValue(java.lang.Object, org.ujorm.tools.model.XmlModel, java.lang.String, java.io.Writer) }
     *   method, where the default implementation calls a {@code toString()} only.
     * @return The original element
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
     * @param value The {@code null} value is silently ignored. Formatting is performed by the
     *   {@link XmlWriter#writeValue(java.lang.Object, org.ujorm.tools.model.XmlModel, java.lang.String, java.io.Writer) }
     *   method, where the default implementation calls a {@code toString()} only.
     * @return The original element
     */
    @NotNull
    public Element setAttributes(
            @NotNull final String name,
            @NotNull final CharSequence separator,
            @NotNull final Object... value) {
        final String val = Stream.of(value)
                .filter(Objects::nonNull)
                .map(v -> v.toString())
                .collect(Collectors.joining(separator));
        internalElement.setAttribute(name, val);
        return this;
    }

    /**
     * Set an attribute with no value
     * @param name Required element name
     * @return The original element
     */
    @NotNull
    public Element setAttribute(@NotNull final String name) {
        return setAttribute(name, "");
    }

    /**
     * An deprecated shortcut for the method {@link #setAttribute(java.lang.String, java.lang.Object) }.
     * @param name Required element name
     * @param value The {@code null} value is silently ignored. Formatting is performed by the
     *   {@link XmlWriter#writeValue(java.lang.Object, org.ujorm.tools.model.XmlModel, java.lang.String, java.io.Writer) }
     *   method, where the default implementation calls a {@code toString()} only.
     * @return The original element
     */

    /**
     * A shortcut for the method {@link #setAttribute(java.lang.String, java.lang.Object) }.
     * @param name Required element name
     * @param value The {@code null} value is silently ignored. Formatting is performed by the
     *   {@link XmlWriter#writeValue(java.lang.Object, org.ujorm.tools.model.XmlModel, java.lang.String, java.io.Writer) }
     *   method, where the default implementation calls a {@code toString()} only.
     * @return The original element
     */
    @NotNull
    public Element setAttr(@NotNull final String name, @Nullable final Object value) {
        return setAttribute(name, value);
    }

    /** Add simple text
     * @param data Text item
     * @return A parent element.
     * @see #addAnchoredText(java.lang.String, java.lang.Object...)
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
     * @return A parent element.
     * @see #addAnchoredText(java.lang.String, java.lang.Object...)
     */
    @NotNull
    public Element addText(@NotNull final Object... data) throws IllegalStateException {
        return addTexts("", data);
    }

    /**
     * Add a template based text with parameters with hight performance.
     *
     * @param template A message template with an ENGLISH locale. See {@link String#format(java.lang.String, java.lang.Object...) for more parameters.
     * @param values A template parameters
     * @return A parent element.
     */
    @NotNull
    @Override
    public Element addTextTemplated(CharSequence template, Object... values) {
        internalElement.addTextTemplated(template, values);
        return this;
    }

    /**
     * Add many words separated by a delimeter
     * @param separator The delimiter must contain no special HTML character.
     * @param data Data to print
     * @return The current element
     * @throws IllegalStateException
     */
    public Element addTexts(
            @NotNull final CharSequence separator,
            @NotNull final Object... data)
            throws IllegalStateException {
        for (int i = 0, max = data.length; i < max; i++) {
            if (i > 0) {
                internalElement.addRawText(separator);
            }
            internalElement.addText(data[i]);
        }
        return this;
    }

    @NotNull
    @Override
    public Element addRawText(@Nullable final Object data) throws IllegalStateException {
        internalElement.addRawText(data);
        return this;
    }

    @NotNull
    public Element addRawText(@NotNull final Object... data) throws IllegalStateException {
        for (Object item : data) {
            internalElement.addRawText(item);
        }
        return this;
    }

    /**
     * Add many words separated by a delimeter
     * @param separator The delimiter must contain no special HTML character.
     * @param data Data to print
     * @return The current element
     * @throws IllegalStateException
     */
    public Element addRawTexts(
            @NotNull final CharSequence separator,
            @NotNull final Object... data)
            throws IllegalStateException {
        for (int i = 0, max = data.length; i < max; i++) {
            if (i > 0) {
                internalElement.addRawText(separator);
            }
            internalElement.addRawText(data[i]);
        }
        return this;
    }

    @NotNull
    @Override
    public Element addComment(CharSequence comment) throws IllegalStateException {
        internalElement.addComment(comment);
        return this;
    }

    @NotNull
    @Override
    public Element addCDATA(CharSequence charData) throws IllegalStateException {
        internalElement.addCDATA(charData);
        return this;
    }

    @NotNull
    @Override
    public void close() throws IllegalStateException {
        internalElement.close();
    }

    // -------------- Add ELEMENT -----

    /**
     * Create new Element
     * @param name The element name
     * @return New instance of the Element
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
     * @return New instance of the Element
     */
    @NotNull
    public Element addElement(@NotNull final String name, @NotNull final CharSequence... cssClasses) {
        return addElement(name).setClass(cssClasses);
    }

    /**
     * Add an element according to a condition.
     * @param enabled A condition for rendering the element.
     * @param name An element name
     * @param cssClasses CSS classes
     * @return New instance of the Element
     */
    @NotNull
    public Element addElementIf(final boolean enabled,
                                @NotNull final String name,
                                @NotNull final CharSequence... cssClasses) {
        return addElement(enabled ? name : XmlBuilder.HIDDEN_NAME).setClass(cssClasses);
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
        final Element result = addTable(cssClass);
        for (final Object[] rowValue : data) {
            if (rowValue != null) {
                final Element rowElement = result.addElement(Html.TR);
                for (final Object value : rowValue) {
                    rowElement.addElement(Html.TD).addText(value);
                }
            }
        }
        return result;
    }

    /** Create a HTML table according to data
     *
     * <h3>Usage</h3>
     * <pre>
     * element.addTable(getCars(), cssClasses, titles,
     *         Car::getId,
     *         Car::getName,
     *         Car::getEnabled);
     * </pre>
     */
    @NotNull
    public <D,V> Element addTable(
            @NotNull final Stream<D> domains,
            @Nullable final CharSequence[] cssClass,
            @Nullable final Object[] headers,
            @NotNull final Function<D,V>... attributes) {

        final Element result = addTable(cssClass != null ? cssClass : new String[0]);
        if (Check.hasLength(headers)) {
            final Element rowElement = result.addElement(Html.THEAD).addElement(Html.TR);
            for (Object value : headers) {
                Element th = rowElement.addElement(Html.TH);
                if (value instanceof Injector) {
                    ((Injector)value).write(th);
                } else {
                    th.addText(value);
                }
            }
        }
        try (Element tBody = result.addElement(TBODY)) {
            final boolean hasRenderer = WebUtils.isType(Column.class, attributes);
            domains.forEach(value -> {
                final Element rowElement = tBody.addElement(Html.TR);
                for (Function<D, V> attribute : attributes) {
                    final Element td = rowElement.addElement(Html.TD);
                    if (hasRenderer && attribute instanceof Column) {
                        ((Column)attribute).write(td, value);
                    } else {
                        td.addText(attribute.apply(value));
                    }
                }
            });
        }
        return result;
    }

    /**
     * Add a link to an image
     * @param imageLink A link to image
     * @param alt An alternate text
     * @param cssClasses Optional CSS classes
     * @return
     * @throws IllegalStateException
     */
    @NotNull
    public Element addImage(
            @NotNull final CharSequence imageLink,
            @NotNull final CharSequence alt,
            @NotNull final CharSequence... cssClasses)
            throws IllegalStateException {
        return addElement(IMAGE, cssClasses)
                .setAttribute(A_ALT, alt)
                .setAttribute(A_SRC, imageLink);
    }

    /**
     * Add an embeded image
     * @param imageStream Stream provides a PNG image and it will be closed after reading.
     * @param alt An alternate text
     * @param cssClasses Optional CSS classes
     * @return
     * @throws IllegalStateException
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

    /** Create a content of an embeded image */
    @NotNull
    private CharSequence createEmbededImage(
            @NotNull final InputStream imageStream,
            @NotNull final StringBuilder result) {
        final int bufferSize = 3 * 1024;
        final Base64.Encoder encoder = Base64.getEncoder();
        try (BufferedInputStream in = new BufferedInputStream(imageStream)) {
            result.append("data:image/png;base64,");
            byte[] chunk = new byte[bufferSize];
            int len = 0;
            while ((len = in.read(chunk)) == bufferSize) {
                result.append(encoder.encodeToString(chunk));
            }
            if (len > 0) {
                chunk = Arrays.copyOf(chunk, len);
                result.append(encoder.encodeToString(chunk));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return result;
    }

    /** Add new body element */
    @NotNull
    public Element addBody(@NotNull final CharSequence... cssClasses) {
        return addElement(BODY, cssClasses);
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

    /** Add new div element */
    @NotNull
    public Element addDiv(@NotNull final CharSequence... cssClasses) {
        return addElement(DIV, cssClasses);
    }

    /** Add new fieldset element including a title
     * @param title An optional title
     * @param cssClasses CSS classes
     * @return An instance of FieldSet
     * @see LEGEND
     */
    @NotNull
    public Element addFieldset(@Nullable final String title, @NotNull final CharSequence... cssClasses) {
        final Element result = addElement(FIELDSET, cssClasses);
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

    /** Add new span element */
    @NotNull
    public Element addSpan(@NotNull final CharSequence... cssClasses) {
        return addElement(SPAN, cssClasses);
    }

    /** Add new paragram element */
    @NotNull
    public Element addParagraph(@NotNull final CharSequence... cssClasses) {
        return addElement(P, cssClasses);
    }

    /** Add new form element */
    @NotNull
    public Element addForm(@NotNull final CharSequence... cssClasses) {
        return addElement(FORM, cssClasses);
    }

    /** Add a top heading (level one)  */
    @NotNull
    public Element addHeading(@NotNull CharSequence title, @NotNull final CharSequence... cssClasses) {
        return addHeading(1, title, cssClasses);
    }

    /** Add new heading with the required level where the first level is the one,  */
    @NotNull
    public Element addHeading(int level, @NotNull CharSequence title, @NotNull final CharSequence... cssClasses) {
        Assert.isTrue(level > 0, "Unsupported level {}", level);
        return addElement(HEADING_PREFIX + level, cssClasses).addText(title);
    }

    /** Add new head of table element */
    @NotNull
    public Element addTableHead(@NotNull final CharSequence... cssClasses) {
        return addElement(THEAD, cssClasses);
    }

    /** Add new table row element */
    @NotNull
    public Element addTableRow(@NotNull final CharSequence... cssClasses) {
        return addElement(TR, cssClasses);
    }

    /** Add new detail of table element */
    @NotNull
    public Element addTableDetail(@NotNull final CharSequence... cssClasses) {
        return addElement(TD, cssClasses);
    }

    /** Add new label element */
    @NotNull
    public Element addLabel(@NotNull final CharSequence... cssClasses) {
        return addElement(LABEL, cssClasses);
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

    /** Add new input element type of text including attributes: name, value, placeholder and title */
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

    /** Add new select element
     * @see #addSelectOptions(java.lang.Object, java.util.Map, java.lang.CharSequence...)
     */
    @NotNull
    public Element addSelect(@NotNull final CharSequence... cssClasses) {
        return addElement(SELECT, cssClasses);
    }

    /** Add options from map to current select element
     * @param value Value of a select element
     * @param options Consider an instance of the {@link LinkedHashMap} class predictable iteration order of options.
     * @param cssClasses
     * @return Return {@code this}
     * @see #addSelect(java.lang.CharSequence...)
     */
    @NotNull
    public Element addSelectOptions(
            @NotNull Object value,
            @NotNull final Map<?,?> options,
            @NotNull final CharSequence... cssClasses) {
        for (Object key : options.keySet()) {
            this.addElement(Html.OPTION)
                    .setAttribute(Html.A_VALUE, key)
                    .setAttribute(Html.A_SELECTED, Objects.equals(value, key) ? Html.A_SELECTED : null)
                    .addText(options.get(key));
        }
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
        final Element result = addButton(cssClasses);
        return result.setType(V_SUBMIT);
    }

    /** Add an anchor element with URL and CSS classes */
    @NotNull
    public Element addAnchor(@NotNull final String url, @NotNull final CharSequence... cssClasses) {
        final Element result = addElement(A, cssClasses);
        return result.setHref(url);
    }

    /**
     * Add a
     * @param url
     * @param text
     * @return The original element!
     */
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

    @NotNull
    public Element addListItem(@NotNull final CharSequence... cssClasses) {
        return addElement(LI, cssClasses);
    }

    /** Set a CSS class attribute optionally, the empty attribute is ignored.
     * @param cssClasses Optional CSS classes. The css item is ignored when the value is empty or {@code null}.
     * @return The current instanlce
     */
    @NotNull
    public Element setClass(@NotNull final CharSequence... cssClasses) {
        if (Check.hasLength(cssClasses)) {
            final StringJoiner builder = new StringJoiner(" ");
            for (CharSequence cssClass : cssClasses) {
                if (Check.hasLength(cssClass)) {
                    builder.add(cssClass);
                }
            }
            final String result = builder.toString();
            if (Check.hasLength(result)) {
                setAttribute(A_CLASS, result);
            }
        }
        return this;
    }

    /** Add a line break */
    @NotNull
    public Element addBreak(@NotNull final CharSequence... cssClasses) {
        return addElement(BR, cssClasses);
    }

    // ---- Static methods ----

    /** Crate a root element
     * @param cssLinks Nullable CSS link array
     */
    @NotNull
    public static Element createHtmlRoot(@NotNull final Object title, @Nullable final CharSequence... cssLinks) {
        return createHtmlRoot(title, null, cssLinks);
    }


    /** Crate a root element
     * @param title A HTML title
     * @param charset A charset
     * @param cssLinks Nullable CSS link array
     */
    @NotNull
    public static Element createHtmlRoot(
            @NotNull final Object title,
            @Nullable final Charset charset,
            @Nullable final CharSequence... cssLinks) {
        XmlModel result = new XmlModel(HTML);
        XmlModel head = result.addElement(HEAD);
        head.addElement(META).setAttribute(A_CHARSET, charset);
        head.addElement(TITLE).addText(title);

        if (cssLinks != null) {
            for (CharSequence cssLink : cssLinks) {
                head.addElement(LINK)
                        .setAttribute(A_HREF, cssLink)
                        .setAttribute(A_REL, "stylesheet");
            }
        }
        return new Element(result);
    }

    /** Set an identifier of the element */
    @NotNull
    public Element setId(@Nullable final CharSequence value) {
        setAttribute(A_ID, value);
        return this;
    }

    /** Set a method of form */
    @NotNull
    public Element setMethod(@Nullable final Object value) {
        setAttribute(A_METHOD, value);
        return this;
    }

    /** Set an action type of from */
    @NotNull
    public Element setAction(@Nullable final Object value) {
        setAttribute(A_ACTION, value);
        return this;
    }

    /** Set a type of input element */
    @NotNull
    public Element setType(@Nullable final Object value) {
        setAttribute(A_TYPE, value);
        return this;
    }

    /** Set an name of input element */
    @NotNull
    public Element setName(@Nullable final CharSequence value) {
        setAttribute(A_NAME, value);
        return this;
    }

    /** Set an value of input element */
    @NotNull
    public Element setValue(@Nullable final Object value) {
        setAttribute(A_VALUE, value);
        return this;
    }

    /** Set name &amp; value to the input element */
    @NotNull
    public Element setNameValue(@Nullable final CharSequence name, @Nullable final Object value) {
        return setName(name).setValue(value);
    }

    /** Set an value of input element */
    @NotNull
    public Element setFor(@Nullable final CharSequence value) {
        setAttribute(A_FOR, value);
        return this;
    }

    /** Row count of a text area */
    @NotNull
    public Element setRows(@Nullable final int value) {
        setAttribute(A_ROWS, value);
        return this;
    }

    /** Column count of a text area */
    @NotNull
    public Element setCols(@Nullable final Object value) {
        setAttribute(A_COLS, value);
        return this;
    }

    /** Column span inside the table */
    @NotNull
    public Element setColSpan(@Nullable final int value) {
        setAttribute(A_COLSPAN, value);
        return this;
    }

    /** Row span inside the table */
    @NotNull
    public Element setRowSpan(@Nullable final int value) {
        setAttribute(A_ROWSPAN, value);
        return this;
    }

    /** Set hyperlink reference */
    @NotNull
    public Element setHref(@Nullable final CharSequence value) {
        setAttribute(A_HREF, value);
        return this;
    }

    /** Set a placeholder name */
    @NotNull
    public Element setHint(@Nullable final CharSequence value) {
        setAttribute(A_PLACEHOLDER, value);
        return this;
    }

    /** Apply body of element by a lambda expression.
     *
     * @deprecated Use the method {@link #next(Consumer)} rather.
     */
    @NotNull
    public ExceptionProvider then(@NotNull final Consumer<Element> builder) {
        return next(builder);
    }

    /** Add nested elements to the element.
     *
     * <h3>Usage</h3>
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

    /** New element for an API element */
    @NotNull
    public static Element of(@NotNull final ApiElement original) {
        return (original instanceof Element)
            ? (Element) original
            : new Element(original);
    }
}
