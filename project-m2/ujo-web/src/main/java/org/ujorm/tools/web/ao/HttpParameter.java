/*
 * Copyright 2020-2021 Pavel Ponec, https://github.com/pponec
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

import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletRequest;
import org.ujorm.tools.Assert;
import org.ujorm.tools.Check;

/**
 * An interface for bulding HTML parameters by an Enumerator.
 *
 * <h3>Usage</h3>
 * <pre class="pre">
 * {
 *    String value = Param.text(httpServletRequest, "my default value");
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

    /** Returns a parameter name */
    @Nonnull
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
    @Nonnull
    default String defaultValue() {
        return "";
    }

    /** Default value is an empty String */
    @Nonnull
    default String of(@Nonnull final ServletRequest request) {
        return of(request, Assert.notNull(defaultValue(), "Default value is required"));
    }

    /** Returns the last parameter value of the request or a default value */
    @Nonnull
    default String of(@Nonnull final ServletRequest request, @Nonnull final String defaultValue) {
        final String[] result = request.getParameterValues(toString());
        return Check.hasLength(result) ? result[result.length - 1] : defaultValue;
    }

    /** Returns a parameter of the request or the default value */
    default boolean of(@Nonnull final ServletRequest request, @Nullable final boolean defaultValue) {
        switch (of(request)) {
            case "true":
                return true;
            case "false":
                return false;
            default:
                return defaultValue;
        }
    }

    /** Returns a parameter of the request or the default value */
    default char of(@Nonnull final ServletRequest request, @Nullable final char defaultValue) {
        final String value = of(request);
        return value.isEmpty() ? defaultValue : value.charAt(0);
    }

    /** Returns a parameter of the request or the default value */
    default short of(@Nonnull final ServletRequest request, @Nullable final short defaultValue) {
        final String value = of(request);
        if (value.isEmpty()) {
            return defaultValue;
        } else try {
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Returns a parameter of the request or the default value */
    default int of(@Nonnull final ServletRequest request, @Nullable final int defaultValue) {
        final String value = of(request);
        if (value.isEmpty()) {
            return defaultValue;
        } else try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Returns a parameter of the request or the default value */
    default long of(@Nonnull final ServletRequest request, @Nullable final long defaultValue) {
        final String value = of(request);
        if (value.isEmpty()) {
            return defaultValue;
        } else try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Returns a parameter of the request or the default value */
    default float of(@Nonnull final ServletRequest request, @Nullable final float defaultValue) {
        final String value = of(request);
        if (value.isEmpty()) {
            return defaultValue;
        } else try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Returns a parameter of the request or the default value */
    default double of(@Nonnull final ServletRequest request, @Nullable final double defaultValue) {
        final String value = of(request);
        if (value.isEmpty()) {
            return defaultValue;
        } else try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /** Returns a parameter of the request or the default value */
    default <V> V of(@Nonnull final ServletRequest request, @Nonnull final V defaultValue, @Nonnull final Function<String, V> decoder) {
        final String value = of(request);
        if (value.isEmpty()) {
            return defaultValue;
        } else try {
            return decoder.apply(value);
        } catch (RuntimeException e) {
            return defaultValue;
        }
    }

    /** Create a default implementation */
    public static HttpParameter of(@Nonnull final String name) {
        return new DefaultHttpParam(name, "");
    }

    /** Create a default implementation */
    public static HttpParameter of(
            @Nonnull final String name,
            @Nonnull final String defaultValue) {
        return new DefaultHttpParam(name, defaultValue);
    }
}
