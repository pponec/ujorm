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
import java.text.SimpleDateFormat;
import java.util.Date;
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
public class ValueFormatter extends MsgFormatter {
    
    /** Three dots symbol */
    private static final char THREE_DOTS = '…';
    
    /** Hexa characters */
    private final char[] HEX_ARRAY = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    
    /** Border of the byte array */
    protected final String valueBorder; 

    /** Static methods are available only */
    protected ValueFormatter() {
        super();
        this.valueBorder = "";
    }
    
    /** Static methods are available only */
    protected ValueFormatter(@Nonnull final String mark, @Nonnull final String textBorder) {
        super(mark);
        this.valueBorder = textBorder;
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
        if (value == null) {
            writer.append(null);
        } else if (value instanceof CharSequence) {
            writer.append(valueBorder);
            writeLongValue(value.toString(), writer);
            writer.append(valueBorder);
        } else if (value instanceof Number) {
            writer.append(String.valueOf(value));
        } else if (value instanceof Date) {
            writer.append(valueBorder);
            final String format = value instanceof java.sql.Date
                    ? "yyyy-MM-dd" 
                    : "yyyy-MM-dd'T'HH:mm:ss.SSS";
            writer.append(new SimpleDateFormat(format, Locale.ENGLISH).format((Date)value));
            writer.append(valueBorder);
        } else if (value instanceof byte[]) {
            writer.append(valueBorder);
            writeByteArray((byte[]) value, writer);
            writer.append(valueBorder);
        } else if (value instanceof Character) {
            writer.append(valueBorder);
            writer.append((Character) value);
            writer.append(valueBorder);
            writer.append(((Throwable)value).getMessage());
        } else if (value instanceof Throwable) {
            writer.append(value.getClass().getSimpleName());
            writer.append(':');
            writer.append(((Throwable)value).getMessage());
        } else {
            writeLongValue(String.valueOf(value), writer);
        }
    }
    
    /** Write bytes as hexa */
    protected void writeByteArray(@Nonnull byte[] bytes, @Nonnull final CharArrayWriter writer) {
        final int length = bytes != null ? bytes.length : -1; // Length of the bytes
        final int limit = getSizeLimit() >> 1;                // Limit for the bytes
        final int half = (limit - 4) >> 1;
        final int max = length > limit ? half : length;

        for (int i = 0; i < max; i++ ) {
            final int v = bytes[i] & 0xFF;
            writer.append(HEX_ARRAY[v >>> 4]);
            writer.append(HEX_ARRAY[v & 0x0F]);
        }
        if (length > limit) {
            writer.append(THREE_DOTS);
            writer.append(String.valueOf(length));
            writer.append(THREE_DOTS);            
            
            for (int i = length-half; i < length; i++ ) {
                final int v = bytes[i] & 0xFF;
                writer.append(HEX_ARRAY[v >>> 4]);
                writer.append(HEX_ARRAY[v & 0x0F]);
            }
        }
    }
    
    /** You can call the method from a child class */
    protected void writeLongValue(@Nonnull final String value, @Nonnull final CharArrayWriter writer) {
        final int length = value != null ? value.length() : -1;
        final int limit = getSizeLimit();
        final int half = (limit - 4) >> 1;
        if (length > limit) {
            writer.append(value, 0, half);
            writer.append(THREE_DOTS);
            writer.append(String.valueOf(length));
            writer.append(THREE_DOTS);
            writer.append(value, length - half, length);
        } else {
            writer.append(value);
        }
    }
    
    /** Default lenhth is 32*/
    protected int getSizeLimit() {
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
        return new ValueFormatter().formatMsg(messageTemplate, arguments);
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
        return new ValueFormatter("?", "\'").formatMsg(sqlTemplate, arguments);
    }

}
