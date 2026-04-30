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
import org.ujorm.tools.web.request.AbstractExchangeContext;
import org.ujorm.tools.web.request.ExchangeContext;
import org.ujorm.tools.web.request.ManyMap;

import static java.time.Month.DECEMBER;
import static java.time.Month.JANUARY;
import static org.junit.jupiter.api.Assertions.*;

/** Test of the HttpParameterEq interface. */
class HttpParameterEqTest {

    /** Delta for double comparisons */
    private static final double DELTA = 0.0000001;

    /** Test of all String variants of the {@code of()} method. */
    @Test
    void testOf_ServletRequest_String() {
        var context = createContext();
        var request = context.request();

        // Testing all 4 default methods for String retrieval
        assertEquals("abc", Param.TEXT.of(context, "def"));
        assertEquals("abc", Param.TEXT.of(context));
        assertEquals("abc", Param.TEXT.of(request, "def"));
        assertEquals("abc", Param.TEXT.of(request));

        assertEquals("def", Param.UNDEFINED.of(context, "def"));
        assertEquals("", Param.UNDEFINED.of(context));
        assertEquals("def", Param.UNDEFINED.of(request, "def"));
        assertEquals("", Param.UNDEFINED.of(request));
    }

    /** Test of the of method for boolean. */
    @Test
    void testOf_ServletRequest_boolean() {
        var context = createContext();
        assertTrue(Param.BOOLEAN.of(context, false));
        assertTrue(Param.UNDEFINED.of(context, true));

        var contextFalse = createContext("TEXT", "false");
        assertFalse(Param.TEXT.of(contextFalse, true));

        var contextInvalid = createContext("TEXT", "invalid_boolean");
        assertFalse(Param.TEXT.of(contextInvalid, false));
        assertTrue(Param.TEXT.of(contextInvalid, true));
    }

    /** Test of the of method for char. */
    @Test
    void testOf_ServletRequest_char() {
        var context = createContext();
        assertEquals('A', Param.CHAR.of(context, 'Z'));
        assertEquals('Z', Param.UNDEFINED.of(context, 'Z'));

        var contextEmpty = createContext("TEXT", "");
        assertEquals('Z', Param.TEXT.of(contextEmpty, 'Z'));
    }

    /** Test of the of method for primitive numbers. */
    @Test
    void testOf_ServletRequest_Numbers() {
        var context = createContext();
        assertEquals((short) 5, Param.SHORT.of(context, (short) 9));
        assertEquals(1, Param.INT.of(context, 9));
        assertEquals(2L, Param.LONG.of(context, 9L));
        assertEquals(3F, Param.FLOAT.of(context, 9F), DELTA);
        assertEquals(4D, Param.DOUBLE.of(context, 9D), DELTA);
    }

    /** Test parsing errors for numeric values. */
    @Test
    void testOf_ServletRequest_NumbersExceptions() {
        var context = createContext("TEXT", "not-a-number");

        assertEquals((short) 9, Param.TEXT.of(context, (short) 9));
        assertEquals(9, Param.TEXT.of(context, 9));
        assertEquals(9L, Param.TEXT.of(context, 9L));
        assertEquals(9F, Param.TEXT.of(context, 9F), DELTA);
        assertEquals(9D, Param.TEXT.of(context, 9D), DELTA);
    }

    /** Test of the of method for Enum. */
    @Test
    void testOf_ServletRequest_Enum() {
        var context = createContext();
        assertEquals(JANUARY, Param.MONTH_ENUM.of(context, DECEMBER));
        assertEquals(DECEMBER, Param.UNDEFINED.of(context, DECEMBER));

        var contextMonth = createContext("TEXT", "DECEMBER");
        assertEquals(DECEMBER, Param.TEXT.of(contextMonth, java.time.Month.class));

        var contextMissing = createContext("TEXT", "MISSING_VALUE");
        assertNull(Param.TEXT.of(contextMissing, java.time.Month.class));
    }

    /** Test of custom decoder function and its exceptions. */
    @Test
    void testOf_ServletRequest_Function() {
        var context = createContext();
        assertEquals(1, Param.INT.of(context, -1, Integer::parseInt));

        var contextInvalid = createContext("TEXT", "invalid");
        assertEquals(-1, Param.TEXT.of(contextInvalid, -1, val -> {
            throw new IllegalArgumentException("Forced exception");
        }));
    }

    /** Test of equalsParamName and CharSequence methods. */
    @Test
    void testCommonMethods() {
        var param = Param.TEXT;
        assertTrue(param.equalsParamName("TEXT"));
        assertFalse(param.equalsParamName("text"));
        assertEquals(4, param.length());
        assertEquals('E', param.charAt(1));
        assertEquals("EX", param.subSequence(1, 3).toString());
        assertEquals("TEXT_SUFFIX", param.concat("_SUFFIX"));
        assertEquals("TEXT", param.concat(""));
    }

    /** Test of buildParameterName method including underscore replacement. */
    @Test
    void testBuildParameterName() {
        var param = Param.TEXT;
        assertEquals("custom-name", param.buildParameterName("custom-name"));
        assertEquals("text", param.buildParameterName(null));

        var paramComplex = Param.COMPLEX_NAME_FOR_TEST;
        assertEquals("complex-name-for-test", paramComplex.buildParameterName(null));
    }

    /** Test of static factory methods of(). */
    @Test
    void testStaticOfMethods() {
        var param1 = HttpParameterEq.of("customParam");
        assertEquals("customParam", param1.paramName());
        assertEquals("", param1.defaultValue());

        var param2 = HttpParameterEq.of("customParam2", "defaultText");
        assertEquals("customParam2", param2.paramName());
        assertEquals("defaultText", param2.defaultValue());

        var context = createContext("customParam", "val1");
        var result1 = param1.of(context);
        assertEquals("val1", result1);

        var context2 = createContext();
        var result2 = param2.of(context2);
        assertEquals("defaultText", result2);
    }

    /** Test of originalName specifically for an Enum implementation. */
    @Test
    void testOriginalName_Enum() {
        var result = SimpleEnum.VALUE_ONE.originalName();
        assertEquals("VALUE_ONE", result);
    }

    /** Test of the originalName method directly using local classes for bulletproof coverage. */
    @Test
    void testOriginalName_ReflectionFallback() {
        // 1. Coverage for the successful 'try' block
        class DynamicParam implements HttpParameterEq<DynamicParam> {
            public String name() { return "DynamicName"; }
            @Override public String toString() { return name(); }
        }
        var validParam = new DynamicParam();
        var result1 = validParam.originalName();
        assertEquals("DynamicName", result1);

        // 2. Coverage for the 'catch' block (InvocationTargetException wrapper)
        class ErrorParam implements HttpParameterEq<ErrorParam> {
            public String name() { throw new RuntimeException("Forced exception for coverage"); }
            @Override public String toString() { return "ErrorName"; }
        }
        var errorParam = new ErrorParam();
        var exceptionResult = assertThrows(IllegalStateException.class, errorParam::originalName);
        assertTrue(exceptionResult.getMessage().contains("Method 'name()' is not available"));

        // 3. Coverage for missing method in cache
        class NoNameParam implements HttpParameterEq<NoNameParam> {
            @Override public String toString() { return "NoName"; }
        }
        var noNameParam = new NoNameParam();
        assertThrows(IllegalStateException.class, noNameParam::originalName);
    }

    /** Test of the NAME_METHOD_CACHE constant directly. */
    @Test
    void testNameMethodCache() {
        var validMethod = HttpParameterEq.NAME_METHOD_CACHE.get(CustomNamedParam.class);
        assertNotNull(validMethod);
        assertEquals("name", validMethod.getName());
        assertTrue(validMethod.isAccessible(), "Method should be accessible");

        var exception = assertThrows(IllegalStateException.class, () ->
                HttpParameterEq.NAME_METHOD_CACHE.get(NamelessParam.class)
        );
        assertTrue(exception.getMessage().contains("Method 'name()' is not available"));
    }

    /** Test of paramValueOf static method variants. */
    @Test
    void testParamValueOf() {
        assertEquals(Param.TEXT, HttpParameterEq.paramValueOf(Param.class, "TEXT"));
        assertEquals(Param.TEXT, HttpParameterEq.paramValueOf(Param.class, "TEXT", Param.UNDEFINED));
        assertEquals(Param.UNDEFINED, HttpParameterEq.paramValueOf(Param.class, "INVALID", Param.UNDEFINED));
        assertEquals(Param.UNDEFINED, HttpParameterEq.paramValueOf(Param.class, "", Param.UNDEFINED));
        assertNull(HttpParameterEq.paramValueOf(Param.class, null));
        assertNull(HttpParameterEq.paramValueOf(Param.class, ""));
    }

    /** Parameter for testing */
    public enum Param implements HttpParameterEq<Param> {
        TEXT,
        BOOLEAN,
        CHAR,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        MONTH_ENUM,
        COMPLEX_NAME_FOR_TEST,
        UNDEFINED;

        @Override
        public String toString() {
            return name();
        }
    }

    /** Simple Enum for testing originalName branch */
    public enum SimpleEnum implements HttpParameterEq<SimpleEnum> {
        VALUE_ONE;
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    /** Custom implementation with name() method */
    static class CustomNamedParam implements HttpParameterEq<CustomNamedParam> {
        public String name() {
            return "CustomName";
        }
        @Override
        public String toString() {
            return name();
        }
    }

    /** Custom implementation without name() method */
    static class NamelessParam implements HttpParameterEq<NamelessParam> {
        @Override
        public String toString() {
            return "Nameless";
        }
    }

    /** Helper method to create full context */
    static AbstractExchangeContext createContext() {
        var map = new ManyMap();
        map.put(Param.TEXT.name(), "abc");
        map.put(Param.BOOLEAN.name(), "true");
        map.put(Param.CHAR.name(), "A");
        map.put(Param.SHORT.name(), "5");
        map.put(Param.INT.name(), "1");
        map.put(Param.LONG.name(), "2");
        map.put(Param.FLOAT.name(), "3");
        map.put(Param.DOUBLE.name(), "4");
        map.put(Param.MONTH_ENUM.name(), JANUARY.name());
        map.put(Param.UNDEFINED.name(), (String) null);

        return ExchangeContext.of(map);
    }

    /** Helper method to create partial context */
    static AbstractExchangeContext createContext(String key, String value) {
        var map = new ManyMap();
        map.put(key, value);
        return ExchangeContext.of(map);
    }
}