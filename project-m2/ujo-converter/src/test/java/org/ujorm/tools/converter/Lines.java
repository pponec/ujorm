package org.ujorm.tools.converter;

import org.jetbrains.annotations.NotNull;

/** Helper class for iterating over text lines. */
public class Lines {

    private final String[] lines;
    private int index = 0;

    /** Creates a new instance from the specified lines. */
    public Lines(String... lines) {
        this.lines = lines;
    }

    /** Returns the next line and increments the index, or an empty string. */
    @NotNull
    public String next() {
        return index < lines.length ? lines[index++] : "";
    }

    /** Sets the index and returns the line at that position. Zero index takes the last items. */
    @NotNull
    public String get(int index) {
        this.index = index < 0 ? lines.length + index : index;
        return next();
    }

    /** Creates a new instance from a multiline text. */
    public static Lines of(String multilineText) {
        var array = multilineText.lines().toArray(String[]::new);
        return new Lines(array);
    }

    /** Creates a new instance from a multiline text with replaced quotes. */
    public static Lines ofQuoted(String multilineText) {
        var array = multilineText.replace('"', '\'')
                .lines()
                .toArray(String[]::new);
        return new Lines(array);
    }

    /** Count or the rows */
    public int size() {
        return lines.length;
    }

    @Override
    public String toString() {
        return String.join("\n", lines);
    }
}
