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
import org.ujorm.tools.Assert;

/**
 * Simple JSON writer for object type of key-value.
 *
 * @author Pavel Ponec
 */
public class JsonWriter implements Closeable {

    private static final char BACKSLASH = '\\';
    private static final char DOUBLE_QUOTE = '"';

    private final Appendable writer;
    private int paramCounter = 0;

    protected JsonWriter(@Nonnull final Appendable writer) {
        this.writer = Assert.notNull(writer, "writer");
    }

    /** Write the value for a CSS ID selector
     *
     * @param elementId ID selector
     * @param values The text array to join.
     * @throws IOException
     */
    public void writeIdSelector(
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
    public void writeClassSelector(
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
        write(keyPrefix);
        write(key);
        writer.append(DOUBLE_QUOTE);
        writer.append(':');
        if (values == null) {
            writer.append("null");
        } else {
            writer.append(DOUBLE_QUOTE);
            for (CharSequence value : values) {
                write(value);
            }
        }
        writer.append(DOUBLE_QUOTE);
    }

    protected void write(@Nonnull final CharSequence item) throws IOException {
        for (int i = 0, max = item.length(); i < max; i++) {
            final char c = item.charAt(i);
            switch (c) {
                case BACKSLASH:
                    writer.append(BACKSLASH);
                    writer.append(BACKSLASH);
                    break;
                case DOUBLE_QUOTE:
                    writer.append(BACKSLASH);
                    writer.append(DOUBLE_QUOTE);
                    break;
                case '\b':
                    writer.append(BACKSLASH);
                    writer.append('b');
                    break;
                case '\f':
                    writer.append(BACKSLASH);
                    writer.append('f');
                    break;
                case '\n':
                    writer.append(BACKSLASH);
                    writer.append('n');
                    break;
                case '\r':
                    writer.append(BACKSLASH);
                    writer.append('r');
                    break;
                case '\t':
                    writer.append(BACKSLASH);
                    writer.append('t');
                    break;
                default:
                    writer.append(c);
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (paramCounter == 0) {
            writer.append('{');
        }
        writer.append('}');
    }

    /** An object factory */
    public static final JsonWriter of(@Nonnull final Appendable writer) {
        return new JsonWriter(writer);
    }

    /** An object factory */
    public static final JsonWriter of(
            @Nonnull final HttpServletRequest request,
            @Nonnull final HttpServletResponse response) throws IllegalStateException, IOException {
        return of(request, response, StandardCharsets.UTF_8, true);
    }

    /** An object factory */
    public static final JsonWriter of(
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
