/*
 * Copyright 2018-2018 Pavel Ponec, https://github.com/pponec
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.sql.rowset.spi.XmlWriter;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.web.ao.Column;
import org.ujorm.tools.web.ao.WebUtils;
import org.ujorm.tools.xml.ApiElement;
import org.ujorm.tools.xml.model.XmlModel;
import static org.ujorm.tools.web.Html.LEGEND;
import org.ujorm.tools.web.ao.HttpParameter;
import org.ujorm.tools.web.ao.Injector;

/**
 * A HTML Element implements some methods for frequently used elements and attributes
 *
 * <h3>Usage</h3>
 *
 * <pre class="pre">
 *    MockServletResponse response = new MockServletResponse();
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
    protected final ApiElement internalElement;

    /** New element for an API element
     * @see #of(org.ujorm.tools.xml.ApiElement)
     */
    protected Element(@Nonnull final ApiElement original) {
        this.internalElement = original;
    }

    @Nonnull
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
    @Nonnull
    @Override
    public final Element setAttribute(@Nonnull final String name, @Nullable final Object value) {
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
    @Nonnull
    public final Element setAttributes(@Nonnull final String name, @Nonnull final CharSequence separator, @Nonnull final Object... value) {
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
    @Nonnull
    public final Element setAttribute(@Nonnull final String name) {
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
    @Deprecated
    @Nonnull
    @Override
    public final Element setAttrib(@Nonnull final String name, @Nullable final Object value) {
        return setAttribute(name, value);
    }

    /**
     * A shortcut for the method {@link #setAttribute(java.lang.String, java.lang.Object) }.
     * @param name Required element name
     * @param value The {@code null} value is silently ignored. Formatting is performed by the
     *   {@link XmlWriter#writeValue(java.lang.Object, org.ujorm.tools.model.XmlModel, java.lang.String, java.io.Writer) }
     *   method, where the default implementation calls a {@code toString()} only.
     * @return The original element
     */
    @Nonnull
    public final Element setAttr(@Nonnull final String name, @Nullable final Object value) {
        return setAttribute(name, value);
    }

    /** Add simple text
     * @param data Text item
     * @return A parent element.
     * @see #addAnchoredText(java.lang.String, java.lang.Object...)
     */
    @Nonnull
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
    @Nonnull
    public Element addText(@Nonnull final Object... data) throws IllegalStateException {
        return addTexts("", data);
    }

    /**
     * Use the method {@link  #addTextTemplated(java.lang.String, java.lang.Object...) } raher.
     *
     * @param template A message template with an ENGLISH locale. See {@link String#format(java.lang.String, java.lang.Object...) for more parameters.
     * @param data A template parameters
     * @return A parent element.
     */
    @Deprecated
    @Nonnull
    public Element addTemplate(@Nonnull final String template, @Nonnull final Object... data)
            throws IllegalStateException {
        return addText(String.format(Locale.ENGLISH, template, data));
    }

    /**
     * Add a template based text with parameters with hight performance.
     *
     * @param template A message template with an ENGLISH locale. See {@link String#format(java.lang.String, java.lang.Object...) for more parameters.
     * @param values A template parameters
     * @return A parent element.
     */
    @Nonnull
    @Override
    public Element addTextTemplated(CharSequence template, Object... values) {
        internalElement.addTextTemplated(template, values);
        return this;
    }

    /**
     * Use the the method {@link #addTexts(java.lang.CharSequence, java.lang.Object...) } rather;
     * @param separator The delimiter must contain no special HTML character.
     * @param data Data to print
     * @return The current element
     * @throws IllegalStateException
     */
    @Deprecated
    public Element addTextSeparted(
            @Nonnull final CharSequence separator,
            @Nonnull final Object... data)
            throws IllegalStateException {
        return addTexts(separator, data);
    }

    /**
     * Add many words separated by a delimeter
     * @param separator The delimiter must contain no special HTML character.
     * @param data Data to print
     * @return The current element
     * @throws IllegalStateException
     */
    public Element addTexts(
            @Nonnull final CharSequence separator,
            @Nonnull final Object... data)
            throws IllegalStateException {
        for (int i = 0, max = data.length; i < max; i++) {
            if (i > 0) {
                internalElement.addRawText(separator);
            }
            internalElement.addText(data[i]);
        }
        return this;
    }

    @Nonnull
    @Override
    public Element addRawText(@Nullable final Object data) throws IllegalStateException {
        internalElement.addRawText(data);
        return this;
    }

    @Nonnull
    public Element addRawText(@Nonnull final Object... data) throws IllegalStateException {
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
            @Nonnull final CharSequence separator,
            @Nonnull final Object... data)
            throws IllegalStateException {
        for (int i = 0, max = data.length; i < max; i++) {
            if (i > 0) {
                internalElement.addRawText(separator);
            }
            internalElement.addRawText(data[i]);
        }
        return this;
    }

    @Nonnull
    @Override
    public Element addComment(CharSequence comment) throws IllegalStateException {
        internalElement.addComment(comment);
        return this;
    }

    @Nonnull
    @Override
    public Element addCDATA(CharSequence charData) throws IllegalStateException {
        internalElement.addCDATA(charData);
        return this;
    }

    @Nonnull
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
    @Override @Nonnull
    public Element addElement(@Nonnull final String name) throws IllegalStateException {
            return new Element(internalElement.addElement(name));
    }

    /**
     * Add a new Element with optional CSS classes
     * @param name A required name of the element
     * @param cssClasses Optional CSS classes.
     * @return New instance of the Element
     */
    @Nonnull
    public final Element addElement(@Nonnull final String name, @Nonnull final CharSequence... cssClasses) {
        return addElement(name).setClass(cssClasses);
    }

    /**
     * Add an element according to a condition.
     * @param enabled A condition for rendering the element.
     * @param name An element name
     * @param cssClasses CSS classes
     * @return New instance of the Element
     */
    @Nonnull
    public final Element addElementIf(final boolean enabled,
            @Nonnull final String name,
            @Nonnull final CharSequence... cssClasses) {
        return addElement(enabled ? name : HIDDEN_NAME).setClass(cssClasses);
    }

    /** Add new Table */
    @Nonnull
    public Element addTable(@Nonnull final CharSequence... cssClasses) {
        return addElement(TABLE, cssClasses);
    }

    /** Add new Table with cellpadding a cellspacing values to zero.
     * @deprecated Use a CSS style rather.
     */
    @Deprecated
    @Nonnull
    public Element addTableNoSpaces(@Nonnull final CharSequence... cssClasses) {
        return addTable(cssClasses)
                .setAttribute(Element.A_CELLPADDING, 0)
                .setAttribute(Element.A_CELLSPACING, 0);
    }

    /** Create a HTML table according to data */
    @Nonnull
    public Element addTable(
            @Nonnull final Object[][] data,
            @Nonnull final CharSequence... cssClass) {
        return addTable(Arrays.asList(data), cssClass);
    }

    /** Create a HTML table according to data */
    @Nonnull
    public Element addTable(
            @Nonnull final Collection<Object[]> data,
            @Nonnull final CharSequence... cssClass) {
        final Element result = addTable(cssClass);
        for (Object[] rowValue : data) {
            final Element rowElement = result.addElement(Html.TR);
            for (Object value : rowValue) {
                rowElement.addElement(Html.TD).addText(value);
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
    @Nonnull
    public <D,V> Element addTable(
            @Nonnull final Stream<D> domains,
            @Nullable final CharSequence[] cssClass,
            @Nullable final Object[] headers,
            @Nonnull final Function<D,V>... attributes) {

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
    @Nonnull
    public Element addImage(
            @Nonnull final CharSequence imageLink,
            @Nonnull final CharSequence alt,
            @Nonnull final CharSequence... cssClasses)
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
    @Nonnull
    public Element addImage(
            @Nonnull final InputStream imageStream,
            @Nonnull final CharSequence alt,
            @Nonnull final CharSequence... cssClasses)
            throws IllegalStateException {
        return addElement(IMAGE, cssClasses)
                .setAttribute(A_ALT, alt)
                .setAttribute(A_SRC, createEmbededImage(imageStream, new StringBuilder(1024)));
    }

    /** Create a content of an embeded image */
    @Nonnull
    protected CharSequence createEmbededImage(
            @Nonnull final InputStream imageStream,
            @Nonnull final StringBuilder result) {
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
    @Nonnull
    public Element addBody(@Nonnull final CharSequence... cssClasses) {
        return addElement(BODY, cssClasses);
    }

    /** Add new body element */
    @Nonnull
    public Element addTitle(@Nonnull final CharSequence... cssClasses) {
        return addElement(TITLE, cssClasses);
    }

    /** Add new body element */
    @Nonnull
    public Element addLink(@Nonnull final CharSequence... cssClasses) {
        return addElement(LINK, cssClasses);
    }

    /** Add new body element */
    @Nonnull
    public Element addStyle(@Nonnull final CharSequence... cssClasses) {
        return addElement(STYLE, cssClasses);
    }

    /** Add new body element */
    @Nonnull
    public Element addScript(@Nonnull final CharSequence... cssClasses) {
        return addElement(SCRIPT, cssClasses);
    }

    /** Add new body element */
    @Nonnull
    public Element addDiv(@Nonnull final CharSequence... cssClasses) {
        return addElement(DIV, cssClasses);
    }

    /** Add new fieldset element including a title
     * @param title An optional title
     * @param cssClasses CSS classes
     * @return An instance of FieldSet
     * @see LEGEND
     */
    @Nonnull
    public Element addFieldset(@Nullable final String title, @Nonnull final CharSequence... cssClasses) {
        final Element result = addElement(FIELDSET, cssClasses);
        if (Check.hasLength(title)) {
            result.addElement(LEGEND).addText(title);
        }
        return result;
    }

    /** Add new body element */
    @Nonnull
    public Element addPreformatted(@Nonnull final CharSequence... cssClasses) {
        return addElement(PRE, cssClasses);
    }

    /** Add new body element */
    @Nonnull
    public Element addSpan(@Nonnull final CharSequence... cssClasses) {
        return addElement(SPAN, cssClasses);
    }

    /** Add new body element */
    @Nonnull
    public Element addParagraph(@Nonnull final CharSequence... cssClasses) {
        return addElement(P, cssClasses);
    }

    /** Add new body element */
    @Nonnull
    public Element addForm(@Nonnull final CharSequence... cssClasses) {
        return addElement(FORM, cssClasses);
    }

    /** Add a top heading (level one)  */
    @Nonnull
    public Element addHeading(@Nonnull CharSequence title, @Nonnull final CharSequence... cssClasses) {
        return addHeading(1, title, cssClasses);
    }

    /** Add new heading with the required level where the first level is the one,  */
    @Nonnull
    public Element addHeading(int level, @Nonnull CharSequence title, @Nonnull final CharSequence... cssClasses) {
        Assert.isTrue(level > 0, "Unsupported level {}", level);
        return addElement(HEADING_PREFIX + level, cssClasses).addText(title);
    }

    /** Add new body element */
    @Nonnull
    public Element addTableHead(@Nonnull final CharSequence... cssClasses) {
        return addElement(THEAD, cssClasses);
    }

    /** Add new body element */
    @Nonnull
    public Element addTableRow(@Nonnull final CharSequence... cssClasses) {
        return addElement(TR, cssClasses);
    }

    /** Add new body element */
    @Nonnull
    public Element addTableDetail(@Nonnull final CharSequence... cssClasses) {
        return addElement(TD, cssClasses);
    }

    /** Add new body element */
    @Nonnull
    public Element addLabel(@Nonnull final CharSequence... cssClasses) {
        return addElement(LABEL, cssClasses);
    }

    /** Add new input element */
    @Nonnull
    public Element addInput(@Nonnull final CharSequence... cssClasses) {
        return addElement(INPUT, cssClasses);
    }

    /** Add new input element type of text */
    @Nonnull
    public Element addTextInput(@Nonnull final CharSequence... cssClasses) {
        return addInput(cssClasses).setType(V_TEXT);
    }

    /** Add new input element type of text including attributes: name, value, placeholder and title
     *
     * @deprecated Use the method of the same name with an explicit value.
     */
    @Deprecated
    @Nonnull
    public Element addTextInput(
            @Nonnull HttpServletRequest req,
            @Nonnull HttpParameter param,
            @Nonnull CharSequence title,
            @Nonnull final CharSequence... cssClasses) {
        return addTextInput(cssClasses)
                .setName(param)
                .setValue(param.of(req))
                .setAttribute(Html.A_PLACEHOLDER, title)
                .setAttribute(Html.A_TITLE, title);
    }

    /** Add new input element type of text including attributes: name, value, placeholder and title */
    @Nonnull
    public <V> Element addTextInp(
            @Nonnull HttpParameter param,
            @Nullable V value,
            @Nonnull CharSequence title,
            @Nonnull final CharSequence... cssClasses) {
        return addTextInput(cssClasses)
                .setName(param)
                .setValue(value)
                .setAttribute(Html.A_PLACEHOLDER, title)
                .setAttribute(Html.A_TITLE, title);
    }

    /** Add new input element type of password */
    @Nonnull
    public Element addPasswordInput(@Nonnull final CharSequence... cssClasses) {
        return addInput(cssClasses).setType(V_PASSWORD);
    }

    /** Add new body element */
    @Nonnull
    public Element addTextArea(@Nonnull final CharSequence... cssClasses) {
        return addElement(TEXT_AREA, cssClasses);
    }

    /** Add new select element
     * @see #addSelectOptions(java.lang.Object, java.util.Map, java.lang.CharSequence...)
     */
    @Nonnull
    public Element addSelect(@Nonnull final CharSequence... cssClasses) {
        return addElement(SELECT, cssClasses);
    }

    /** Add options from map to current select element
     * @param value Value of a select element
     * @param options Consider an instance of the {@link LinkedHashMap} class predictable iteration order of options.
     * @param cssClasses
     * @return Return {@code this}
     * @see #addSelect(java.lang.CharSequence...)
     */
    @Nonnull
    public Element addSelectOptions(
            @Nonnull Object value,
            @Nonnull final Map<?,?> options,
            @Nonnull final CharSequence... cssClasses) {
        for (Object key : options.keySet()) {
            this.addElement(Html.OPTION)
                    .setAttribute(Html.A_VALUE, key)
                    .setAttribute(Html.A_SELECTED, Objects.equals(value, key) ? Html.A_SELECTED : null)
                    .addText(options.get(key));
        }
        return this;
    }

    /** Add new body element */
    @Nonnull
    public Element addOption(@Nonnull final CharSequence... cssClasses) {
        return addElement(OPTION, cssClasses);
    }

    /** Add new body element */
    @Nonnull
    public Element addButton(@Nonnull final CharSequence... cssClasses) {
        return addElement(BUTTON, cssClasses);
    }

    /** Add a submit button */
    @Nonnull
    public Element addSubmitButton(@Nonnull final CharSequence... cssClasses) {
        final Element result = addButton(cssClasses);
        return result.setType(V_SUBMIT);
    }

    /** Add an anchor element with URL and CSS classes */
    @Nonnull
    public Element addAnchor(@Nonnull final String url, @Nonnull final CharSequence... cssClasses) {
        final Element result = addElement(A, cssClasses);
        return result.setHref(url);
    }

    /** Use the method {@link #addLinkedText(java.lang.String, java.lang.Object...) rather. */
    @Deprecated
    @Nonnull
    public Element addAnchoredText(@Nonnull final String url, @Nonnull final Object... text) {
        return addLinkedText(url, text);
    }

    /**
     * Add a
     * @param url
     * @param text
     * @return The original element!
     */
    @Nonnull
    public Element addLinkedText(@Nonnull final String url, @Nonnull final Object... text) {
        addElement(A).setHref(url).addTexts("", text);
        return this;
    }

    /** Add new body element */
    @Nonnull
    public Element addUnorderedlist(@Nonnull final CharSequence... cssClasses) {
        return addElement(UL, cssClasses);
    }

    /** Add new body element */
    @Nonnull
    public Element addOrderedList(@Nonnull final CharSequence... cssClasses) {
        return addElement(OL, cssClasses);
    }

    @Nonnull
    public Element addListItem(@Nonnull final CharSequence... cssClasses) {
        return addElement(LI, cssClasses);
    }

    /** Set a CSS class attribute optionally, the empty attribute is ignored.
     * @param cssClasses Optional CSS classes. The css item is ignored when the value is empty or {@code null}.
     * @return The current instanlce
     */
    @Nonnull
    public Element setClass(@Nonnull final CharSequence... cssClasses) {
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
    @Nonnull
    public Element addBreak(@Nonnull final CharSequence... cssClasses) {
        return addElement(BR, cssClasses);
    }

    // ----- An attributes ----------

    /** Set a CSS class attribute rather */
    @Deprecated
    @Nonnull
    public Element setCellPadding(final int value) {
        setAttribute(A_CELLPADDING, value);
        return this;
    }

    /** Set a CSS class attribute rather */
    @Deprecated
    @Nonnull
    public Element setCellSpacing(final int value) {
        setAttribute(A_CELLSPACING, value);
        return this;
    }

    // ---- Static methods ----

    /** Crate a root element
     * @param cssLinks Nullable CSS link array
     */
    @Nonnull
    public static Element createHtmlRoot(@Nonnull final Object title, @Nullable final CharSequence... cssLinks) {
        return createHtmlRoot(title, null, cssLinks);
    }


    /** Crate a root element
     * @param title A HTML title
     * @param charset A charset
     * @param cssLinks Nullable CSS link array
     */
    @Nonnull
    public static Element createHtmlRoot(
            @Nonnull final Object title,
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
    @Nonnull
    public Element setId(@Nullable final CharSequence value) {
        setAttribute(A_ID, value);
        return this;
    }

    /** Set a method of form */
    @Nonnull
    public Element setMethod(@Nullable final Object value) {
        setAttribute(A_METHOD, value);
        return this;
    }

    /** Set an action type of from */
    @Nonnull
    public Element setAction(@Nullable final Object value) {
        setAttribute(A_ACTION, value);
        return this;
    }

    /** Set a type of input element */
    @Nonnull
    public Element setType(@Nullable final Object value) {
        setAttribute(A_TYPE, value);
        return this;
    }

    /** Set an name of input element */
    @Nonnull
    public Element setName(@Nullable final CharSequence value) {
        setAttribute(A_NAME, value);
        return this;
    }

    /** Set an value of input element */
    @Nonnull
    public Element setValue(@Nullable final Object value) {
        setAttribute(A_VALUE, value);
        return this;
    }

    /** Set an value of input element */
    @Nonnull
    public Element setFor(@Nullable final CharSequence value) {
        setAttribute(A_VALUE, value);
        return this;
    }

    /** Row count of a text area */
    @Nonnull
    public Element setRows(@Nullable final int value) {
        setAttribute(A_ROWS, value);
        return this;
    }

    /** Column count of a text area */
    @Nonnull
    public Element setCols(@Nullable final Object value) {
        setAttribute(A_COLS, value);
        return this;
    }

    /** Column span inside the table */
    @Nonnull
    public Element setColSpan(@Nullable final int value) {
        setAttribute(A_COLSPAN, value);
        return this;
    }

    /** Row span inside the table */
    @Nonnull
    public Element setRowSpan(@Nullable final int value) {
        setAttribute(A_ROWSPAN, value);
        return this;
    }

    /** Set hyperlink reference */
    @Nonnull
    public Element setHref(@Nullable final CharSequence value) {
        setAttribute(A_HREF, value);
        return this;
    }


    /** Apply an element builder */
    @Nonnull
    public ExceptionProvider then(@Nonnull final Consumer<Element> builder) {
        try {
            builder.accept(this);
            close();
        } catch (Throwable e) {
            return ExceptionProvider.of(e);
        }
        return ExceptionProvider.of();
    }

    /** String value */
    @Override
    @Nonnull
    public String toString() {
        return internalElement.toString();
    }

    /** New element for an API element */
    @Nonnull
    public static final Element of(@Nonnull final ApiElement original) {
        return (original instanceof Element)
            ? (Element) original
            : new Element(original);
    }
}
