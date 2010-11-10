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
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import org.ujoframework.Ujo;
import org.ujoframework.UjoProperty;
import org.ujoframework.extensions.ListUjoProperty;

/**
 * A tool for encoding an object to a text and a text back to the object.
 * @author Pavel Ponec
 */
public class UjoCoder {

    /** Date formater and parser with second precision.
     * @see http://www.javacodegeeks.com/2010/07/java-best-practices-dateformat-in.html Performacce tip
     */
    public static final ThreadLocal<SimpleDateFormat> FORMAT_DATE = new ThreadLocal<SimpleDateFormat>() {
       @Override protected SimpleDateFormat initialValue() { return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH); }
    };

    /** Date formater and parser with daily accuracy.
     * @see http://www.javacodegeeks.com/2010/07/java-best-practices-dateformat-in.html Performacce tip
     */
    public static final ThreadLocal<SimpleDateFormat> FORMAT_DAY = new ThreadLocal<SimpleDateFormat>() {
       @Override protected SimpleDateFormat initialValue() { return new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH); }
    };

    /** Typ konstruktoru */
    public static final Class[] CONSTRUCTOR_TYPE = new Class[]{String.class};

    // === CONVERTING VALUES ===
    
    /** Returns a list separator */
    public char getSeparator() {
        return ',';
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
            if (UjoManager.isUsable(p)) {
                sb.append('-').append(p);
                p = locale.getVariant();
                if (UjoManager.isUsable(p)) {
                    sb.append('-').append(p);
                }
            }
            result = sb.toString();
        } else if (value instanceof Date) {
            result = value instanceof java.sql.Date
                    ? FORMAT_DAY.get().format((java.sql.Date) value)
                    : FORMAT_DATE.get().format((Date) value)
                    ;
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
        } else if (value instanceof List) {
            StringBuilder lresult = new StringBuilder(64);
            List lvalue = (List) value;
            char separator = getSeparator();
            int size = lvalue.size();
            for (int i=0; i<size; i++) {
                if (i>0) { lresult.append(separator); }
                final String txt = encodeValue(lvalue.get(i), regenerationTest);
                if (regenerationTest && txt.indexOf(separator)>=0) {
                    String msg
                        = "The item of list '" + txt
                        + "' must not contain the separator character '" + separator + "'"
                        ;
                    throw new IllegalArgumentException(msg);
                }
                lresult.append(txt);
            }
            result = lresult.toString();
        } else {
            if (regenerationTest) {
                try { // Testing of a regeneration:
                    Constructor c = value.getClass().getConstructor(CONSTRUCTOR_TYPE);
                } catch (Throwable e) {
                    throw new IllegalArgumentException("Unsupported type: " + value.getClass().getName(), e);
                }
            }
            result = String.valueOf(value);
        }
        return result;
    }

    /** Restore an Object value from a String representation and write it into ujo.
     * <br>If value can't be decoded, an IllegalArgumentException is throwed.
     */
    @SuppressWarnings("unchecked")
    public Object decodeValue(final UjoProperty property, final String aValue, final Class type) throws IllegalArgumentException {
        if (property instanceof ListUjoProperty) {
            if (aValue==null || aValue.length()==0) { return null; }
            List result = new ArrayList();
            ListUjoProperty propertyList = (ListUjoProperty) property;
            String separator = String.valueOf(getSeparator());
            StringTokenizer st = new StringTokenizer(aValue, separator);
            while (st.hasMoreTokens()) {
                final String text = st.nextToken();
                final Object val  = decodeValue(propertyList.getItemType(), text);
                result.add(val);
            }
            return result;
        } else {
            final Object result = decodeValue(type!=null ? type : property.getType(), aValue);
            return result;
        }
    }


    /** Restore an Object value from a String representation.
     * <br>If value can't be decoded, an IllegalArgumentException is throwed.
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
            if (Integer.class==type) {
                // Memory optimalization: returns the same instances in interval from -128 to 127 include.
                final int result = Integer.parseInt(aValue);
                return result;
            }
            if (Number.class.isAssignableFrom(type)) {
                if (Short.class==type) {
                    final short result = Short.parseShort(aValue);
                    return result;
                }
                if (Long.class==type) {
                    final Long result = new Long(aValue);
                    return 0L==result ? 0L : result;
                }
                if (Float.class==type) {
                    final Float result = new Float(aValue);
                    return 0f==result ? 0F : result;
                }
                if (Double.class==type) {
                    final Double result = new Double(aValue);
                    return 0d==result ? 0D : result;
                }
                if (BigDecimal.class==type) {
                    final BigDecimal result = new BigDecimal(aValue);
                    return result;
                }
                if (true) {
                    final Constructor constructor = type.getConstructor(CONSTRUCTOR_TYPE);
                    final Object result = constructor.newInstance(new Object[]{aValue});
                    return result;
                }
            }
            if (Boolean.class==type) {
                final Boolean result = Boolean.valueOf(aValue);
                return result;
            }
            if (Character.class==type) {
                final char result = (char) Integer.parseInt(aValue);
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
                    Date result = java.sql.Date.class.isAssignableFrom(type)
                            ?  new java.sql.Date(FORMAT_DAY.get().parse(aValue).getTime())
                            :  FORMAT_DATE.get().parse(aValue)
                            ;
                    return result;
                } catch (ParseException ex) {
                    throw new IllegalArgumentException("\"" + aValue + "\" " + type, ex);
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
    public boolean isContainerType(final Class baseType) {
        final boolean result 
            =  Ujo.class.isAssignableFrom(baseType)
            || List.class.isAssignableFrom(baseType)
            || Object[].class.isAssignableFrom(baseType)
            ;
        return result;
    }
}
