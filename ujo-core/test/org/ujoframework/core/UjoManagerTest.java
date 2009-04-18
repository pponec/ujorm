/*
 * UjoManagerTest.java
 * JUnit based test
 *
 * Created on 27. June 2007, 19:21
 */

package org.ujoframework.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Locale;
import org.ujoframework.MyTestCase;
import org.ujoframework.UjoProperty;
import org.ujoframework.core.ujos.UjoCSV;

/**
 *
 * @author Pavel Ponec
 */
public class UjoManagerTest extends MyTestCase {
    
    public UjoManagerTest(String testName) {
        super(testName);
    }
    
    private static Class suite() {
        return UjoManagerTest.class;
    }
    
    /**
     * Test of encodeBytes method, of class org.ujoframework.core.UjoManager.
     */
    public void testEncodeBytes() {
        Class type = Color.class;
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
     * Test of encodeBytes method, of class org.ujoframework.core.UjoManager.
     */
    public void testEncodeColor() {
        Class type = Color.class;
        UjoManager manager = UjoManager.getInstance();
        Color expected;
        Color result  ;
        
        //
        expected = new Color(0x000001);
        result   = (Color) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("1", expected, result);
        //
        expected = new Color(0x100001);
        result   = (Color) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("2", expected, result);
        //
        expected = new Color(0x100000);
        result   = (Color) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("3", expected, result);
        //
        expected = new Color(0xaabbcc);
        result   = (Color) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("4", expected, result);
        //
        expected = new Color(0xAABBCC);
        result   = (Color) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("5", expected, result);
        //
    }
    
    
    /**
     * Test of encodeBytes method, of class org.ujoframework.core.UjoManager.
     */
    public void testEncodeCharset() {
        Class charset = Charset.class;
        UjoManager manager = UjoManager.getInstance();
        Charset expected;
        Charset result  ;
        
        //
        expected = Charset.forName("windows-1250");
        result   = (Charset) manager.decodeValue(charset, manager.encodeValue(expected, false));
        assertEquals("1", expected, result);
        //
        expected = Charset.forName("utf8");
        result   = (Charset) manager.decodeValue(charset, manager.encodeValue(expected, false));
        assertEquals("2", expected, result);
        //
        expected = Charset.forName("UTF-8");
        result   = (Charset) manager.decodeValue(charset, manager.encodeValue(expected, false));
        assertEquals("3", expected, result);
        //
        expected = Charset.forName("ascii");
        result   = (Charset) manager.decodeValue(charset, manager.encodeValue(expected, false));
        assertEquals("4", expected, result);
        //
        expected = Charset.forName("cp1250");
        result   = (Charset) manager.decodeValue(charset, manager.encodeValue(expected, false));
        assertEquals("5", expected, result);
        //
        expected = null;
        result   = (Charset) manager.decodeValue(charset, manager.encodeValue(expected, false));
        assertEquals("6", expected, result);
    }
        
    
    /**
     * Test of encodeBytes method, of class org.ujoframework.core.UjoManager.
     */
    public void testEncodeLocale() {
        Class type = Locale.class;
        UjoManager manager = UjoManager.getInstance();
        Locale expected;
        Locale result  ;
        
        //
        expected = Locale.getDefault();
        result   = (Locale) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("1", expected, result);
        //
        expected = new Locale("cs", "CZ");
        result   = (Locale) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("2", expected, result);
        //
        expected = new Locale("en", "GB");
        result   = (Locale) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("2", expected, result);
        //
        //
        expected = new Locale("cs");
        result   = (Locale) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("2", expected, result);
        //
        expected = new Locale("en");
        result   = (Locale) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("2", expected, result);
        //
        expected = new Locale("cs", "CZ", "XX");
        result   = (Locale) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("2", expected, result);
        //
        expected = new Locale("en", "GB", "XX");
        result   = (Locale) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("2", expected, result);
    }
    
    /**
     * Test of encodeBytes method, of class org.ujoframework.core.UjoManager.
     */
    public void testEncodeDim() {
        Class type = Dimension.class;
        UjoManager manager = UjoManager.getInstance();
        Dimension expected;
        Dimension result  ;
        
        //
        expected = new Dimension(0,0);
        result   = (Dimension) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("1", expected, result);
        //
        expected = new Dimension(-1,1);
        result   = (Dimension) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("2", expected, result);
        //
        expected = new Dimension(Integer.MIN_VALUE,Integer.MAX_VALUE);
        result   = (Dimension) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("3", expected, result);
        //
        expected = new Dimension(-500,-600);
        result   = (Dimension) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("4", expected, result);
        //
        expected = new Dimension(800,660);
        result   = (Dimension) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("5", expected, result);
        //
    }
    
    /**
     * Test of encodeBytes method, of class org.ujoframework.core.UjoManager.
     */
    public void testEncodeRectangle2() {
        Class type = Rectangle.class;
        UjoManager manager = UjoManager.getInstance();
        Rectangle expected;
        Rectangle result  ;
        
        //
        expected = new Rectangle(0,0);
        result   = (Rectangle) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("1", expected, result);
        //
        expected = new Rectangle(-1,1);
        result   = (Rectangle) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("2", expected, result);
        //
        expected = new Rectangle(Integer.MIN_VALUE,Integer.MAX_VALUE);
        result   = (Rectangle) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("3", expected, result);
        //
        expected = new Rectangle(-500,-600);
        result   = (Rectangle) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("4", expected, result);
        //
        expected = new Rectangle(800,660);
        result   = (Rectangle) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("5", expected, result);
        //
    }
    
    /**
     * Test of encodeBytes method, of class org.ujoframework.core.UjoManager.
     */
    public void testEncodeRectangle4() {
        Class type = Rectangle.class;
        UjoManager manager = UjoManager.getInstance();
        Rectangle expected;
        Rectangle result  ;
        
        //
        expected = new Rectangle(0,0,0,0);
        result   = (Rectangle) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("1", expected, result);
        //
        expected = new Rectangle(-1,1,-2,2);
        result   = (Rectangle) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("2", expected, result);
        //
        expected = new Rectangle(Integer.MIN_VALUE,Integer.MAX_VALUE,Integer.MIN_VALUE,Integer.MAX_VALUE);
        result   = (Rectangle) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("3", expected, result);
        //
        expected = new Rectangle(-500,-600,-500,-600);
        result   = (Rectangle) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("4", expected, result);
        //
        expected = new Rectangle(800,660,10,20);
        result   = (Rectangle) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("5", expected, result);
        //
    }
    
    /**
     * ENUM test
     */
    public void testEnum() {
        Class type = SampleEnum.class;
        UjoManager manager = UjoManager.getInstance();
        Enum expected;
        Enum result  ;

        expected = SampleEnum.ONE;
        result   = (Enum) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("1", expected, result);

        expected = SampleEnum.TWO;
        result   = (Enum) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("2", expected, result);
        
        expected = null;
        result   = (Enum) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("3", expected, result);
    }
    
    /**
     * CLAS test
     */
    public void testClass() {
        Class type = Class.class;
        UjoManager manager = UjoManager.getInstance();
        Class expected;
        Class result  ;

        expected = String.class;
        result   = (Class) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("1", expected, result);

        expected = Color.class;
        result   = (Class) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("2", expected, result);
        
        expected = null;
        result   = (Class) manager.decodeValue(type, manager.encodeValue(expected, false));
        assertEquals("3", expected, result);
    }

//    /** Test of UjoManager.getPropertyField(..) */
//    public void testPropertyField() throws IllegalArgumentException, IllegalAccessException {
//
//        UjoCSV ujo = new UjoCSV();
//
//        for (UjoProperty p1 : ujo.readProperties()) {
//            Field field = UjoManager.getInstance().getPropertyField(ujo, p1);
//            Object p2 = field.get(null);
//            assertSame(p1, p2);
//
//            if (p2==UjoCSV.P1) {
//                Column column = field.getAnnotation(Column.class);
//                assertNotNull(column);
//                assertEquals(true, column.pk());
//            }
//
//        }
//    }
    
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
    
}
