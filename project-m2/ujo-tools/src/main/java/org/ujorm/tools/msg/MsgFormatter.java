/*
 * Copyright 2017-2026 Pavel Ponec, https://github.com/pponec
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

import java.io.IOException;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.ujorm.tools.Check;
import org.ujorm.tools.common.ObjectUtils;

/**
 * Formatter of log messages where argument is located by the {@code {} } characters.
 * See the next correct asserts:
 * <pre class="pre">
 *  assertEquals("TEST"    , MsgFormatter.format("TE{}T", "S"));
 *  assertEquals("TE, S, T", MsgFormatter.format("TE", "S", "T"));
 *  assertEquals("TES{}"   , MsgFormatter.format("TE{}{}", "S"));
 * </pre>
 * @author Pavel Ponec
 * @since 1.73
 */
@Unmodifiable
public class MsgFormatter {

    /** An undefined writter */
    @Nullable
    private static final Appendable NO_WRITER = null;

    /** Parameter mark */
    protected static final String DEFAULT_MARK = "{}";
    /** Separator of unmarked arguments '{}' was changed to a single space from release 1.91 */
    protected static final char SEPARATOR = ' ';

    /** The parameter mark in the template. */
    private final String mark;

    /** Static methods are available only */
    protected MsgFormatter() {
        this(DEFAULT_MARK);
    }

    /** Static methods are available only */
    protected MsgFormatter(@NotNull final String mark) {
        this.mark = mark;
    }

    /**
     * Format the message, see the next correct asserts:
     * <pre class="pre">
     *  assertEquals("TEST"    , MsgFormatter.format("TE{}T", "S"));
     *  assertEquals("TE, S, T", MsgFormatter.format("TE", "S", "T"));
     *  assertEquals("TES{}"   , MsgFormatter.format("TE{}{}", "S"));
     * </pre>
     * @param writer An optional writer
     * @param messageTemplate Template where argument position is marked by the {@code {}} characters.
     * @param argumentValues Optional arguments, where the {@code Supplier} interface is supported.
     * @return A result text or an empty text, if the writer is available.
     */
    @NotNull
    public <T> String formatMsg
        ( @Nullable final Appendable writer
        , @Nullable final CharSequence messageTemplate
        , @Nullable final T... argumentValues) throws IOException
        {
        final String template = String.valueOf(messageTemplate);
        if (!Check.hasLength(argumentValues)) {
            return template;
        }
        final Object[] arguments = argumentValues.length == 1
            && argumentValues[0] instanceof Object[]
            ? (Object[]) argumentValues[0] // Convert a single argument type of array
            : argumentValues;

        final int max = template.length();
        final Appendable out = writer != null ? writer : new StringBuilder(Math.max(32, max + (max >> 1)));
        int last = 0;

        for (final Object arg : arguments) {
            final int i = template.indexOf(mark, last);
            if (i >= last) {
                out.append(template, last, i);
                last = i + mark.length();
                writeValue(arg, out, true);
            } else {
                if (last < max) {
                    out.append(template, last, max);
                    last = max;
                }
                writeValue(arg, out, false);
            }
        }
        if (last < max) {
            out.append(template, last, max);
        }
        return writer != null ? "" : out.toString();
    }

    /**
     * Format the message from Object array
     * @param templateAndArguments The first item is a template where parameters are located by {@code "{}"}
     * text and the next arguments are optional parameters of the template.
     * @return In case the argument have no length, the result message is {@code null}.
     */
    @Nullable
    protected <T> String formatMsg(@Nullable Appendable writer, @Nullable final T... templateAndArguments) throws IOException {
        if (Check.hasLength(templateAndArguments)) {
            final String template = String.valueOf(templateAndArguments[0]);
            final Object[] params = new Object[templateAndArguments.length - 1];
            System.arraycopy(templateAndArguments, 1, params, 0, params.length);
            return formatMsg(writer, template, params);
        } else {
            return null;
        }
    }

    /**
     * Print argument to the Writter with an optional format.
     * @param out Appendable
     * @param value Value where the {@code Supplier} interface is supported.
     */
    protected void writeValue(@Nullable final Object value, @NotNull final Appendable out, final boolean marked) throws IOException {
        final Object val = value instanceof Supplier
                ? ((Supplier)value).get()
                : value;
        if (marked) {
            out.append(val != null
                    ? val.toString()
                    : String.valueOf(val));
        } else if (val instanceof Throwable) {
            out.append('\n');
            ((Throwable)val).printStackTrace(ObjectUtils.toPrintWriter(out));
        } else {
            out.append(SEPARATOR);
            out.append(String.valueOf(val));
        }
    }

    // --------------- STATIC METHODS ----------------------

   /**
     * Format the message, see the next correct asserts:
     * <pre class="pre">
     *  assertEquals("TEST"    , MsgFormatter.format("TE{}T", "S"));
     *  assertEquals("TE S T", MsgFormatter.format("TE", "S", "T"));
     *  assertEquals("TES{}"   , MsgFormatter.format("TE{}{}", "S"));
     * </pre>
     * @param messageTemplate Template where argument position is marked by the {@code {}} characters.
     * @param arguments Optional arguments, where the {@code Supplier} interface is supported.
     * @return
     */
    @NotNull
    public static <T> String format
    ( @Nullable final CharSequence messageTemplate
    , @Nullable final T... arguments) {
        try {
            return new MsgFormatter().formatMsg(NO_WRITER, messageTemplate, arguments);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Format the message from Object array
     * @param templateAndArguments The first item is a template where parameters are located by {@code "{}"}.
     * The {@code Supplier} interface is supported.
     * text and the next arguments are optional parameters of the template.
     * @return In case the argument have no length, the result message is {@code null}.
     */
    @Nullable
    public static <T> String format(@Nullable final T... templateAndArguments) {
        try {
            return new MsgFormatter().formatMsg(NO_WRITER, templateAndArguments);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
