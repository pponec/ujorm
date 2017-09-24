/*
 *  Copyright 2017-2017 Pavel Ponec
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ujorm.tools;

import java.util.Collection;
import javax.annotation.Nullable;

/**
 * Common checker. See the next positive tests:
 * <pre class="pre">
 *  Check.hasLength("ABC");
 *  Check.hasLength('A','B','C');
 *  Check.hasLength(Arrays.asList("A", "B", "C"));
 * </pre>
 * @author Pavel Ponec
 */
public abstract class Check {

    /** Only static method are implemented */
    private Check() {
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static boolean hasLength(@Nullable final byte[] array) {
        return array != null && array.length > 0;
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static boolean hasLength(@Nullable final char[] array) {
        return array != null && array.length > 0;
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static boolean hasLength(@Nullable final Object... array) {
        return array != null && array.length > 0;
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static boolean hasLength(@Nullable final Collection<?> values) {
        return values != null && values.size() > 0;
    }

    /** Checks if the argument is not empty, nor {@code null}. */
    public static boolean hasLength(@Nullable final CharSequence value) {
        return value != null && value.length() > 0;
    }

    /** Checks if the argument is empty or {@code null}. */
    public static boolean isEmpty(@Nullable final byte[] array) {
        return !hasLength(array);
    }

    /** Checks if the argument is empty or {@code null}. */
    public static boolean isEmpty(@Nullable final char[] array) {
        return !hasLength(array);
    }

    /** Checks if the argument is empty or {@code null}. */
    public static boolean isEmpty(@Nullable final Object... array) {
        return !hasLength(array);
    }

    /** Checks if the argument is empty or {@code null}. */
    public static boolean isEmpty(@Nullable final Collection<?> value) {
        return !hasLength(value);
    }

    /** Checks if the argument is empty or {@code null}. */
    public static boolean isEmpty(@Nullable final CharSequence value) {
        return !hasLength(value);
    }

}
