/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ujoframework.core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;

/**
 *
 * @author Ponec
 */
public class UjoCoder {

    /** Date formatter / parser */
    private /*public*/ static final SimpleDateFormat FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    /** Typ konstruktoru */
    public static final Class[] CONSTRUCTOR_TYPE = new Class[]{String.class};
    // === CONVERTING VALUES ===
    /** Encode value to a String representation. */
    public String encodeValue(Ujo ujo, UjoProperty property) {
        Object origValue = ujo.readValue(property);
        Class type = origValue != null ? origValue.getClass() : property.getType();
        if (isContainerType(type)) {
            return null;
        }
        final String result = encodeValue(origValue, false);
        return result;
    }

    /**
     * Convert value to a String representation. 
     * @param value The value
     * @param regenerationTest Perform a decoding for an unknown data types.
     */
    public String encodeValue(Object value, final boolean regenerationTest) {
        String result;
        if (value == null) {
            result = null;
        } else if (String.class==value.getClass()) {
            result = (String) value;
        } else if (value instanceof Number) {
            result = String.valueOf(value);
        } else if (byte[].class==value.getClass()) {
            result = encodeBytes((byte[]) value);
        } else if (char[].class==value.getClass()) {
            result = new String((char[]) value);
        } else if (Character.class==value.getClass()) {
            result = Integer.toString(((Character) value).charValue());
        } else if (Locale.class==value.getClass()) {
            Locale locale = (Locale) value;
            StringBuilder sb = new StringBuilder(10);
            sb.append(locale.getLanguage());
            String p = locale.getCountry();
            if (UjoManager.isValid(p)) {
                sb.append('-').append(p);
                p = locale.getVariant();
                if (UjoManager.isValid(p)) {
                    sb.append('-').append(p);
                }
            }
            result = sb.toString();
        } else if (value instanceof Date) {
            synchronized (FORMAT_DATE) {
               result = FORMAT_DATE.format((Date) value);
            }
        } else if (value instanceof Color) {
            result = Integer.toHexString(((Color) value).getRGB() & 0xffffff | 0x1000000).substring(1).toUpperCase();
        } else if (value instanceof File) {
            result = ((File) value).getPath();
        } else if (value instanceof Dimension) {
            result = ((Dimension) value).width + "," + ((Dimension) value).height;
        } else if (value instanceof Enum) {
            result = ((Enum) value).name();
        } else if (value instanceof Rectangle) {
            Rectangle r = (Rectangle) value;
            result = new StringBuilder(32)
                .append(r.x)
                .append(',')
                .append(r.y)
                .append(',')
                .append(r.width)
                .append(',')
                .append(r.height)
                .toString()
                ;
        } else if (value instanceof Class) {
            result = ((Class) value).getName();
        } else if (value instanceof Charset) {
            result = ((Charset) value).name();
        } else {
            if (regenerationTest) {
                try { // Testing of a regeneration:
                    Constructor c = value.getClass().getConstructor(CONSTRUCTOR_TYPE);
                } catch (Throwable ex) {
                    throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName());
                }
            }
            result = String.valueOf(value);
        }
        return result;
    }

    /** Restore an Object value from a String representation.
     * <br>If value can't be decodet, an IllegalArgumentException is throwed.
     */
    @SuppressWarnings("unchecked")
    public Object decodeValue(final Class type, final String aValue) throws IllegalArgumentException {

        try {
            if (aValue == null
            ||  String.class==type) {  
                return aValue;
            }
            if (aValue.length() == 0) {
                return null;
            }
            if (Number.class.isAssignableFrom(type)) {
                final Constructor constructor = type.getConstructor(CONSTRUCTOR_TYPE);
                final Object result = constructor.newInstance(new Object[]{aValue});
                return result;
            }
            if (Boolean.class==type) {
                final Boolean result = Boolean.valueOf(aValue);
                return result;
            }
            if (Boolean.class==type) {
                final Boolean result = Boolean.valueOf(aValue);
                return result;
            }
            if (Character.class==type) {
                final Character result = Character.valueOf((char) Integer.parseInt(aValue));
                return result;
            }
            if (byte[].class==type) {
                final byte[] result = decodeBytes(aValue);
                return result;
            }
            if (char[].class==type) {
                final char[] result = aValue.toCharArray();
                return result;
            }
            if (Locale.class==type) {
                StringTokenizer sTok = new StringTokenizer(aValue, "-");
                final String p1 = sTok.hasMoreTokens() ? sTok.nextToken() : "";
                final String p2 = sTok.hasMoreTokens() ? sTok.nextToken() : "";
                final String p3 = sTok.hasMoreTokens() ? sTok.nextToken() : "";
                final Locale result = new Locale(p1, p2, p3);
                return result;
            }
            if (Class.class==type) {
                final Class result = Class.forName(aValue);
                return result;
            }
            if (type.isEnum()) {
                final Enum result = Enum.valueOf(type, aValue);
                return result;
            }
            if (Dimension.class.isAssignableFrom(type)) {
                StringTokenizer st = new StringTokenizer(aValue, ",");
                final int w = Integer.parseInt(st.nextToken());
                final int h = Integer.parseInt(st.nextToken());
                final Dimension result = new Dimension(w, h);
                return result;
            }
            if (Rectangle.class.isAssignableFrom(type)) {
                StringTokenizer st = new StringTokenizer(aValue, ",");
                final int x = Integer.parseInt(st.nextToken());
                final int y = Integer.parseInt(st.nextToken());
                final int w = Integer.parseInt(st.nextToken());
                final int h = Integer.parseInt(st.nextToken());
                final Rectangle result = new Rectangle(x, y, w, h);
                return result;
            }
            if (Date.class.isAssignableFrom(type)) {
                try {
                    synchronized (FORMAT_DATE) {
                        final Date result = FORMAT_DATE.parse(aValue);
                        return result;
                    }
                } catch (ParseException ex) {
                    new IllegalArgumentException("\"" + aValue + "\" " + type, ex);
                }
            }
            if (Color.class.isAssignableFrom(type)) {
                final Color result = new java.awt.Color(Integer.parseInt(aValue, 16));
                return result;
            }
            if (File.class.isAssignableFrom(type)) {
                final File result = new File(aValue);
                return result;
            }
            if (Charset.class.isAssignableFrom(type)) {
                final Charset result = Charset.forName(aValue);
                return result;
            }
            if (true) {
                    final Constructor constructor = type.getConstructor(CONSTRUCTOR_TYPE);
                    final Object result = constructor.newInstance(new Object[]{aValue});
                    return result;
            }
        } catch (Throwable e) {
            throw new IllegalArgumentException("Can't decode \"" + aValue + "\" to " + type, e);
        }
        return null;
    }

    /**
     * Decode HexaString into byte array.
     * @param hexString NULL value is not supported.
     * @return Byte array
     */
    protected byte[] decodeBytes(String hexString) {
        byte[] bytes = new byte[hexString.length() >> 1];
        for (int i = 0, h = 0; i < bytes.length; i++, h += 2) {
            bytes[i] = (byte) Integer.parseInt(hexString.substring(h, h + 2), 16);
        }
        return bytes;
    }

    /**
     * Encode bytes to hexadecimal String.
     * @param bytes NULL value is not supported
     * @return A hexadecimal text
     */
    protected String encodeBytes(byte[] bytes) {
        // table to convert a nibble to a hex char.
        final char[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuilder sb = new StringBuilder(bytes.length << 1);
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            sb.append(hexChar[(b & 0xf0) >>> 4]);
            sb.append(hexChar[b & 0x0f]);
        }
        return sb.toString();
    }

    /**
     * Returns true, if the Class is type of Ujo, List or Object[].
     */
    public boolean isContainerType(Class baseType) {
        final boolean result = Ujo.class.isAssignableFrom(baseType) || List.class.isAssignableFrom(baseType) || Object[].class.isAssignableFrom(baseType);
        return result;
    }
}
