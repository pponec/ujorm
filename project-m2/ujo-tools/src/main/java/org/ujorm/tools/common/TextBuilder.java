/*
 * Copyright 2021-2026 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.common;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.ujorm.tools.Assert;

/**
 * Building a text.
 *
 * <h2>Usage</h2>
 * <pre>
 *  String result = new TextBuilder()
 *      .line("A", "B", "C").add("\n")
 *      .line("X", "Y").add("Z")
 *      .toString();
 *  String expected = "ABC\nXYZ";
 *  assertEquals(expected, result);
 * </pre>
 *
 * @author Pavel Ponec
 */
public class TextBuilder implements CharSequence {

    public static final char NEW_LINE = '\n';

    @NotNull
    private final StringBuilder builder;

    public TextBuilder() {
        this(new StringBuilder());
    }

    public TextBuilder(final StringBuilder builder) {
        this.builder = Assert.notNull(builder, "builder");
    }

    public TextBuilder add(@NotNull final CharSequence... items) {
        for (CharSequence item : items) {
            builder.append(item);
        }
        return this;
    }

    public TextBuilder line(@NotNull final CharSequence... items) {
        if (!endsByNewLine()) {
            builder.append(NEW_LINE);
        }
        return add(items);
    }

    public TextBuilder emptyLine() {
        line();
        builder.append(NEW_LINE);
        return this;
    }

    public boolean isEmpty() {
        return builder.length() == 0;
    }

    protected final boolean endsByNewLine() {
        final int length = builder.length();
        return length == 0 || builder.charAt(length - 1) == NEW_LINE;
    }

    public void writeTo(@NotNull final Appendable writer) throws IOException {
        writer.append(builder);
    }


    @Override
    public final int length() {
        return builder.length();
    }

    @Override
    public final char charAt(int index) {
        return builder.charAt(index);
    }

    @Override
    public final CharSequence subSequence(int from, int to) {
        return builder.subSequence(from, to);
    }

    @NotNull
    @Override
    public String toString() {
        return builder.toString();
    }

}
