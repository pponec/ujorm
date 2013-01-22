/**
 * Copyright (C) 2007-2013, Pavel Ponec, contact: http://ponec.net/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/gpl-2.0.txt
 */
package org.ujorm.core;

import java.io.IOException;
import java.io.Reader;
import junit.framework.TestCase;

/**
 * RingBuffer test
 * @author Pavel Ponec
 */
public class RingBufferTest extends TestCase {

    public RingBufferTest(String testName) {
        super(testName);
    }

    private static Class suite() {
        return RingBufferTest.class;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of add method, of class RingBuffer.
     */
    public void testAdd() {
        System.out.println("add");

        RingBuffer s = new RingBuffer(3);

        s.add('a');
        s.add('b');
        s.add('c');
        
        //
        assertFalse(s.equals("a"));
        assertFalse(s.equals("ab"));
        assertTrue (s.equals("abc"));
        assertFalse(s.equals("abcd"));
                
        //
        assertEquals("abc", s.toString());
        assertEquals("bc", s.substring(1, 3));
        assertEquals('a', s.charAt(0));

        //
        s.add('d');
        assertEquals("bcd", s.toString());
        assertEquals("cd", s.substring(1, 3));
        assertEquals('b', s.charAt(0));
    }

    /**
     * Test of add method, of class RingBuffer.
     */
    public void testFindWord_1() throws IOException {
        String text = "xxx ${abc} def";
        String word = RingBuffer.findWordNoTrim(text, "${", "}");
        assertEquals("abc", word);
    }

    /**
     * Test of add method, of class RingBuffer.
     */
    public void testFindWord_2() throws IOException {
        Reader reader = RingBuffer.createReader("xxx ${abc} def");
        String word = RingBuffer.findWord(reader, "", "$");
        assertEquals("xxx", word);
        //
        word = RingBuffer.findWord(reader, "", "f");
        assertEquals("{abc} de", word);
        //
        word = RingBuffer.findWord(reader, "", "XYZ");
        assertEquals("", word);
    }

    public void testFindWord_2b() throws IOException {
        final Reader reader = RingBuffer.createReader("xxx ${abc} ${def} zzz");

        String word1 = RingBuffer.findWord(reader, "${", "}");
        assertEquals("abc", word1);

        String word2 = RingBuffer.findWord(reader, "${", "}");
        assertEquals("def", word2);

        String word3 = RingBuffer.findWord(reader, "${", "}");
        assertEquals("", word3);
    }

    /**
     * Test of add method, of class RingBuffer.
     */
    public void testFindWord_3() throws IOException {
        String text = "123-abc-789-def";

        String word = RingBuffer.findWord(text, "ABC", "DEF");
        assertEquals("", word);

        String word2 = RingBuffer.findWord(text, "abc", "DEF");
        assertEquals("", word2);

        String word3 = RingBuffer.findWord(text, "", "abc");
        assertEquals("123-", word3);

        String word4 = RingBuffer.findWord(text, null, "abc");
        assertEquals("123-", word4);

        String word5 = RingBuffer.findWord(text, "abc", "");
        assertEquals("", word5);

        String word6 = RingBuffer.findWord(text, "abc", null);
        assertEquals("", word6);
    }

    /**
     * Test of add method, of class RingBuffer.
     */
    public void testFindWord_4() throws IOException {
        Reader reader = RingBuffer.createReader("xxxAx ${abc} xex");

        String word1 = RingBuffer.findWord(reader, "x", "x");
        assertEquals("", word1);

        String word2 = RingBuffer.findWord(reader, "x", "x");
        assertEquals("A", word2);

        String word3 = RingBuffer.findWord(reader, "${", "}");
        assertEquals("abc", word3);

        String word4 = RingBuffer.findWord(reader, "x", "x");
        assertEquals("e", word4);
    }

    /**
     * Test of add method, of class RingBuffer.
     */
    public void testFindWord_5() throws IOException {
        Reader reader = RingBuffer.createReader("{{}}{{A}} {{abc}} {{def}}");

        String word1 = RingBuffer.findWord(reader, "{{", "}}");
        assertEquals("", word1);

        String word2 = RingBuffer.findWord(reader, "{{", "}}");
        assertEquals("A", word2);

        String word3 = RingBuffer.findWord(reader, "{{", "}}");
        assertEquals("abc", word3);

        String word4 = RingBuffer.findWord(reader, "{{", "}}");
        assertEquals("def", word4);
    }

    /**
     * Test of add method, of class RingBuffer.
     */
    public void testFindWord_6() throws IOException {
        Reader reader = RingBuffer.createReader("abcabccabcee");

        String word1 = RingBuffer.findWord(reader, "", "c");
        assertEquals("ab", word1);

        String word2 = RingBuffer.findWord(reader, "", "cc");
        assertEquals("ab", word2);

        String word3 = RingBuffer.findWord(reader, "", "ee");
        assertEquals("abc", word3);
    }

    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
}
