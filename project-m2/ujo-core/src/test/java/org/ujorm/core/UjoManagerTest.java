/*
 * UjoManagerTest.java
 * JUnit based test
 *
 * Created on 27. June 2007, 19:21
 */

package org.ujorm.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.ujorm.MyTestCase;

/**
 *
 * @author Pavel Ponec
 */
public class UjoManagerTest extends MyTestCase {

    /**
     * Test of encodeBytes method, of class org.ujorm.core.UjoManager.
     */
    @Test
    public void testEncodeBytes() {
        UjoCoder coder = UjoManager.getInstance().getCoder();
        byte[] expected;
        byte[] result  ;

        //
        expected = new byte[] {};
        result   = coder.decodeBytes(coder.encodeBytes(expected));
        assertEquals("1", expected, result);
        //
        expected = new byte[] {0};
        result   = coder.decodeBytes(coder.encodeBytes(expected));
        assertEquals("2", expected, result);
        //
        expected = new byte[] {0,0};
        result   = coder.decodeBytes(coder.encodeBytes(expected));
        assertEquals("3", expected, result);
        //
        expected = new byte[] {0,0,0};
        result   = coder.decodeBytes(coder.encodeBytes(expected));
        assertEquals("4", expected, result);
        //
        expected = new byte[] {1};
        result   = coder.decodeBytes(coder.encodeBytes(expected));
        assertEquals("5", expected, result);
        //
        expected = new byte[] {0,1};
        result   = coder.decodeBytes(coder.encodeBytes(expected));
        assertEquals("6", expected, result);
        //
        expected = new byte[] {0,0,1};
        result   = coder.decodeBytes(coder.encodeBytes(expected));
        assertEquals("7", expected, result);
        //
        expected = new byte[] {2,2};
        result   = coder.decodeBytes(coder.encodeBytes(expected));
        assertEquals("5", expected, result);
        //
        expected = new byte[] {0,2,2};
        result   = coder.decodeBytes(coder.encodeBytes(expected));
        assertEquals("6", expected, result);
        //
        expected = new byte[] {3,3,3};
        result   = coder.decodeBytes(coder.encodeBytes(expected));
        assertEquals("7", expected, result);
        //
        expected = new byte[] {(byte)240,(byte)241,(byte)250};
        result   = coder.decodeBytes(coder.encodeBytes(expected));
        assertEquals("8", expected, result);
        //
        expected = new byte[] {(byte)128,(byte)128,(byte)128};
        result   = coder.decodeBytes(coder.encodeBytes(expected));
        assertEquals("9", expected, result);
        //
        expected = new byte[1000*1000];
        Arrays.fill(expected, (byte)120);
        result   = coder.decodeBytes(coder.encodeBytes(expected));
        assertEquals("A", expected, result);
    }

    /**
     * Test of encodeBytes method, of class org.ujorm.core.UjoManager.
     */
    @Test
    public void testEncodeColor() {
        Class<Color> type = Color.class;
        UjoManager manager = UjoManager.getInstance();
        Color expected;
        Color result  ;

        //
        expected = new Color(0x000001);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "1");
        //
        expected = new Color(0x100001);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "2");
        //
        expected = new Color(0x100000);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "3");
        //
        expected = new Color(0xaabbcc);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "4");
        //
        expected = new Color(0xAABBCC);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "5");
        //
    }

    /**
     * Test classess of  java.time
     */
    @Test
    public void testLocalDate() {
        Class<LocalDate> type = LocalDate.class;
        UjoManager manager = UjoManager.getInstance();
        LocalDate expected, result;
        //
        expected = LocalDate.now();
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "1");
    }

    /**
     * Test classess of  java.time
     */
    @Test
    public void testLocalTime() {
        Class<LocalTime> type = LocalTime.class;
        UjoManager manager = UjoManager.getInstance();
        LocalTime expected, result;
        //
        expected = LocalTime.now();
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "1");
    }

    /**
     * Test classess of  java.time
     */
    @Test
    public void testLocalDateTime() {
        Class<LocalDateTime> type = LocalDateTime.class;
        UjoManager manager = UjoManager.getInstance();
        LocalDateTime expected, result;
        //
        expected = LocalDateTime.now();
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "1");
    }

    /**
     * Test classess of  java.time
     */
    @Test
    public void testZonedDateTime() {
        Class<ZonedDateTime> type = ZonedDateTime.class;
        UjoManager manager = UjoManager.getInstance();
        ZonedDateTime expected, result;
        //
        expected = ZonedDateTime.now();
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "1");
    }

    /**
     * Test of encodeBytes method, of class org.ujorm.core.UjoManager.
     */
    @Test
    public void testEncodeCharset() {
        Class<Charset> charset = Charset.class;
        UjoManager manager = UjoManager.getInstance();
        Charset expected;
        Charset result  ;

        //
        expected = Charset.forName("windows-1250");
        result   = manager.decodeValue(charset, manager.encodeValue(expected, false));
        assertEquals(expected, result, "1");
        //
        expected = StandardCharsets.UTF_8;
        result   = manager.decodeValue(charset, manager.encodeValue(expected, false));
        assertEquals(expected, result, "2");
        //
        expected = StandardCharsets.UTF_8;
        result   = manager.decodeValue(charset, manager.encodeValue(expected, false));
        assertEquals(expected, result, "3");
        //
        expected = StandardCharsets.US_ASCII;
        result   = manager.decodeValue(charset, manager.encodeValue(expected, false));
        assertEquals(expected, result, "4");
        //
        expected = Charset.forName("cp1250");
        result   = manager.decodeValue(charset, manager.encodeValue(expected, false));
        assertEquals(expected, result, "5");
        //
        expected = null;
        result   = manager.decodeValue(charset, manager.encodeValue(expected, false));
        assertEquals(expected, result, "6");
    }


    /**
     * Test of encodeBytes method, of class org.ujorm.core.UjoManager.
     */
    @Test
    public void testEncodeLocale() {
        Class<Locale> type = Locale.class;
        UjoManager manager = UjoManager.getInstance();
        Locale expected;
        Locale result  ;

        //
        expected = Locale.getDefault();
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "1");
        //
        expected = new Locale("cs", "CZ");
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "2");
        //
        expected = new Locale("en", "GB");
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "2");
        //
        //
        expected = new Locale("cs");
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "2");
        //
        expected = new Locale("en");
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals( expected, result, "2");
        //
        expected = new Locale("cs", "CZ", "XX");
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "2");
        //
        expected = new Locale("en", "GB", "XX");
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "2");
    }

    /**
     * Test of encodeBytes method, of class org.ujorm.core.UjoManager.
     */
    @Test
    public void testEncodeDim() {
        Class<Dimension> type = Dimension.class;
        UjoManager manager = UjoManager.getInstance();
        Dimension expected;
        Dimension result  ;

        //
        expected = new Dimension(0,0);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "1");
        //
        expected = new Dimension(-1,1);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "2");
        //
        expected = new Dimension(Integer.MIN_VALUE,Integer.MAX_VALUE);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "3");
        //
        expected = new Dimension(-500,-600);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "4");
        //
        expected = new Dimension(800,660);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "5");
        //
    }

    /**
     * Test of encodeBytes method, of class org.ujorm.core.UjoManager.
     */
    @Test
    public void testEncodeRectangle2() {
        Class<Rectangle> type = Rectangle.class;
        UjoManager manager = UjoManager.getInstance();
        Rectangle expected;
        Rectangle result  ;

        //
        expected = new Rectangle(0,0);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "1");
        //
        expected = new Rectangle(-1,1);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "2");
        //
        expected = new Rectangle(Integer.MIN_VALUE,Integer.MAX_VALUE);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "3");
        //
        expected = new Rectangle(-500,-600);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "4");
        //
        expected = new Rectangle(800,660);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "5");
        //
    }

    /**
     * Test of encodeBytes method, of class org.ujorm.core.UjoManager.
     */
    @Test
    public void testEncodeRectangle4() {
        Class<Rectangle> type = Rectangle.class;
        UjoManager manager = UjoManager.getInstance();
        Rectangle expected;
        Rectangle result  ;

        //
        expected = new Rectangle(0,0,0,0);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "1");
        //
        expected = new Rectangle(-1,1,-2,2);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "2");
        //
        expected = new Rectangle(Integer.MIN_VALUE,Integer.MAX_VALUE,Integer.MIN_VALUE,Integer.MAX_VALUE);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals( expected, result, "3");
        //
        expected = new Rectangle(-500,-600,-500,-600);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "4");
        //
        expected = new Rectangle(800,660,10,20);
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "5");
        //
    }

    /**
     * ENUM test
     */
    @Test
    public void testEnum() {
        Class<SampleEnum> type = SampleEnum.class;
        UjoManager manager = UjoManager.getInstance();
        Enum expected;
        Enum result  ;

        expected = SampleEnum.ONE;
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "1");

        expected = SampleEnum.TWO;
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "2");

        expected = null;
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "3");
    }

    /**
     * Sample EnumWrapper test
     */
    @Test
    public void testEnumWrapper() {
        Class<SampleEnumWrapper> type = SampleEnumWrapper.class;
        UjoManager manager = UjoManager.getInstance();
        SampleEnumWrapper expected;
        SampleEnumWrapper result  ;

        expected = SampleEnumWrapper.ONE;
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "1");

        expected = SampleEnumWrapper.TWO;
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "1");

        expected = null;
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "1");
    }

    /**
     * Sample NumberWrapper test
     */
    @Test
    public void testNumberWrapper() {
        Class<SampleNumberWrapper> type = SampleNumberWrapper.class;
        UjoManager manager = UjoManager.getInstance();
        SampleNumberWrapper expected;
        SampleNumberWrapper result  ;

        expected = new SampleNumberWrapper("1.01");
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected.getNumber(), result.getNumber(), "1");

        expected = new SampleNumberWrapper("2.23");
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected.getNumber(), result.getNumber(), "2");

        expected = null;
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "3");
    }

    /**
     * CLAS test
     */
    @Test
    public void testClass() {
        Class<Class> type = Class.class;
        UjoManager manager = UjoManager.getInstance();
        Class expected;
        Class result  ;

        expected = String.class;
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "1");

        expected = Color.class;
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "1");

        expected = null;
        result   = manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals(expected, result, "1");
    }



}
