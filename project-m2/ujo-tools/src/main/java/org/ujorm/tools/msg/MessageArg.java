/*
 * Copyright 2012-2026 Pavel Ponec, https://github.com/pponec
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
package org.ujorm.tools.msg;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;
import org.ujorm.tools.common.ObjectUtils;

/**
 * Message Argument
 * @param <T> The parameter has only a descriptive meaning with no functionality.
 * @author Pavel Ponec
 * @see MessageService
 * @since 1.54
 */
public final class MessageArg<T> implements Serializable, CharSequence {

    /** Two-character mark ("${") to introducing a template argument. */
    public static final String PARAM_BEG = "${";

    /** The mark ("}") to finishing a template argument. */
    public static final char PARAM_END = '}';

    /** Name of the argument */
    @NotNull
    private final String name;

    /** Optional format of the argument */
    @Nullable
    private final String format;

    /** A code name for a template */
    @NotNull
    private final String code;

    /** Name constructor */
    public MessageArg(@NotNull final String name) {
        this(name, null);
    }

    /** Common constructor
     * @param name Argument name
     * @param format Format syntax is described on
     * <a href="https://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html">java.util.Formatter</a>
     */
    public MessageArg(@NotNull final String name, @Nullable final String format) {
        Assert.notNull(name, "Name is required", name);
        Assert.isTrue(name.indexOf(PARAM_END) < 0  , "Forbidden character {} in argument {}", PARAM_END, name);
        Assert.isTrue(format == null
                   || format.indexOf(PARAM_END) < 0, "Forbidden character {} in argument {}", PARAM_END, format);
        this.name = name;
        this.format = format;
        this.code = toCode();
    }

    /** Get Name of argument */
    @NotNull
    public String getName() {
        return name;
    }

    /** An alias for method {@code #getName()} */
    @NotNull
    public String name() {
        return name;
    }

    /** Get optional format of the argument */
    @Nullable
    public String getFormat() {
        return format;
    }

    /** A code name for a template */
    @NotNull
    public String getCode() {
        return code;
    }

    /** Convert attributes to a code */
    @NotNull
    private String toCode() {
        final StringBuilder result = new StringBuilder(PARAM_BEG.length() + 1 + name.length()
                + (format != null ? format.length() + 1 : 0));
        result.append(PARAM_BEG).append(name);
        if (Check.hasLength(format)) {
            result.append(',').append(format);
        }
        result.append(PARAM_END);
        return result.toString();
    }

    /** Get a value from a map */
    public <T extends Object> T getValue(final Map<String, Object> map) {
        return (T) map.get(name);
    }

    /** Returns a code name */
    @Override
    public String toString() {
        return getCode();
    }

    @Override
    public int length() {
        return getCode().length();
    }

    @Override
    public char charAt(final int index) {
        return getCode().charAt(index);
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        return getCode().subSequence(start, end);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj
                || ObjectUtils.check(obj, MessageArg.class, v
                        -> Objects.equals(name, v.name)
                        && Objects.equals(format, v.format));
    }

    // --- STATIC METHOD ---

    public static <T> MessageArg<T> of(@NotNull String name) {
        return new MessageArg<>(name);
    }

    public static <T> MessageArg<T> of(@NotNull String name, @Nullable String format) {
        return new MessageArg<>(name, format);
    }

}
