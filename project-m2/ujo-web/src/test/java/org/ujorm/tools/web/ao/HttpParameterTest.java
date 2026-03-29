/*
 * Copyright 2020-2026 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.web.ao;

import org.junit.jupiter.api.Test;
import org.ujorm.tools.web.request.HttpContext;
import org.ujorm.tools.web.request.ManyMap;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.junit.jupiter.api.Assertions.*;

/** Test of the HttpParameter interface. */
public class HttpParameterTest {

    private static final double DELTA = 0.0000001;

    /** Test of the of method for String. */
    @Test
    public void testOf_ServletRequest_String() {
        assertEquals("abc", Param.TEXT.of(context(), "x"));
        assertEquals("x", Param.UNDEFINED.of(context(), "x"));
    }

    /** Test of the of method for boolean. */
    @Test
    public void testOf_ServletRequest_boolean() {
        assertEquals(true, Param.BOOLEAN.of(context(), false));
        assertEquals(true, Param.UNDEFINED.of(context(), true));
    }

    /** Test of the of method for char. */
    @Test
    public void testOf_ServletRequest_char() {
        assertEquals('A', Param.CHAR.of(context(), 'Z'));
        assertEquals('Z', Param.UNDEFINED.of(context(), 'Z'));
    }

    /** Test of the of method for short. */
    @Test
    public void testOf_ServletRequest_short() {
        assertEquals((short) 5, Param.SHORT.of(context(), (short) 9));
        assertEquals((short) 9, Param.UNDEFINED.of(context(), (short) 9));
    }

    /** Test of the of method for int. */
    @Test
    public void testOf_ServletRequest_int() {
        assertEquals(1, Param.INT.of(context(), 9));
        assertEquals(9, Param.UNDEFINED.of(context(), 9));
    }

    /** Test of the of method for long. */
    @Test
    public void testOf_ServletRequest_long() {
        assertEquals(2L, Param.LONG.of(context(), 9L));
        assertEquals(9L, Param.UNDEFINED.of(context(), 9L));
    }

    /** Test of the of method for float. */
    @Test
    public void testOf_ServletRequest_float() {
        assertEquals(3F, Param.FLOAT.of(context(), 9F), DELTA);
        assertEquals(9F, Param.UNDEFINED.of(context(), 9F), DELTA);
    }

    /** Test of the of method for double. */
    @Test
    public void testOf_ServletRequest_double() {
        assertEquals(4D, Param.DOUBLE.of(context(), 9D), DELTA);
        assertEquals(9D, Param.UNDEFINED.of(context(), 9D), DELTA);
    }

    /** Test of the of method for Enum. */
    @Test
    public void testOf_ServletRequest_Enum() {
        assertEquals(JANUARY, Param.MONTH_ENUM.of(context(), DECEMBER));
        assertEquals(DECEMBER, Param.UNDEFINED.of(context(), DECEMBER));
        assertEquals(Param2.TEXT, Param2.TEXT.of(context(), Param2.UNDEFINED));
    }

    /** Test of custom decoder function. */
    @Test
    public void testOf_ServletRequest_Function() {
        assertEquals(1, Param.INT.of(context(), -1, Integer::parseInt));
        assertEquals(-1, Param.UNDEFINED.of(context(), -1, Integer::parseInt));
    }

    /** Test of equalsParamName method. */
    @Test
    public void testEqualsParamName() {
        var param = Param.TEXT;
        assertTrue(param.equalsParamName("TEXT"));
        assertFalse(param.equalsParamName("text"));
        assertFalse(param.equalsParamName(null));

        var param2 = Param2.TEXT;
        assertTrue(param2.equalsParamName("a"));
        assertFalse(param2.equalsParamName("TEXT"));
    }

    /** Test of CharSequence overridden methods. */
    @Test
    public void testCharSequenceMethods() {
        var param = Param.TEXT;
        assertEquals(4, param.length());
        assertEquals('E', param.charAt(1));
        assertEquals("EX", param.subSequence(1, 3).toString());
    }

    /** Test of concat method. */
    @Test
    public void testConcat() {
        var param = Param.TEXT;
        assertEquals("TEXT_SUFFIX", param.concat("_SUFFIX"));
        assertEquals("TEXT", param.concat(""));
    }

    /** Test of static factory methods. */
    @Test
    public void testStaticOf() {
        var param1 = HttpParameter.of("customParam");
        assertEquals("customParam", param1.paramName());
        assertEquals("", param1.defaultValue());

        var param2 = HttpParameter.of("customParam2", "defaultText");
        assertEquals("customParam2", param2.paramName());
        assertEquals("defaultText", param2.defaultValue());
    }

    /** Test name of the HTTP method. */
    @Test
    public void testOfNamedAttributes() {
        assertEquals("a", Param2.TEXT.toString());
        assertEquals("b", Param2.BOOLEAN.toString());
        assertEquals("c", Param2.CHAR.toString());
        assertEquals("short", Param2.SHORT.toString());
        assertEquals("d", Param2.INT.toString());
        assertEquals("long", Param2.LONG.toString());
        assertEquals("float", Param2.FLOAT.toString());
        assertEquals("double", Param2.DOUBLE.toString());
        assertEquals("month-enum", Param2.MONTH_ENUM.toString());
        assertEquals("undefined", Param2.UNDEFINED.toString());
    }

    /** Test of originalName method on Enum (fast-path). */
    @Test
    public void testOriginalName_Enum() {
        var param = Param.TEXT;
        assertEquals("TEXT", param.originalName());
    }

    /** Test of originalName method on non-Enum class. */
    @Test
    public void testOriginalName_NonEnum() {
        var param = new CustomNamedParam();
        assertEquals("CustomName", param.originalName());
    }

    /** Test of originalName method on non-Enum class without name() method. */
    @Test
    public void testOriginalName_MissingMethod() {
        var param = new NamelessParam();
        var exception = assertThrows(IllegalStateException.class, param::originalName);
        assertTrue(exception.getMessage().contains("Method 'name()' is not available"));
    }

    /** Test of paramValueOf static method. */
    @Test
    public void testParamValueOf() {
        assertEquals(Param.TEXT, HttpParameter.paramValueOf(Param.class, "TEXT"));
        assertEquals(Param2.TEXT, HttpParameter.paramValueOf(Param2.class, "a"));
        assertNull(HttpParameter.paramValueOf(Param2.class, "TEXT"));
    }

    // --- Helper methods and inner classes ---

    /** Helper method to create context */
    private HttpContext context() {
        var map = new ManyMap();
        map.put(Param.TEXT.name(), "abc");
        map.put(Param.BOOLEAN.name(), Boolean.TRUE.toString());
        map.put(Param.CHAR.name(), "A");
        map.put(Param.SHORT.name(), String.valueOf(5));
        map.put(Param.INT.name(), String.valueOf(1));
        map.put(Param.LONG.name(), String.valueOf(2L));
        map.put(Param.FLOAT.name(), String.valueOf(3F));
        map.put(Param.DOUBLE.name(), String.valueOf(4D));
        map.put(Param.MONTH_ENUM.name(), JANUARY.name());
        map.put(Param.UNDEFINED.name(), (String) null);

        // Add value for Param2 based on its custom paramName
        map.put(Param2.TEXT.paramName(), Param2.TEXT.paramName());

        return HttpContext.of(map);
    }

    /** Custom implementation with name() method */
    static class CustomNamedParam implements HttpParameter {
        public String name() {
            return "CustomName";
        }

        @Override
        public String toString() {
            return name();
        }
    }

    /** Custom implementation without name() method */
    static class NamelessParam implements HttpParameter {
        @Override
        public String toString() {
            return "Nameless";
        }
    }

    /** Parameter */
    public enum Param implements HttpParameter {
        TEXT,
        BOOLEAN,
        CHAR,
        SHORT,
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
        SHORT(null),
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