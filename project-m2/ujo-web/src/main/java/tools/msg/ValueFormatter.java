/*
 * Copyright 2017-2022 Pavel Ponec, https://github.com/pponec
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
package tools.msg;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.function.Supplier;

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
@Unmodifiable
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
    protected ValueFormatter(@NotNull final String mark, @NotNull final String textBorder) {
        super(mark);
        this.valueBorder = textBorder;
    }

    /**
     * Print argument to the Writter with an optional format.
     * @param out Appendable
     * @param value A one value where the {@code Supplier} interface is supported.
     */
    @Override
    protected void writeValue(@Nullable final Object value, @NotNull final Appendable out, final boolean marked) throws IOException  {
        final Object val = value instanceof Supplier
                ? ((Supplier)value).get()
                : value;
        if (!marked) {
            out.append(SEPARATOR);
        }
        if (val == null) {
            out.append(null);
        } else if (val instanceof CharSequence) {
            out.append(valueBorder);
            writeLongValue((CharSequence) val, out);
            out.append(valueBorder);
        } else if (val instanceof Number) {
            out.append(String.valueOf(val));
        } else if (val instanceof Date) {
            out.append(valueBorder);
            final String format = val instanceof java.sql.Date
                    ? "yyyy-MM-dd"
                    : "yyyy-MM-dd'T'HH:mm:ss.SSS";
            out.append(new SimpleDateFormat(format, Locale.ENGLISH).format((Date)val));
            out.append(valueBorder);
        } else if (val instanceof byte[]) {
            out.append(valueBorder);
            writeByteArray((byte[]) val, out);
            out.append(valueBorder);
        } else if (val instanceof Character) {
            out.append(valueBorder);
            out.append((Character) val);
            out.append(valueBorder);
            out.append(((Throwable)val).getMessage());
        } else if (val instanceof Enum) {
            out.append(((Enum) val).name());
        } else if (val instanceof Throwable) {
            out.append(val.getClass().getSimpleName());
            out.append(':');
            out.append(((Throwable)val).getMessage());
        } else {
            writeLongValue(String.valueOf(val), out);
        }
    }

    /** Write bytes as hexa */
    protected void writeByteArray(@NotNull byte[] bytes, @NotNull final Appendable out) throws IOException {
        final int length = bytes != null ? bytes.length : -1; // Length of the bytes
        final int limit = getSizeLimit() >> 1;                // Limit for the bytes
        final int half = (limit - 4) >> 1;
        final int max = length > limit ? half : length;

        for (int i = 0; i < max; i++ ) {
            final int v = bytes[i] & 0xFF;
            out.append(HEX_ARRAY[v >>> 4]);
            out.append(HEX_ARRAY[v & 0x0F]);
        }
        if (length > limit) {
            out.append(THREE_DOTS);
            out.append(String.valueOf(length));
            out.append(THREE_DOTS);

            for (int i = length-half; i < length; i++ ) {
                final int v = bytes[i] & 0xFF;
                out.append(HEX_ARRAY[v >>> 4]);
                out.append(HEX_ARRAY[v & 0x0F]);
            }
        }
    }

    /** You can call the method from a child class */
    protected void writeLongValue(@NotNull final CharSequence value, @NotNull final Appendable out) throws IOException {
        final int length = value != null ? value.length() : -1;
        final int limit = getSizeLimit();
        final int half = (limit - 4) >> 1;
        if (length > limit) {
            out.append(value, 0, half);
            out.append(THREE_DOTS);
            out.append(String.valueOf(length));
            out.append(THREE_DOTS);
            out.append(value, length - half, length);
        } else {
            out.append(value);
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
    @NotNull
    public static <T> String format
    ( @Nullable final String messageTemplate
    , @Nullable final T... arguments) {
        try {
            return new ValueFormatter().formatMsg(null, messageTemplate, arguments);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
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
    @NotNull
    public static <T> String formatSql
    ( @Nullable final String sqlTemplate
    , @Nullable final T... arguments) {
        try {
            return new ValueFormatter("?", "'").formatMsg(null, sqlTemplate, arguments);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
