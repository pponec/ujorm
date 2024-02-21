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

import org.jetbrains.annotations.NotNull;

/**
 * An interface for bulding HTML parameters by an Enumerator.
 *
 * <h3>Usage</h3>
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

    @NotNull
    private final String name;
    @NotNull
    private final String defaultValue;

    DefaultHttpParam(@NotNull String name, @NotNull String defaultValue) {
        this.name = name;
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
        return name;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return name.subSequence(start, end);
    }

    @Override
    public int length() {
        return name.length();
    }

    @Override
    public char charAt(int index) {
        return name.charAt(index);
    }

}
