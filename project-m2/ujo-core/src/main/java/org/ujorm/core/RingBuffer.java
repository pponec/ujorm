/*
 *  Copyright 2001-2014 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.core;

import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * A 'ring buffer' implementation whith a required length of the buffer.
 * See a sample use case of finding some substrings:
 * <pre>
 *    String text = "xxx ${abc} ${def} xyz";
 *    Reader reader = RingBuffer.createReader(text);
 *    String word_1 = RingBuffer.findWord(reader, "${", "}");
 *    assert "abc".equals(word_1)
 *    String word_2 = RingBuffer.findWord(reader, "${", "}");
 *    assert "def".equals(word_2)
 * </pre>

 * @author Pavel Ponec
 * @version 2001-10-01
 * @see #findWord(java.io.Reader, java.lang.String, java.lang.String) Sample of use.
 */
final public class RingBuffer implements CharSequence {

    /** The UTF-8 Charset, see {@link java.nio.charset.StandardCharsets#UTF_8} from Java 7 */
    public static final Charset UTF8 = Charset.forName("UTF-8");
    /** The string content (buffer)*/
    private final char[] b;
    /** Length */
    public final int length;
    /** Start of the string */
    private int pos;

    /**
     * Creates new RingBuffer
     * @param length Length of buffer form 1 to Integer.MAX_INT.
     * @throws IllegalArgumentException If the length is smaller than the one.
     */
    public RingBuffer(int length) throws IllegalArgumentException {
        if (length <= 0) {
            throw new IllegalArgumentException("The RingBufer must not be empty");
        }
        this.length = length;
        this.b = new char[length];
    }

    final public void add(char c) {
        b[pos] = c;
        pos = ++pos % this.length;
    }

    /** Returns a character from position 'i' */
    @Override
    public char charAt(int i) {
        return b[(pos + i) % length];
    }

    /** The method tests if the current object equals to an argument. */
    @Override
    public boolean equals(final Object s) {
        if (s instanceof RingBuffer) {
            final RingBuffer r = (RingBuffer)s;
            return r.length()==this.length
                && equalsInternal(r);
        }
        return false;
    }

    /** The method compare an argument type of CharSequence */
    public boolean equalsSequence(final CharSequence s) {
        return s != null
            && s.length() == this.length
            && equalsInternal(s)
            ;
    }

    /** The equals test for an internal use only.
     * @param s Sequence must not be the {@code null] and must have got the <strong>same length</strong> as the RingBuffer
     * @return Returns true, ir the sequence equalsTo this object.
     * @throws IndexOutOfBoundsException if the argument have not the same length
     */
    @SuppressWarnings("empty-statement")
    protected boolean equalsInternal(final CharSequence s) throws IndexOutOfBoundsException {
        int i;
        for (i = 0; i < this.length && s.charAt(i) == b[(pos + i) % length]; i++);
        return i == length;
    }

    /** Export to the String. */
    @Override
    public String toString() {
        char[] t = new char[length];
        for (int i = 0; i < this.length; i++) {
            t[i] = b[(pos + i) % length];
        }
        return new String(t);
    }

    /** Export to the String */
    public String substring(int begIndex, int endIndex) {
        int i;
        if (endIndex <= begIndex) {
            return "";
        }
        char[] t = new char[endIndex - begIndex];
        for (i = begIndex; i < endIndex; i++) {
            t[i - begIndex] = b[(pos + i) % length];
        }
        return new String(t);
    }

    /** Length of the String */
    @Override
    final public int length() {
        return length;
    }

    /** Get a sub-sequence */
    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    // ============ STATIC METHODS ============

    /**
     * Find a word betveen beg and end text from current cursor and TRIM the result.
     * The method is designed for a very large data source (a character stream).
     * <br/>
     * Sample:
     * <pre>
     *    String text = "xxx ${abc} def";
     *    String word = RingBuffer.findWord(text, "${", "}");
     *    assert "abc".equals(word)
     * </pre>
     * @param inputUtf8 A data stream in the UTF-8 format.
     * @param beg Start tag (text) where the empty value means find end from the current cursor.
     * @param end End tag (text) must not be empty.
     * @return Return a result between beg and end tags (texts). The result is newer NULL.
     * @throws IOException
     */
    public static String findWord(final InputStream inputUtf8, final String beg, final String end) throws IOException {
        return findWord(createReader(inputUtf8, UTF8), beg, end);
    }

    /**
     * Find a word betveen beg and end text from current cursor and TRIM the result.
     * The method is designed for a very large data source (a character stream).
     * <br/>
     * Sample:
     * <pre>
     *    String text = "xxx ${abc} def";
     *    String word = RingBuffer.findWord(text, "${", "}");
     *    assert "abc".equals(word)
     * </pre>
     * @param reader A data source
     * @param beg Start tag (text) where the empty value means find end from the current cursor.
     * @param end End tag (text) must not be empty.
     * @return Return a result between beg and end tags (texts). The result is newer NULL.
     * @throws IOException
     */
    public static String findWord(final Reader reader, final String beg, final String end) throws IOException {
        return findWordNoTrim(reader, beg, end).trim();
    }

    /**
     * Find a word from current cursor betveen a penultimate and the last tag.
     * The method is designed for a very large data source (a character stream).
     * <br/>
     * Sample:
     * <pre>
     *    String text = "xxx ${abc} def";
     *    String word = RingBuffer.findWord(text, "${", "}");
     *    assert "abc".equals(word)
     * </pre>
     * @param reader A data source
     * @param tags a not-null and not-empty text values
     * @return Return a text before the last tag. The result is newer NULL.
     * @throws IOException
     */
    public static String findWord(final Reader reader, final String... tags) throws IOException {
        return findWordNoTrim(reader, tags).trim();
    }

    /**
     * Find a word betveen beg and end text from the source start and trim the result.
     * The method is designed for a very large data source (a character stream).
     * <br/>
     * @param source a Data source
     * @param beg Start tag (text) where the empty value means find end from the current cursor.
     * @param end End tag (text) must not be empty.
     * @return Return a result between beg and end tags (texts). The result is newer NULL.
     * @throws IOException
     */
    public static String findWord(final String source, final String beg, final String end) throws IOException {
        return findWordNoTrim(createReader(source), beg, end).trim();
    }

    /**
     * Find a word betveen beg and end text from the source start.
     * The method is designed for a very large data source (a character stream).
     * <br/>
     * Sample:
     * <pre>
     *    String text = "xxx ${abc} def";
     *    String word = RingBuffer.findWord(text, "${", "}");
     *    assert "abc".equals(word)
     * </pre>
     * @param reader A data source
     * @param beg Start tag (text) where the empty value means find end from the current cursor.
     * @param end End tag (text) must not be empty.
     * @return Return a result between beg and end tags (texts). The result is newer NULL.
     * @throws IOException
     */
    public static String findWordNoTrim(final String source, final String beg, final String end) throws IOException {
        return findWordNoTrim(createReader(source), beg, end);
    }

    /**
     * Find a word betveen beg and end text from current cursor.
     * The method is designed for a very large data source (a character stream).
     * @param reader A data source
     * @param beg The start tag (text) where the empty (or {@code null}) value means find an End from the current cursor.
     * @param end The end tag (text) where the empty (or {@code null}) value means leave the reading and return an empty String.
     * @return Return a result between beg and end tags (texts). The result is newer NULL.
     * @throws IOException
     */
    public static String findWordNoTrim(final Reader reader, String beg, String end) throws IOException {
        boolean begEmpty = beg == null || beg.isEmpty();
        boolean endEmpty = end == null || end.isEmpty();

        if (begEmpty && endEmpty) {
            return "";
        }
        if (begEmpty) {
            return findWordNoTrim(reader, end);
        }
        if (endEmpty) {
            findWordNoTrim(reader, beg);
            return "";
        }
        return findWordNoTrim(reader, new String[]{beg, end});
    }

    /**
     * Find a word from current cursor betveen a penultimate and the last tag.
     * The method is designed for a very large data source (a character stream).
     * @param reader A data source
     * @param tags a not-null and not-empty text values
     * @return Return a text before the last tag. The result is newer NULL.
     * @throws IOException
     */
    @SuppressWarnings("empty-statement")
    public static String findWordNoTrim(final Reader reader, final String... tags) throws IOException {
        if (tags.length == 0) {
            return "";
        }
        final StringBuilder result = new StringBuilder(64);
        int i = 0, last = tags.length - 1;
        String tag = tags[i];
        RingBuffer ring = new RingBuffer(tag.length());

        int c;
        while ((c = reader.read()) != -1) {
            ring.add((char) c);

            if (ring.equalsInternal(tag)) {
                if (i==last) {
                    // Remove a part of the the finish tag:
                    if (ring.length>1 && result.length()>0) {
                        result.setLength(result.length() - ring.length + 1);
                    }
                    return result.toString();
                } else {
                    tag = tags[++i];
                    if (tag.isEmpty()) {
                        break;
                    }
                    ring = new RingBuffer(tag.length());
                }
            } else if (i==last) {
                result.append((char) c);
            }
        }
        return "";
    }

    /** Create Reader from the UTF-8 encode source */
    public static Reader createReader(File file) throws FileNotFoundException {
        return createReader(file, UTF8);
    }

    /** Create Reader */
    public static Reader createReader(File file, Charset charset) throws FileNotFoundException {
        return createReader(new FileInputStream(file), charset);
    }

    /** Create Reader */
    public static Reader createReader(String text) {
        return new CharArrayReader(text.toCharArray());
    }

    /** Create Reader from the UTF-8 encode source */
    public static Reader createReader(URL url) throws IOException {
        return createReader(url, UTF8);
    }

    /** Create Reader */
    public static Reader createReader(URL url, Charset charset) throws IOException {
        return createReader(url.openStream(), charset);
    }
    /** Create Reader */
    public static Reader createReader(InputStream is, Charset charset) {
        return new InputStreamReader(is, charset);
    }

}