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

import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.ujorm.tools.Assert;

/**
 * Simple JSON writer for object type of key-value.
 *
 * @author Pavel Ponec
 */
public final class JsonWriter implements Appendable {

    private static final char BACKSLASH = '\\';
    static final char DOUBLE_QUOTE = '"';

    private final Appendable writer;

    JsonWriter(@NotNull final Appendable writer) {
        this.writer = Assert.notNull(writer, "writer");
    }

    @Override
    public Appendable append(CharSequence csq) throws IOException {
        return append(csq, 0, csq.length());
    }

    @Override
    public Appendable append(
            @NotNull final CharSequence csq,
            final int start,
            final int end)
            throws IOException {
        for (int i = start; i < end; i++) {
            append(csq.charAt(i));
        }
        return this;
    }

    @Override
    public Appendable append(final char c) throws IOException {
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
        return this;
    }

    @NotNull
    public Appendable original() {
        return writer;
    }
}
