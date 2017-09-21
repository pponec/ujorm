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
 * @author Pavel Ponec
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
     * Format the message from Object array
     * @param templateWithArguments The first argument is a template where parameters are located by {@code "{}"}
     * text and the next arguments are optional parameters of the template.
     * @return In case the argument have not a length, the result is {@code null}.
     */
    @Nullable
    public static String format(@Nullable final Object... templateWithArguments) {
        if (Check.hasLength(templateWithArguments)) {
            final String template = String.valueOf(templateWithArguments[0]);
            final Object[] params = new Object[templateWithArguments.length - 1];
            System.arraycopy(templateWithArguments, 1, params, 0, params.length);
            return format(template, params);
        } else {
            return null;
        }
    }

    /**
     * Format the message
     * @param template Template where argument position is marked by the {@code {}} characters.
     * @param arguments
     * @return
     */
    public static String format(@Nonnull final String template, @Nullable Object... arguments) {
        if (!Check.hasLength(arguments)) {
            return template;
        }

        final StringBuilder result = new StringBuilder(Math.max(64, template.length() * 2));
        int max = template.length();
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

}
