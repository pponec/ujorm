/*
 * Copyright 2012-2022 Pavel Ponec, https://github.com/pponec
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
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.ujorm.tools.Assert;
import org.ujorm.tools.common.ObjectUtils;

/**
 * Message Service. See the next example:
 * <pre class="pre">
 *  final MessageArg TYPE = MessageArg.of("TYPE");
 *  final MessageArg NAME = MessageArg.of("NAME");
 *
 *  String expResult = "The ORM framework Ujorm.";
 *  String template = "The " + TYPE + " framework " + NAME + ".";
 *  String result = MessageService.formatMsg(template, TYPE, "ORM", NAME, "Ujorm");
 *  assertEquals(expResult, result);
 * </pre>
 *
 * or an similar usage:
 *
 * <pre class="pre">
 *  final MessageArg NAME = MessageArg.of("NAME");
 *  final MessageArg TYPE = MessageArg.of("TYPE");
 *
 *  String expResult = "The ORM framework Ujorm.";
 *  String expTemplate = "The ${TYPE} framework ${NAME}.";
 *  String template = service.template("The ", TYPE, " framework ", NAME, ".");
 *
 *  Map&lt;String, Object&gt; args = new HashMap&lt;&gt;();
 *  args.put(TYPE.name(), "ORM");
 *  args.put(NAME.name(), "Ujorm");
 *
 *  String result = service.format(template, args);
 *  assertEquals(expTemplate, template);
 *  assertEquals(expResult, result);
 * </pre>
 *
 * @author Pavel Ponec
 * @since 1.53
 * @see MessageArg
 */
@Unmodifiable
public class MessageService {

    /** Two-character mark ("${") to introducing a template argument. */
    protected final String begTag ;
    /** The mark ("}") to finishing a template argument. */
    protected final char endTag;

    /** Default locale */
    @NotNull
    protected final Locale defaultLocale;

    /** Create new instance with the {@code Locale.ENGLISH} */
    public MessageService() {
        this(MessageArg.PARAM_BEG, MessageArg.PARAM_END, Locale.ENGLISH);
    }

    public MessageService(
            @NotNull final String begTag,
            @NotNull final char endTag,
            @NotNull final Locale defaultLocale) {
        this.begTag = Assert.hasLength(begTag, "begTag");
        this.endTag = endTag;
        this.defaultLocale = Assert.notNull(defaultLocale, "defaultLocale");
    }

    /** Create a map from man pairs key-value
     * @param args Key-value pairs
     */
    public <T> Map<String, Object> map(@NotNull final T... args) {
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
    public <T> String template(@NotNull final T... args) {
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
     * <pre class="pre">"The input date ${KEY,%s} must be less than: ${DATE,%F}"</pre>
     * or
     * <pre class="pre">"The input date ${KEY,%s} must be less than: ${DATE,%tY-%tm-%td %tH:%tM:%tS}"</pre>
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
     * <pre class="pre">"The input date ${KEY,%s} must be less than: ${DATE,%F}"</pre>
     * or
     * <pre class="pre">"The input date ${KEY,%s} must be less than: ${DATE,%tY-%tm-%td %tH:%tM:%tS}"</pre>
     * The format expression is separated by the character (,) a and it is not mandatory.
     * @param locale The target locale for an argument format, the {@code null} locale will be replaced by the {@code defaultLocale}.
     * @param key The Key (see a {@link MessageArg})
     * @param value The Value
     * @param keyValuePairs Key-value pairs
     * @see Formatter
     */
    public final String format(
            @Nullable final String msg,
            @Nullable final Locale locale,
            @NotNull final CharSequence key,
            @Nullable final Object value,
            @NotNull final Object... keyValuePairs) {

        final Map<String, Object> map = map(keyValuePairs);
        map.put(convertKey(key), value);
        return format(msg, map, locale);
    }

    /**
     * Format a template message using named variables.
     * Each variable must be surrounded by two marks "${" and "}".
     * The first mark is forbidden in a common text and can be replaced by the variable #{MARK}.
     * @param msg Template message, see the simple example:
     * <pre class="pre">"The input date ${KEY,%s} must be less than: ${DATE,%F}"</pre>
     * or
     * <pre class="pre">"The input date ${KEY,%s} must be less than: ${DATE,%tY-%tm-%td %tH:%tM:%tS}"</pre>
     * The format expression is separated by the character (,) a and it is not mandatory.
     * @param args Key-value map arguments where arguments type of {@link Supplier} ares supported.
     * @param locale The target locale for an argument format, the {@code null} locale will be replaced by the {@code defaultLocale}.
     * @return The result message or an empty String if the writter is available.
     * @see Formatter
     */
    public final String format(
            @Nullable final String msg,
            @Nullable final Map<String, Object> args,
            @Nullable Locale locale) {
        try {
            return format(null, msg, args, locale);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Format a template message using named variables.
     * Each variable must be surrounded by two marks "${" and "}".
     * The first mark is forbidden in a common text and can be replaced by the variable #{MARK}.
     * @param writer An optional writer.
     * @param msg Template message, see the simple example:
     * <pre class="pre">"The input date ${KEY,%s} must be less than: ${DATE,%F}"</pre>
     * or
     * <pre class="pre">"The input date ${KEY,%s} must be less than: ${DATE,%tY-%tm-%td %tH:%tM:%tS}"</pre>
     * The format expression is separated by the character (,) a and it is not mandatory.
     * @param args Key-value map arguments where arguments type of {@link Supplier} ares supported.
     * @param locale The target locale for an argument format, the {@code null} locale will be replaced by the {@code defaultLocale}.
     * @return The result message of an empty string of writter is available.
     * @see Formatter
     * @deprecated Use the {@link #formatMsg(String, Map, Appendable)} rather.
     */
    @Deprecated
    public final String format(
            @Nullable final Appendable writer,
            @Nullable final String msg,
            @Nullable final Map<String, Object> args,
            @Nullable Locale locale) throws IOException {
        if (msg == null || args == null) {
            return String.valueOf(msg);
        }
        final int max = msg.length();
        final Appendable result = writer != null ? writer : new StringBuilder(Math.max(32, max + (max >> 1)));
        format(msg, args, locale, result);
        return writer == null ? result.toString() : "";
    }

    /**
     * Format a template message using named variables.
     * Each variable must be surrounded by two marks "${" and "}".
     * The first mark is forbidden in a common text and can be replaced by the variable #{MARK}.
     * @param msg Template message, see the simple example:
     * <pre class="pre">"The input date ${KEY,%s} must be less than: ${DATE,%F}"</pre>
     * or
     * <pre class="pre">"The input date ${KEY,%s} must be less than: ${DATE,%tY-%tm-%td %tH:%tM:%tS}"</pre>
     * The format expression is separated by the character (,) a and it is not mandatory.
     * @param args Key-value map arguments where arguments type of {@link Supplier} ares supported.
     * @param locale The target locale for an argument format, the {@code null} locale will be replaced by the {@code defaultLocale}.
     * @param writer A required writer.
     * @return The result message of an empty string of writter is available.
     * @see Formatter
     */
    public final void format(
            @Nullable final String msg,
            @Nullable final Map<String, Object> args,
            @Nullable Locale locale,
            @NotNull final Appendable writer
    ) throws IOException  {
        if (msg == null || args == null) {
            writer.append(String.valueOf(msg));
        }
        final int max = msg.length();
        int i, last = 0;
        while ((i = msg.indexOf(begTag, last)) >= 0) {
            final int end = msg.indexOf(endTag, i);
            final String expr = msg.substring(i + begTag.length(), end);
            final int formatIndex = expr.indexOf(',');
            final String key = expr.substring(0, formatIndex >= 0 ? formatIndex : expr.length());
            final Object value = args.get(key);
            final Object val = value instanceof Supplier
                ? ((Supplier)value).get()
                : value;
            if (val != null) {
                writer.append(msg, last, i);
                if (formatIndex > 0) {
                    new Formatter(writer, locale != null ? locale : defaultLocale).format
                          ( expr.substring(1 + formatIndex)
                          , val, val, val, val, val, val); // Simplify Date format
                } else {
                    writeValue(val, writer, locale);
                }
            } else {
                writer.append(msg, last, end + 1);
            }
            last = end + 1;
        }
        writer.append(msg, last, max);
    }

     /** Convert value.
     * The method can be overwrited for special data types, for example: {@code Key -> Key.getFullName() }.
     */
    @Nullable
    protected String convertKey(@NotNull final Object key) {
        return key instanceof MessageArg
            ? ((MessageArg)key).getName()
            : key.toString();
    }

    /** Write a value to the output buffer.
     * The method can be overwrited to escaping values.
     *  The method can be overwrited for special data types.
     */
    protected void writeValue
        ( @NotNull final Object value
        , @NotNull final Appendable writer
        , @Nullable final Locale locale
        ) throws IOException {
        if (value instanceof Throwable) {
            ((Throwable)value).printStackTrace(ObjectUtils.toPrintWriter(writer));
        } else {
            writer.append(value.toString());
        }
    }

    // ---------------- STATIC METHOD ----------------

    /** Format a target message by a template with arguments */
    public static final String formatMsg(@Nullable final String template, @Nullable final Map<String, Object> args) {
        return new MessageService().format(template, args);
    }

    /** Format a target message by a template with arguments type of Map */
    public static final String formatMsg(
            @Nullable final String template,
            @NotNull final CharSequence key,
            @Nullable final Object value,
            @NotNull final Object... keyValuePairs) {
        return new MessageService().format(template, null, key, value, keyValuePairs);
    }

    /** Format a target message by a template with arguments */
    public static final void formatMsg(@Nullable final String template, @Nullable final Map<String, Object> args, @NotNull Appendable writer) {
        try {
            new MessageService().format(template, args, Locale.ENGLISH, writer);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
