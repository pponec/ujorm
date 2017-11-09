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
package org.ujorm.tools;

import java.io.CharArrayWriter;
import java.io.PrintWriter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

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
    protected static final String MARK = "{}";
    /** Separator */
    protected static final String SEPARATOR = ", ";

    /** Static methods are available only */
    protected MsgFormatter() {
    }

    /**
     * Format the message, see the next correct asserts:
     * <pre class="pre">
     *  assertEquals("TEST"    , MsgFormatter.format("TE{}T", "S"));
     *  assertEquals("TE, S, T", MsgFormatter.format("TE", "S", "T"));
     *  assertEquals("TES{}"   , MsgFormatter.format("TE{}{}", "S"));
     * </pre>
     * @param messageTemplate Template where argument position is marked by the {@code {}} characters.
     * @param arguments Optional arguments
     * @return
     */
    @Nonnull
    public String formatMsg
        ( @Nullable final String messageTemplate
        , @Nullable final Object... arguments)
        {
        final String template = messageTemplate != null ? messageTemplate : String.valueOf(messageTemplate);
        if (!Check.hasLength(arguments)) {
            return template;
        }

        final int max = template.length();
        final CharArrayWriter result = new CharArrayWriter(Math.max(32, max + (max >> 1)));
        int last = 0;

        for (Object arg : arguments) {
            final int i = template.indexOf(MARK, last);
            if (i >= last) {
                result.append(template, last, i);
                last = i + MARK.length();
                writeValue(arg, result, true);
            } else {
                if (last < max) {
                    result.append(template, last, max);
                    last = max;
                }
                writeValue(arg, result, false);
            }
        }
        if (last < max) {
            result.append(template, last, max);
        }
        return result.toString();
    }

    /**
     * Format the message from Object array
     * @param templateAndArguments The first item is a template where parameters are located by {@code "{}"}
     * text and the next arguments are optional parameters of the template.
     * @return In case the argument have no length, the result message is {@code null}.
     */
    @Nullable
    public String formatMsg(@Nullable final Object... templateAndArguments) {
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
     * @param writer Writer
     * @param value Values
     */
    protected void writeValue(@Nullable final Object value, @Nonnull final CharArrayWriter writer, final boolean mark) {
        if (mark) {
            writer.append(value != null
                    ? value.toString()
                    : String.valueOf(value));
        } else {
           if (value instanceof Throwable) {
            writer.append('\n');
            ((Throwable)value).printStackTrace(new PrintWriter(writer, true));
           } else {
               writer.append(SEPARATOR);
               writer.append(String.valueOf(value));
           }
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
     * @param arguments Optional arguments
     * @return
     */
    @Nonnull
    public static String format
        ( @Nullable final String messageTemplate
        , @Nullable final Object... arguments)
        {
            return new MsgFormatter().formatMsg(messageTemplate, arguments);
        }

    /**
     * Format the message from Object array
     * @param templateAndArguments The first item is a template where parameters are located by {@code "{}"}
     * text and the next arguments are optional parameters of the template.
     * @return In case the argument have no length, the result message is {@code null}.
     */
    @Nullable
    public static String format(@Nullable final Object... templateAndArguments) {
        return new MsgFormatter().formatMsg(templateAndArguments);
    }
}
