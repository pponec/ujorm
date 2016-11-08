/*
 *  Copyright 2012-2014 Pavel Ponec
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
package org.ujorm.validator;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.ujorm.Key;

/**
 * Message Service
 * @author Pavel Ponec
 */
public class MessageService {

    /** Two-character mark ("${") to introducing a template argument. */
    protected static final String PARAM_BEG = "${";
    /** The mark ("}") to finishing a template argument. */
    protected static final char PARAM_END = '}';

    /** Create a map from man pairs key-value
     * @param args Key-value pairs
     */
    public Map<String, Object> map(Object... args) {
        int max = args.length >> 1;
        final Map<String, Object> result = new HashMap(max + 3);
        for (int j = 0; j < max; j++) {
            final int i = j << 1;
            final Object value = args[i + 1];
            result.put(args[i].toString(), value instanceof Key ? ((Key) value).getFullName() : value);
        }
        return result;
    }

    /** Create a message template from argument pairs key-value
     * @param args Sequence of the Objects and Arguments
     */
    public String template(Object... args) {
        final StringBuilder result = new StringBuilder(256);
        for (Object arg : args) {
            if (arg instanceof MessageArg) {
                result.append(PARAM_BEG).append(arg).append(PARAM_END);
            } else {
                result.append(arg);
            }
        }
        return result.toString();
    }

    /**
     * Format a template message using named variables.
     * Each variable must be surrounded by two marks "${" and "}".
     * The first mark is forbidden in a common text and can be replaced by the variable #{MARK}.
     * @param msg Template message, see the simple example:
     * <pre class="pre">{@code "The input date ${KEY,%s} must be less than: ${DATE,%F}"}</pre>
     * or
     * <pre class="pre">{@code "The input date ${KEY,%s} must be less than: ${DATE,%tY-%tm-%td %tH:%tM:%tS}"}</pre>
     * The format expression is separated by the character (,) a and it is not mandatory.
     * @param args Key-value map arguments
     * @param locale The target locale for an argument format, the {@code null} locale will be replaced by the ENGLISH.
     * @return Target result
     * @see Formatter
     */
    protected final String format(final String msg, final Map<String, Object> args, Locale locale) {
        if (msg == null || args == null) {
            return String.valueOf(msg);
        }
        if (locale == null) {
            locale = Locale.ENGLISH;
        }
        final StringBuffer result = new StringBuffer(255);
        int i, last = 0;
        while ((i = msg.indexOf(PARAM_BEG, last)) >= 0) {
            final int end = msg.indexOf(PARAM_END, i);
            final String expr = msg.substring(i + PARAM_BEG.length(), end);
            final int formatIndex = expr.indexOf(',');
            final String key = expr.substring(0, formatIndex >= 0 ? formatIndex : expr.length());
            final Object value = args.get(key);
            if (value != null) {
                result.append(msg.substring(last, i));
                final Object niceValue = formatIndex > 0
                    ? new Formatter(locale).format(expr.substring(1 + formatIndex)
                    , value, value, value, value, value, value) // Simplify Date format
                    : value;
                appendValue(niceValue.toString(), result);
            } else {
                result.append(msg.substring(last, end + 1));
            }
            last = end + 1;
        }
        result.append(msg.substring(last));
        return result.toString();
    }

    /** Append a value to the output buffer.
     * The method can be overwrited to escaping values.
     */
    protected void appendValue(final String value, final StringBuffer result) {
        result.append(value);
    }
}
