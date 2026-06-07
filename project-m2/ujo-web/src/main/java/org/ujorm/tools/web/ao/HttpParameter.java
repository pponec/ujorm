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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ujorm.tools.Check;

/**
 * An interface for building HTML parameters by an Enumerator.
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
public interface HttpParameter extends HttpParameterEq<HttpParameter> {

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