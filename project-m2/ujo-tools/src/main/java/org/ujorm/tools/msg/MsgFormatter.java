/*
 * Copyright 2017-2017 Pavel Ponec, https://github.com/pponec
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
import java.io.PrintWriter;
import java.io.Writer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.ujorm.tools.Check;

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
@Immutable
public class MsgFormatter {

    /** Parameter mark */
    protected static final String DEFAULT_MARK = "{}";
    /** Separator */
    protected static final String SEPARATOR = ", ";

    /** The parameter mark in the template. */
    private final String mark;

    /** Static methods are available only */
    protected MsgFormatter() {
        this(DEFAULT_MARK);
    }

    /** Static methods are available only */
    protected MsgFormatter(@Nonnull final String mark) {
        this.mark = mark;
    }

    /**
     * Format the message, see the next correct asserts:
     * <pre class="pre">
     *  assertEquals("TEST"    , MsgFormatter.format("TE{}T", "S"));
     *  assertEquals("TE, S, T", MsgFormatter.format("TE", "S", "T"));
     *  assertEquals("TES{}"   , MsgFormatter.format("TE{}{}", "S"));
     * </pre>
     * @param messageTemplate Template where argument position is marked by the {@code {}} characters.
     * @param argumentValues Optional arguments, where the {@code Supplier} interface is supported.
     * @return A result text
     */
    @Nonnull
    public <T> String formatMsg
        ( @Nullable final CharSequence messageTemplate
        , @Nullable final T... argumentValues)
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
        final StringBuilder out = new StringBuilder(Math.max(32, max + (max >> 1)));
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
        return out.toString();
    }

    /**
     * Format the message from Object array
     * @param templateAndArguments The first item is a template where parameters are located by {@code "{}"}
     * text and the next arguments are optional parameters of the template.
     * @return In case the argument have no length, the result message is {@code null}.
     */
    @Nullable
    public <T> String formatMsg(@Nullable final T... templateAndArguments) {
        if (Check.hasLength(templateAndArguments)) {
            final String template = String.valueOf(templateAndArguments[0]);
            final Object[] params = new Object[templateAndArguments.length - 1];
            System.arraycopy(templateAndArguments, 1, params, 0, params.length);
            return format(template, params);
        } else {
            return null;
        }
    }

    /**
     * Print argument to the Writter with an optional format.
     * @param out Writer
     * @param value Value where the {@code Supplier} interface is supported.
     */
    protected void writeValue(@Nullable final Object value, @Nonnull final StringBuilder out, final boolean marked) {
        final Object val = value instanceof Supplier
                ? ((Supplier)value).get()
                : value;
        if (marked) {
            out.append(val != null
                    ? val.toString()
                    : String.valueOf(val));
        } else if (val instanceof Throwable) {
            out.append('\n');
            ((Throwable)val).printStackTrace(getPrintWriter(out));
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
     *  assertEquals("TE, S, T", MsgFormatter.format("TE", "S", "T"));
     *  assertEquals("TES{}"   , MsgFormatter.format("TE{}{}", "S"));
     * </pre>
     * @param messageTemplate Template where argument position is marked by the {@code {}} characters.
     * @param arguments Optional arguments, where the {@code Supplier} interface is supported.
     * @return
     */
    @Nonnull
    public static <T> String format
    ( @Nullable final String messageTemplate
    , @Nullable final T... arguments) {
        return new MsgFormatter().formatMsg(messageTemplate, arguments);
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
        return new MsgFormatter().formatMsg(templateAndArguments);
    }

    /** Convert appendable to object type of PrintWriter */
    @Nonnull
    protected static PrintWriter getPrintWriter(@Nonnull final Appendable appendable) {
        final Writer myWriter = new Writer() {
            @Override
            public void flush() throws IOException {}
            @Override
            public void close() throws IOException {}
            @Override
            public void write(final char[] cbuf, final int off, final int len) throws IOException {
                for (int i = 0, max = off + len; i < max; i++) {
                    appendable.append(cbuf[i]);
                }
            }
        };
        return new PrintWriter(myWriter, false);
    }
}
