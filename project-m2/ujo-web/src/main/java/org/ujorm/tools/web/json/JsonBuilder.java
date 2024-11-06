/*
 * Copyright 2020-2022 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.web.json;

import java.io.Closeable;
import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.web.HtmlElement;
import org.ujorm.tools.web.ao.ObjectProvider;
import org.ujorm.tools.web.request.RContext;
import org.ujorm.tools.xml.config.HtmlConfig;

/**
 * Simple JSON writer for object type of key-value.
 *
 * @author Pavel Ponec
 */
public class JsonBuilder implements Closeable {

    private static final char DOUBLE_QUOTE = JsonWriter.DOUBLE_QUOTE;

    /** An original writer */
    @NotNull
    private final Appendable writer;
    /** JSON writer with character escaping */
    @NotNull
    private final JsonWriter jsonWriter;
    /** HTML config */
    private final HtmlConfig config;
    /** Parameter counter */
    private int paramCounter = 0;
    /** Dummy selector to run a JavaScript */
    private final String JAVACRIPT_DUMMY_SELECTOR = "";

    /** Constructor with a default HTML config */
    protected JsonBuilder(@NotNull final Appendable writer) {
        this(writer, HtmlConfig.ofEmptyElement());
    }

    /** Common constructor */
    protected JsonBuilder(@NotNull final Appendable writer, HtmlConfig config) {
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
    public JsonBuilder writeId(
            @NotNull final CharSequence elementId,
            @Nullable final CharSequence... values) throws IOException {
        writePrefix(SelectorType.ID.prefix, elementId, values);
        return this;
    }


    /** Write the value for a CSS CLASS selector
     *
     * @param elementId ID selector
     * @param values The text array to join.
     * @throws IOException
     */
    public JsonBuilder writeClass(
            @NotNull final CharSequence elementId,
            @Nullable final CharSequence... values) throws IOException {
        writePrefix(SelectorType.CLASS.prefix, elementId, values);
        return this;
    }

    /** Write a key-value
     *
     * @param key A JSON key
     * @param values The text array to join.
     * @throws IOException
     */
    public JsonBuilder write(
            @NotNull final CharSequence key,
            @Nullable final CharSequence... values) throws IOException {
        writePrefix(SelectorType.INCLUDED.prefix, key, values);
        return this;
    }

    /** Write a key-value with a prefix
     *
     * @param key A JSON key
     * @param values The text array to join.
     * @throws IOException
     */
    public JsonBuilder writePrefix(
            @NotNull final String keyPrefix,
            @NotNull final CharSequence key,
            @Nullable final CharSequence... values) throws IOException {
        writeKey(keyPrefix, key);
        if (values == null) {
            writer.append("null");
        } else {
            writer.append(DOUBLE_QUOTE);
            for (CharSequence value : values) {
                jsonWriter.append(value);
            }
        }
        writer.append(DOUBLE_QUOTE);
        return this;
    }

    /** Write a Javascript to a call.
     * The response can contain only one Javascript code,
     * so this method can be called only once per request.
     */
    public JsonBuilder writeJs(@Nullable final CharSequence... javascript) throws IOException {
        return write(JAVACRIPT_DUMMY_SELECTOR, javascript);
    }

    /** Write a JSON property */
    private void writeKey(final String keyPrefix, final CharSequence key) throws IOException {
        writer.append(paramCounter++ == 0 ? '{' : ',');
        writer.append(DOUBLE_QUOTE);
        jsonWriter.append(keyPrefix);
        jsonWriter.append(key);
        writer.append(DOUBLE_QUOTE);
        writer.append(':');
    }

    // --- VALUE PROVIDER ---


    /** Write the value for a CSS ID selector
     *
     * @param elementId ID selector
     * @param valueProvider A value provider
     * @throws IOException
     */
    public JsonBuilder writeId(
            @NotNull final CharSequence elementId,
            @NotNull final ValueProvider valueProvider) throws IOException {
        return write(SelectorType.ID.prefix, elementId, valueProvider);
    }


    /** Write the value for a CSS CLASS selector
     *
     * @param elementId ID selector
     * @param valueProvider A value provider
     * @throws IOException
     */
    public JsonBuilder writeClass(
            @NotNull final CharSequence elementId,
            @NotNull final ValueProvider valueProvider) throws IOException {
        return write(SelectorType.CLASS.prefix, elementId, valueProvider);
    }

    /** Write a key-value
     *
     * @param key A JSON key
     * @param valueProvider A value provider
     * @throws IOException
     */
    public JsonBuilder write(
            @NotNull final CharSequence key,
            @NotNull final ValueProvider valueProvider) throws IOException {
        return write(SelectorType.INCLUDED.prefix, key, valueProvider);
    }

    /**
     *
     * @param keyPrefix Key Prefix
     * @param key Main Key
     * @param valueProvider A value provider
     * @throws IOException
     */
    public JsonBuilder write(
            @NotNull final String keyPrefix,
            @NotNull final CharSequence key,
            @NotNull final ValueProvider valueProvider)
            throws IOException {

        writeKey(keyPrefix, key);
        writer.append(DOUBLE_QUOTE);
        try (HtmlElement root = HtmlElement.of(jsonWriter, config)) {
            valueProvider.accept(root.original());
        }
        writer.append(DOUBLE_QUOTE);
        return this;
    }

    @Override
    public void close() throws IOException {
        if (paramCounter == 0) {
            writer.append('{');
        }
        writer.append('}');
    }

    // --- OBJECT PROVIER ---

    /** An experimental feature: write the value for a CSS ID selector
     *
     * @param elementId ID selector
     * @param objectProvider A value provider
     * @throws IOException
     */
    public JsonBuilder writeIdObj(
            @NotNull final CharSequence elementId,
            @NotNull final ObjectProvider objectProvider) throws IOException {
        return writeObj(SelectorType.ID.prefix, elementId, objectProvider);
    }


    /** An experimental feature: write the value for a CSS CLASS selector
     *
     * @param elementId ID selector
     * @param objectProvider A value provider
     * @throws IOException
     */
    public JsonBuilder writeClassObj(
            @NotNull final CharSequence elementId,
            @NotNull final ObjectProvider objectProvider) throws IOException {
        return writeObj(SelectorType.CLASS.prefix, elementId, objectProvider);
    }

    /** An experimental feature: write a key-object value
     *
     * @param key A JSON key
     * @param objectProvider A value provider
     * @throws IOException
     */
    public JsonBuilder writeObj(
            @NotNull final CharSequence key,
            @NotNull final ObjectProvider objectProvider) throws IOException {
        return writeObj(SelectorType.INCLUDED.prefix, key, objectProvider);
    }

    /**
     * An experimental feature: write key-object value
     *
     * @param keyPrefix Key Prefix
     * @param key Main Key
     * @param objectProvider A value provider
     * @throws IOException
     */
    public JsonBuilder writeObj(
            @NotNull final String keyPrefix,
            @NotNull final CharSequence key,
            @NotNull final ObjectProvider objectProvider)
            throws IOException {

        writeKey(keyPrefix, key);
        objectProvider.accept(this);
        writer.append(DOUBLE_QUOTE);
        return this;
    }

    // --- UTILS ---

    /** An object factory */
    @NotNull
    public static final JsonBuilder of(
            @NotNull final RContext context,
            @NotNull final HtmlConfig config) {
        return of(context.writer(), config);
    }

    /** An object factory */
    @NotNull
    public static final JsonBuilder of(@NotNull final RContext context) {
        return of(context.writer());
    }

    /** An object factory */
    @NotNull
    public static final JsonBuilder of(@NotNull final Appendable writer) {
        return new JsonBuilder(writer);
    }

    /** An object factory. The MAIN factory method. */
    @NotNull
    public static final JsonBuilder of(
            @NotNull final Appendable writer,
            @NotNull final HtmlConfig config) {
        return new JsonBuilder(writer, config);
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

        SelectorType(@NotNull String prefix) {
            this.prefix = prefix;
        }

        @NotNull
        public String getPrefix() {
            return prefix;
        }
    }
}
