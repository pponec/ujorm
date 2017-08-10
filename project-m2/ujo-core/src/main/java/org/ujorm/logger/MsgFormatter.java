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
package org.ujorm.logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Formatter of log messages where argument is located by the  {@code {} } characters.
 * @author Pavel Ponec
 */
public class MsgFormatter {

    /** Parameter mark */
    protected static final String MARK = "{}";
    /** Separator */
    protected static final String SEPARATOR = ", ";

    /**
     * Format the message
     * @param template Template where argument position is marked by the {@code {} } characters.
     * @param arguments
     * @return
     */
    public String format(@Nonnull final String template, @Nullable Object ... arguments) {
        if (arguments == null || arguments.length == 0) {
            return template;
        }

        final StringBuilder result = new StringBuilder(Math.max(24, template.length() * 2));
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
