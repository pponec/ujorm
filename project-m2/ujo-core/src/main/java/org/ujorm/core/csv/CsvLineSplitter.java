package org.ujorm.core.csv;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * An interface for splitting a CSV line into an array of string values.
 */
public interface CsvLineSplitter {

    /**
     * Splits a given text into an array of strings based on the implementation's delimiter logic.
     * * @param text The input string to be split.
     * @param maxFields The maximum number of fields (substrings) to extract from the text.
     * Extra fields are typically ignored.
     * @return An array of parsed string values. Never returns null.
     */
    @NotNull
    String[] split(String text, int maxFields);

    /** Very fast simple splitter dynamically resizing array if the capacity is exceeded. */
    @NotNull
    static CsvLineSplitter ofSimple(final char delimiter) {
        return (text, maxFields) -> {
            if (text == null || text.isEmpty()) return new String[0];

            var result = new String[maxFields];
            var count = 0;
            var start = 0;

            while (true) {
                if (count == result.length) result = Arrays.copyOf(result, 2 + (count << 1));
                var end = text.indexOf(delimiter, start);

                result[count++] = end < 0 ? text.substring(start) : text.substring(start, end);
                if (end < 0) break;

                start = end + 1;
            }
            return count == result.length ? result : Arrays.copyOf(result, count);
        };
    }

    /**
     * Fast CSV line splitter supporting optional quotes around fields and quote character.
     * Properly ignores delimiters enclosed within double quotes and
     * unescapes inner double quotes ("" to ").
     * Optimized by avoiding regex and iterating characters sequentially.
     *
     * @param delimiter The character used to separate fields.
     * @param quoteChar The character used to quote fields.
     * @return A CsvLineSplitter instance.
     */
    @NotNull
    static CsvLineSplitter ofQuoted(final char delimiter, final char quoteChar) {
        final var quoteStr = String.valueOf(quoteChar);
        final var doubleQuoteStr = quoteStr + quoteStr;
        return (text, maxFields) -> {
            if (text == null || text.isEmpty()) return new String[0];

            var result = new String[maxFields];
            var count = 0;
            var start = 0;
            var inQuotes = false;
            var len = text.length();

            // Iterate up to len (inclusive) to process the final field smoothly
            for (var i = 0; i <= len && count < maxFields; i++) {
                var isEnd = (i == len);
                var c = isEnd ? '\0' : text.charAt(i);
                if (c == quoteChar) {
                    inQuotes = !inQuotes;
                } else if ((c == delimiter && !inQuotes) || isEnd) {
                    var s = start;
                    var e = i;
                    // Strip surrounding quotes if present and unescape internal quotes
                    if (e > s && text.charAt(s) == quoteChar && text.charAt(e - 1) == quoteChar) {
                        s++;
                        e--;
                        result[count++] = text.substring(s, e).replace(doubleQuoteStr, quoteStr);
                    } else {
                        result[count++] = text.substring(s, e);
                    }
                    start = i + 1;
                }
            }
            return count == maxFields ? result : Arrays.copyOf(result, count);
        };
    }
}