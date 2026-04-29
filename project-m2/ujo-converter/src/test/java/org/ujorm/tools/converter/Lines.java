package org.ujorm.tools.converter;

import org.jetbrains.annotations.NotNull;

/** Helper class for iterating over text lines. */
public class Lines {

    private final String[] lines;
    private int index = 0;
    private final boolean quoted;

    /** Creates a new instance from the specified lines. */
    public Lines(String... lines) {
        this(false, lines);
    }

    private Lines(boolean quoted, String... lines) {
        this.quoted = quoted;
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

    /**
     * Vyhledá v textu první řádek, který se shoduje na substring.
     * Pokud ho najde, nastaví index na další řádek a vrátí true.
     * Jinak vrátí false.
     */
    public boolean findLine(String line) {
        var searchStr = quoted ? line.replace('"', '\'') : line;
        for (var i = 0; i < lines.length; i++) {
            if (lines[i].contains(searchStr)) {
                this.index = i + 1;
                return true;
            }
        }
        return false;
    }

    /** Creates a new instance from a multiline text. */
    public static Lines of(String multilineText) {
        var array = multilineText.lines().toArray(String[]::new);
        return new Lines(false, array);
    }

    /** Creates a new instance from a multiline text with replaced quotes. */
    public static Lines ofQuoted(String multilineText) {
        var array = multilineText.replace('"', '\'')
                .lines()
                .toArray(String[]::new);
        return new Lines(true, array);
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