package org.ujorm.tools.common;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

class UnicodeCharacterTest {

    /** Text 1 character long including 1 emoticon. */
    private static final String s1 = "\uD83D\uDE0A";
    /** Text 6 characters long including 2 emoticons. */
    private static final String s2 = s1 + "-" + s1 + "-A1";
    /** Text 8 characters long including 1 emoticon. */
    private static final String s3 = "Hello \uD83C\uDF0D!";

    @Test
    void lenhth() {
        assertEquals(1, UnicodeCharacter.length(s1));
        assertEquals(6, UnicodeCharacter.length(s2));
        assertEquals(8, UnicodeCharacter.length(s3));
    }

    @Test
    void codePoint() {
        var character = UnicodeCharacter.of(s1, 0);
        var point = character.codePoint();
        assertEquals(128522, point);

        character = UnicodeCharacter.of(s2, 4);
        point = character.codePoint();
        assertEquals('A', point);

        character = UnicodeCharacter.of(s3, 0);
        point = character.codePoint();
        assertEquals('H', point);
    }

    @Test
    void isSupplementary() {
        var character = UnicodeCharacter.of(s1,0);
        assertTrue(character.isSupplementary());
    }

    @Test
    void isEmoji() {
        var character = UnicodeCharacter.of(s2,0);
        assertTrue(character.isEmoji());

        character = UnicodeCharacter.of(s2,1);
        assertFalse(character.isEmoji());
    }

    @Test
    void isLetter() {
        var character = UnicodeCharacter.of(s2,4);
        assertTrue(character.equals('A'));
        assertTrue(character.isLetter());

        character = UnicodeCharacter.of(s2,5);
        assertTrue(character.equals('1'));
        assertFalse(character.isLetter());

        character = UnicodeCharacter.of(s1,0);
        assertFalse(character.isLetter());
    }

    @Test
    void isDigit() {
        var character = UnicodeCharacter.of(s2,4);
        assertFalse(character.isDigit());

        character = UnicodeCharacter.of(s2,5);
        assertTrue(character.isDigit());

        character = UnicodeCharacter.of(s1,0);
        assertFalse(character.isDigit());
    }

    @Test
    void testToString() {
        var character = UnicodeCharacter.of(s1, 0);
        var text = character.toString();
        assertEquals(s1, text);
    }

    @Test
    void testEquals() {
        var char0 = UnicodeCharacter.of(s2, 0);
        var char1 = UnicodeCharacter.of(s2, 1);
        var char2 = UnicodeCharacter.of(s2, 2);

        assertTrue(char0.equals(char0));
        assertTrue(char0.equals(char2));
        assertFalse(char1.equals(char0));
        assertFalse(char1.equals(char2));
    }

    @Test
    void testEqualsChar() {
        var char0 = UnicodeCharacter.of(s2, 0);
        var char1 = UnicodeCharacter.of(s2, 1);
        var char2 = UnicodeCharacter.of(s2, 2);

        assertFalse(char0.equals('-'));
        assertTrue (char1.equals('-'));
        assertFalse(char2.equals('-'));
    }

    @Test
    void stream() {
        var char0 = UnicodeCharacter.stream(s2).skip(0).findFirst().get();
        var char1 = UnicodeCharacter.stream(s2).skip(1).findFirst().get();
        var char2 = UnicodeCharacter.stream(s2).skip(2).findFirst().get();

        assertEquals(UnicodeCharacter.of(s2, 0), char0);
        assertEquals(UnicodeCharacter.of(s2, 1), char1);
        assertEquals(UnicodeCharacter.of(s2, 2), char2);
    }

    @Test
    void testIsEmoji() {
        var char0 = UnicodeCharacter.of(s2, 0);
        var char1 = UnicodeCharacter.of(s2, 1);
        var char2 = UnicodeCharacter.of(s2, 2);

        assertTrue(char0.isEmoji());
        assertFalse(char1.isEmoji());
        assertTrue(char2.isEmoji());
    }

    @Test
    void writeTo() throws IOException {
        var char0 = UnicodeCharacter.of(s2, 0);
        var char1 = UnicodeCharacter.of(s2, 1);
        var char2 = UnicodeCharacter.of(s2, 2);

        var writer = new StringWriter();
        char0.writeTo(writer);
        char1.writeTo(writer);
        char2.writeTo(writer);

        assertEquals(s2.substring(0, 5), writer.toString()); // !
    }
}