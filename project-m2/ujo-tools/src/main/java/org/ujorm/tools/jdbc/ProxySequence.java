/*
 * Copyright 2018 Pavel Ponec
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ujorm.tools.jdbc;

import org.jetbrains.annotations.NotNull;

/**
 * A proxy CharSequence implementation
 * @author Pavel Ponec
 */
public abstract class ProxySequence implements CharSequence {

    /** An original sequence */
    @NotNull
    private final CharSequence orig;

    public ProxySequence(@NotNull final CharSequence orig) {
        this.orig = orig;
    }

    @Override
    public final int length() {
        return orig.length();
    }

    @Override
    public final char charAt(final int index) {
        return orig.charAt(index);
    }

    @Override
    public final CharSequence subSequence(final int start, final int end) {
        return orig.subSequence(start, end);
    }

    @Override
    public final String toString() {
        return orig.toString();
    }

}
