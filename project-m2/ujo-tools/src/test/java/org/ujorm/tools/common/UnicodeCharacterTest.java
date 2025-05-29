package org.ujorm.tools.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Pavel Ponec, https://github.com/pponec/
 */
class UnicodeCharacterTest {

    /** Text 1 character long including 1 emoticon. */
    private static final String s1 = "\uD83D\uDE0A";
    /** Text 6 characters long including 2 emoticons. */
    private static final String s2 = s1 + "-" + s1 + "-A1";
    /** Text 8 characters long including 1 emoticon. */
    private static final String s3 = "Hello \uD83C\uDF0D!";

    @Test
    void charCount() {
        assertEquals(1, UnicodeCharacter.charCount(s1));
        assertEquals(6, UnicodeCharacter.charCount(s2));
        assertEquals(8, UnicodeCharacter.charCount(s3));
    }

    @Test
    void codePoint() {
        var character = UnicodeCharacter.charAt(0, s1);
        var point = character.codePoint();
        assertEquals(128522, point);

        character = UnicodeCharacter.charAt(4, s2);
        point = character.codePoint();
        assertEquals('A', point);

        character = UnicodeCharacter.charAt(0, s3);
        point = character.codePoint();
        assertEquals('H', point);
    }

    @Test
    void isSupplementary() {
        var character = UnicodeCharacter.charAt(0, s1);
        assertTrue(character.isSupplementary());
    }

    @Test
    void isEmoji() {
        var character = UnicodeCharacter.charAt(0, s2);
        assertTrue(character.isEmoji());

        character = UnicodeCharacter.charAt(1, s2);
        assertFalse(character.isEmoji());
    }

    @Test
    void isLetter() {
        var character = UnicodeCharacter.charAt(4, s2);
        assertTrue(character.equals('A'));
        assertTrue(character.isLetter());

        character = UnicodeCharacter.charAt(5, s2);
        assertTrue(character.equals('1'));
        assertFalse(character.isLetter());

        character = UnicodeCharacter.charAt(0, s1);
        assertFalse(character.isLetter());
    }

    @Test
    void isDigit() {
        var character = UnicodeCharacter.charAt(4, s2);
        assertFalse(character.isDigit());

        character = UnicodeCharacter.charAt(5, s2);
        assertTrue(character.isDigit());

        character = UnicodeCharacter.charAt(0, s1);
        assertFalse(character.isDigit());
    }

    @Test
    void testToString() {
        var character = UnicodeCharacter.charAt(0, s1);
        var text = character.toString();
        assertEquals(s1, text);
    }

    @Test
    void testEquals() {
        var char0 = UnicodeCharacter.charAt(0, s2);
        var char1 = UnicodeCharacter.charAt(1, s2);
        var char2 = UnicodeCharacter.charAt(2, s2);

        assertTrue(char0.equals(char0));
        assertTrue(char0.equals(char2));
        assertFalse(char1.equals(char0));
        assertFalse(char1.equals(char2));
    }

    @Test
    void charAt_equalsChar() {
        var char0 = UnicodeCharacter.charAt(0, s3);
        var char1 = UnicodeCharacter.charAt(1, s3);
        var char2 = UnicodeCharacter.charAt(7, s3);
        var char3 = UnicodeCharacter.charAt(-1, s3);

        assertTrue(char0.equals('H'));
        assertTrue(char1.equals('e'));
        assertTrue(char2.equals('!'));
        assertTrue(char3.equals('!'));

        assertFalse(char0.equals('-'));
        assertFalse(char1.equals('-'));
        assertFalse(char2.equals('-'));
        assertFalse(char3.equals('-'));
    }

    @Test
    void stream() {
        var char0 = UnicodeCharacter.stream(s2).skip(0).findFirst().get();
        var char1 = UnicodeCharacter.stream(s2).skip(1).findFirst().get();
        var char2 = UnicodeCharacter.stream(s2).skip(2).findFirst().get();

        assertEquals(UnicodeCharacter.charAt(0, s2), char0);
        assertEquals(UnicodeCharacter.charAt(1, s2), char1);
        assertEquals(UnicodeCharacter.charAt(2, s2), char2);
    }

    @Test
    void testIsEmoji() {
        var char0 = UnicodeCharacter.charAt(0, s2);
        var char1 = UnicodeCharacter.charAt(1, s2);
        var char2 = UnicodeCharacter.charAt(2, s2);

        assertTrue(char0.isEmoji());
        assertFalse(char1.isEmoji());
        assertTrue(char2.isEmoji());
    }

    @Test
    void writeTo() throws IOException {
        var char0 = UnicodeCharacter.charAt(0, s2);
        var char1 = UnicodeCharacter.charAt(1, s2);
        var char2 = UnicodeCharacter.charAt(2, s2);

        var writer = new StringWriter();
        char0.writeTo(writer);
        char1.writeTo(writer);
        char2.writeTo(writer);

        assertEquals(s2.substring(0, 5), writer.toString()); // !
    }

    @Test
    void compareTo() {
        var char0 = UnicodeCharacter.charAt(0, s2);
        var char1 = UnicodeCharacter.charAt(1, s2);
        var char2 = UnicodeCharacter.charAt(2, s2);
        var char3 = UnicodeCharacter.charAt(3, s2);
        var char4 = UnicodeCharacter.charAt(4, s2);

        assertEquals(1, char0.compareTo(char1));
        assertEquals(0, char0.compareTo(char2));
        assertEquals(0, char1.compareTo(char3));
        assertEquals(-1, char4.compareTo(char2));
    }
}