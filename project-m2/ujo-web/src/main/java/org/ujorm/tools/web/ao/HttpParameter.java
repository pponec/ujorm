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

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.Check;
import org.ujorm.tools.web.request.HttpContext;
import org.ujorm.tools.web.request.URequest;

/**
 * An interface for bulding HTML parameters by an Enumerator.
 * The implementation the method {@link Object#toString()} is required!
 *
 * <h4>Usage</h4>
 * <pre class="pre">
 * {
 *    String value = Param.text(ServletRequest, "my default value");
 * }
 * enum Param implements HttpParam {
 *     REGEXP,
 *     TEXT;
 *     @Override public String toString() {
 *         return name().toLowerCase();
 *     }
 * }
 * </pre>
 *
 * @author Pavel Ponec
 */
public interface HttpParameter extends CharSequence {

    /** An empty text value */
    String EMPTY_VALUE = "";

    /** Cache for the 'name' method to avoid reflection overhead and access issues. */
    ClassValue<Method> NAME_METHOD_CACHE = new ClassValue<>() {
        @Override
        protected Method computeValue(Class<?> type) {
            try {
                var method = type.getMethod("name");
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Method 'name()' is not available on " + type.getName(), e);
            }
        }
    };

    /** Returns a result of the expression {@code name().toLowerCase()} */
    @NotNull
    @Override
    String toString();

    /** Returns the HTTP parameter name, defaults to the toString() method. */
    @NotNull
    default String paramName() {
        return toString();
    }

    /** Compare argument with a result of the method {@link #toString()}. */
    default boolean equalsParamName(@Nullable String name) {
        return toString().equals(name);
    }

    @Override
    default int length() {
        return toString().length();
    }

    @Override
    default char charAt(int index) {
        return toString().charAt(index);
    }

    @Override
    default CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    /** Returns a non-null default text value.
     * The standard value is an empty String, override it for a change. */
    @NotNull
    default String defaultValue() {
        return "";
    }

    /** Build a default non-null parameter name. */
    @NotNull
    default String buildParameterName(@Nullable String name) {
        return name != null ? name : originalName().toLowerCase(Locale.ENGLISH).replace('_', '-');
    }

    /** Method for an internal use: get a raw name of the HTTP parameter.
     * The method can be called from the {@link #buildParameterName(java.lang.String)} method.
     * NOTE: The method was renamed from obsolete {@code name()} due a Kotlin compatibility. */
    @NotNull
    default String originalName() {
        if (this instanceof Enum<?> e) {
            return e.name();
        }
        try {
            var method = NAME_METHOD_CACHE.get(getClass());
            return String.valueOf(method.invoke(this));
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new IllegalStateException("Method 'name()' is not available", e);
        }
    }

    /** Join the parameter name with another non-null text */
    default String concat(@NotNull String text) {
        if (text.isEmpty()) {
            return this.toString();
        }
        return this + text;
    }

    /** Returns the last parameter value of the request or a default value. The MAIN method */
    @NotNull
    default String of(@NotNull final URequest request, @NotNull final String defaultValue) {
        var results = request.parameters(toString());
        var result = Check.hasLength(results) ? results[results.length - 1] : defaultValue;
        return result != null ? result : defaultValue;
    }

    /** Returns the last parameter value of the request or a default value */
    @NotNull
    default String of(@NotNull final HttpContext context, @NotNull final String defaultValue) {
        return of(context.request(), defaultValue);
    }

    /** Default value is an empty String */
    @NotNull
    default String of(@NotNull final HttpContext context) {
        return of(context.request(), defaultValue());
    }

    /** Default value is an empty String */
    @NotNull
    default String of(@NotNull final URequest request) {
        return of(request, defaultValue());
    }

    /** Returns a parameter of the request or the default value */
    default boolean of(@NotNull final HttpContext context, @Nullable final boolean defaultValue) {
        switch (of(context)) {
            case "true":
                return true;
            case "false":
                return false;
            default:
                return defaultValue;
        }
    }

    /** Returns a parameter of the request or the default value */
    default char of(@NotNull final HttpContext context, @Nullable final char defaultValue) {
        var value = of(context);
        return value.isEmpty() ? defaultValue : value.charAt(0);
    }

    /** Returns a parameter of the request or the default value */
    default short of(@NotNull final HttpContext context, @Nullable final short defaultValue) {
        var value = of(context, EMPTY_VALUE);
        if (value.isEmpty()) {
            return defaultValue;
        } else try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Returns a parameter of the request or the default value */
    default int of(@NotNull final HttpContext context, @Nullable final int defaultValue) {
        var value = of(context, EMPTY_VALUE);
        if (value.isEmpty()) {
            return defaultValue;
        } else try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Returns a parameter of the request or the default value */
    default long of(@NotNull final HttpContext context, @Nullable final long defaultValue) {
        var value = of(context, EMPTY_VALUE);
        if (value.isEmpty()) {
            return defaultValue;
        } else try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Returns a parameter of the request or the default value */
    default float of(@NotNull final HttpContext context, @Nullable final float defaultValue) {
        var value = of(context, EMPTY_VALUE);
        if (value.isEmpty()) {
            return defaultValue;
        } else try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Returns a parameter of the request or the default value */
    default double of(@NotNull final HttpContext context, @Nullable final double defaultValue) {
        var value = of(context, EMPTY_VALUE);
        if (value.isEmpty()) {
            return defaultValue;
        } else try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Returns a parameter of the request or the Enum class */
    @NotNull
    default <V extends Enum<V>> V of(@NotNull final HttpContext context, @NotNull final V defaultValue) {
        var result = of(context, (Class<V>) defaultValue.getClass());
        return result != null ? result : defaultValue;
    }

    /** Returns a parameter of the request or the default value */
    @Nullable
    default <V extends Enum<V>> V of(@NotNull final HttpContext context, @NotNull final Class<V> clazz) {
        var value = of(context);
        for (var item : clazz.getEnumConstants()) {
            if (item instanceof HttpParameter p) {
                if (p.equalsParamName(value)) {
                    return item;
                }
            } else if (item.name().equals(value)) {
                return item;
            }
        }
        return null;
    }

    /** Returns a parameter of the request or the default value */
    default <V> V of(@NotNull final HttpContext context, @NotNull final V defaultValue, @NotNull final Function<String, V> decoder) {
        var value = of(context, EMPTY_VALUE);
        if (value.isEmpty()) {
            return defaultValue;
        } else try {
            return decoder.apply(value);
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }

    /** Create a default implementation */
    static HttpParameter of(@NotNull final String name) {
        return new DefaultHttpParam(name, EMPTY_VALUE);
    }

    /** Create a default implementation */
    static HttpParameter of(
            @NotNull final String name,
            @NotNull final String defaultValue) {
        return new DefaultHttpParam(name, defaultValue);
    }

    /** Returns an enum constant by its parameter name or the default value */
    @NotNull
    static <V extends Enum<V> & HttpParameter> V paramValueOf(
            @NotNull final Class<V> clazz,
            @Nullable final String paramName,
            @NotNull final V defaultValue) {
        if (Check.hasLength(paramName)) {
            for (var item : clazz.getEnumConstants()) {
                if (item.equalsParamName(paramName)) {
                    return item;
                }
            }
        }
        return defaultValue;
    }

    /** Returns an enum constant by its parameter name or null */
    @Nullable
    static <V extends Enum<V> & HttpParameter> V paramValueOf(
            @NotNull final Class<V> clazz,
            @Nullable final String paramName) {
        return paramValueOf(clazz, paramName, null);
    }
}