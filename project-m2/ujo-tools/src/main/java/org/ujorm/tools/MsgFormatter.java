/*
 *  Copyright 2017-2017 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.tools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
public abstract class MsgFormatter {

    /** Parameter mark */
    protected static final String MARK = "{}";
    /** Separator */
    protected static final String SEPARATOR = ", ";

    /** Static methods are available only */
    private MsgFormatter() {
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
    public static String format
        ( @Nullable final String messageTemplate
        , @Nullable final Object... arguments)
        {
        final String template = messageTemplate != null ? messageTemplate : String.valueOf(messageTemplate);
        if (!Check.hasLength(arguments)) {
            return template;
        }

        final StringBuilder result = new StringBuilder(Math.max(64, template.length() * 2));
        final int max = template.length();
        int last = 0;

        for (Object arg : arguments) {
            final int i = template.indexOf(MARK, last);
            if (i >= last) {
                result.append(template, last, i);
                last = i + MARK.length();
            } else {
                if (last < max) {
                    result.append(template, last, max);
                    last = max;
                }
                result.append(SEPARATOR);
            }
            result.append(arg);
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
     * @return In case the argument have not a length, the result is {@code null}.
     */
    @Nonnull
    public static String format(@Nullable final Object... templateAndArguments) {
        if (Check.hasLength(templateAndArguments)) {
            final String template = String.valueOf(templateAndArguments[0]);
            final Object[] params = new Object[templateAndArguments.length - 1];
            System.arraycopy(templateAndArguments, 1, params, 0, params.length);
            return format(template, params);
        } else {
            return format (null, templateAndArguments);
        }
    }
}
