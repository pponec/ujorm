package tools.common;

import java.io.*;
import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Represents one valid Unicode character (code point).
 * Immutable and type-safe alternative to the primitive `int` code point.
 * @author Pavel Ponec, https://github.com/pponec/
 */
public final class UnicodeCharacter
        implements Comparable<UnicodeCharacter>, Serializable {

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
     * @throws IOException if an I/O error occurs
     */
    public void writeTo(final Writer writer) throws IOException {
        writer.write(Character.toChars(codePoint));
    }

    /** Create a Stream of the UnicodeCharacter objects */
    public static Stream<UnicodeCharacter> stream(final CharSequence text) {
        if (text == null) throw new IllegalArgumentException("text is required");
        return text.codePoints().mapToObj(c -> of(c));
    }

    /** Create a Stream of the UnicodeCharacter objects */
    public static Stream<UnicodeCharacter> stream(InputStream inputStream, Charset charset) {
        final var reader = new InputStreamReader(inputStream, charset);
        final var iterator = new PrimitiveIterator.OfInt() {
            int next = -1;
            boolean done = false;

            @Override
            public boolean hasNext() {
                if (done) return false;
                if (next != -1) return true;
                try {
                    next = reader.read();
                    if (next == -1) {
                        done = true;
                        return false;
                    }
                    if (Character.isHighSurrogate((char) next)) {
                        int low = reader.read();
                        if (low == -1) {
                            throw new IllegalArgumentException("Unpaired high surrogate at end of stream");
                        }
                        next = Character.toCodePoint((char) next, (char) low);
                    }
                    return true;
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public int nextInt() {
                if (!hasNext()) throw new NoSuchElementException();
                int result = next;
                next = -1;
                return result;
            }
        };

        var result = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.NONNULL),
                        false)
                .map(UnicodeCharacter::of);

        return result.onClose(() -> {
            try {
                reader.close();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    /** The factory method allows to generate different implementations in the future according to the parameter. */
    public static UnicodeCharacter of(final int codePoint) {
        return new UnicodeCharacter(codePoint);
    }

    /** Get a Unicode character at the index
     *
     * @param index Index of the character in the text, negative value takes characters from the end.
     * @param text Text resource.
     * @return Unicode character object.
     * @see #charCount(String) returns a limit of the index.
     */
    public static UnicodeCharacter charAt(final int index, final CharSequence text) {
        if (text == null) throw new IllegalArgumentException("text is required");
        final var idx = index < 0 ? charCount(text.toString()) + index : index;
        final var offset = Character.offsetByCodePoints(text, 0, idx);
        final var codePoint = Character.codePointAt(text, offset);
        return new UnicodeCharacter(codePoint);
    }

    /** Returns count of the Unicode characters of the text */
    public static int charCount(String text) {
        return text.codePointCount(0, text.length());
    }

    /** Compare two objects */
    public int compareTo(UnicodeCharacter other) {
        return Integer.compare(this.codePoint, other.codePoint);
    }
}

