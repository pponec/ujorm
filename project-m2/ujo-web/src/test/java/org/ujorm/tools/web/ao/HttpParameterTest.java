package org.ujorm.tools.web.ao;

import java.time.Month;
import static java.time.Month.JANUARY;
import org.springframework.mock.web.MockHttpServletRequest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static java.time.Month.*;

/**
 *
 * @author Pavel Ponec
 */
public class HttpParameterTest {

    /**
     * Test of of method, of class HttpParameter.
     */
    @Test
    public void testOf_ServletRequest_String() {
        String defaultValue = "x";
        String result = Param.TEXT.of(request(), defaultValue);
        assertEquals("acb", result);
        
        result = Param.UNDEFINED.of(request(), defaultValue);
        assertEquals(defaultValue, result);
    }

    /**
     * Test of of method, of class HttpParameter.
     */
    @Test
    public void testOf_ServletRequest_boolean() {
        boolean defaultValue = true;
        Boolean result = Param.BOOLEAN.of(request(), false);
        assertEquals(true, result);
        
        result = Param.UNDEFINED.of(request(), defaultValue);
        assertEquals(defaultValue, result);
    }

    /**
     * Test of of method, of class HttpParameter.
     */
    @Test
    public void testOf_ServletRequest_char() {
        char defaultValue = 'Z';
        char result = Param.CHAR.of(request(), defaultValue);
        assertEquals("acb", result);
        
        result = Param.UNDEFINED.of(request(), defaultValue);
        assertEquals(defaultValue, result);
    }

    /**
     * Test of of method, of class HttpParameter.
     */
    @Test
    public void testOf_ServletRequest_int() {
        int defaultValue = 9;
        int result = Param.INT.of(request(), defaultValue);
        assertEquals(1, result);
        
        result = Param.UNDEFINED.of(request(), defaultValue);
        assertEquals(defaultValue, result);
    }

    /**
     * Test of of method, of class HttpParameter.
     */
    @Test
    public void testOf_ServletRequest_long() {
        long defaultValue = 9L;
        long result = Param.LONG.of(request(), defaultValue);
        assertEquals(2L, result);
        
        result = Param.UNDEFINED.of(request(), defaultValue);
        assertEquals(defaultValue, result);
    }

    /**
     * Test of of method, of class HttpParameter.
     */
    @Test
    public void testOf_ServletRequest_float() {
        float defaultValue = 9F;
        float result = Param.TEXT.of(request(), defaultValue);
        assertEquals("acb", result);
        
        result = Param.UNDEFINED.of(request(), defaultValue);
        assertEquals(defaultValue, result);
    }

    /**
     * Test of of method, of class HttpParameter.
     */
    @Test
    public void testOf_ServletRequest_double() {
        double defaultValue = 9D;
        double result = Param.TEXT.of(request(), defaultValue);
        assertEquals("acb", result);
        
        result = Param.UNDEFINED.of(request(), defaultValue);
        assertEquals(defaultValue, result);
    }

    /**
     * Test of of method, of class HttpParameter.
     */
    @Test
    public void testOf_ServletRequest_Enum() {
        Month defaultValue = DECEMBER;
        Month result = Param.MONTH_ENUM.of(request(), defaultValue);
        assertEquals(JANUARY, result);
        
        result = Param.UNDEFINED.of(request(), defaultValue);
        assertEquals(defaultValue, result);
    }

    
    // --- Helper methods ---
    
    private MockHttpServletRequest request() {
        MockHttpServletRequest result = new MockHttpServletRequest();
        result.setParameter(Param.TEXT.name(), "abc");
        result.setParameter(Param.BOOLEAN.name(), Boolean.TRUE.toString());
        result.setParameter(Param.CHAR.name(), "A");
        result.setParameter(Param.INT.name(), String.valueOf(1));
        result.setParameter(Param.LONG.name(), String.valueOf(2L));
        result.setParameter(Param.FLOAT.name(), String.valueOf(3F));
        result.setParameter(Param.DOUBLE.name(), String.valueOf(4D));
        result.setParameter(Param.MONTH_ENUM.name(), JANUARY.name());
        result.setParameter(Param.UNDEFINED.name(), (String) null);
        
        return result;
    }

    /** Parameter */
    public enum Param implements HttpParameter {
        TEXT,
        BOOLEAN,
        CHAR,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        MONTH_ENUM,
        UNDEFINED;

        public String toString() {
            return name().toString();
        }
    }
    
}
