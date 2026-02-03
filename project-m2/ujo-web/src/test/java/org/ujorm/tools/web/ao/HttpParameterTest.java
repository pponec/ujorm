package org.ujorm.tools.web.ao;

import java.time.Month;
import static java.time.Month.JANUARY;
import org.junit.jupiter.api.Test;
import org.ujorm.tools.web.request.ManyMap;
import org.ujorm.tools.web.request.HttpContext;

import static org.junit.jupiter.api.Assertions.*;
import static java.time.Month.*;

/**
 *
 * @author Pavel Ponec
 */
public class HttpParameterTest {

    private static final double DELTA = 0.0000001;

    /**
     * Test of of method, of class HttpParameter.
     */
    @Test
    public void testOf_ServletRequest_String() {
        String defaultValue = "x";
        String result = Param.TEXT.of(context(), defaultValue);
        assertEquals("abc", result);

        result = Param.UNDEFINED.of(context(), defaultValue);
        assertEquals(defaultValue, result);
    }

    /**
     * Test of of method, of class HttpParameter.
     */
    @Test
    public void testOf_ServletRequest_boolean() {
        boolean defaultValue = true;
        Boolean result = Param.BOOLEAN.of(context(), false);
        assertEquals(true, result);

        result = Param.UNDEFINED.of(context(), defaultValue);
        assertEquals(defaultValue, result);
    }

    /**
     * Test of of method, of class HttpParameter.
     */
    @Test
    public void testOf_ServletRequest_char() {
        char defaultValue = 'Z';
        char result = Param.CHAR.of(context(), defaultValue);
        assertEquals('A', result);

        result = Param.UNDEFINED.of(context(), defaultValue);
        assertEquals(defaultValue, result);
    }

    /**
     * Test of method, of class HttpParameter.
     */
    @Test
    public void testOf_ServletRequest_int() {
        int defaultValue = 9;
        int result = Param.INT.of(context(), defaultValue);
        assertEquals(1, result);

        result = Param.UNDEFINED.of(context(), defaultValue);
        assertEquals(defaultValue, result);
    }

    /**
     * Test of of method, of class HttpParameter.
     */
    @Test
    public void testOf_ServletRequest_long() {
        long defaultValue = 9L;
        long result = Param.LONG.of(context(), defaultValue);
        assertEquals(2L, result);

        result = Param.UNDEFINED.of(context(), defaultValue);
        assertEquals(defaultValue, result);
    }

    /**
     * Test of method, of class HttpParameter.
     */
    @Test
    public void testOf_ServletRequest_float() {
        float defaultValue = 9F;
        float result = Param.FLOAT.of(context(), defaultValue);
        assertEquals(3F, result, DELTA);

        result = Param.UNDEFINED.of(context(), defaultValue);
        assertEquals(defaultValue, result, DELTA);
    }

    /**
     * Test of of method, of class HttpParameter.
     */
    @Test
    public void testOf_ServletRequest_double() {
        double defaultValue = 9D;
        double result = Param.DOUBLE.of(context(), defaultValue);
        assertEquals(4D, result, DELTA);

        result = Param.UNDEFINED.of(context(), defaultValue);
        assertEquals(defaultValue, result, DELTA);
    }

    /**
     * Test of method, of class HttpParameter.
     */
    @Test
    public void testOf_ServletRequest_Enum() {
        Month defaultValue = DECEMBER;
        HttpContext dummyContext = context();
        Month result = Param.MONTH_ENUM.of(dummyContext, defaultValue);
        assertEquals(JANUARY, result);

        result = Param.UNDEFINED.of(context(), defaultValue);
        assertEquals(defaultValue, result);
    }

    /**
     * Test name of the HTTP method.
     */
    @Test
    public void testOfNamedAttributes() {
        assertEquals("a", Param2.TEXT.toString());
        assertEquals("b", Param2.BOOLEAN.toString());
        assertEquals("c", Param2.CHAR.toString());
        assertEquals("d", Param2.INT.toString());
        assertEquals("long", Param2.LONG.toString());
        assertEquals("float", Param2.FLOAT.toString());
        assertEquals("double", Param2.DOUBLE.toString());
        assertEquals("month-enum", Param2.MONTH_ENUM.toString());
        assertEquals("undefined", Param2.UNDEFINED.toString());
    }

    // --- Helper methods ---

    private HttpContext context() {
        ManyMap map = new ManyMap();
        map.put(Param.TEXT.name(), "abc");
        map.put(Param.BOOLEAN.name(), Boolean.TRUE.toString());
        map.put(Param.CHAR.name(), "A");
        map.put(Param.INT.name(), String.valueOf(1));
        map.put(Param.LONG.name(), String.valueOf(2L));
        map.put(Param.FLOAT.name(), String.valueOf(3F));
        map.put(Param.DOUBLE.name(), String.valueOf(4D));
        map.put(Param.MONTH_ENUM.name(), JANUARY.name());
        map.put(Param.UNDEFINED.name(), (String) null);

        return HttpContext.of(map);
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
            return name();
        }
    }

        /** Parameter */
    public enum Param2 implements HttpParameter {
        TEXT("a"),
        BOOLEAN("b"),
        CHAR("c"),
        INT("d"),
        LONG(null),
        FLOAT(null),
        DOUBLE(null),
        MONTH_ENUM(null),
        UNDEFINED(null);

        private final String paramName;

        Param2(String name) {
            this.paramName = buildParameterName(name);
        }

        public String toString() {
            return paramName;
        }
    }

}
