package org.ujorm.mapper.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

/** A lightweight, fixed-size bit vector backed by a long array for fast bit manipulations. */
public final class BitSet {

    /** Number of bits in a single long word. */
    private static final int BITS_PER_WORD = Long.SIZE;

    private final long[] words;

    /** Creates a fixed-size bit mask based on the total column count. */
    public BitSet(int columnCount) {
        var wordCount = (columnCount + (BITS_PER_WORD - 1)) / BITS_PER_WORD;
        this.words = new long[wordCount];
    }

    /** Sets or clears the modification flag for a specific column index. */
    public void setValue(int columnIndex, boolean isModified) {
        var wordIndex = columnIndex / BITS_PER_WORD;
        var bitPosition = columnIndex % BITS_PER_WORD;

        if (isModified) {
            this.words[wordIndex] |= (1L << bitPosition);
        } else {
            this.words[wordIndex] &= ~(1L << bitPosition);
        }
    }

    /** Checks if the column at the given index was modified. */
    public boolean getValue(int columnIndex) {
        var wordIndex = columnIndex / BITS_PER_WORD;
        var bitPosition = columnIndex % BITS_PER_WORD;
        return (this.words[wordIndex] & (1L << bitPosition)) != 0;
    }

    /** Returns true if the set has no active bit. */
    public boolean isEmpty() {
        for (var word : this.words) {
            if (word != 0L) {
                return false;
            }
        }
        return true;
    }

    /** Returns an array containing the exact indices of all modified columns. */
    public int[] getActive() {
        var activeCount = 0;
        for (var word : this.words) {
            activeCount += Long.bitCount(word);
        }

        if (activeCount == 0) {
            return new int[0];
        }

        var result = new int[activeCount];
        var resultIndex = 0;

        for (var wordIndex = 0; wordIndex < this.words.length; wordIndex++) {
            var mask = this.words[wordIndex];

            while (mask != 0) {
                var bitPosition = Long.numberOfTrailingZeros(mask);
                result[resultIndex++] = (wordIndex * BITS_PER_WORD) + bitPosition;
                mask &= (mask - 1); // Clears the lowest set bit
            }
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BitSet otherMask)) return false;
        return Arrays.equals(this.words, otherMask.words);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.words);
    }

    @Override
    public String toString() {
        return Arrays.stream(getActive())
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(","));
    }

    /** Factory method to create a new instance . */
    public static BitSet of(int columnCount) {
        return new BitSet(columnCount);
    }
}