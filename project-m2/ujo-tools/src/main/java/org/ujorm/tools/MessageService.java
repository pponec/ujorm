/*
 * Copyright 2012-2017 Pavel Ponec, https://github.com/pponec
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
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Message Service. See the next example
 * <pre class="pre">
 *  final MessageService service = new MessageService();
 *  final MessageArg&lt;String&gt; NAME = new MessageArg&lt;&gt;("NAME");
 *  final MessageArg&lt;String&gt; TYPE = new MessageArg&lt;&gt;("TYPE");
 *
 *  String expResult = "The ORM framework Ujorm.";
 *  String expTemplate = "The ${TYPE} framework ${NAME}.";
 *  String template = service.template("The ", TYPE, " framework ", NAME, ".");
 *  Map<String, Object> args = service.map
 *      ( TYPE, "ORM"
 *      , NAME, "Ujorm");
 *  String result = service.format(template, args);
 *  assertEquals(expTemplate, template);
 *  assertEquals(expResult, result);
 * </pre>
 * @author Pavel Ponec
 * @since 1.53
 */
@Immutable
public class MessageService {

    /** Two-character mark ("${") to introducing a template argument. */
    public static final String PARAM_BEG = "${";
    /** The mark ("}") to finishing a template argument. */
    public static final char PARAM_END = '}';

    /** Default locale */
    @Nonnull
    protected final Locale defaultLocale;

    /** Create new instance with the {@code Locale.ENGLISH} */
    public MessageService() {
        this(Locale.ENGLISH);
    }

    public MessageService(@Nonnull final Locale defaultLocale) {
        Assert.notNull(defaultLocale);
        this.defaultLocale = defaultLocale;
    }

    /** Create a map from man pairs key-value
     * @param args Key-value pairs
     */
    public Map<String, Object> map(@Nonnull final Object... args) {
        final int max = args.length;
        final Map<String, Object> result = new HashMap(max >> 1);
        for (int i = 1; i < max; i += 2) {
            result.put(convertKey(args[i - 1]), args[i]);
        }
        return result;
    }

    /** Create a message template from argument pairs key-value
     * @param args Sequence of the Objects and Arguments
     */
    public String template(@Nonnull final Object... args) {
        final StringBuilder result = new StringBuilder(256);
        for (Object arg : args) {
            result.append(arg);
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
     * @return Target result
     * @see Formatter
     */
    public final String format(@Nullable final String msg, @Nullable final Map<String, Object> args) {
        return format(msg, args, null);
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
     * @param args Key-value map arguments where arguments type of {@link Supplier} ares supported.
     * @param locale The target locale for an argument format, the {@code null} locale will be replaced by the {@code defaultLocale}.
     * @return Target result
     * @see Formatter
     */
    public final String format(@Nullable final String msg, @Nullable final Map<String, Object> args, @Nullable Locale locale) {
        if (msg == null || args == null) {
            return String.valueOf(msg);
        }
        final int max = msg.length();
        final CharArrayWriter result = new CharArrayWriter(Math.max(32, max + (max >> 1)));
        int i, last = 0;
        while ((i = msg.indexOf(PARAM_BEG, last)) >= 0) {
            final int end = msg.indexOf(PARAM_END, i);
            final String expr = msg.substring(i + PARAM_BEG.length(), end);
            final int formatIndex = expr.indexOf(',');
            final String key = expr.substring(0, formatIndex >= 0 ? formatIndex : expr.length());
            final Object value = args.get(key);
            final Object val = value instanceof Supplier
                ? ((Supplier)value).get()
                : value;
            if (val != null) {
                result.append(msg, last, i);
                if (formatIndex > 0) {
                    new Formatter(result, locale != null ? locale : defaultLocale).format
                          ( expr.substring(1 + formatIndex)
                          , val, val, val, val, val, val); // Simplify Date format
                } else {
                    writeValue(val, result, locale);
                }
            } else {
                result.append(msg, last, end + 1);
            }
            last = end + 1;
        }
        result.append(msg, last, max);
        return result.toString();
    }

     /** Convert value.
     * The method can be overwrited for special data types, for example: {@code Key -> Key.getFullName() }.
     */
    @Nullable
    protected String convertKey(@Nonnull final Object key) {
        return key instanceof MessageArg
            ? ((MessageArg)key).getName()
            : key.toString();
    }

    /** Write a value to the output buffer.
     * The method can be overwrited to escaping values.
     *  The method can be overwrited for special data types.
     */
    protected void writeValue
        ( @Nonnull final Object value
        , @Nonnull final CharArrayWriter writer
        , @Nullable final Locale locale
    ) {
        if (value instanceof Throwable) {
            ((Throwable)value).printStackTrace(new PrintWriter(writer, true));
        } else {
            writer.append(value.toString());
        }
    }

    // ---------------- STATIC METHOD ----------------

    /** Format a target message by a template with arguments */
    public static final String formatMsg(@Nullable final String template, @Nullable final Map<String, Object> args) {
        return new MessageService().format(template, args);
    }

}
