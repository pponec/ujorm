/*
 * Copyright 2020-2020 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.web.ao;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.ujorm.tools.web.HtmlElement;
import org.ujorm.tools.xml.config.HtmlConfig;

/**
 * Simple JSON writer for object type of key-value.
 *
 * @author Pavel Ponec
 */
public class JsonBuilder implements Closeable {

    private static final char DOUBLE_QUOTE = JsonWriter.DOUBLE_QUOTE;

    /** An original writer */
    @Nonnull
    private final Appendable writer;
    /** JSON writer with character escaping */
    @Nonnull
    private final JsonWriter jsonWriter;
    /** HTML config */
    private final HtmlConfig config;
    /** Parameter counter */
    private int paramCounter = 0;

    /** Constructor with a default HTML config */
    protected JsonBuilder(@Nonnull final Appendable writer) {
        this(writer, HtmlConfig.ofEmptyElement());
    }

    /** Common constructor */
    protected JsonBuilder(@Nonnull final Appendable writer, HtmlConfig config) {
        this.writer = writer;
        this.jsonWriter = new JsonWriter(writer);
        this.config = config;
    }

    /** Write the value for a CSS ID selector
     *
     * @param elementId ID selector
     * @param values The text array to join.
     * @throws IOException
     */
    public void writeId(
            @Nonnull final CharSequence elementId,
            @Nullable final CharSequence... values) throws IOException {
        write(SelectorType.ID.prefix, elementId, values);
    }


    /** Write the value for a CSS CLASS selector
     *
     * @param elementId ID selector
     * @param values The text array to join.
     * @throws IOException
     */
    public void writeClass(
            @Nonnull final CharSequence elementId,
            @Nullable final CharSequence... values) throws IOException {
        write(SelectorType.CLASS.prefix, elementId, values);
    }

    /** Write a key-value
     *
     * @param key A JSON key
     * @param values The text array to join.
     * @throws IOException
     */
    public void write(
            @Nonnull final CharSequence key,
            @Nullable final CharSequence... values) throws IOException {
        write(SelectorType.INCLUDED.prefix, key, values);
    }

    /** Write a key-value
     *
     * @param key A JSON key
     * @param values The text array to join.
     * @throws IOException
     */
    public void write(
            @Nonnull final String keyPrefix,
            @Nonnull final CharSequence key,
            @Nullable final CharSequence... values) throws IOException {
        writer.append(paramCounter++ == 0 ? '{' : ',');
        writer.append(DOUBLE_QUOTE);
        jsonWriter.append(keyPrefix);
        jsonWriter.append(key);
        writer.append(DOUBLE_QUOTE);
        writer.append(':');
        if (values == null) {
            writer.append("null");
        } else {
            writer.append(DOUBLE_QUOTE);
            for (CharSequence value : values) {
                jsonWriter.append(value);
            }
        }
        writer.append(DOUBLE_QUOTE);
    }

    // --- VALUE PROVIDER ---


    /** Write the value for a CSS ID selector
     *
     * @param elementId ID selector
     * @param valueProvider A value provider
     * @throws IOException
     */
    public void writeId(
            @Nonnull final CharSequence elementId,
            @Nonnull final ValueProvider valueProvider) throws IOException {
        write(SelectorType.ID.prefix, elementId, valueProvider);
    }


    /** Write the value for a CSS CLASS selector
     *
     * @param elementId ID selector
     * @param valueProvider A value provider
     * @throws IOException
     */
    public void writeClass(
            @Nonnull final CharSequence elementId,
            @Nonnull final ValueProvider valueProvider) throws IOException {
        write(SelectorType.CLASS.prefix, elementId, valueProvider);
    }

    /** Write a key-value
     *
     * @param key A JSON key
     * @param valueProvider A value provider
     * @throws IOException
     */
    public void write(
            @Nonnull final CharSequence key,
            @Nonnull final ValueProvider valueProvider) throws IOException {
        write(SelectorType.INCLUDED.prefix, key, valueProvider);
    }

    /**
     *
     * @param keyPrefix Key Prefix
     * @param key Main Key
     * @param valueProvider A value provider
     * @throws IOException
     */
    public void write(
            @Nonnull final String keyPrefix,
            @Nonnull final CharSequence key,
            @Nonnull final ValueProvider valueProvider)
            throws IOException {

        writer.append(paramCounter++ == 0 ? '{' : ',');
        writer.append(DOUBLE_QUOTE);
        jsonWriter.append(keyPrefix);
        jsonWriter.append(key);
        writer.append(DOUBLE_QUOTE);
        writer.append(':');
        writer.append(DOUBLE_QUOTE);
        try (HtmlElement root = HtmlElement.of(config, jsonWriter)) {
            valueProvider.accept(root.original());
        }
        writer.append(DOUBLE_QUOTE);
    }

    @Override
    public void close() throws IOException {
        if (paramCounter == 0) {
            writer.append('{');
        }
        writer.append('}');
    }

    /** An object factory */
    @Nonnull
    public static final JsonBuilder of(@Nonnull final Appendable writer) {
        return new JsonBuilder(writer);
    }

    /** An object factory */
    @Nonnull
    public static final JsonBuilder of(
            @Nonnull final HttpServletRequest request,
            @Nonnull final HttpServletResponse response) throws IllegalStateException, IOException {
        return of(request, response, StandardCharsets.UTF_8, true);
    }

    /** An object factory */
    @Nonnull
    public static final JsonBuilder of(
            @Nonnull final HttpServletRequest request,
            @Nonnull final HttpServletResponse response,
            @Nonnull final Charset charset,
            final boolean setHeader) throws IllegalStateException, IOException {

        if (setHeader) {
            response.addHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
            response.addHeader("Pragma", "no-cache"); // HTTP 1.0
            response.addHeader("Expires", "0"); // Proxies
        }
        request.setCharacterEncoding(charset.toString());
        response.setCharacterEncoding(charset.toString());
        return of(response.getWriter());
    }

    /** CSS selector types */
    public enum SelectorType {
        /** CSS selector by ID */
        ID("#"),
        /** CSS selector by CLASS */
        CLASS("."),
        /** CSS selector is included */
        INCLUDED("");

        final String prefix;

        private SelectorType(@Nonnull String prefix) {
            this.prefix = prefix;
        }

        @Nonnull
        public String getPrefix() {
            return prefix;
        }
    }
}
