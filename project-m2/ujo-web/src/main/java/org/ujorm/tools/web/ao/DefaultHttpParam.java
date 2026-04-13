/*
 * Copyright 2020-2026 Pavel Ponec, https://github.com/pponec
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

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface for bulding HTML parameters by an Enumerator.
 *
 * <h4>Usage</h4>
 * <pre class="pre">
 * {
 * String value = Param.TEXT(ServletRequest, "my default value"); } enum Param implements HttpParam { REGEXP, TEXT;
 *
 * @Override public String toString() { return name().toLowerCase(); } }
 * </pre>
 *
 * @author Pavel Ponec
 */
public final class DefaultHttpParam implements HttpParameter {

    /** The name of the parameter */
    @NotNull
    private final String paramName;

    /** The default value of the parameter */
    @NotNull
    private final String defaultValue;

    /** Creates a new instance */
    DefaultHttpParam(@NotNull String paramName, @NotNull String defaultValue) {
        this.paramName = paramName;
        this.defaultValue = defaultValue;
    }

    @NotNull
    @Override
    public String defaultValue() {
        return defaultValue;
    }

    @NotNull
    @Override
    public String toString() {
        return paramName;
    }

    @NotNull
    @Override
    public String paramName() {
        return paramName;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return paramName.subSequence(start, end);
    }

    @Override
    public int length() {
        return paramName.length();
    }

    @Override
    public char charAt(int index) {
        return paramName.charAt(index);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return this == obj  || (obj instanceof HttpParameter other
                && Objects.equals(this.paramName, other.paramName()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.paramName);
    }
}