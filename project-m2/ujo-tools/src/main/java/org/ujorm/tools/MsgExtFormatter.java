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
import java.util.Base64;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
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
public class MsgExtFormatter extends MsgFormatter{

    /** Static methods are available only */
    protected MsgExtFormatter() {
        super();
    }
    
    /** Static methods are available only */
    protected MsgExtFormatter(@Nonnull final String mark) {
        super(mark);
    }

    /**
     * Print argument to the Writter with an optional format.
     * @param writer Writer
     * @param value Values
     */
    @Override
    protected void writeValue(@Nullable final Object value, @Nonnull final CharArrayWriter writer, final boolean marked) {
        if (!marked) {
            writer.append(SEPARATOR);        
        }
        if (value instanceof CharSequence) {
           writeLongValue(value.toString(), writer);
        } else if (value instanceof Number) {
            writer.append(String.valueOf(value));
        } else if (value instanceof Date) {
            final String format = value instanceof java.sql.Date
                    ? "%1$tY-%1$tm-%1$td" 
                    : "%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS";
            new Formatter(writer, Locale.ENGLISH).format(format, value);
        } else if (value instanceof byte[]) {
            writeLongValue(Base64.getEncoder().encodeToString((byte[])value), writer);
        } else if (value instanceof Throwable) {
            writer.append(value.getClass().getSimpleName());
            writer.append(':');
            writer.append(((Throwable)value).getMessage());
        } else {
            writeLongValue(String.valueOf(value), writer);
        }
    }
    
    /** You can call the method from a child class */
    protected void writeLongValue(@Nonnull final String value, @Nonnull final CharArrayWriter writer) {
        final int length = value != null ? value.length() : -1;
        final int limit = getStringLimit();
        final int half = (limit - 4) >> 1;
        if (length > limit) {
            writer.append(value, 0, half);
            writer.append("…");
            writer.append(String.valueOf(length));
            writer.append("…");
            writer.append(value, length - half, length);
        } else {
            writer.append(value);
        }
    }
    
    /** Default lenhth is 32*/
    protected int getStringLimit() {
        return 32;
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
    , @Nullable final Object... arguments) {
        return new MsgExtFormatter().formatMsg(messageTemplate, arguments);
    }
    
   /**
     * Format the SQL where makup character is {@code '?'}.
     * <pre class="pre">
     *  assertEquals("TEST"    , MsgFormatter.format("TE?T", "S"));
     *  assertEquals("TE, S, T", MsgFormatter.format("TE", "S", "T"));
     *  assertEquals("TES{}"   , MsgFormatter.format("TE??", "S"));
     * </pre>
     * @param sqlTemplate SQL template where argument position is marked by the {@code '?'} characters.
     * @param arguments Optional arguments
     * @return
     */
    @Nonnull
    public static String formatSql
    ( @Nullable final String sqlTemplate
    , @Nullable final Object... arguments) {
        return new MsgExtFormatter("?").formatMsg(sqlTemplate, arguments);
    }

}
