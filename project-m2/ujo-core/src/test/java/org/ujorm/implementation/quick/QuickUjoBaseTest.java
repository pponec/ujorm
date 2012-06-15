/*
 * AUnifiedDataObjectTest.java
 * JUnit based test
 *
 * Created on 3. June 2007, 23:31
 */

package org.ujorm.implementation.quick;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import junit.framework.*;
import org.ujorm.MyTestCase;
import org.ujorm.UjoPropertyList;

/**
 * TextCase
 * @author Pavel Ponec
 */
public class QuickUjoBaseTest extends MyTestCase {
    private static final Class CLASS = QuickUjoBaseTest.class;
    
    public QuickUjoBaseTest(String testName) {
        super(testName);
    }
    
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(CLASS);
        return suite;
    }
    
    @Override
    protected void setUp() throws Exception {
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of readValue method,
     */
    public void testPropertyType() throws Throwable {
        System.out.println("testPropertyType");
        
        Type type1 = getGenerics(QuickUjoImpl.class, QuickUjoImpl.PRO_P0.getName())[1];
        Type type2 = getGenerics(QuickUjoImpl.class, QuickUjoImpl.PRO_P1.getName())[1];
        Type type3 = getGenerics(QuickUjoImpl.class, QuickUjoImpl.PRO_P2.getName())[1];
        Type type4 = getGenerics(QuickUjoImpl.class, QuickUjoImpl.PRO_P3.getName())[1];
        //
        assertEquals(Long   .class, type1);
        assertEquals(Integer.class, type2);
        assertEquals(String .class, type3);
        assertEquals(Date   .class, type4);
        //
        assertEquals(Long   .class, QuickUjoImpl.PRO_P0.getType());
        assertEquals(Integer.class, QuickUjoImpl.PRO_P1.getType());
        assertEquals(String .class, QuickUjoImpl.PRO_P2.getType());
        assertEquals(Date   .class, QuickUjoImpl.PRO_P3.getType());
    }
    
    /**
     * Test of readValue method,
     */
    public void testPropertyChildype() throws Throwable {
        System.out.println("testPropertyType");
        
        Type type0 = getGenerics(QuickUjoImpl.class, QuickUjoImplChild.PRO_P0.getName())[1];
        Type type1 = getGenerics(QuickUjoImpl.class, QuickUjoImplChild.PRO_P1.getName())[1];
        Type type2 = getGenerics(QuickUjoImpl.class, QuickUjoImplChild.PRO_P2.getName())[1];
        Type type3 = getGenerics(QuickUjoImpl.class, QuickUjoImplChild.PRO_P3.getName())[1];
        Type type4 = getGenerics(QuickUjoImpl.class, QuickUjoImplChild.PRO_P4.getName())[1];
        //
        assertEquals(Long   .class, type0);
        assertEquals(Integer.class, type1);
        assertEquals(String .class, type2);
        assertEquals(Date   .class, type3);
        assertEquals(Class  .class, type4);
        //
        assertEquals(Long   .class, QuickUjoImplChild.PRO_P0.getType());
        assertEquals(Integer.class, QuickUjoImplChild.PRO_P1.getType());
        assertEquals(String .class, QuickUjoImplChild.PRO_P2.getType());
        assertEquals(Date   .class, QuickUjoImplChild.PRO_P3.getType());
        assertEquals(Class  .class, QuickUjoImplChild.PRO_P4.getType());
        assertEquals(Long   .class, QuickUjoImplChild.PRO_P5.getType());
        assertEquals(Integer.class, QuickUjoImplChild.PRO_P6.getType());
        assertEquals(String .class, QuickUjoImplChild.PRO_P7.getType());
        assertEquals(Date   .class, QuickUjoImplChild.PRO_P8.getType());
        assertEquals(Class  .class, QuickUjoImplChild.PRO_P9.getType());
    }
    
    /** Regurns array of generic parameters */
    private static Type[] getGenerics(Class type, String fieldName) {
        try {
            final Field field = type.getDeclaredField(fieldName);
            final Type[] types = getGenerics(field);
            return types;
        } catch (NoSuchFieldException ex) {
            throw new IllegalStateException(ex);
        } catch (SecurityException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** Regurns array of generic parameters */
    private static Type[] getGenerics(Field field) {
        final ParameterizedType type = (ParameterizedType) field.getGenericType();
        final Type[] types = type.getActualTypeArguments();
        return types;
    }    
    
    /**
     * Test of readValue method,
     */
    public void testReadWrite() throws Throwable {
        System.out.println("testReadWrite");
        
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Class<?> o4 = Color.class;
        
        QuickUjoImpl ujb = new QuickUjoImpl();
        
        QuickUjoImpl.PRO_P0.setValue(ujb, o0);
        QuickUjoImpl.PRO_P1.setValue(ujb, o1);
        QuickUjoImpl.PRO_P2.setValue(ujb, o2);
        QuickUjoImpl.PRO_P3.setValue(ujb, o3);
        QuickUjoImpl.PRO_P4.setValue(ujb, o4);
        
        assertEquals(o0, QuickUjoImpl.PRO_P0.of(ujb));
        assertEquals(o1, QuickUjoImpl.PRO_P1.of(ujb));
        assertEquals(o2, QuickUjoImpl.PRO_P2.of(ujb));
        assertEquals(o3, QuickUjoImpl.PRO_P3.of(ujb));
        assertEquals(o4, QuickUjoImpl.PRO_P4.of(ujb));
    }
    
    public void testSpeedTime() throws Throwable {
        System.out.println("A1:testSpeedTime: " + suite().toString());
        
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Class<?> o4 = Color.class;
        Object result;
        
        callGC();
        long time1 = System.currentTimeMillis();
        
        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            QuickUjoImpl ujb = new QuickUjoImpl();
            QuickUjoImpl.PRO_P0.setValue(ujb, o0);
            QuickUjoImpl.PRO_P1.setValue(ujb, o1);
            QuickUjoImpl.PRO_P2.setValue(ujb, o2);
            QuickUjoImpl.PRO_P3.setValue(ujb, o3);
            QuickUjoImpl.PRO_P4.setValue(ujb, o4);
            QuickUjoImpl.PRO_P0.setValue(ujb, o0);
            QuickUjoImpl.PRO_P1.setValue(ujb, o1);
            QuickUjoImpl.PRO_P2.setValue(ujb, o2);
            QuickUjoImpl.PRO_P3.setValue(ujb, o3);
            QuickUjoImpl.PRO_P4.setValue(ujb, o4);
            
            assertEquals(o0, QuickUjoImpl.PRO_P0.getValue(ujb));
            assertEquals(o1, QuickUjoImpl.PRO_P1.getValue(ujb));
            assertEquals(o2, QuickUjoImpl.PRO_P2.getValue(ujb));
            assertEquals(o3, QuickUjoImpl.PRO_P3.getValue(ujb));
            assertEquals(o4, QuickUjoImpl.PRO_P4.getValue(ujb));
            assertEquals(o0, QuickUjoImpl.PRO_P0.getValue(ujb));
            assertEquals(o1, QuickUjoImpl.PRO_P1.getValue(ujb));
            assertEquals(o2, QuickUjoImpl.PRO_P2.getValue(ujb));
            assertEquals(o3, QuickUjoImpl.PRO_P3.getValue(ujb));
            assertEquals(o4, QuickUjoImpl.PRO_P4.getValue(ujb));
        }
        long time2 = System.currentTimeMillis();
        
        System.out.println("A1:TIME: " + (time2-time1)/1000f + " [sec]");
    }
    
    public void testSpeedTimeRecur() throws Throwable {
        System.out.println("A2:testSpeedTime (recur): " + suite().toString());
        
        Long    o0 = new Long(Long.MAX_VALUE);
        Integer o1 = new Integer(1);
        String  o2 ="TEST";
        Date    o3 = new Date();
        Class<?> o4 = Color.class;
        Object result;
        
        callGC();
        long time1 = System.currentTimeMillis();
        
        for (int i=getTimeLoopCount()-1; i>=0; i--) {
            QuickUjoImplChild ujb = new QuickUjoImplChild();
            
            QuickUjoImplChild.PRO_P0.setValue(ujb, o0);
            QuickUjoImplChild.PRO_P1.setValue(ujb, o1);
            QuickUjoImplChild.PRO_P2.setValue(ujb, o2);
            QuickUjoImplChild.PRO_P3.setValue(ujb, o3);
            QuickUjoImplChild.PRO_P4.setValue(ujb, o4);
            QuickUjoImplChild.PRO_P5.setValue(ujb, o0);
            QuickUjoImplChild.PRO_P6.setValue(ujb, o1);
            QuickUjoImplChild.PRO_P7.setValue(ujb, o2);
            QuickUjoImplChild.PRO_P8.setValue(ujb, o3);
            QuickUjoImplChild.PRO_P9.setValue(ujb, o4);
            
            assertEquals(o0, QuickUjoImplChild.PRO_P0.getValue(ujb));
            assertEquals(o1, QuickUjoImplChild.PRO_P1.getValue(ujb));
            assertEquals(o2, QuickUjoImplChild.PRO_P2.getValue(ujb));
            assertEquals(o3, QuickUjoImplChild.PRO_P3.getValue(ujb));
            assertEquals(o4, QuickUjoImplChild.PRO_P4.getValue(ujb));
            assertEquals(o0, QuickUjoImplChild.PRO_P5.getValue(ujb));
            assertEquals(o1, QuickUjoImplChild.PRO_P6.getValue(ujb));
            assertEquals(o2, QuickUjoImplChild.PRO_P7.getValue(ujb));
            assertEquals(o3, QuickUjoImplChild.PRO_P8.getValue(ujb));
            assertEquals(o4, QuickUjoImplChild.PRO_P9.getValue(ujb));
        }
        long time2 = System.currentTimeMillis();
        
        System.out.println("A2:TIME: " + (time2-time1)/1000f + " [sec]");
    }
        
    
    /** Test of properties */
    public void testGetProperties1() throws Throwable {
        QuickUjoImpl ujb1 = new QuickUjoImpl();
        UjoPropertyList props = ujb1.readProperties();
        
        assertEquals(QuickUjoImpl.PRO_P0, props.get(0));
        assertEquals(QuickUjoImpl.PRO_P1, props.get(1));
        assertEquals(QuickUjoImpl.PRO_P2, props.get(2));
        assertEquals(QuickUjoImpl.PRO_P3, props.get(3));
        assertEquals(QuickUjoImpl.PRO_P4, props.get(4));
    }
    
    
    public static void main(java.lang.String[] argList) {
        junit.textui.TestRunner.run(suite());
    }
    
}
