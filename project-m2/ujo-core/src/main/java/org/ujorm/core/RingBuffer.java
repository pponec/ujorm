/*
 *  Copyright 2001-2011 Pavel Ponec
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

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    /** The UTF-8 Charset */
    public static final Charset UTF8 = Charset.forName("UTF-8");

    /** The string content (buffer)*/
    private final char[] b;

    /** Length */
    public final int length;

    /** Start of the string */
    private int pos = 0;

    /** Creates new RingBuffer */
    public RingBuffer(int length) {
        this.b = new char[length];
        this.length = length;
    }

    final public void add(char c) {
       b[pos] = c;
       pos = ++pos % this.length;
    }

    /** The equals test */
    public boolean equals(String s) {
        return equals(s.toCharArray());
    }

    /** The equals test */
    public boolean equals(char[] s) {
       int i;
       for(i=0; i<this.length && s[i]==b[(pos + i) % length]; i++) {}
       return (i==length);
    }

    /** Returns a character from position 'i' */
    @Override
    public char charAt(int i) {
       return b[(pos + i) % length];
    }

    /** Export to the String. */
    @Override
    public String toString() {
       char[] t = new char[length];
       for(int i=0; i<this.length; i++) t[i] = b[(pos + i) % length];
       return (new String(t));
    }

    /** Export to the String */
    public String substring(int begIndex, int endIndex) {
       int i;
       if (endIndex<=begIndex) return "" ;
       char[] t = new char[endIndex - begIndex];
       for(i=begIndex; i<endIndex; i++) {
           t[i-begIndex] = b[(pos + i) % length];
       }
       return (new String(t));
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
     * @param beg Start tag (text) where the empty value means find end from the current cursor.
     * @param end End tag (text) must not be empty.
     * @return Return a result between beg and end tags (texts). The result is newer NULL.
     * @throws IOException
     */
    public static String findWordNoTrim(final Reader reader, final String beg, final String end) throws IOException {
        final StringBuilder result = new StringBuilder();
        boolean secondState = beg==null || beg.length()==0;
        char[] border = (secondState ? end : beg).toCharArray();
        RingBuffer ring = new RingBuffer(border.length);

        int c;
        while ((c = reader.read()) != -1) {

            ring.add((char) c);
            if (secondState) {
                result.append((char) c);
            }

            if (ring.equals(border)) {
                if (secondState) {
                    break;
                } else {
                    secondState = true;
                    border = end.toCharArray();
                    ring = new RingBuffer(border.length);
                }
            }
        }

        // Remove the finish tag.
        if (result.length() > end.length()) {
            result.setLength(result.length() - end.length());
        }

        return result.toString();
    }

    /** Create Reader from the UTF-8 encode source */
    public static Reader createReader(File file) throws FileNotFoundException {
        return createReader(file, UTF8);
    }

    /** Create Reader */
    public static Reader createReader(File file, Charset charset) throws FileNotFoundException {
        final InputStreamReader reader = new InputStreamReader(new FileInputStream(file), charset);
        return new BufferedReader(reader);
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
        final InputStreamReader reader = new InputStreamReader(url.openStream(), charset);
        return reader;
    }

}