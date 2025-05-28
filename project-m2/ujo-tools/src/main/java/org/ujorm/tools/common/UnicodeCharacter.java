package org.ujorm.tools.common;

import java.io.IOException;
import java.io.Writer;
import java.util.stream.Stream;

/**
 * Represents one valid Unicode character (code point).
 * Immutable and type-safe alternative to the primitive `int` code point.
 */
public final class UnicodeCharacter {

    /** A character number */
    private final int codePoint;

    private UnicodeCharacter(int codePoint) {
        if (!Character.isValidCodePoint(codePoint)) {
            throw new IllegalArgumentException("Illegal Unicode codePoint: " + codePoint);
        }
        this.codePoint = codePoint;
    }

    /** A character number */
    public int codePoint() {
        return codePoint;
    }

    public boolean isSupplementary() {
        return Character.isSupplementaryCodePoint(codePoint);
    }

    public boolean isEmoji() {
        return Character.getType(codePoint) == Character.OTHER_SYMBOL;
    }

    public boolean isLetter() {
        return Character.isLetter(codePoint);
    }

    public boolean isDigit() {
        return Character.isDigit(codePoint);
    }


    public String toString() {
        return new String(Character.toChars(codePoint));
    }

    /**
     * Checks whether this UnicodeCharacter is equal to the given primitive char.
     *
     * @param c the primitive char to compare with
     * @return true if this UnicodeCharacter represents the same character as the given char
     */
    public boolean equals(final char c) {
        return this.codePoint == c;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        return o instanceof UnicodeCharacter unicode
                && unicode.codePoint == this.codePoint;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(codePoint);
    }

    /**
     * Writes this UnicodeCharacter to the given Writer.
     *
     * @param writer the Writer to write the character to
     * @throws java.io.IOException if an I/O error occurs
     */
    public void writeTo(final Writer writer) throws IOException {
        writer.write(Character.toChars(codePoint));
    }

    /** Create a Stream of the UnicodeCharacter objects */
    public static Stream<UnicodeCharacter> stream(final CharSequence text) {
        if (text == null) throw new IllegalArgumentException("text is required");
        return text.codePoints().mapToObj(c -> of(c));
    }

    /** The factory method allows to generate different implementations in the future according to the parameter. */
    public static UnicodeCharacter of(final int codePoint) {
        return new UnicodeCharacter(codePoint);
    }

    /** Get a Unicode character at the index */
    public static UnicodeCharacter of(final CharSequence text, final int index) {
        if (text == null) throw new IllegalArgumentException("text is required");
        final var length = text.length();
        var i = 0;
        var codePointIndex = 0;

        while (i < length) {
            final var codePoint = Character.codePointAt(text, i);
            if (codePointIndex == index) {
                return new UnicodeCharacter(codePoint);
            }
            i += Character.charCount(codePoint);
            codePointIndex++;
        }

        throw new IndexOutOfBoundsException("" + index);
    }
}

