/*
 * Copyright 2020-2022 Pavel Ponec, https://github.com/pponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.web.ao;

import java.util.Locale;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.Check;
import org.ujorm.tools.web.request.RContext;
import org.ujorm.tools.web.request.URequest;

/**
 * An interface for bulding HTML parameters by an Enumerator.
 *
 * <h3>Usage</h3>
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

    /** Returns a parameter name */
    @NotNull
    @Override
    String toString();

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
     *  The standard value is an empty String, override it for a change. */
    @NotNull
    default String defaultValue() {
        return "";
    }

    /** Build a default non-null parameter name. */
    @NotNull
    default String buildParameterName(@Nullable String name) {
        return name != null ? name : originalName().toLowerCase(Locale.ENGLISH).replace('_', '-');
    }

    /** Get a raw name of the HTTP parameter.
     * The method can be called from the {@link #buildParameterName(java.lang.String)} method.
     * NOTE: The method was renamed from obsolete {@code name()} due a Kotlin compatibility. */
    @NotNull
    default String originalName() {
        try {
            return String.valueOf(getClass().getMethod("name").invoke(this));
        } catch (ReflectiveOperationException | SecurityException e) {
            throw new IllegalStateException("Method 'name()' is not available", e);
        }
    }

    /** Returns the last parameter value of the request or a default value. The MAIN method */
    @NotNull
    default String of(@NotNull final URequest request, @NotNull final String defaultValue) {
        final String[] results = request.getParameters(toString());
        final String result = Check.hasLength(results) ? results[results.length - 1] : defaultValue;
        return result != null ? result : defaultValue;
    }

    /** Returns the last parameter value of the request or a default value */
    @NotNull
    default String of(@NotNull final RContext context, @NotNull final String defaultValue) {
        return of(context.request(), defaultValue);
    }

    /** Default value is an empty String */
    @NotNull
    default String of(@NotNull final RContext context) {
        return of(context.request(), defaultValue());
    }

    /** Default value is an empty String */
    @NotNull
    default String of(@NotNull final URequest request) {
        return of(request, defaultValue());
    }

    /** Returns a parameter of the request or the default value */
    default boolean of(@NotNull final RContext context, @Nullable final boolean defaultValue) {
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
    default char of(@NotNull final RContext context, @Nullable final char defaultValue) {
        final String value = of(context);
        return value.isEmpty() ? defaultValue : value.charAt(0);
    }

    /** Returns a parameter of the request or the default value */
    default short of(@NotNull final RContext context, @Nullable final short defaultValue) {
        final String value = of(context, EMPTY_VALUE);
        if (value.isEmpty()) {
            return defaultValue;
        } else try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Returns a parameter of the request or the default value */
    default int of(@NotNull final RContext context, @Nullable final int defaultValue) {
        final String value = of(context, EMPTY_VALUE);
        if (value.isEmpty()) {
            return defaultValue;
        } else try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Returns a parameter of the request or the default value */
    default long of(@NotNull final RContext context, @Nullable final long defaultValue) {
        final String value = of(context, EMPTY_VALUE);
        if (value.isEmpty()) {
            return defaultValue;
        } else try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Returns a parameter of the request or the default value */
    default float of(@NotNull final RContext context, @Nullable final float defaultValue) {
        final String value = of(context, EMPTY_VALUE);
        if (value.isEmpty()) {
            return defaultValue;
        } else try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Returns a parameter of the request or the default value */
    default double of(@NotNull final RContext context, @Nullable final double defaultValue) {
        final String value = of(context, EMPTY_VALUE);
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
    default <V extends Enum<V>> V of(@NotNull final RContext context, @NotNull final V defaultValue) {
        final V result = of(context, (Class<V>) defaultValue.getClass());
        return result != null ? result : defaultValue;
    }

    /** Returns a parameter of the request or the default value */
    @Nullable
    default <V extends Enum<V>> V of(@NotNull final RContext context, @NotNull final Class<V> clazz) {
        final String value = of(context);
        for (Enum item : clazz.getEnumConstants()) {
            if (item.name().equals(value)) {
                return (V) item;
            }
        }
        return null;
    }

    /** Returns a parameter of the request or the default value */
    default <V> V of(@NotNull final RContext context, @NotNull final V defaultValue, @NotNull final Function<String, V> decoder) {
        final String value = of(context, EMPTY_VALUE);
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
}
